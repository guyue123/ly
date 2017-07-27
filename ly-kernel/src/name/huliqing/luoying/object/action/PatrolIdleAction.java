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
package name.huliqing.luoying.object.action;

import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.util.TempVars;
import java.util.ArrayList;
import java.util.List;
import name.huliqing.luoying.Factory;
import name.huliqing.luoying.data.ActionData;
import name.huliqing.luoying.data.SkillData;
import name.huliqing.luoying.layer.network.EntityNetwork;
import name.huliqing.luoying.layer.network.SkillNetwork;
import name.huliqing.luoying.layer.service.ActorService;
import name.huliqing.luoying.object.module.SkillModule;
import name.huliqing.luoying.object.skill.Skill;
import name.huliqing.luoying.xml.ObjectData;
import name.huliqing.luoying.object.entity.DataListener;

/**
 * 简单的IDLE行为，巡逻，会在一个地点来回走动
 * @author huliqing
 */
public class PatrolIdleAction extends AbstractAction implements IdleAction, DataListener {
    private final ActorService actorService = Factory.get(ActorService.class);
//    private final SkillService skillService = Factory.get(SkillService.class);
    private final SkillNetwork skillNetwork = Factory.get(SkillNetwork.class);
    private final EntityNetwork entityNetwork = Factory.get(EntityNetwork.class);
    private SkillModule skillModule;
    
    // 限制:半径范围内的最多坐标点
    private int walkPosTotal = 7;
    // 限制:半径范围
    private float walkRadius = 5;
    // 半径平方,用于优化计算
    private float walkRadiusSquared = FastMath.pow(walkRadius, 2);
    // 直径平方,用于优化计算
    private float walkDiameterSquared = FastMath.pow(walkRadius * 2, 2);
    // 等待的最长时间,单位秒
    private float waitingTimeMax = 10;
    
    // ---- inner

    // 限制:每一次走动允许的时长,由于半径内可能存在障碍物而可能一直无法走到currentPos,
    // walkTimeLimit可用于fix该问题，当walkTimeUsed使用达到walkTimeLimit限制时可能
    // 进行转向
    private float walkTimeLimit = 5;
    private float walkTimeUsed;
    
    // 当次等待的时间,该值小于或等于waitingTimeMax
    private float waitingTime;
    // 当次等待已经使用的时间
    private float waitingTimeUsed;
    // 是否处于等待状态
    private boolean waiting;
    
    // 巡逻的原点
    private final Vector3f patrolOrgin = new Vector3f();
    // 自动生成的巡逻坐标点，围绕着patrolOrgin生成的。
    private List<Vector3f> idlePositions;
    // 当前的巡逻坐标点,每隔一定时间都会自动从idlePositions中重新选择一个使用
    private Vector3f currentPos; 
    
    // idle动作缓存
    private final List<Skill> idleSkills = new ArrayList<Skill>();
    
    // 当巡逻位置上有比较多角色时，防止角色之间冲撞
    private final Detour rayDetour = new RayDetour(this);
    private boolean needRedir;
    
    // 缓存技能id
    private Skill walkSkill;
    private Skill runSkill;
    private Skill waitSkill;
    
    // 记录角色的idle的中心位置点
    private Vector3f bornPlace;
    
    public PatrolIdleAction() {
        super();
    }

    @Override
    public void setData(ActionData data) {
        super.setData(data);
        walkPosTotal = data.getAsInteger("walkPosTotal", walkPosTotal);
        
        walkRadius = data.getAsFloat("walkRadius", walkRadius);
        walkRadiusSquared = FastMath.pow(walkRadius, 2);
        walkDiameterSquared = FastMath.pow(walkRadius * 2, 2);
        
        waitingTimeMax = data.getAsFloat("waitingTimeMax", waitingTimeMax);
    }
    
    /**
     * 设置IDLE坐标点数量
     * @param walkPosTotal 
     */
    public void setWalkPosTotal(int walkPosTotal) {
        this.walkPosTotal = walkPosTotal;
    }
    
    /**
     * 设置IDLE半径
     * @param walkRadius 
     */
    public void setWalkRadius(float walkRadius) {
        this.walkRadius = walkRadius;
        this.walkRadiusSquared = FastMath.pow(walkRadius, 2);
        this.walkDiameterSquared = FastMath.pow(walkRadius * 2, 2);
    }

    /**
     * 设置IDLE等待的允许最长时间,单位秒
     * @param waitingTimeMax 
     */
    public void setWaitingTimeMax(float waitingTimeMax) {
        this.waitingTimeMax = waitingTimeMax;
    }
    
    @Override
    public void initialize() {
        super.initialize();
        skillModule = actor.getModule(SkillModule.class);
        
        // 初始化巡逻点坐标,如果没有指别设置角色的出生地点则将当前世界位置作为巡逻
        // 的原点,并在这个原点上向四周生成几个巡逻坐标点。
        if (idlePositions == null) {
            if (bornPlace == null) {
                bornPlace = new Vector3f(actor.getSpatial().getWorldTranslation());
            }
            patrolOrgin.set(bornPlace);
            // 生成可用idle坐标点.
            // 逻辑:随机在角色原始坐标的周围生成几个idle坐标点.
            idlePositions = new ArrayList<Vector3f>(walkPosTotal);
            int angle = 360 / walkPosTotal;
            int startAngle = FastMath.nextRandomInt(0, 180);
            Vector3f startPoint = new Vector3f(0, 0, walkRadius);
            Quaternion quater = new Quaternion();
            idlePositions.add(patrolOrgin.clone());
            for (int i = 0; i < walkPosTotal; i++) {
                quater.fromAngleAxis((startAngle + angle * i) * FastMath.DEG_TO_RAD
                        , Vector3f.UNIT_Y.clone());
                Vector3f pos = quater.mult(startPoint);
                idlePositions.add(pos.addLocal(patrolOrgin));
            }
            currentPos = idlePositions.get(FastMath.nextRandomInt(0, idlePositions.size() - 1));
            
            // debug
//            DebugUtils.debugPoints(idlePositions, actor.getActor().getParent());
        }
        
        rayDetour.setActor(actor);
        rayDetour.setAutoFacing(true);
        rayDetour.setUseRun(false);

        List<Skill> waitSkills = skillModule.getSkillWait(null);
        if (waitSkills != null && !waitSkills.isEmpty()) {
            waitSkill = waitSkills.get(0);
        }
        List<Skill> walkSkills = skillModule.getSkillWalk(null);
        if (walkSkills != null && !walkSkills.isEmpty()) {
            walkSkill = walkSkills.get(0);
        }
        List<Skill> runSkills = skillModule.getSkillRun(null);
        if (runSkills != null && !runSkills.isEmpty()) {
            runSkill = runSkills.get(0);
        }
        
        // 缓存IDLE技能并添加侦听器
        recacheIdleSkill();
        
        actor.addDataListener(this);
    }

    @Override
    public void cleanup() {
        actor.removeDataListener(this);
        super.cleanup(); 
    }

    @Override
    public void doLogic(float tpf) {
        // 1.IDLE 
        if (waiting) {
            // Waiting to walk: 等待时间到达之后,切换到另一个目标点进行走动.
            waitingTimeUsed += tpf;
            
            if (waitingTimeUsed >= waitingTime) {
                waitingTimeUsed = 0;
                waiting = false;
                walkTimeUsed = 0;
                currentPos = idlePositions.get(FastMath.nextRandomInt(0, idlePositions.size() - 1));
                gotoPos(currentPos);
            }
            
        } else {
            // walking to wait: 到达目标位置后转为空闲或等待.
            walkTimeUsed += tpf;
            
            // 偿试避障
            if (rayDetour.detouring(tpf)) {
                needRedir = true;
                return;
            }
            
            // 如果rayDetour重定了方向则应该重新执行一次gotoPos
            if (needRedir) {
                needRedir = false;
                gotoPos(currentPos);
            }
            
            // 走动过程判断，防止角色走出圈外。
            // 该情况发生在当角色走出idle范围外后返回的情况(如追踪攻击敌人后返回)
            float angle = actorService.getViewAngle(actor, currentPos);
            if (angle < 90 && actor.getSpatial().getWorldTranslation().distanceSquared(patrolOrgin) > walkRadiusSquared) {
                
                // ignore:不能切换到"等待"或“空闲”状态,这时候角色还在圈外，需要
                // 等它一直走到巡逻点范围内.
                
            } else if (angle > 90 || walkTimeUsed >= walkTimeLimit) {
                
                // 当发生以下情况时将角色从walk状态转为空闲或等待。
                // 1.正确走到目标位置
                // 2.走过头(viewAngle与currentPos方向相反则判断为走过头,当方向相反时这个角度大约在170~180度.
                // 3.存在障碍物一直走不到目标位置，达到限制的允许时间。
                Skill idleSkill = getIdleSkill();
                if (idleSkill != null && entityNetwork.useObjectData(actor, idleSkill.getData().getUniqueId())) {
                    // 如果存在idleSkill并执行成功则目的达到。
                } else {
                    
                    // 注意：如果idle不存在或idle执行失败（如冷却中），这时需要转到playWait
                    // 否则角色会停不下来，一直走到巡逻点外直到达到时间限制。这有点不太好。
//                    skillNetwork.playSkill(actor, waitSkill, false);
                    entityNetwork.useObjectData(actor, waitSkill.getData().getUniqueId());
                    
                }
                waitingTime = waitingTimeMax * FastMath.nextRandomFloat();
                waiting = true;
            }
        }
    }
    
    private void gotoPos(Vector3f pos) {
        TempVars tv = TempVars.get();
        Vector3f targetPos = tv.vect1.set(pos);
        // 如果距离比较远(在radius外),则使用跑路的方式,否则使用步行.
        // 注：角色从巡逻范围内最长可走一个直径距离，这里用squared比较所以直径也
        // 需要为平方形式，+10主要是判断在距离巡逻范围10码远时使用跑步方式走回来
        if (actor.getSpatial().getWorldTranslation().distanceSquared(targetPos) > walkDiameterSquared + 10) {
            skillNetwork.playWalk(actor
                    , runSkill
                    , targetPos.subtract(actor.getSpatial().getWorldTranslation(), targetPos).normalizeLocal(), true, false);
        } else {
            skillNetwork.playWalk(actor
                    , (walkSkill != null ? walkSkill : runSkill)
                    , targetPos.subtract(actor.getSpatial().getWorldTranslation(), targetPos).normalizeLocal()
                    , true, false);
        }
        tv.release();
    }
    
    // 随机获取一个idle技能
    private Skill getIdleSkill() {
        if (idleSkills.isEmpty()) {
            return null;
        }
        return idleSkills.get(FastMath.nextRandomInt(0, idleSkills.size() - 1));
    }

    @Override
    public void onDataAdded(ObjectData data, int amount) {
        if (data instanceof SkillData) {
            recacheIdleSkill();
        }
    }

    @Override
    public void onDataRemoved(ObjectData data, int amount) {
        if (data instanceof SkillData) {
            recacheIdleSkill();
        }
    }

    @Override
    public void onDataUsed(ObjectData data) {
        // ignore
    }

    private void recacheIdleSkill() {
        idleSkills.clear();
        skillModule.getSkillIdle(idleSkills);
    }
}
