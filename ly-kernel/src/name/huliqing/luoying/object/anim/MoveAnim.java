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
package name.huliqing.luoying.object.anim;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.util.TempVars;
import name.huliqing.luoying.data.AnimData;

/**
 * 直线移动的动画
 * @author huliqing
 */
public final class MoveAnim extends AbstractAnim<Spatial> {
//    private final static Logger LOG = Logger.getLogger(MoveAnim.class.getName());

    // 开始位置及结束位置
    private Vector3f startPos;
    private Vector3f endPos;
    private Vector3f startPosOffset;
    private Vector3f endPosOffset;
    // 是否朝向目标 
    private boolean facing;
    
    // ---- inner
    private final Vector3f trueStartPos = new Vector3f();
    private final Vector3f trueEndPos = new Vector3f();
    
    @Override
    public void setData(AnimData data) {
        super.setData(data);
        this.startPos = data.getAsVector3f("startPos");
        this.endPos = data.getAsVector3f("endPos");
        this.startPosOffset = data.getAsVector3f("startPosOffset");
        this.endPosOffset = data.getAsVector3f("endPosOffset");
        this.facing = data.getAsBoolean("facing", facing);
    }
    
    @Override
    protected void doAnimInit() {
        trueStartPos.set(startPos != null ? startPos : target.getWorldTranslation());
        if (startPosOffset != null) {
            trueStartPos.addLocal(startPosOffset);
        }
        
        trueEndPos.set(endPos != null ? endPos : target.getWorldTranslation());
        if (endPosOffset != null) {
            trueEndPos.addLocal(endPosOffset);
        }
        
        target.setLocalTranslation(trueStartPos);
        
        if (facing) {
            target.lookAt(trueEndPos, Vector3f.UNIT_Y);
        }
    }

    @Override
    protected void doAnimUpdate(float interpolation) {
        TempVars tv = TempVars.get();
        FastMath.extrapolateLinear(interpolation, trueStartPos, trueEndPos, tv.vect1);
        target.setLocalTranslation(tv.vect1);
        tv.release();
    }

    @Override
    public void cleanup() {
        super.cleanup();
    }

//    public Vector3f getStartPos() {
//        return startPos;
//    }

    public void setStartPos(Vector3f startPos) {
        if (this.startPos == null) {
            this.startPos = new Vector3f();
        }
        this.startPos.set(startPos);
    }

//    public Vector3f getEndPos() {
//        return endPos;
//    }

    public void setEndPos(Vector3f endPos) {
        if (this.endPos == null) {
            this.endPos = new Vector3f();
        }
        this.endPos.set(endPos);
    }

//    public boolean isFacing() {
//        return facing;
//    }
//
//    public void setFacing(boolean facing) {
//        this.facing = facing;
//    }

    
}
