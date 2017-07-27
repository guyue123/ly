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
package name.huliqing.luoying.object.skill;

/**
 * 时间检测点控制器,注意，如果是循环的技能，可能需要在循环开始时进行reset.
 * @author huliqing
 */
public class PointChecker {
    
    // 各个时间点
    private float[] points;
    // 总时间范围
    private float maxTime;
    // 当前获取到的检测点，
    private int currentIndex = -1; 
    // 下一个检测点索引
    private int nextIndex; 
    
    /**
     * 设置技能的检测时间点。
     * @param checkPoint 
     */
    public void setCheckPoint(float[] checkPoint) {
        this.points = checkPoint;
    }
    
    /**
     * 设置技能所使用的最大时长。
     * @param maxTime 
     */
    public void setMaxTime(float maxTime) {
        this.maxTime = maxTime;
    }
    
    /**
     * 获取下一个检测点的索引，如果检测点未到达或已经完成所有检测点，则返回-1;
     * @param currentTime
     * @return 
     */
    public int nextPoint(float currentTime) {
        if (points == null || points.length == 0) {
            return -1;
        }
        for (int i = nextIndex; i < points.length; i++) {
            if (currentTime >= (points[i] * maxTime)) {
                currentIndex = i;
                nextIndex = i + 1;
                return currentIndex;
            } else {
                break;
            }
        }
        return -1;
    }
    
    /**
     * 获取当前获得到的检测点索引，如果还没有检测点，则返回-1.
     * @return 
     */
    public int getIndex() {
        return currentIndex;
    }
    
    /**
     * 重置检测点，该方法将当前已经获得的检测点倒回到开始状态。
     */
    public void rewind() {
        currentIndex = -1;
        nextIndex = 0;
    }
    
}
