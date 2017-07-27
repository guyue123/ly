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
package name.huliqing.luoying.object.entity.impl;

import com.jme3.asset.AssetManager;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Spatial;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;
import name.huliqing.luoying.LuoYing;
import name.huliqing.luoying.data.EntityData;
import name.huliqing.luoying.object.entity.ModelEntity;

/**
 * 天空盒模型,天空作为一类特别的物体不继承自ModelEntity
 * @author huliqing
 */
public class SkyBoxEntity extends ModelEntity {

    // 基本文件夹路径，如：“Textures/tex/sky/default_min/"
    // 如果指定了这个路径，则把它加在west,east,north,south,up,down前面
    private String baseDir;
    
    private String west;
    private String east;
    private String north;
    private String south;
    private String up;
    private String down;
    
    // ---- inner
    private Spatial sky;

    @Override
    public void setData(EntityData data) {
        super.setData(data);
        baseDir = data.getAsString("baseDir");
        west = data.getAsString("west");
        east = data.getAsString("east");
        north = data.getAsString("north");
        south = data.getAsString("south");
        up = data.getAsString("up");
        down = data.getAsString("down");
    }

    @Override
    public void updateDatas() {
        super.updateDatas();
    }
    
    @Override
    protected Spatial loadModel() {
        AssetManager am = LuoYing.getApp().getAssetManager();
        Texture w = am.loadTexture(baseDir != null ? baseDir + west : west);
        Texture e = am.loadTexture(baseDir != null ? baseDir + east : east);
        Texture n = am.loadTexture(baseDir != null ? baseDir + north : north);
        Texture s = am.loadTexture(baseDir != null ? baseDir + south : south);
        Texture u = am.loadTexture(baseDir != null ? baseDir + up : up);
        Texture d = am.loadTexture(baseDir != null ? baseDir + down : down);
        sky = SkyFactory.createSky(am, w, e, n, s, u, d);
        return sky;
    }
    
    @Override
    public void initEntity() {
        super.initEntity();
        // 这是SKY必须覆盖掉的设置，
        sky.setCullHint(Spatial.CullHint.Never);
        sky.setQueueBucket(RenderQueue.Bucket.Sky);
        // 对于天空盒来说，位置没有关系，把位置移到一个较远的距离可以避免在用射线检测获取场景物体的时候错误获得天空盒
        // 特别是在编辑器中拖放物体到场景的时候会导致物体拖放到了空中，而不是在地面上。
        sky.setLocalTranslation(-100000, -100000, -100000);
    }
    
}
