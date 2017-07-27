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

import com.jme3.light.DirectionalLight;
import com.jme3.math.Vector3f;
import name.huliqing.luoying.data.EntityData;
import name.huliqing.luoying.object.entity.LightEntity;
import name.huliqing.luoying.object.entity.NonModelEntity;
import name.huliqing.luoying.object.scene.Scene;

/**
 *  直射光实体
 * @author huliqing
 */
public class DirectionalLightEntity extends NonModelEntity implements LightEntity{
 
    private final DirectionalLight light = new DirectionalLight();
    
    @Override
    public void setData(EntityData data) {
        super.setData(data);
        light.setName(data.getName());
        light.setColor(data.getAsColor("color", light.getColor()));
        light.setDirection(data.getAsVector3f("direction", light.getDirection()));
    }
    
    @Override
    public void updateDatas() {
        super.updateDatas();
        data.setAttribute("name", light.getName());
        data.setAttribute("color", light.getColor());
        data.setAttribute("direction", light.getDirection());
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        light.setEnabled(enabled);
    }
    
    @Override
    public void initEntity() {
        light.setEnabled(isEnabled());
    }

    @Override
    public void onInitScene(Scene scene) {
        super.onInitScene(scene); 
        scene.getRoot().addLight(light);
    }

    @Override
    public void cleanup() {
        scene.getRoot().removeLight(light);
        super.cleanup();
    }
    
    @Override
    public DirectionalLight getLight() {
        return light;
    }
    
    public Vector3f getDirection() {
        return light.getDirection();
    }
    
    public void setDirection(Vector3f direction) {
        light.setDirection(direction);
    }
    
    
}
