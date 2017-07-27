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
package name.huliqing.editor.tiles;

import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Line;
import com.jme3.util.TempVars;
import name.huliqing.luoying.utils.MaterialUtils;

/**
 *
 * @author huliqing
 */
public class AxisLine extends Node {
    
    private final Geometry line;
    
    public AxisLine() {
        Line lineShape = new Line(new Vector3f(0, 0, -10000)
                , new Vector3f(0, 0, 10000));
        line = new Geometry("debugLine", lineShape);
        line.setMaterial(MaterialUtils.createUnshaded());
        attachChild(line);
    }
    
    public void setDirection(Vector3f direction) {
        TempVars tv = TempVars.get();
        tv.quat1.lookAt(direction, Vector3f.UNIT_Y);
        if (getParent() != null) {
            tv.quat2.set(getParent().getWorldRotation()).inverseLocal();
            tv.quat2.multLocal(tv.quat1);
            tv.quat2.normalizeLocal();
            setLocalRotation(tv.quat2);
        } else {
            setLocalRotation(tv.quat1);
        }
        tv.release();
    }
    
    public void setColor(ColorRGBA color) {
        line.getMaterial().setColor("Color", color);
    }
}
