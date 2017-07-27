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
package name.huliqing.ly.mess;

import com.jme3.math.Vector3f;
import com.jme3.network.HostedConnection;
import com.jme3.network.serializing.Serializable;
import name.huliqing.luoying.Factory;
import name.huliqing.luoying.layer.service.PlayService;
import name.huliqing.luoying.data.ConnData;
import name.huliqing.luoying.mess.GameMess;
import name.huliqing.luoying.network.GameServer;
import name.huliqing.luoying.object.actor.Actor;
import name.huliqing.luoying.object.entity.Entity;
import name.huliqing.ly.layer.network.GameNetwork;

/**
 * 向目标位置移动
 * @author huliqing
 */
@Serializable
public class ActionRunMess extends GameMess {
    
    // 角色唯一id
    private long actorId;
    // 目标位置
    private Vector3f pos;

    public long getActorId() {
        return actorId;
    }

    public void setActorId(long actorId) {
        this.actorId = actorId;
    }

    public Vector3f getPos() {
        return pos;
    }

    public void setPos(Vector3f pos) {
        this.pos = pos;
    }

    @Override
    public void applyOnServer(GameServer gameServer, HostedConnection source) {
        super.applyOnServer(gameServer, source);
        
        // remove20161121
//        PlayService playService = Factory.get(PlayService.class);
//        ActorNetwork actorNetwork = Factory.get(ActorNetwork.class);
//        ActionService actionService = Factory.get(ActionService.class);
//        
//        Entity actor = (Actor) playService.getEntity(actorId);
//        if (actor == null) 
//            return;
//
//        ConnData cd = source.getAttribute(ConnData.CONN_ATTRIBUTE_KEY);
//        Long uniqueActorId = cd != null ? cd.getEntityId() : null;
//        
//        if (uniqueActorId != actor.getData().getUniqueId()) {
//            return; // 不允许控制别人的角色
//        }
//        
//        // remove20151229服务端以后一直打开AI
////        actorService.setAutoAi(actor, false);
//   
//        // remove20161103
////        // 必须清除跟随
////        actorNetwork.setFollow(actor, -1);
//        
//        // 走向目标,action不需要广播
//        actionService.playRun(actor, pos);

        // 角色必须存在
        Entity actor = (Actor) Factory.get(PlayService.class).getEntity(actorId);
        if (actor == null) 
            return;

        // 不允许控制别人的角色
        ConnData cd = source.getAttribute(ConnData.CONN_ATTRIBUTE_KEY);
        long uniqueActorId = cd != null ? cd.getEntityId() : -1;
        if (uniqueActorId != actor.getData().getUniqueId()) {
            return; 
        }
        
        // 走向目标,action不需要广播
        GameNetwork gameNetwork = Factory.get(GameNetwork.class);
        gameNetwork.playRunToPos(actor, pos);
    }
    
}
