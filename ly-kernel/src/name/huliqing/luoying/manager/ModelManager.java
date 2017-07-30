/*
 * ModelManager.java 4 juil. 07
 *
 * Sweet Home 3D, Copyright (c) 2007 Emmanuel PUYBARET / eTeks <info@eteks.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package name.huliqing.luoying.manager;

import com.jme3.bounding.BoundingBox;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 * Singleton managing 3D models cache.
 * This manager supports 3D models with an OBJ, DAE, 3DS or LWS format by default. 
 * Additional classes implementing Java 3D <code>Loader</code> interface may be 
 * specified in the <code>com.eteks.sweethome3d.j3d.additionalLoaderClasses</code>
 * (separated by a space or a colon :) to enable the support of other formats.<br> 
 * Note: this class is compatible with Java 3D 1.3.
 * @author Emmanuel Puybaret
 */
public class ModelManager {
  private static final float MINIMUM_SIZE = 0.001f;

  private static ModelManager instance;
  
  
  /**
   * 获得模型的长宽高
   * @param sp
   * @return
   */
  public Vector3f getSize(Spatial sp) {
	  if (sp instanceof Node) {
		  return getSize((Node)sp);
	  }
	  
	  return null;
  }
  
  /**
   * Returns the size of 3D shapes of <code>node</code> after an additional <code>transformation</code>.
   * This method computes the exact box that contains all the shapes,
   * contrary to <code>node.getBounds()</code> that returns a bounding 
   * sphere for a scene.
   * @param node     the root of a model 
   * @param transformation the transformation applied to the model  
   *                 or <code>null</code> if no transformation should be applied to node.
   */
  public Vector3f getSize(Node node) {
    BoundingBox bounds = getBounds(node);
    if (bounds == null) {
    	return null;
    }
    
    Vector3f lower = new Vector3f();
    bounds.getMin(lower);
    Vector3f upper = new Vector3f();
    bounds.getMax(upper);
    return new Vector3f(Math.max(getMinimumSize(), (float)(upper.x - lower.x)), 
        Math.max(getMinimumSize(), (float)(upper.y - lower.y)), 
        Math.max(getMinimumSize(), (float)(upper.z - lower.z)));
  }
 
  /**
   * Returns the bounds of the 3D shapes of <code>node</code> with an additional <code>transformation</code>.
   * This method computes the exact box that contains all the shapes, contrary to <code>node.getBounds()</code> 
   * that returns a bounding sphere for a scene.
   * @param node     the root of a model 
   * @param transformation the transformation applied to the model  
   *                 or <code>null</code> if no transformation should be applied to node.
   */
  public BoundingBox getBounds(Node node) {
    BoundingBox objectBounds = computeBounds(node, null);
    if (objectBounds == null) {
    	return null;
    }
    
    Vector3f lower = new Vector3f();
    objectBounds.getMin(lower);
    if (lower.x == Float.POSITIVE_INFINITY) {
      throw new IllegalArgumentException("Node has no bounds");
    }
    return objectBounds;
  }

  private BoundingBox computeBounds(Node node, BoundingBox bounds) {
  	return mergeBoundingBox(node, bounds);
  }
  
  private BoundingBox mergeBoundingBox(Node node, BoundingBox bbox) {
      for (Spatial sp : node.getChildren()) {
        if (sp instanceof Node) {
      	  mergeBoundingBox((Node)sp, bbox);
      	  continue;
        } else if (sp instanceof Geometry) {
          Geometry ge = (Geometry)sp;
          
          if (bbox == null) {
        	  if (ge.getModelBound() instanceof BoundingBox) {
        		  bbox = (BoundingBox)ge.getModelBound();
        	  }
          } else {
        	  bbox.merge(ge.getModelBound());
          }
        }
      }
      
      return bbox;
    }
  
  
  /**
   * Returns the center vector of the model <code>node</code>
   * to let it fill a box of the given <code>width</code> centered on the origin.
   * @param node     the root of a model with any size and location
   */
  public Vector3f getModelCenter(Node node) {
	    // Get model bounding box size 
	    BoundingBox modelBounds = getBounds(node);
	    if (modelBounds == null) {
	    	return null;
	    }
	    Vector3f lower = new Vector3f();
	    modelBounds.getMin(lower);
	    Vector3f upper = new Vector3f();
	    modelBounds.getMax(upper);
	    //  model center

	    return new Vector3f(-(-lower.x - (upper.x - lower.x) / 2), 
	            -(-lower.y - (upper.y - lower.y) / 2), 
	            -(-lower.z - (upper.z - lower.z) / 2));
  }
  
  /**
   * 获得模型的中心点
   * @param sp
   * @return
   */
  public Vector3f getModelCenter(Spatial sp) {
	  if (sp instanceof Node) {
		  Vector3f c = getModelCenter((Node)sp);
		  if (c == null) {
			  return null;
		  }
		  
		  return c.mult(sp.getLocalScale());
	  }
	  
	  return null;
  }
  
  
  /**
   * Returns an instance of this singleton. 
   */
  public static ModelManager getInstance() {
    if (instance == null) {
      instance = new ModelManager();
    }
    return instance;
  }

  /**
   * Returns the minimum size of a model.
   */
  private float getMinimumSize() {
    return MINIMUM_SIZE;
  }
  
  /**
   * 获得物体中心坐标
   * @param sp
   * @return
   */
  public Vector3f getTranslation(Spatial sp) {
	  Vector3f v3f = getInstance().getModelCenter(sp);
	  if (v3f == null) {
		  return sp.getWorldTranslation().clone();
	  }
	  
	  Vector3f center = sp.getWorldTranslation().clone().add(sp.getLocalRotation().mult(v3f));
	  return center;
  }
}
