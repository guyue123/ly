import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;

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


/**
 *
 * @author huliqing
 */
public class Test {
    
    public static void main(String[] args) {
    	Vector2f v1 = new Vector2f(-1, -1);
    	System.out.println(rotate(FastMath.PI/4, true).angleBetween(v1) * 180/FastMath.PI);
    	System.out.println(rotate(FastMath.PI/2 + FastMath.PI/6, false).angleBetween(v1) * 180/FastMath.PI);
    	
    	
    	
    	Vector2f V3 = new Vector2f(0.80266863f, -0.59642506f);
    	Vector2f V4 = new Vector2f(-0.19634892f, 0.98053414f);
    	System.out.println(V3.angleBetween(V4) * 180/FastMath.PI);
        
        // 旋转30
/*        System.out.println(rotate(FastMath.PI/6, true));
        System.out.println(rotate(FastMath.PI/4, true));
        System.out.println(rotate(FastMath.PI/3, true));
        System.out.println(rotate(FastMath.PI/2, true));
        System.out.println(rotate(FastMath.PI/1, true));
        System.out.println(rotate(FastMath.PI + FastMath.PI/6 , true));
        System.out.println(rotate(FastMath.PI + FastMath.PI/4 , true));
        System.out.println(rotate(FastMath.PI + FastMath.PI/3 , true));
        System.out.println(rotate(FastMath.PI + FastMath.PI/2 , true));*/
    }
    
    public static Vector2f rotate(float angle, boolean cw) {
    	Vector2f v1 = new Vector2f(-1, -1);
    	v1.rotateAroundOrigin(angle, cw);
    	return v1;
    }
}
