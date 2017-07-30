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
package name.huliqing.luoying.object;

import java.io.File;

import com.jme3.asset.AssetLocator;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.asset.plugins.ZipLocator;
import com.jme3.bounding.BoundingBox;
import com.jme3.bounding.BoundingSphere;
import com.jme3.bounding.BoundingVolume;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

import name.huliqing.luoying.LuoYing;
import name.huliqing.luoying.manager.ModelManager;
import name.huliqing.luoying.utils.GeometryUtils;

/**
 * 这个类只允许在loader包内调用，在其它包的类不要直接调用该类（除测试用之外）。
 * @author huliqing
 */
public class AssetLoader {
    
    /**
     * 载入模型，但是会根据系统是否使用light进行makeUnshaded
     * @param file
     * @return 
     */
    public static Spatial loadModel(String file) {
    	File f = new File(file);
    	String locatorPath = null;
    	Class<? extends AssetLocator> locatorClass = null;
    	if (f.exists() && f.isFile()) {
    		if (f.getName().toLowerCase().endsWith("obj")) {
    			LuoYing.getAssetManager().registerLocator(f.getParent(), FileLocator.class);
    			file = f.getName();
    			locatorPath = f.getParent();
    			locatorClass = FileLocator.class;
    		} else if (f.getName().toLowerCase().endsWith("zip")) {
    			LuoYing.getAssetManager().registerLocator(f.getAbsolutePath(), ZipLocator.class);
    			file = f.getName().substring(0, f.getName().lastIndexOf(".")) + ".obj";
    			
    			locatorPath = f.getAbsolutePath();
    			locatorClass = ZipLocator.class;
    		}
    	}
    	// 删除缓存模型
    	LuoYing.getAssetManager().clearCache();
    	Spatial sp = LuoYing.getAssetManager().loadModel(file);
    	
/*    	if (sp instanceof Node) {
    		Vector3f v3f = ModelManager.getInstance().getSize(sp);
    		System.out.println(v3f);
    		
    		Vector3f v3f2 = ModelManager.getInstance().getModelCenter(sp);
    		System.out.println(v3f2);
    	}*/
    	
    	if (locatorPath != null) {
    		LuoYing.getAssetManager().unregisterLocator(locatorPath, locatorClass);
    	}
    	return sp;
    }

    
    /**
     * 重新设置模型中心
     * @param node
     */
    private static void relocationObj(Node node) {
      for (Spatial sp : node.getChildren()) {
        if (sp instanceof Node) {
          relocationObj((Node)sp);
          continue;
        } else if (sp instanceof Geometry) {
          Geometry ge = (Geometry)sp;
          BoundingVolume bv = ge.getModelBound();
          Vector3f center = bv.getCenter();
          
          // 重置对象位置
          if (bv instanceof BoundingBox) {
            BoundingBox bb = (BoundingBox)bv;
            float x = bb.getXExtent();
            float y = bb.getYExtent();
            float z = bb.getZExtent();
            Vector3f newCenter = new Vector3f(center.getX() - x, center.getY() - y, center.getZ() - z);
            bv.setCenter(newCenter);
            
            bb.setXExtent(0);
            bb.setYExtent(0);
            bb.setZExtent(0);
          } else if (bv instanceof BoundingSphere) {
            BoundingSphere bp = (BoundingSphere)bv;
          }
        }
      }
    }
    
    /**
     * 载入模型，这个方法载入模型时会偿试把模型的材质替换为unshaded的。
     * @param file
     * @return 
     */
    public static Spatial loadModelUnshaded(String file) {
        Spatial model = LuoYing.getAssetManager().loadModel(file);
        GeometryUtils.makeUnshaded(model);
        return model;
    }
    
}
