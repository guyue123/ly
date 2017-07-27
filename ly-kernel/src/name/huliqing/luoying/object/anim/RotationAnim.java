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
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.util.TempVars;
import name.huliqing.luoying.data.AnimData;

/**
 * @author huliqing
 */
public final class RotationAnim extends AbstractAnim<Spatial> {
//    private final static Logger logger = Logger.getLogger(RotationAnim.class.getName());
    
    // 旋转
    private Vector3f axis = Vector3f.UNIT_Y.clone();
    // 旋转弧度
    private float angle = FastMath.TWO_PI;
    // 是否反转方向 
    private boolean invert;
    
    // 当动画结束后是否复原旋转位置
    private boolean restore;
    private final Quaternion origin = new Quaternion();
    
    public RotationAnim() {
        super();
    }
    
    @Override
    public void setData(AnimData data) {
        super.setData(data);
        this.axis = data.getAsVector3f("axis", axis);
        Float degree = data.getAsFloat("degree");
        if (degree != null) {
            angle = degree * FastMath.DEG_TO_RAD;
        }
        this.invert = data.getAsBoolean("invert", invert);
        this.restore = data.getAsBoolean("restore", restore);
    }

    @Override
    public void updateDatas() {
        super.updateDatas();
    }

    /**
     * 设置要旋转的角度数，与 {@link #setAngle(float) } 一样，但是参数是以
     * <b>角度</b>传入的。
     * @param degree 
     * @deprecated 后续不再开放这个参数
     */
    public void setAngleDegree(float degree) {
        this.angle = degree * FastMath.DEG_TO_RAD;
    }
 
    @Override
    protected void doAnimInit() {
        this.axis.normalizeLocal();
        origin.set(target.getLocalRotation());
    }

    @Override
    protected void doAnimUpdate(float interpolation) {
//        logger.log(Level.INFO, "interpolation={0}", interpolation);
        TempVars tv = TempVars.get();
        tv.quat1.fromAngleNormalAxis(interpolation * angle * (invert ? -1 : 1), axis);
        origin.mult(tv.quat1, tv.quat2);
        target.setLocalRotation(tv.quat2);
        tv.release();
    }

    @Override
    public void cleanup() {
        if (restore && origin != null) {
            target.setLocalRotation(origin);
        }
        super.cleanup();
    }
    
    
}
