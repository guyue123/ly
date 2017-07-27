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
package name.huliqing.luoying.mess;

import com.jme3.network.HostedConnection;
import com.jme3.network.serializing.Serializable;
import name.huliqing.luoying.Factory;
import name.huliqing.luoying.layer.network.EntityNetwork;
import name.huliqing.luoying.layer.service.EntityService;
import name.huliqing.luoying.layer.service.PlayService;
import name.huliqing.luoying.network.GameClient;
import name.huliqing.luoying.network.GameServer;
import name.huliqing.luoying.object.entity.Entity;

/**
 * 给目标Entity(target)的指定属性添加值，目标属性必须存在，并且必须是Number类型的，否则什么也不做。
 * @author huliqing
 */
@Serializable
public class EntityHitNumberAttributeMess extends GameMess {
    
    // 受攻击的对象
    private long entityId;
    
    // 角色的属性名称
    private String attribute;
    
    // 属性值
    private float addValue;

    // 攻击源的id，如果不存在则标记为-1
    private long hitterId = -1;
    
    /**
     * 获取作用值的源角色，如果源Entity不存在，则这个值返回-1。
     * @return 
     */
    public long getHitterId() {
        return hitterId;
    }

    /**
     * 设置发起源的id(Entity id), 这个发起源表示是哪一个Entity对目标产生了属性作用值。比如,当角色A对角色B的属性造成了
     * 3点伤害的时候，那么角色A就是发起源。
     * @param hitterId 
     */
    public void setHitterId(long hitterId) { 
        this.hitterId = hitterId;
    }

    /**
     * 获取接收属性值作用的目标角色。
     * @return 
     */
    public long getEntityId() {
        return entityId;
    }

    /**
     * 设置接受属性值作用的目标角色,或者说是被击中的角色。
     * @param entityId 
     */
    public void setEntityId(long entityId) {
        this.entityId = entityId;
    }

    /**
     * 获取所作用的属性名称
     * @return 
     */
    public String getAttribute() {
        return attribute;
    }

    /**
     * 设置属性名称，目标（target）必须存在这个属性，并且必须是Number类型的，否则没有意义。
     * @param attribute 
     */
    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    /**
     * 获取属性的作用值。
     * @return 
     */
    public float getAddValue() {
        return addValue;
    }

    /**
     * 设置属性的作用值, 这个作用值将直接添加到目标target指定的属性上, 这个值可正可负。
     * @param addValue 
     */
    public void setAddValue(float addValue) {
        this.addValue = addValue;
    }

    @Override
    public void applyOnClient(GameClient gameClient) {
        super.applyOnClient(gameClient);
        PlayService playService = Factory.get(PlayService.class);
        Entity hitter = null;
        if (hitterId > 0) {
            hitter = playService.getEntity(hitterId);
        }
        Entity entity = playService.getEntity(entityId);
        if (entity != null) {
            Factory.get(EntityService.class).hitNumberAttribute(entity, attribute, addValue, hitter);
        }
    }

    @Override
    public void applyOnServer(GameServer gameServer, HostedConnection source) {
        super.applyOnServer(gameServer, source);
        PlayService playService = Factory.get(PlayService.class);
        Entity hitter = null;
        if (hitterId > 0) {
            hitter = playService.getEntity(hitterId);
        }
        Entity entity = playService.getEntity(entityId);
        if (entity != null) {
            Factory.get(EntityNetwork.class).hitNumberAttribute(entity, attribute, addValue, hitter);
        }
    }
    
}
