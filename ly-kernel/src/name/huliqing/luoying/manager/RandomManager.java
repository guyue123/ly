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
package name.huliqing.luoying.manager;

import java.util.Random;

/**
 * 随机数管理器, 随机数是预先产生的。这个管理器会预先生成127个固定的随机数。
 * 通过设置随机种子可以重新产生随机数。
 * 游戏过程中可以通过定时从服务端向客户端发送种子以同步更新随机数。
 * @author huliqing
 */
public class RandomManager {
    
    private final static int MAX_RANDOM = 127;
    
    private static long seed;
    private static final float[] VALUES = new float[MAX_RANDOM];
    private static byte index = 0;
    
    static {
        RandomManager.setRandomSeed(9999L);
    }
    
    /**
     * 获取下一个随机值。
     * @return 
     */
    public static float getNextValue() {
         if (index >= MAX_RANDOM) {
            index = 0;
        }
        return VALUES[index++];
    }
    
    /**
     * 设置随机种子，注意：该方法会重新产生随机数。
     * @param seed 
     */
    public static void setRandomSeed(long seed) {
        RandomManager.seed = seed;
        Random random  = new Random(seed);
        for (int i = 0; i < MAX_RANDOM; i++) {
            VALUES[i] = random.nextFloat();
        }
    }
    
    /**
     * 获得当前的随机种子
     * @return 
     */
    public static long getRandomSeed() {
        return seed;
    }
    
    /**
     * 获取当前随机数的索引值。
     * @return 
     */
    public static byte getIndex() {
        return index;
    }
    
    /**
     * 设置随机数索引, 不要直接调用，这个方法只有在作为客户端与服务端同步随机数索引值时才会被调用。
     * @param index 
     */
    public static void setIndex(byte index) {
        RandomManager.index = index;
    }
    
    public static void main(String[] args) {
        for (int i = 0; i < 127; i++) {
            System.out.println(i + "->" + getNextValue() + ", index=" + index);
        }
        System.out.println("...");
    }
}
