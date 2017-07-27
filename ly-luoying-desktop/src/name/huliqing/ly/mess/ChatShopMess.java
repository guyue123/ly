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

import com.jme3.network.HostedConnection;
import com.jme3.network.serializing.Serializable;
import name.huliqing.luoying.Factory;
import name.huliqing.ly.layer.network.ChatNetwork;
import name.huliqing.ly.layer.service.ChatService;
import name.huliqing.luoying.layer.service.PlayService;
import name.huliqing.luoying.mess.GameMess;
import name.huliqing.luoying.network.GameClient;
import name.huliqing.luoying.network.GameServer;
import name.huliqing.luoying.object.entity.Entity;

/**
 * 角色向另一个角色购买商品
 * @author huliqing
 */
@Serializable
public class ChatShopMess extends GameMess {
    private final transient PlayService playService = Factory.get(PlayService.class);
    private final transient ChatNetwork chatNetwork = Factory.get(ChatNetwork.class);
    private final transient ChatService chatService = Factory.get(ChatService.class);
        
    private long seller;
    private long buyer;
    private long objectId;
    private int count;
    private float discount;
 
    public long getSeller() {
        return seller;
    }

    public void setSeller(long seller) {
        this.seller = seller;
    }

    public long getBuyer() {
        return buyer;
    }

    public void setBuyer(long buyer) {
        this.buyer = buyer;
    }

    public long getObjectId() {
        return objectId;
    }

    public void setObjectId(long objectId) {
        this.objectId = objectId;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public float getDiscount() {
        return discount;
    }

    public void setDiscount(float discount) {
        this.discount = discount;
    }

    @Override
    public void applyOnServer(GameServer gameServer, HostedConnection source) {
        super.applyOnServer(gameServer, source);
        Entity sellerActor = playService.getEntity(seller);
        Entity buyerActor = playService.getEntity(buyer);
        if (sellerActor == null || buyerActor == null) {
            return;
        }
        chatNetwork.chatShop(sellerActor, buyerActor, objectId, count, discount);
    }

    @Override
    public void applyOnClient(GameClient gameClient) {
        super.applyOnClient(null);
        Entity sellerActor = playService.getEntity(seller);
        Entity buyerActor = playService.getEntity(buyer);
        if (sellerActor == null || buyerActor == null) {
            return;
        }
        chatService.chatShop(sellerActor, buyerActor, objectId, count, discount);
    }
    
}
