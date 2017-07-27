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

import com.jme3.math.Vector3f;
import name.huliqing.luoying.object.action.RunAction;
import name.huliqing.luoying.data.LogicData;
import name.huliqing.luoying.object.Loader;
import name.huliqing.luoying.object.action.Action;
import name.huliqing.luoying.object.action.FightAction;
import name.huliqing.luoying.object.action.FollowAction;
import name.huliqing.luoying.object.module.ActionModule;

/**
 * 必须两个行为：FightAction, RunAction
 * 角色逻辑：
 * 1.受到敌人攻击时会和敌人战斗(不会主动查找敌人)
 * 2.没有敌人时就朝着目标前进,需要为逻辑指定一个目标地点，默认为Vector3f.ZERO
 * @author huliqing
 */
public class PositionLogic extends AbstractLogic {
    private ActionModule actionModule;
    
    // 允许走到的最近距离
    private float nearest = 5;
    
    // ---- inner
    protected RunAction runAction;
    
    @Override
    public void setData(LogicData data) {
        super.setData(data); 
        runAction = (RunAction) Loader.load(data.getAsString("runAction"));
        Vector3f position = data.getAsVector3f("position");
        if (position != null) {
            runAction.setPosition(position);
        }
        nearest = data.getAsFloat("nearest", nearest);
    }

    @Override
    public void initialize() {
        super.initialize();
        actionModule = actor.getModule(ActionModule.class);
        runAction.setActor(actor);
    }
    
    public void setPosition(Vector3f position) {
        runAction.setPosition(position);
    }
    
    /**
     * 设置允许走到目标位置的最近距离，如果设置为0，则将走到精确的目标位置
     * @param distance 
     */
    public void setNearestDistance(float distance) {
        runAction.setNearest(distance);
    }

    private Action tempAction;
    @Override
    protected void doLogic(float tpf) {
        tempAction = actionModule.getAction();
        if ((tempAction  instanceof FightAction) || (tempAction  instanceof FollowAction)) {
            return;
        }
        
        if (!runAction.isEndPosition()) {
            actionModule.startAction(runAction);
        }
    }
    
}
