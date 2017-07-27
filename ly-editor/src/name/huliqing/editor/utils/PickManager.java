/*
 * LuoYing is a program used to make 3D RPG game.
 * Copyright (c) 2014-2016 Huliqing <31703299@qq.com>
 * 
 * This file is part of LuoYing.
 *
 * LuoYing is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LuoYing is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with LuoYing.  If not, see <http://www.gnu.org/licenses/>.
 */
package name.huliqing.editor.utils;

import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Quad;
import name.huliqing.editor.edit.Mode;

/**
 *
 * @author dokthar
 */
public class PickManager {

    private Vector3f startPickLoc;
    private Vector3f finalPickLoc;
    private Vector3f startSpatialLocation;
    private Quaternion origineRotation;
    private final Node plane;
    private Spatial spatial;
    private Mode transformationType;

    public static final Quaternion PLANE_XY = new Quaternion().fromAngleAxis(0, new Vector3f(1, 0, 0));
    public static final Quaternion PLANE_YZ = new Quaternion().fromAngleAxis(-FastMath.PI / 2, new Vector3f(0, 1, 0));//YAW090
    public static final Quaternion PLANE_XZ = new Quaternion().fromAngleAxis(FastMath.PI / 2, new Vector3f(1, 0, 0)); //PITCH090

    public PickManager() {
        float size = 1000;
        Geometry g = new Geometry("plane", new Quad(size, size));
        g.setLocalTranslation(-size / 2, -size / 2, 0);
        plane = new Node();
        plane.attachChild(g);
    }

    public void reset() {
        startPickLoc = null;
        finalPickLoc = null;
        startSpatialLocation = null;
        spatial = null;
    }

    public void initiatePick(Spatial selectedSpatial, Quaternion planeRotation, Mode type, Camera camera, Vector2f screenCoord) {
        spatial = selectedSpatial;
        startSpatialLocation = selectedSpatial.getWorldTranslation().clone();

        setTransformation(planeRotation, type, camera);
        plane.setLocalTranslation(startSpatialLocation);

//        startPickLoc = SceneEditTool.pickWorldLocation(camera, screenCoord, plane, null);
        startPickLoc = name.huliqing.luoying.manager.PickManager.pickPoint(camera, screenCoord, plane);
    }

    public void setTransformation(Quaternion planeRotation, Mode type, Camera camera) {
        Quaternion rot = new Quaternion();
        transformationType = type;
        if (null != transformationType) {
            switch (transformationType) {
                case LOCAL:
                    rot.set(spatial.getWorldRotation());
                    rot.multLocal(planeRotation);
                    origineRotation = spatial.getWorldRotation().clone();
                    break;
                case GLOBAL:
                    rot.set(planeRotation);
                    origineRotation = new Quaternion(Quaternion.IDENTITY);
                    break;
                case CAMERA:
                    rot.set(camera.getRotation());
                    origineRotation = camera.getRotation();
                    break;
                default:
                    break;
            }
        }
        plane.setLocalRotation(rot);
    }
    
    /**
     *
     * @param camera
     * @param screenCoord
     * @return true if the the new picked location is set, else return false.
     */
    public boolean updatePick(Camera camera, Vector2f screenCoord) {
//        finalPickLoc = SceneEditTool.pickWorldLocation(camera, screenCoord, plane, null);
        finalPickLoc = name.huliqing.luoying.manager.PickManager.pickPoint(camera, screenCoord, plane);
        return finalPickLoc != null;
    }

    /**
     *
     * @return the start location in WorldSpace
     */
    public Vector3f getStartLocation() {
        return startSpatialLocation;
    }

    /**
     *
     * @return the vector from the tool origin to the start location, in
     * WorldSpace
     */
    public Vector3f getStartOffset() {
        return startPickLoc.subtract(startSpatialLocation);
    }

    /**
     *
     * @return the vector from the tool origin to the final location, in
     * WorldSpace
     */
    public Vector3f getFinalOffset() {
        return finalPickLoc.subtract(startSpatialLocation);
    }

    /**
     *
     * @return the angle between the start location and the final location
     */
    public float getAngle() {
        Vector3f v1, v2;
        v1 = startPickLoc.subtract(startSpatialLocation);
        v2 = finalPickLoc.subtract(startSpatialLocation);
        return v1.angleBetween(v2);
    }

    /**
     *
     * @return the Quaternion rotation in the WorldSpace
     */
    public Quaternion getRotation() {
        return getRotation(Quaternion.IDENTITY);
    }

    /**
     *
     * @return the Quaternion rotation in the ToolSpace
     */
    public Quaternion getLocalRotation() {
        return getRotation(origineRotation.inverse());
    }

    /**
     * Get the Rotation into a specific custom space.
     *
     * @param transforme the rotation to the custom space (World to Custom
     * space)
     * @return the Rotation in the custom space
     */
    public Quaternion getRotation(Quaternion transforme) {
        Vector3f v1, v2;
        v1 = transforme.mult(startPickLoc.subtract(startSpatialLocation).normalize());
        v2 = transforme.mult(finalPickLoc.subtract(startSpatialLocation).normalize());
        Vector3f axis = v1.cross(v2);
        float angle = v1.angleBetween(v2);
        return new Quaternion().fromAngleAxis(angle, axis);
    }

    /**
     *
     * @return the translation in WorldSpace
     */
    public Vector3f getTranslation() {
        return finalPickLoc.subtract(startPickLoc);
    }

    /**
     *
     * @param axisConstrainte
     * @return
     */
    public Vector3f getTranslation(Vector3f axisConstrainte) {
        Vector3f localConstrainte = (origineRotation.mult(axisConstrainte)).normalize(); // according to the "plane" rotation
        Vector3f constrainedTranslation = localConstrainte.mult(getTranslation().dot(localConstrainte));
        return constrainedTranslation;
    }

    /**
     *
     * @param axisConstrainte
     * @return
     */
    public Vector3f getLocalTranslation(Vector3f axisConstrainte) {
        return getTranslation(origineRotation.inverse().mult(axisConstrainte));
    }

}
