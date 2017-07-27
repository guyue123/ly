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
package name.huliqing.luoying.object.bullet;

import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import name.huliqing.luoying.object.entity.Entity;

/**
 * @author huliqing
 */
public interface Bullet extends Entity {
    
    /**
     * 设置子弹的开始射击位置
     * @param startPoint 
     */
    void setStart(Vector3f startPoint);
    
    /**
     * 设置子弹的目标射击位置
     * @param endPoint 
     */
    void setEnd(Vector3f endPoint);
    
    /**
     * 设置子弹的速度
     * @param speed 
     */
    void setSpeed(float speed);
    
    /**
     * 销毁子弹，调用这个方法之后，子弹的逻辑将不会再执行，等着被清理、回收的节奏。一般来说，
     * 应该只有在子弹明确击中目标后或者希望立确销毁子弹的时候才应该调用这个方法，因为调用这个方法之后子弹将立即从
     * 场景中消失
     * @see #requestConsume() 
     */
    void consume();
    
    /**
     * 判断子弹是否已经消耗完，完成子弹的使命。
     * @return 
     */
    boolean isConsumed();
    
//    /**
//     * 请求销毁子弹，默认情况下该方法什么也不做，具体是否应该销毁子弹由不同类型的子弹的实际情况决定，该方法由子类实
//     * 现决定。
//     * @see #consume() 
//     */
//    void requestConsume();
    
    /**
     * 添加一个子弹侦听器
     * @param listener 
     */
    void addListener(BulletListener listener);
    
    /**
     * 删除子弹侦听器
     * @param listener
     * @return 
     */
    boolean removeListener(BulletListener listener);
    
    /**
     * 获取发射该子弹的源，比如一个角色？或是一个未知的存在？
     * @return 
     */
    Entity getSource();
    
    /**
     * 设置发射子弹的源
     * @param source 
     */
    void setSource(Entity source);
    
    /**
     * 判断当前子弹是否击中目标
     * @param target
     * @return 
     */
    boolean isHit(Spatial target);
    
    /**
     * 判断当前子弹是否击中目标点。
     * @param target
     * @return 
     */
    boolean isHit(Vector3f target);
    
}
