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
package name.huliqing.luoying.object.module;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import name.huliqing.luoying.Config;
import name.huliqing.luoying.data.DropData;
import name.huliqing.luoying.data.ModuleData;
import name.huliqing.luoying.object.Loader;
import name.huliqing.luoying.object.attribute.Attribute;
import name.huliqing.luoying.object.attribute.BooleanAttribute;
import name.huliqing.luoying.object.drop.Drop;
import name.huliqing.luoying.object.entity.Entity;
import name.huliqing.luoying.utils.FastStack;
import name.huliqing.luoying.xml.ObjectData;
import name.huliqing.luoying.object.entity.HitAttributeListener;

/**
 * “掉落模块”， 给实体配置这个模块，这样实体就具有了掉落各种各样物品的功能，通常配置给活动类型的角色。
 * 这样就可以让角色在死亡的时候可以随机掉落一些物体，
 * 例如：掉落经验值、杂物（ItemData)、装备等。该模块需要绑定一些参数，用于判断角色是否死亡。
 * @author huliqing
 */
public class DropModule extends AbstractModule {
 
    private static final Logger LOG = Logger.getLogger(DropModule.class.getName());

    // 绑定角色的“死亡”属性
    private String bindDeadAttribute;
    
    // remove20161218,不要再在DropModule中使用掉落声效，这会造成所有角色掉落物品的时候都会有提示音。
//    // 掉落物品时的默认提示声效
//    private String[] sounds; 
    
    // ---- inner
    private List<Drop> drops;
    private BooleanAttribute deadAttribute;
    
    // 用于监听Entity受到攻击致死事件,并处理物品掉落
    private final HitAttributeListener hitDeadEntityListener = new KilledEntityAttributeListener();

    @Override
    public void setData(ModuleData data) {
        super.setData(data); 
        bindDeadAttribute = data.getAsString("bindDeadAttribute");
//        sounds = data.getAsArray("sounds");
    }
    
    @Override
    public void initialize() {
        super.initialize(); 
        if (bindDeadAttribute != null) { 
            deadAttribute = getAttribute(bindDeadAttribute, BooleanAttribute.class);
        }
        
        List<DropData> dropDatas = entity.getData().getObjectDatas(DropData.class, new ArrayList<DropData>());
        if (dropDatas != null) {
            for (DropData id : dropDatas) {
                addDrop((Drop) Loader.load(id));
            }
        }
        entity.addHitAttributeListener(hitDeadEntityListener);
    }

    @Override
    public void cleanup() {
        if (drops != null) {
            drops.clear();
        }
        entity.removeHitAttributeListener(hitDeadEntityListener);
        super.cleanup(); 
    }
    
    /**
     * 添加一个掉落设置
     * @param drop 
     * @return  
     */
    private boolean addDrop(Drop drop) {
        if (drops == null) {
            drops = new ArrayList<Drop>(5);
        }
        if (drops.contains(drop))
            return false;
        
        drops.add(drop);
        entity.getData().addObjectData(drop.getData());
        return true;
    }
    
    /**
     * 处理掉落物品给指定角色, 注：物品是从当前角色掉落到指定角色(target)身上。
     * @param target
     */
    public void doDrop(Entity target) {
        if (!isEnabled()) 
            return;
        
        if (drops == null) {
            return;
        }
        for (int i = 0; i < drops.size(); i++) {
            drops.get(i).doDrop(entity, target);
        }
    }

    @Override
    public boolean handleDataAdd(ObjectData handleData, int amount) {
        if (!(handleData instanceof DropData))
            return false;
        
        if (drops != null) {
            for (Drop d : drops) {
                if (d.getData() == handleData || d.getData().getId().equals(handleData.getId())) {
                    return false; // 已经存在
                }
            }
        }
        return addDrop((Drop) Loader.load(data));
    }

    @Override
    public boolean handleDataRemove(ObjectData handleData, int amount) {
        if (!(handleData instanceof DropData))
            return false;
        
        if (drops == null) {
            return false;
        }
        Drop found = null;
        for (Drop d : drops) {
            if (d.getData() == handleData) {
                found = d;
                break;
            }
        }
        if (found == null) {
            return false;
        }
        drops.remove(found);
        entity.getData().removeObjectData(found.getData());
        return true;
    }

    @Override
    public boolean handleDataUse(ObjectData handleData) {
        if (!(handleData instanceof DropData))
            return false;
        
        Drop drop = Loader.load(handleData);
        drop.doDrop(entity, null);
        return true;
    }
    
    private class KilledEntityAttributeListener implements HitAttributeListener{
        // 这里必须用Stack方式，因为onHitAttributeBefore，onHitAttributeAfter随然是成对出现的，
        // 但是不能保证他们是连续调用的。
        private final FastStack<Boolean> deadStack = new FastStack<Boolean>(2);
        
        @Override
        public void onHitAttributeBefore(Attribute attribute, Object hitValue, Entity hitter) {
            if (deadAttribute == null) 
                return;
            deadStack.push(deadAttribute.getValue());
        }
        
        @Override
        public void onHitAttributeAfter(Attribute attribute, Object hitValue, Entity hitter, Object oldValue) {
            if (deadAttribute == null)
                return;
            // killed
            boolean oldDeadState = deadStack.pop();
            if (!oldDeadState && deadAttribute.getValue()) {
                if (Config.debug) {
                    LOG.log(Level.INFO, "{0} hit by {1} with hitValue={2}, oldValue={3}, dead doDrop now.", 
                            new Object[] {entity.getData().getId(), hitter != null ? hitter.getData().getId() : null, hitValue, oldValue});
                }
                doDrop(hitter);
            }
        }
    }
}
