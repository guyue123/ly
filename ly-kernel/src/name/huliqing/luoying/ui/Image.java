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
package name.huliqing.luoying.ui;

import com.jme3.asset.TextureKey;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture2D;
import name.huliqing.luoying.constants.AssetConstants;

/**
 * Image
 * @author huliqing
 */
public class Image extends Geometry {
    
    private float width = 1f;
    private float height = 1f;
    
    public Image() {
        this("", false);
    }
    
    public Image(String name, boolean flipY) {
        super(name, new Quad(1, 1, flipY));
        setQueueBucket(Bucket.Gui);
        setCullHint(CullHint.Never);
        
        Material mat = new Material(UIFactory.getUIConfig().getAssetManager(), AssetConstants.MATERIAL_VIEW);
        mat.setColor("Color", ColorRGBA.White);
        setMaterial(mat);
    }
    
    public void setWidth(float width) {
        this.width = width;
        setLocalScale(width, height, 1);
    }
    
    public void setHeight(float height) {
        this.height = height;
        setLocalScale(width, height, 1);
    }
    
    /**
     * Set the position of the picture in pixels.
     * The origin (0, 0) is at the bottom-left of the screen.
     * 
     * @param x The x coordinate
     * @param y The y coordinate
     */
    public void setPosition(float x, float y) {
        float z = getLocalTranslation().getZ();
        setLocalTranslation(x, y, z);
    }
    
    public void setFile(String file) {
        
        TextureKey texKey;
        Texture2D tex;
        try {
            texKey = new TextureKey(file, true);
            tex = (Texture2D) UIFactory.getUIConfig().getAssetManager().loadTexture(texKey);
            material.setTexture("Texture", tex);
        } catch (Exception e) {
            texKey = new TextureKey(UIFactory.getUIConfig().getMissIcon(), true);
            tex = (Texture2D) UIFactory.getUIConfig().getAssetManager().loadTexture(texKey);
            material.setTexture("Texture", tex);
        }
    }
    
    public void setColor(ColorRGBA color) {
        material.setColor("Color", color);
    }
    
    public void setUseAlpha(boolean useAlpha) {
        material.getAdditionalRenderState().setBlendMode(useAlpha ? BlendMode.Alpha : BlendMode.Off);
    }
}
