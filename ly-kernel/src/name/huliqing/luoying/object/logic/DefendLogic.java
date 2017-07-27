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
package name.huliqing.luoying.object.logic;

import com.jme3.math.FastMath;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import name.huliqing.luoying.Factory;
import name.huliqing.luoying.data.LogicData;
import name.huliqing.luoying.data.SkillData;
import name.huliqing.luoying.layer.network.EntityNetwork;
import name.huliqing.luoying.layer.service.EntityService;
import name.huliqing.luoying.layer.service.SkillService;
import name.huliqing.luoying.object.actor.Actor;
import name.huliqing.luoying.object.attribute.Attribute; 
import name.huliqing.luoying.object.entity.Entity;
import name.huliqing.luoying.object.module.ActorListener;
import name.huliqing.luoying.object.module.ActorModule;
import name.huliqing.luoying.object.module.SkillModule;
import name.huliqing.luoying.object.skill.Skill;
import name.huliqing.luoying.object.skill.AttackSkill;
import name.huliqing.luoying.object.skill.ShotSkill;
import name.huliqing.luoying.xml.ObjectData;
import name.huliqing.luoying.object.module.SkillListener;
import name.huliqing.luoying.object.entity.DataListener;

/**
 * 防守逻辑
 * @author huliqing
 */
public class DefendLogic extends AbstractLogic implements DataListener, SkillListener, ActorListener {
//    private static final Logger LOG = Logger.getLogger(DefendLogic.class.getName());
    
    private final SkillService skillService = Factory.get(SkillService.class);
    private final EntityService entityService = Factory.get(EntityService.class);
    private final EntityNetwork entityNetwork = Factory.get(EntityNetwork.class);
    private ActorModule actorModule;
    private SkillModule skillModule;
    
    // 使用哪一个属性作为防守概率及躲闪概率
    private String defendRateAttribute;
    private String duckRateAttribute;
    // 哪些属性会响应OnHit,即监听哪些属性被击中
    private List<String> listenAttributes;
    
    // 被当前侦听(skillListener)的其它角色
    private Set<Entity> listenersActors;
    // 指定要监听的目标角色所发出的技能,当目标角色发出这些技能时，当前角色会偿试进行防守或躲闪
    private long listenSkillTypes;
    // 当前角色可以用来防守的技能类型
    private long defendSkillTypes;
    // 当前角色可以用来躲闪的技能类型。
    private long duckSkillTypes;
    
    // ---- 节能
    // 判断是否有可用的技能进行防守
    private boolean hasUsableSkill = false;
    
    private boolean needRecacheSkill = true;
    
    private List<Skill> defendSkills;
    private List<Skill> duckSkills;
    
    @Override
    public void setData(LogicData data) {
        super.setData(data); 
        interval = 3;
        defendRateAttribute = data.getAsString("defendRateAttribute");
        duckRateAttribute = data.getAsString("duckRateAttribute");
        listenAttributes = Arrays.asList(data.getAsArray("listenAttributes"));
        listenSkillTypes = skillService.convertSkillTypes(data.getAsArray("listenSkillTypes"));
        defendSkillTypes = skillService.convertSkillTypes(data.getAsArray("defendSkillTypes"));
        duckSkillTypes = skillService.convertSkillTypes(data.getAsArray("duckSkillTypes"));
    }

    @Override
    public void setActor(Entity self) {
        super.setActor(self); 
        actorModule = actor.getModule(ActorModule.class);
        skillModule = actor.getModule(SkillModule.class);
    }
    
    @Override
    public void initialize() {
        super.initialize();
        actorModule.addActorListener(this);
        actor.addDataListener(this);
        recacheSkill();
    }
    
    @Override
    public void cleanup() {
        // 清理当前角色的侦听器
        actorModule.removeActorListener(this);
        actor.removeDataListener(this);
        
        // 清理其它被当前逻辑侦听的角色
        if (listenersActors != null) {
            for (Entity other : listenersActors) {
                skillService.removeListener(other, this);
            }
            listenersActors.clear();
        }
        super.cleanup();
    }

    @Override
    public void onActorTargetLocked(Entity sourceBeLocked, Entity other) {
        if (!hasUsableSkill) 
            return;
        
        if (sourceBeLocked.getData().getUniqueId() == other.getData().getUniqueId()) 
            return;
        
        if (!(other instanceof Actor)) {
            return;
        }
        
        if (!isEnemy(other)) {
            return;
        }
        
        // 当被other锁定时给other添加侦听器，以侦察other的攻击。以便进行防守和躲闪
        skillService.addListener(other, this);
        
        // 记录被侦听的对象，以便在当前角色销毁或退出时清理
        if (listenersActors == null) {
            listenersActors = new HashSet<Entity>();
        }
        listenersActors.add(other);
    }

    @Override
    public void onActorTargetReleased(Entity sourceBeReleased, Entity other) {
        if (!hasUsableSkill) 
            return;
        
        // 当other不再把source当前目标时就不再需要侦听了。
        skillService.removeListener(other, this);
        
        // 清理
        if (listenersActors != null) {
            listenersActors.remove(other);
        }
    }

    @Override
    public void onActorHitTarget(Entity sourceHitter, Entity beHit, Attribute hitAttribute, Object newValue, Object oldValue, boolean killed) {
        // ignore
    }

    // 受到攻击时将目标设为首要敌人
    @Override
    public void onActorHitByTarget(Entity sourceBeHit, Entity hitter, Attribute hitAttribute, Object newValue, Object oldValue, boolean killed) {
//        // hitValue>0为增益效果，不处理
//        // 这里不作用于player，不要影响player控制的目标的设置
//        if (actorModule.isDead() || actorModule.isPlayer() || hitValue > 0)
//            return; 
        
        // 被击中的属性不在监听范围内则不处理。
        if (!listenAttributes.contains(hitAttribute.getName())) {
            return;
        }
        
        if (hitter instanceof Actor) {
            if (!isEnemy(hitter)) {
                return;
            }
        }
        
        if (hitter != null) {
            setTarget(hitter);
        }
    }

    @Override
    public void onDataAdded(ObjectData data, int amount) {
        if (data instanceof SkillData) {
            needRecacheSkill = true;
        }
    }

    @Override
    public void onDataRemoved(ObjectData data, int amount) {
        if (data instanceof SkillData) {
            needRecacheSkill = true;
        }
    }

    @Override
    public void onDataUsed(ObjectData data) {
        // ignore
    }

    @Override
    public boolean onSkillHookCheck(Skill skill) {
        return true;
    }
    
    @Override
    public void onSkillStart(Skill skill) {
//        if (Config.debug) {
//            LOG.log(Level.INFO, "defendActorLogic==> onSkillStart, actor={0}, skill={1}", new Object[] {skill.getActor().getData().getId(), skill.getData().getId()});
//        }
        
        if (!hasUsableSkill) 
            return;
        
        // 暂不支持shot类技能的防守
        if (skill instanceof ShotSkill)
            return;
        
        // 不是所侦听的技能(只有listenSkillTypes所指定的技能类型才需要防守或躲闪 )
        if ((listenSkillTypes & skill.getTypes()) == 0) 
            return;
        
        // 2.正常防守,只有普通攻击技能才允许防守
        if (skill instanceof AttackSkill) {
            AttackSkill aSkill = (AttackSkill) skill;
            if (aSkill.isDefendable()) {
                // 在可防守的技能下给一个机会使用躲闪技能,以避免可能出现的一直使用防守技能的问题。
                if (FastMath.nextRandomFloat() < 0.75f) {
                    if (doDefend()) {
                        return;
                    }
                }
            }
        }
        
        // 3.闪避防守
        if (getSkillModule().isPlayingSkill(duckSkillTypes)) {
            return;
        }
        doDuck();
    }
    
    @Override
    public void onSkillEnd(Skill skill) {
        // ignore
    }
    
    private boolean doDefend() {
//        LOG.log(Level.INFO, "defendActorLogic==> doDefend actor={0} defendRateAttribute={1}, defendSkill size={2}"
//                , new Object[] {actor.getData().getId(), defendRateAttribute, defendSkills.size()});
        if (defendRateAttribute != null && defendSkills.size() > 0) {
            float defendRate = entityService.getNumberAttributeValue(actor, duckRateAttribute, 0).floatValue();
            if(defendRate >= FastMath.nextRandomFloat()) {
                Skill defendSkill = defendSkills.get(FastMath.nextRandomInt(0, defendSkills.size() - 1));
                
//                skillNetwork.playSkill(actor, defendSkill, false); // remove20161217
                entityNetwork.useObjectData(actor, defendSkill.getData().getUniqueId());
                
                return true;
            }
        }
        return false;
    }
    
    private boolean doDuck() {
//        LOG.log(Level.INFO, "defendActorLogic==> doDuck actor={0} duckRateAttribute={1}, duckSkill size={2}"
//                , new Object[] {actor.getData().getId(), duckRateAttribute, duckSkills.size()});
        if (duckRateAttribute != null && duckSkills.size() > 0) {
            float duckRate = entityService.getNumberAttributeValue(actor, defendRateAttribute, 0).floatValue();
            if (duckRate >= FastMath.nextRandomFloat()) {
                Skill duckSkill = duckSkills.get(FastMath.nextRandomInt(0, duckSkills.size() - 1));
                
//                skillNetwork.playSkill(actor, duckSkill, false);// remove20161217
                entityNetwork.useObjectData(actor, duckSkill.getData().getUniqueId());
                
                return true;
            }
        }
        return false;
    }

    @Override
    protected void doLogic(float tpf) {
        // 技能重新缓存技能
        if (needRecacheSkill) {
            recacheSkill();
        }
    }
    
    // 重装缓存技能
    private void recacheSkill() {
        if (defendSkills != null) {
            defendSkills.clear();
        }
        if (duckSkills != null) {
            duckSkills.clear();
        }
        defendSkills = getSkillModule().getSkillByTypes(defendSkillTypes, defendSkills);
        duckSkills = getSkillModule().getSkillByTypes(duckSkillTypes, duckSkills);
        hasUsableSkill = (defendSkills != null && defendSkills.size() > 0) || (duckSkills != null && duckSkills.size() > 0);
        needRecacheSkill = false;

//        LOG.log(Level.INFO, "needRecache defend/duck Skill, actor={0}, defendSkills={1}, duckSkills={2}"
//                , new Object[]{actor.getData().getId(), defendSkills.size(), duckSkills.size()});
    }

    private SkillModule getSkillModule() {
        if (skillModule == null) {
            skillModule = actor.getModule(SkillModule.class);
        }
        return skillModule;
    }

}
