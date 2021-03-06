/*
 * Copyright (c) 2009-2012 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package name.huliqing.luoying.object.actor;

import com.jme3.animation.Skeleton;
import com.jme3.animation.SkeletonControl;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Matrix4f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.RendererException;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.*;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.shader.VarType;
import com.jme3.util.SafeArrayList;
import com.jme3.util.TempVars;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * 重写的SkeletonControl，主要排除了当子节点也带有SkeletonControl时的冲突导
 * 致子节点模型存在变形的问题。
 * // Fix bug =========================================================
 * // 对于子组件包含SkeletonControl的节点不要进行处理。因为这些节点本身
 * // 可能也包含动画。比如：武器“弓”，有拉弓动画等，这些节点如果也进行
 * // 处理则可能导致弓模型变形
 * // Fix bug =========================================================
 * @author huliqing
 */
public class CustomSkeletonControl extends SkeletonControl implements Cloneable {

    /**
     * The skeleton of the model.
     */
    private Skeleton skeleton;
    /**
     * List of targets which this controller effects.
     */
    private SafeArrayList<Mesh> targets = new SafeArrayList<Mesh>(Mesh.class);
    /**
     * Used to track when a mesh was updated. Meshes are only updated if they
     * are visible in at least one camera.
     */
    private boolean wasMeshUpdated = false;
    
    /**
     * User wishes to use hardware skinning if available.
     */
    private transient boolean hwSkinningDesired = false;
    
    /**
     * Hardware skinning is currently being used.
     */
    private transient boolean hwSkinningEnabled = false;
    
    /**
     * Hardware skinning was tested on this GPU, results
     * are stored in {@link #hwSkinningSupported} variable.
     */
    private transient boolean hwSkinningTested = false;
    
    /**
     * If hardware skinning was {@link #hwSkinningTested tested}, then
     * this variable will be set to true if supported, and false if otherwise.
     */
    private transient boolean hwSkinningSupported = false;
    
    /**
     * Bone offset matrices, recreated each frame
     */
    private transient Matrix4f[] offsetMatrices;
    /**
     * Material references used for hardware skinning
     */
    private Set<Material> materials = new HashSet<Material>();

    /**
     * Serialization only. Do not use.
     */
    public CustomSkeletonControl() {
    }

    private void switchToHardware() {
        // Next full 10 bones (e.g. 30 on 24 bones)
        int numBones = ((skeleton.getBoneCount() / 10) + 1) * 10;
        for (Material m : materials) {
            m.setInt("NumberOfBones", numBones);
        }
        for (Mesh mesh : targets) {
            if (mesh.isAnimated()) {
                mesh.prepareForAnim(false);
            }
        }
    }

    private void switchToSoftware() {
        for (Material m : materials) {
            if (m.getParam("NumberOfBones") != null) {
                m.clearParam("NumberOfBones");
            }
        }
        for (Mesh mesh : targets) {
            if (mesh.isAnimated()) {
                mesh.prepareForAnim(true);
            }
        }
    }

    private boolean testHardwareSupported(RenderManager rm) {
        for (Material m : materials) {
            // Some of the animated mesh(es) do not support hardware skinning,
            // so it is not supported by the model.
            if (m.getMaterialDef().getMaterialParam("NumberOfBones") == null) {
                Logger.getLogger(CustomSkeletonControl.class.getName()).log(Level.WARNING, 
                        "Not using hardware skinning for {0}, " + 
                        "because material {1} doesn''t support it.", 
                        new Object[]{spatial, m.getMaterialDef().getName()});
                
                return false;
            }
        }

        switchToHardware();
        
        try {
            rm.preloadScene(spatial);
            return true;
        } catch (RendererException e) {
            Logger.getLogger(CustomSkeletonControl.class.getName()).log(Level.WARNING, "Could not enable HW skinning due to shader compile error:", e);
            return false;
        }
    }

    /**
     * Specifies if hardware skinning is preferred. If it is preferred and
     * supported by GPU, it shall be enabled, if its not preferred, or not
     * supported by GPU, then it shall be disabled.
     * 
     * @see #isHardwareSkinningUsed() 
     */
    @Override
    public void setHardwareSkinningPreferred(boolean preferred) {
        hwSkinningDesired = preferred;
    }
    
    /**
     * @return True if hardware skinning is preferable to software skinning.
     * Set to false by default.
     * 
     * @see #setHardwareSkinningPreferred(boolean) 
     */
    @Override
    public boolean isHardwareSkinningPreferred() {
        return hwSkinningDesired;
    }
    
    /**
     * @return True is hardware skinning is activated and is currently used, false otherwise.
     */
    @Override
    public boolean isHardwareSkinningUsed() {
        return hwSkinningEnabled;
    }
    
    /**
     * Creates a skeleton control. The list of targets will be acquired
     * automatically when the control is attached to a node.
     *
     * @param skeleton the skeleton
     */
    public CustomSkeletonControl(Skeleton skeleton) {
        super(skeleton);
        this.skeleton = skeleton;
    }


    private void findTargets(Node node) {
        Mesh sharedMesh = null;        

        for (Spatial child : node.getChildren()) {
            
            // Fix bug =========================================================
            // 对于子组件包含SkeletonControl的节点不要进行处理。因为这些节点本身
            // 可能也包含动画。比如：武器“弓”，有拉弓动画等，这些节点如果也进行
            // 处理则可能导致弓模型变形
            // Fix bug =========================================================
            if (child.getControl(SkeletonControl.class) != null) {
                continue;
            }
            
            if (child instanceof Geometry) {
                Geometry geom = (Geometry) child;

                // is this geometry using a shared mesh?
                Mesh childSharedMesh = geom.getUserData(UserData.JME_SHAREDMESH);

                if (childSharedMesh != null) {
                    // Don鈥檛 bother with non-animated shared meshes
                    if (childSharedMesh.isAnimated()) {
                        // child is using shared mesh,
                        // so animate the shared mesh but ignore child
                        if (sharedMesh == null) {
                            sharedMesh = childSharedMesh;
                        } else if (sharedMesh != childSharedMesh) {
                            throw new IllegalStateException("Two conflicting shared meshes for " + node);
                        }
                        materials.add(geom.getMaterial());
                    }
                } else {
                    Mesh mesh = geom.getMesh();
                    if (mesh.isAnimated()) {
                        targets.add(mesh);
                        materials.add(geom.getMaterial());
                    }
                }
            } else if (child instanceof Node) {
                findTargets((Node) child);
            }
        }

        if (sharedMesh != null) {
            targets.add(sharedMesh);
        }

    }

    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
//        updateTargetsAndMaterials(spatial);
    }

    private void controlRenderSoftware() {
        resetToBind(); // reset morph meshes to bind pose

        offsetMatrices = skeleton.computeSkinningMatrices();

        for (Mesh mesh : targets) {
            // NOTE: This assumes that code higher up
            // Already ensured those targets are animated
            // otherwise a crash will happen in skin update
            softwareSkinUpdate(mesh, offsetMatrices);
        }     
    }

    private void controlRenderHardware() {
        offsetMatrices = skeleton.computeSkinningMatrices();
        for (Material m : materials) {
            m.setParam("BoneMatrices", VarType.Matrix4Array, offsetMatrices);
        }
    }

    
    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        if (!wasMeshUpdated) {
            updateTargetsAndMaterials(spatial);
            
            // Prevent illegal cases. These should never happen.
            assert hwSkinningTested || (!hwSkinningTested && !hwSkinningSupported && !hwSkinningEnabled);
            assert !hwSkinningEnabled || (hwSkinningEnabled && hwSkinningTested && hwSkinningSupported);

            if (hwSkinningDesired && !hwSkinningTested) {
                hwSkinningTested = true;
                hwSkinningSupported = testHardwareSupported(rm);

                if (hwSkinningSupported) {
                    hwSkinningEnabled = true;
                    
                    Logger.getLogger(CustomSkeletonControl.class.getName()).log(Level.INFO, "Hardware skinning engaged for " + spatial);
                } else {
                    switchToSoftware();
                }
            } else if (hwSkinningDesired && hwSkinningSupported && !hwSkinningEnabled) {
                switchToHardware();
                hwSkinningEnabled = true;
            } else if (!hwSkinningDesired && hwSkinningEnabled) {
                switchToSoftware();
                hwSkinningEnabled = false;
            }

            if (hwSkinningEnabled) {
                controlRenderHardware();
            } else {
                controlRenderSoftware();
            }

            wasMeshUpdated = true;
        }
    }

    @Override
    protected void controlUpdate(float tpf) {
        wasMeshUpdated = false;
     }

    //only do this for software updates
    void resetToBind() {
        for (Mesh mesh : targets) {
            if (mesh.isAnimated()) {
                Buffer bwBuff = mesh.getBuffer(Type.BoneWeight).getData();
                Buffer biBuff = mesh.getBuffer(Type.BoneIndex).getData();
                if (!biBuff.hasArray() || !bwBuff.hasArray()) {
                    mesh.prepareForAnim(true); // prepare for software animation
                }
                VertexBuffer bindPos = mesh.getBuffer(Type.BindPosePosition);
                VertexBuffer bindNorm = mesh.getBuffer(Type.BindPoseNormal);
                VertexBuffer pos = mesh.getBuffer(Type.Position);
                VertexBuffer norm = mesh.getBuffer(Type.Normal);
                FloatBuffer pb = (FloatBuffer) pos.getData();
                FloatBuffer nb = (FloatBuffer) norm.getData();
                FloatBuffer bpb = (FloatBuffer) bindPos.getData();
                FloatBuffer bnb = (FloatBuffer) bindNorm.getData();
                pb.clear();
                nb.clear();
                bpb.clear();
                bnb.clear();

                //reseting bind tangents if there is a bind tangent buffer
                VertexBuffer bindTangents = mesh.getBuffer(Type.BindPoseTangent);
                if (bindTangents != null) {
                    VertexBuffer tangents = mesh.getBuffer(Type.Tangent);
                    FloatBuffer tb = (FloatBuffer) tangents.getData();
                    FloatBuffer btb = (FloatBuffer) bindTangents.getData();
                    tb.clear();
                    btb.clear();
                    tb.put(btb).clear();
                }


                pb.put(bpb).clear();
                nb.put(bnb).clear();
            }
        }
    }

    @Override
    public Mesh[] getTargets() {        
        return targets.toArray(new Mesh[targets.size()]);
    }

    /**
     * Update the mesh according to the given transformation matrices
     *
     * @param mesh then mesh
     * @param offsetMatrices the transformation matrices to apply
     */
    private void softwareSkinUpdate(Mesh mesh, Matrix4f[] offsetMatrices) {

        VertexBuffer tb = mesh.getBuffer(Type.Tangent);
        if (tb == null) {
            //if there are no tangents use the classic skinning
            applySkinning(mesh, offsetMatrices);
        } else {
            //if there are tangents use the skinning with tangents
            applySkinningTangents(mesh, offsetMatrices, tb);
        }


    }

    /**
     * Method to apply skinning transforms to a mesh's buffers
     *
     * @param mesh the mesh
     * @param offsetMatrices the offset matices to apply
     */
    private void applySkinning(Mesh mesh, Matrix4f[] offsetMatrices) {
        int maxWeightsPerVert = mesh.getMaxNumWeights();
        if (maxWeightsPerVert <= 0) {
            throw new IllegalStateException("Max weights per vert is incorrectly set!");
        }

        int fourMinusMaxWeights = 4 - maxWeightsPerVert;

        // NOTE: This code assumes the vertex buffer is in bind pose
        // resetToBind() has been called this frame
        VertexBuffer vb = mesh.getBuffer(Type.Position);
        FloatBuffer fvb = (FloatBuffer) vb.getData();
        fvb.rewind();

        VertexBuffer nb = mesh.getBuffer(Type.Normal);
        FloatBuffer fnb = (FloatBuffer) nb.getData();
        fnb.rewind();

        // get boneIndexes and weights for mesh
        ByteBuffer ib = (ByteBuffer) mesh.getBuffer(Type.BoneIndex).getData();
        FloatBuffer wb = (FloatBuffer) mesh.getBuffer(Type.BoneWeight).getData();

        ib.rewind();
        wb.rewind();

        float[] weights = wb.array();
        byte[] indices = ib.array();
        int idxWeights = 0;

        TempVars vars = TempVars.get();

        float[] posBuf = vars.skinPositions;
        float[] normBuf = vars.skinNormals;

        int iterations = (int) FastMath.ceil(fvb.limit() / ((float) posBuf.length));
        int bufLength = posBuf.length;
        for (int i = iterations - 1; i >= 0; i--) {
            // read next set of positions and normals from native buffer
            bufLength = Math.min(posBuf.length, fvb.remaining());
            fvb.get(posBuf, 0, bufLength);
            fnb.get(normBuf, 0, bufLength);
            int verts = bufLength / 3;
            int idxPositions = 0;

            // iterate vertices and apply skinning transform for each effecting bone
            for (int vert = verts - 1; vert >= 0; vert--) {
                // Skip this vertex if the first weight is zero.
                if (weights[idxWeights] == 0) {
                    idxPositions += 3;
                    idxWeights += 4;
                    continue;
                }

                float nmx = normBuf[idxPositions];
                float vtx = posBuf[idxPositions++];
                float nmy = normBuf[idxPositions];
                float vty = posBuf[idxPositions++];
                float nmz = normBuf[idxPositions];
                float vtz = posBuf[idxPositions++];

                float rx = 0, ry = 0, rz = 0, rnx = 0, rny = 0, rnz = 0;

                for (int w = maxWeightsPerVert - 1; w >= 0; w--) {
                    float weight = weights[idxWeights];
                    Matrix4f mat = offsetMatrices[indices[idxWeights++] & 0xff];

                    rx += (mat.m00 * vtx + mat.m01 * vty + mat.m02 * vtz + mat.m03) * weight;
                    ry += (mat.m10 * vtx + mat.m11 * vty + mat.m12 * vtz + mat.m13) * weight;
                    rz += (mat.m20 * vtx + mat.m21 * vty + mat.m22 * vtz + mat.m23) * weight;

                    rnx += (nmx * mat.m00 + nmy * mat.m01 + nmz * mat.m02) * weight;
                    rny += (nmx * mat.m10 + nmy * mat.m11 + nmz * mat.m12) * weight;
                    rnz += (nmx * mat.m20 + nmy * mat.m21 + nmz * mat.m22) * weight;
                }

                idxWeights += fourMinusMaxWeights;

                idxPositions -= 3;
                normBuf[idxPositions] = rnx;
                posBuf[idxPositions++] = rx;
                normBuf[idxPositions] = rny;
                posBuf[idxPositions++] = ry;
                normBuf[idxPositions] = rnz;
                posBuf[idxPositions++] = rz;
            }

            fvb.position(fvb.position() - bufLength);
            fvb.put(posBuf, 0, bufLength);
            fnb.position(fnb.position() - bufLength);
            fnb.put(normBuf, 0, bufLength);
        }

        vars.release();

        vb.updateData(fvb);
        nb.updateData(fnb);

    }

    /**
     * Specific method for skinning with tangents to avoid cluttering the
     * classic skinning calculation with null checks that would slow down the
     * process even if tangents don't have to be computed. Also the iteration
     * has additional indexes since tangent has 4 components instead of 3 for
     * pos and norm
     *
     * @param maxWeightsPerVert maximum number of weights per vertex
     * @param mesh the mesh
     * @param offsetMatrices the offsetMaytrices to apply
     * @param tb the tangent vertexBuffer
     */
    private void applySkinningTangents(Mesh mesh, Matrix4f[] offsetMatrices, VertexBuffer tb) {
        int maxWeightsPerVert = mesh.getMaxNumWeights();

        if (maxWeightsPerVert <= 0) {
            throw new IllegalStateException("Max weights per vert is incorrectly set!");
        }

        int fourMinusMaxWeights = 4 - maxWeightsPerVert;

        // NOTE: This code assumes the vertex buffer is in bind pose
        // resetToBind() has been called this frame
        VertexBuffer vb = mesh.getBuffer(Type.Position);
        FloatBuffer fvb = (FloatBuffer) vb.getData();
        fvb.rewind();

        VertexBuffer nb = mesh.getBuffer(Type.Normal);

        FloatBuffer fnb = (FloatBuffer) nb.getData();
        fnb.rewind();


        FloatBuffer ftb = (FloatBuffer) tb.getData();
        ftb.rewind();


        // get boneIndexes and weights for mesh
        ByteBuffer ib = (ByteBuffer) mesh.getBuffer(Type.BoneIndex).getData();
        FloatBuffer wb = (FloatBuffer) mesh.getBuffer(Type.BoneWeight).getData();

        ib.rewind();
        wb.rewind();

        float[] weights = wb.array();
        byte[] indices = ib.array();
        int idxWeights = 0;

        TempVars vars = TempVars.get();


        float[] posBuf = vars.skinPositions;
        float[] normBuf = vars.skinNormals;
        float[] tanBuf = vars.skinTangents;

        int iterations = (int) FastMath.ceil(fvb.limit() / ((float) posBuf.length));
        int bufLength = 0;
        int tanLength = 0;
        for (int i = iterations - 1; i >= 0; i--) {
            // read next set of positions and normals from native buffer
            bufLength = Math.min(posBuf.length, fvb.remaining());
            tanLength = Math.min(tanBuf.length, ftb.remaining());
            fvb.get(posBuf, 0, bufLength);
            fnb.get(normBuf, 0, bufLength);
            ftb.get(tanBuf, 0, tanLength);
            int verts = bufLength / 3;
            int idxPositions = 0;
            //tangents has their own index because of the 4 components
            int idxTangents = 0;

            // iterate vertices and apply skinning transform for each effecting bone
            for (int vert = verts - 1; vert >= 0; vert--) {
                // Skip this vertex if the first weight is zero.
                if (weights[idxWeights] == 0) {
                    idxTangents += 4;
                    idxPositions += 3;
                    idxWeights += 4;
                    continue;
                }

                float nmx = normBuf[idxPositions];
                float vtx = posBuf[idxPositions++];
                float nmy = normBuf[idxPositions];
                float vty = posBuf[idxPositions++];
                float nmz = normBuf[idxPositions];
                float vtz = posBuf[idxPositions++];

                float tnx = tanBuf[idxTangents++];
                float tny = tanBuf[idxTangents++];
                float tnz = tanBuf[idxTangents++];

                // skipping the 4th component of the tangent since it doesn't have to be transformed
                idxTangents++;

                float rx = 0, ry = 0, rz = 0, rnx = 0, rny = 0, rnz = 0, rtx = 0, rty = 0, rtz = 0;

                for (int w = maxWeightsPerVert - 1; w >= 0; w--) {
                    float weight = weights[idxWeights];
                    Matrix4f mat = offsetMatrices[indices[idxWeights++]];

                    rx += (mat.m00 * vtx + mat.m01 * vty + mat.m02 * vtz + mat.m03) * weight;
                    ry += (mat.m10 * vtx + mat.m11 * vty + mat.m12 * vtz + mat.m13) * weight;
                    rz += (mat.m20 * vtx + mat.m21 * vty + mat.m22 * vtz + mat.m23) * weight;

                    rnx += (nmx * mat.m00 + nmy * mat.m01 + nmz * mat.m02) * weight;
                    rny += (nmx * mat.m10 + nmy * mat.m11 + nmz * mat.m12) * weight;
                    rnz += (nmx * mat.m20 + nmy * mat.m21 + nmz * mat.m22) * weight;

                    rtx += (tnx * mat.m00 + tny * mat.m01 + tnz * mat.m02) * weight;
                    rty += (tnx * mat.m10 + tny * mat.m11 + tnz * mat.m12) * weight;
                    rtz += (tnx * mat.m20 + tny * mat.m21 + tnz * mat.m22) * weight;
                }

                idxWeights += fourMinusMaxWeights;

                idxPositions -= 3;

                normBuf[idxPositions] = rnx;
                posBuf[idxPositions++] = rx;
                normBuf[idxPositions] = rny;
                posBuf[idxPositions++] = ry;
                normBuf[idxPositions] = rnz;
                posBuf[idxPositions++] = rz;

                idxTangents -= 4;

                tanBuf[idxTangents++] = rtx;
                tanBuf[idxTangents++] = rty;
                tanBuf[idxTangents++] = rtz;

                //once again skipping the 4th component of the tangent
                idxTangents++;
            }

            fvb.position(fvb.position() - bufLength);
            fvb.put(posBuf, 0, bufLength);
            fnb.position(fnb.position() - bufLength);
            fnb.put(normBuf, 0, bufLength);
            ftb.position(ftb.position() - tanLength);
            ftb.put(tanBuf, 0, tanLength);
        }

        vars.release();

        vb.updateData(fvb);
        nb.updateData(fnb);
        tb.updateData(ftb);


    }

    private void updateTargetsAndMaterials(Spatial spatial) {
        targets.clear();
        materials.clear();           
        if (spatial != null && spatial instanceof Node) {
            Node node = (Node) spatial;                        
            findTargets(node);
        }
    }
}
