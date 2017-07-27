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
package name.huliqing.ly.animation;

import com.jme3.material.MatParam;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.SceneGraphVisitor;
import com.jme3.scene.Spatial;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.util.TempVars;
import java.util.ArrayList;
import java.util.List;
import name.huliqing.luoying.ui.Text;

/**
 * 线性直线运动
 * @author huliqing
 */
public class LinearMove extends SimpleMotion {
    
    private List<Material> matList;
    private List<Text> textList;
    private Vector3f originScale;
    
    public LinearMove() {}

    @Override
    protected void doInit() {
        super.doInit();
        
        if (useAlpha) {
            if (matList == null) {
                matList = new ArrayList<Material>();
            }
            if (textList == null) {
                textList = new ArrayList<Text>();
            }
            matList.clear();
            textList.clear();
            target.breadthFirstTraversal(new SceneGraphVisitor() {
                @Override
                public void visit(Spatial spatial) {
                    if (spatial.getCullHint() == CullHint.Always) {
                        // donothing
                    } else if (spatial instanceof Geometry) {
                        Material mat = ((Geometry) spatial).getMaterial();
                        if (!matList.contains(mat)) {
                            matList.add(mat);
                        }
                    } else if (spatial instanceof Text) {
                        Text t = (Text) spatial;
                        textList.add(t);
                    }
                }
            });
        }
        
        // 记住原始缩放
        if (useScale) {
            if (originScale == null) {
                originScale = new Vector3f();
            }
            originScale.set(target.getLocalScale());
        }
    }

    @Override
    public void cleanup() {
        if (textList != null) {
            for (Text t : textList) {
                t.setAlpha(1);
            }
            textList.clear();
        }
        if (matList != null) {
            matList.clear();
        }
        if (useScale) {
            TempVars tv = TempVars.get();
            tv.vect1.set(originScale).multLocal(endScale);
            target.setLocalScale(tv.vect1);
            tv.release();
        }
        super.cleanup();
    }

    @Override
    protected void doMotion(Spatial ui, float factor) {
        startPos.interpolateLocal(endPos, factor);
        ui.setLocalTranslation(startPos);
    }

    @Override
    protected void doAlpha(Spatial ui, float factor) {
        // 该方法还有BUG，会导致采用相同texture的UI都出现透明现象。
        for (Material mat : matList) {
            mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
            MatParam pm = mat.getParam("Color");
            if (pm != null) {
                ColorRGBA color = (ColorRGBA) pm.getValue();
                color.a = factor;
            }
        }
        for (Text t : textList) {
            t.setAlpha(factor);
        }
    }

    @Override
    protected void doScale(Spatial ui, float factor) {
        float scale = FastMath.interpolateLinear(factor, startScale, endScale);
        TempVars tv = TempVars.get();
        tv.vect1.set(originScale).multLocal(scale);
        ui.setLocalScale(tv.vect1);
        tv.release();
    }
}
