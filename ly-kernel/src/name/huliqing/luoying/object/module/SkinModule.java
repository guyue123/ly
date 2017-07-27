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

import com.jme3.util.SafeArrayList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import name.huliqing.luoying.Config;
import name.huliqing.luoying.Factory;
import name.huliqing.luoying.data.ModuleData;
import name.huliqing.luoying.data.SkinData;
import name.huliqing.luoying.layer.service.DefineService;
import name.huliqing.luoying.message.StateCode;
import name.huliqing.luoying.object.Loader;
import name.huliqing.luoying.object.attribute.CollectionAttribute;
import name.huliqing.luoying.object.skin.Skin;
import name.huliqing.luoying.object.skin.Weapon;
import name.huliqing.luoying.object.skin.WeaponSkin;
import name.huliqing.luoying.xml.ObjectData;

/**
 * 皮肤模块，让实体具有换肤功能，主要处理SkinData这一类物体
 * @author huliqing
 */
public class SkinModule extends AbstractModule {
    private static final Logger LOG = Logger.getLogger(SkinModule.class.getName());
    private final DefineService defineService = Factory.get(DefineService.class);
    
    // 监听角色装备、武器等的穿脱
    private List<SkinListener> skinListeners;
    
    // 缓存当前正在使用的武器状态, 武器状态用来标识不同的武器组合，不同组合的武器或者不同排列顺序的武器都会产生
    // 唯一的武器状态码。
    private long cacheWeaponState = 0L;
    
    // 角色所有的皮肤，包含装备、武器（含skinUseds）
    private SafeArrayList<Skin> skinAll;
    // 当前穿在身上的装备和武器
    private SafeArrayList<Skin> skinUsed;
    
    // 当前武器状态
    private boolean weaponTakeOn;
    
    // 绑定一个用于存放武器槽位的属性，必须是CollectionAttribute,这个属性中存放着角色优先支持的武器槽位列表（id)
    private String bindWeaponSlotAttribute;
    
    // 武器槽位属性
    private CollectionAttribute<String> weaponSlotAttribute;

    @Override
    public void setData(ModuleData data) {
        super.setData(data);
        weaponTakeOn = data.getAsBoolean("weaponTakeOn", weaponTakeOn);
        bindWeaponSlotAttribute = data.getAsString("bindWeaponSlotAttribute");
    }
     
    @Override
    public void updateDatas() {
        super.updateDatas();
        data.setAttribute("weaponTakeOn", weaponTakeOn);
    }
    
    @Override
    public void initialize() {
        super.initialize(); 
        if (bindWeaponSlotAttribute != null) {
            weaponSlotAttribute = entity.getAttribute(bindWeaponSlotAttribute);
        }
        
        // 穿上普通装备
        List<SkinData> skinDatas =  entity.getData().getObjectDatas(SkinData.class, new ArrayList<SkinData>());
        if (skinDatas != null && !skinDatas.isEmpty()) {
            skinAll = new SafeArrayList<Skin>(Skin.class);
            for (SkinData sd : skinDatas) {
                Skin skin = Loader.load(sd);
                skinAll.add(skin);
                if (sd.isUsed() && !sd.isBaseSkin()) {
                    attachInner(skin);
                }
            }
        }
        
        attachBaseSkin();
    }

    @Override
    public void cleanup() {
        super.cleanup();
    }
    
    /**
     * 给角色换上装备
     * @param skin
     */
    private void attachSkin(Skin skin) {
        if (!skin.canUse(entity)) {
            return;
        }
        
        // 提前结束其它正在装备过程中的装备
        doEndAllSkinning();
        
        // 脱下排斥的装备
        detachConflicts(skin);

        // 穿上装备
        attachInner(skin);

        // 穿上基本装备进行补全
        attachBaseSkin();
    }

    /**
     * 脱下某件装备
     * @param skin 
     */
    private void detachSkin(Skin skin) {
        // 不需要doEndAllSkinning
//        doEndAllSkinning();
        
        // 脱下装备
        detachInner(skin);
        
        // 穿上基本装备进行补全
        attachBaseSkin();
    }
    
    /**
     * 脱下与指定skin相“排斥”的其它装备
     * @param skin 
     */
    private void detachConflicts(Skin skin) {
        if (skinUsed != null) {
            long conflicts = skin.getConflictParts();
            for (Skin conflictSkin : skinUsed.getArray()) {
                if ((conflictSkin.getConflictParts() & conflicts) != 0) {
                    detachInner(conflictSkin);
                }
            }
        }
    }
    
    /**
     * 补全角色身上的基本装备。
     */
    private void attachBaseSkin() {
        // 获取当前角色已经装备的所有skinTypes
        long typeUsed = 0;
        if (skinUsed != null) {
            for (Skin s : skinUsed.getArray()) {
                typeUsed |= s.getParts();
            }
        }

        if (skinAll != null) {
            for (Skin baseSkin : skinAll.getArray()) {
                if (skinUsed != null && skinUsed.contains(baseSkin)) {
                    continue;
                }
                if (!baseSkin.getData().isBaseSkin()) {
                    continue;
                }
                if ((typeUsed & baseSkin.getParts()) == 0) {
                    attachInner(baseSkin);
                }
            }
        }
    }
    
    private void attachInner(Skin skin) {
        // 穿上装备
        if (skinUsed == null) {
            skinUsed = new SafeArrayList<Skin>(Skin.class); 
        }
        if (!skinUsed.contains(skin)) {
            skinUsed.add(skin);
        }
        skin.attach(entity);
        // 重新缓存武器状态
        if (skin instanceof Weapon) {
            cacheWeaponState();
        }
        if (skinListeners != null) {
            for (SkinListener sl : skinListeners) {
                sl.onSkinAttached(entity, skin);
            }
        }
    }
    
    private void detachInner(Skin skin) {
        skinUsed.remove(skin);
        skin.detach(entity);
        if (skin instanceof Weapon) {
            cacheWeaponState();
        }
        if (skinListeners != null) {
            for (SkinListener sl : skinListeners) {
                sl.onSkinDetached(entity, skin);
            }
        }
    }
    
    /**
     * 判断当前武器是“取出”的还是“挂起”的
     * @return 
     */
    public boolean isWeaponTakeOn() {
        return weaponTakeOn;
    }
    
    /**
     * 把武器备上。
     */
    public void takeOnWeapon() {
        if (weaponTakeOn) {
            return;
        }
        // 提前结束其它正在装备过程中的装备
        doEndAllSkinning();
        
        weaponTakeOn = true;
        updateDatas();
        if (skinUsed != null) {
            for (Skin s : skinUsed.getArray()) {
                if (s instanceof WeaponSkin) {
                    ((WeaponSkin) s).takeOn(entity);
                }
            }
        }
    }
    
    /**
     * 把武器收起
     */
    public void takeOffWeapon() {
        if (!weaponTakeOn) {
            return;
        }
        // 提前结束其它正在装备过程中的装备
        doEndAllSkinning();
        
        weaponTakeOn = false;
        updateDatas();
        if (skinUsed != null) {
            for (Skin s : skinUsed.getArray()) {
                if (s instanceof WeaponSkin) {
                    ((WeaponSkin) s).takeOff(entity);
                }
            }
        }
    }
    
    /**
     * 结束当前正在使用的所有装备的装配过程,因为装备是有“排斥”性的，一些装备与另一些装备是不能同时穿在身上的。
     * 当一件装备要装配到角色身上时，角色身上可能还有其它正在装配(skinning)的装备，这些装备可能会造成冲突。
     * 所以使用这个方法来提前结束正在装配过程中的其它装备, 让这些装备处于正常状态，
     * 这样新装备在装配到角色身上之前可以正确判断并排斥存在冲突的装备。
     */
    private void doEndAllSkinning() {
        if (skinUsed == null)
            return;
        for (Skin s : skinUsed) {
            if (s.isSkinning()) {
                s.forceEndSkinning();
            }
        }
    }
    
    /**
     * 从角色包裹中查找指定ID的Skin
     * @param sd
     * @return 
     */
    public Skin getSkin(SkinData sd) {
        if (skinAll == null)
            return null;
        
        for (Skin s : skinAll) {
            if (isSameSkin(s.getData(), sd)) {
                return s;
            }
        }
        return null;
    }
    
    /**
     * 判断两件装备是否相同，目前两件ID（类型）相同的装备就认为是同一件。
     * @param sd1
     * @param sd2
     * @return 
     */
    private boolean isSameSkin(SkinData sd1, SkinData sd2) {
        // xxx 根据装备属性来区别不同的装备
        return sd1.getId().equals(sd2.getId());
    }
    
    /**
     * 获取当前正在使用中的皮肤，返回的列表只能只读，如果没有则返回空列表.
     * @return 
     */
    public List<Skin> getUsingSkins() {
        if (skinUsed != null) {
            return Collections.unmodifiableList(skinUsed);
        }
        return Collections.EMPTY_LIST;
    }
    
    /**
     * 获取角色可支持的武器槽位的列表,如果角色不支持武器槽位则返回空列表.
     * @return 
     */
    public List<String> getSupportedSlots() {
        if (weaponSlotAttribute != null) {
            return new ArrayList<String>(weaponSlotAttribute.values());
        }
        return Collections.EMPTY_LIST;
    }
    
    /**
     * 获取角色当前正在使用的武器状态。
     * @return 
     */
    public long getWeaponState() {
        if (cacheWeaponState <= 0) {
            cacheWeaponState();
        }
        return cacheWeaponState;
    }
    
    public void addSkinListener(SkinListener skinListener) {
        if (skinListeners == null) {
            skinListeners = new ArrayList<SkinListener>();
        }
        if (!skinListeners.contains(skinListener)) {
            skinListeners.add(skinListener);
        }
    }

    public boolean removeSkinListener(SkinListener skinListener) {
        return skinListeners != null && skinListeners.remove(skinListener);
    }

    public List<SkinListener> getSkinListeners() {
        return skinListeners;
    }

    /**
     * 重新计算并缓存角色的武器状态和当前武器列表
     * @param actor
     */
    private void cacheWeaponState() {
        cacheWeaponState = 0;
        if (skinUsed != null) {
            for (Skin s : skinUsed) {
                if (s instanceof Weapon) {
                    cacheWeaponState |= defineService.getWeaponTypeDefine().convert(((Weapon)s).getWeaponType());
                }
            }
        }
        if (Config.debug) {
            LOG.log(Level.INFO, "cacheWeaponState, actor={0}, weaponStateToBinary={1}, weaponsToString={2}"
                    , new Object[] {entity.getData().getId()
                            , Long.toBinaryString(cacheWeaponState)
                            , defineService.getWeaponTypeDefine().toString(cacheWeaponState)
                    });
        }
    }
    
    @Override
    public boolean handleDataAdd(ObjectData hData, int amount) {
        if (!(hData instanceof SkinData)) 
            return false;
            
         if (amount <= 0)
            return false;

        Skin skin = getSkin((SkinData)hData);
        if (skin != null) {
            skin.getData().setTotal(skin.getData().getTotal() + amount);
        } else {
            skin = Loader.load(hData);
            skin.getData().setTotal(amount);
            if (skinAll == null) {
                skinAll = new SafeArrayList<Skin>(Skin.class);
            }
            skinAll.add(skin);
            entity.getData().addObjectData(skin.getData());
        }
        
        if (skinListeners != null) {
            for (int i = 0; i < skinListeners.size(); i++) {
                skinListeners.get(i).onSkinAdded(entity, skin);
            }
        }
        
        addEntityDataAddMessage(StateCode.DATA_ADD, hData, amount);
        
        return true;
    }

    @Override
    public boolean handleDataRemove(ObjectData hData, int amount) {
        if (!(hData instanceof SkinData)) 
            return false;
            
        Skin skin = getSkin((SkinData) hData);
        if (skin == null) {
            addEntityDataRemoveMessage(StateCode.DATA_REMOVE_FAILURE_NOT_FOUND, hData, amount);
            return false;
        }
        // 正在使用中的装备不能删除
        if (skinUsed != null && skinUsed.contains(skin)) {
            addEntityDataRemoveMessage(StateCode.DATA_REMOVE_FAILURE_IN_USING, hData, amount);
            return false;
        }
        
        skin.getData().setTotal(skin.getData().getTotal() - amount);
        if (skin.getData().getTotal() <= 0) {
            entity.getData().removeObjectData(skin.getData());
            skinAll.remove(skin);
        }
        
        if (skinListeners != null) {
            for (int i = 0; i < skinListeners.size(); i++) {
                skinListeners.get(i).onSkinRemoved(entity, skin);
            }
        }
        addEntityDataRemoveMessage(StateCode.DATA_REMOVE, hData, amount);
        return true;
    }

    @Override
    public boolean handleDataUse(ObjectData hData) {
        if (!(hData instanceof SkinData)) 
            return false;
            
        // 使用一件装备之前必须先拥有这件装备（use Entity.addObjectData()）
        Skin skin = getSkin((SkinData) hData);
        if (skin == null) {
            addEntityDataUseMessage(StateCode.DATA_USE_FAILURE_NOT_FOUND, hData);
            return false;
        }
        
        // Outfit
        if (skin.getData().getWeaponType() == null) {
            if (skin.getData().isUsed()) {
                detachSkin(skin);
            } else {
                attachSkin(skin);
            }
            addEntityDataUseMessage(StateCode.DATA_USE, hData);
            return true;
        }
        
        // Weapon
        if (skin.getData().isUsed()) {
            if (isWeaponTakeOn()) {
                takeOffWeapon();
            } else {
                detachSkin(skin);
            }
        } else {
            attachSkin(skin);
        }
        addEntityDataUseMessage(StateCode.DATA_USE, hData);
        return true;
    }
    

}
