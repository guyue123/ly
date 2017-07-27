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
package name.huliqing.ly.view.shortcut;

import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import com.jme3.scene.shape.Quad;
import name.huliqing.luoying.Factory;
import name.huliqing.luoying.LuoYing;
import name.huliqing.luoying.data.SkillData;
import name.huliqing.luoying.layer.network.EntityNetwork;
import name.huliqing.luoying.object.entity.Entity;
import name.huliqing.luoying.utils.MaterialUtils;
import name.huliqing.luoying.xml.ObjectData;
import name.huliqing.ly.layer.network.GameNetwork;
import name.huliqing.ly.layer.service.GameService;
import name.huliqing.luoying.object.entity.DataListener;

/**
 * 用于技能(skill)的快捷方式
 * @author huliqing
 */
public class SkillShortcut extends BaseUIShortcut<SkillData> implements DataListener {
    private final GameService gameService = Factory.get(GameService.class);
    private final GameNetwork gameNetwork = Factory.get(GameNetwork.class);
    private final EntityNetwork entityNetwork = Factory.get(EntityNetwork.class);
    
     // 技能CD遮罩颜色
    private final ColorRGBA maskColor = new ColorRGBA(1.0f, 0, 0.5f, 0.5f);
    // 材质
    private final Material maskMat = MaterialUtils.createSkillCooldown(maskColor);
    // 遮罩实体
    private final Geometry maskObj = new Geometry("mask");
     // 遮罩的缩放,比skillUI边框小一点就行。
    private final float maskScale = .85f;
    // 更新频率，冷却时间越长更新频率越慢，以节约资源
    private float interval;
    private float intervalUsed;
    // 判断是否需要更新mask,主要用于提高性能
    private boolean needCheckAndUpdateMask = true;
    
    private Control updateControl;

    @Override
    public void initialize() {
        super.initialize();
        
        createMask(maskObj, maskMat);
        view.attachChild(maskObj);
        
        // 更新频率，以一秒钟更新30次为标准频率。冷却时间越长更新频率越低。
        // 在低于1秒的冷却时间内尽可能快的更新。
        interval = 1f / 120 * objectData.getCooldown();
        
        // 用于更新技能 shortcut图标
        if (updateControl == null) {
            updateControl = new AbstractControl() {
                @Override
                protected void controlUpdate(float tpf) {updateShortcut(tpf);}
                @Override
                protected void controlRender(RenderManager rm, ViewPort vp) {}
            };
            entity.getSpatial().addControl(updateControl);
        }
        
        entity.addDataListener(this);
    }
    
    /**
     * 更新技能图标冷却效果
     * @param tpf 
     */
    public void updateShortcut(float tpf) {
        // 这里主要为提高性能，不过当界面存在多个相当的技能图标时，技能的冷却提示
        // 只有当前被点击的才会产生动画效果。如果这里注释掉则可同时看到相同技能的
        // 冷却提示动画。
        if (!needCheckAndUpdateMask) {
            return;
        }
        
        // 使用interval来降低更新频率,冷却时间越长则更新频率越低。
        intervalUsed += tpf;
        if (intervalUsed > interval) {
            intervalUsed = 0;
            // params: Color, Percent
            float p = getTimePercent();
            maskMat.setFloat("Percent", p);
            if (p >= 1.0f) {
                needCheckAndUpdateMask = false;
            }
        }
    }

    @Override
    public void cleanup() {
        if (updateControl != null) {
            entity.getSpatial().removeControl(updateControl);
        }
        entity.removeDataListener(this);
        super.cleanup();
    }

    @Override
    public void removeObject() {
        // 技能不能删除
    }
    
    @Override
    public void onShortcutClick(boolean pressed) {
        if (!pressed) {
//            if (objectData.getCooldown() > 0) {
//                needCheckAndUpdateMask = true;
//            }
//            Skill skill = skillService.getSkill(actor, objectData.getId());
//            if (skill == null) 
//                return;

            // 一些技能在执行前必须设置目标对象。
            // 注意：gameService.getTarget()是获取当前游戏主目标，是“玩家行为”，不能把它
            // 放到skillNetwork.playSkill中去。
            Entity target = gameService.getTarget();
            if (target != null) {
                gameNetwork.setTarget(entity, target.getEntityId());
            }
            
            // 执行技能
            entityNetwork.useObjectData(entity, objectData.getUniqueId());
        }
    }

    @Override
    protected void updateShortcutViewChildren(float width, float height) {
        super.updateShortcutViewChildren(width, height); 
        maskObj.setLocalScale(width * maskScale, height * maskScale, 1);
        maskObj.setLocalTranslation(width * (1 - maskScale) * 0.5f, height * (1 - maskScale) * 0.5f, 0);
    }
    
    private float getTimePercent() {
        long lapse = LuoYing.getGameTime() - objectData.getLastPlayTime();
        float percent = lapse / (objectData.getCooldown() * 1000);
        return percent;
    }
    
    private void createMask(Geometry mask, Material maskMat) {
        Quad quad = new Quad(1,1);
        mask.setMesh(quad);
        mask.setMaterial(maskMat);
        // 非常重要，shortcut是放置在GUI上的，所以这里必须指定Bucket为GUI，否则会看不到模型。
        mask.setQueueBucket(RenderQueue.Bucket.Gui);
    }

    @Override
    public void onDataAdded(ObjectData data, int amount) {
        // ignore
    }

    @Override
    public void onDataRemoved(ObjectData data, int amount) {
        // ignore
    }

    @Override
    public void onDataUsed(ObjectData data) {
        if (data.getUniqueId() == objectData.getUniqueId()) {
            if (objectData.getCooldown() > 0) {
                needCheckAndUpdateMask = true;
            }
        }
    }
}
