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
package test;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;

/**
 *
 * @author huliqing
 */
public class Test {
    
    public static void main(String[] args) {
/*    	startPickLoc:(679.20575, 22.333984, 651.58655)
    	endPickLoc:(465.3236, 22.333862, 707.4158)
    	startSpatialLocation:(526.39923, 22.334003, 491.9757)*/
    	
    	
    	Vector3f v3f1 = new Vector3f(679.20575f, 425.333984f,  651.58655f);
    	Vector3f v3f2 = new Vector3f(665.3236f, 66.333862f, 757.4158f);
    	Vector3f v3f3 = new Vector3f(526.39923f, 22.334003f, 491.9757f);
    	
    	Quaternion transforme = new Quaternion();
    	Vector3f v1 = transforme.mult(v3f1.subtract(v3f3).normalize());
    	Vector3f v2 = transforme.mult(v3f2.subtract(v3f3).normalize());
    	
/*    	Vector3f v1 = v3f1.subtract(v3f3);
    	Vector3f v2 = v3f2.subtract(v3f3);*/
    	
    	System.out.println(v1.angleBetween(v2));
    }
}
