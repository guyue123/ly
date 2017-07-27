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
package name.huliqing.luoying.object.talent;

import name.huliqing.luoying.data.TalentData;
import name.huliqing.luoying.xml.DataProcessor;
import name.huliqing.luoying.object.entity.Entity;

/**
 * 天赋，注：天赋是不会结束的，一旦获得就一直存在，除非手动移除。
 * 所以没有isEnd()方法。
 * @author huliqing
 * @param <T>
 */
public interface Talent<T extends TalentData> extends DataProcessor<T> {
    
    @Override
    public void setData(T data);

    @Override
    public T getData();
    
    /**
     * 初始化天赋,当给指定角色添加天赋时该方法应该被调用一次，以进行初始化。
     */
    void initialize();
    
    /**
     * 判断天赋是否已经初始化
     * @return 
     */
    boolean isInitialized();
    
    /**
     * 清理并释放资源，当天赋从目标身上移除时应该执行一次该方法。
     */
    void cleanup();
    
    /**
     * 获取天赋的当前等级
     * @return 
     */
    int getLevel();
    
    /**
     * 设置天赋的级别
     * @param level 
     */
    void setLevel(int level);
    
    /**
     * 获取天赋的最高等级限制
     * @return 
     */
    int getMaxLevel();
    
    /**
     * 设置天赋的最高等级限制
     * @param maxLevel
     */
    void setMaxLevel(int maxLevel);
    
    /**
     * 设置天赋的作用目标。
     * @param actor 
     */
    void setActor(Entity actor);
    
}
