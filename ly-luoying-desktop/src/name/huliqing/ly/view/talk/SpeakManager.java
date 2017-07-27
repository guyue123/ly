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
package name.huliqing.ly.view.talk;

import com.jme3.util.SafeArrayList;
import name.huliqing.luoying.object.entity.Entity;
import name.huliqing.luoying.object.game.Game;
import name.huliqing.luoying.object.gamelogic.AbstractGameLogic;

/**
 * 说话，谈话管理,该类主要管理谈话逻辑，每个逻辑都包装成Talk，每个Talk在执
 * 行完之后都会被移除。如果需要循环执行一个talk，则需要判断talk是否结束，再
 * 将talk添加到当前管理器中{@link #addTalk(name.huliqing.fighter.game.manager.talk.Talk) }
 * ,Talk不增加循环设置主要是为了避免无意的操作，在添加循环talk之后即忘记移除的情况发生，造成资源浪费。
 * @author huliqing
 */
public class SpeakManager extends AbstractGameLogic {
    
    private final static SpeakManager INSTANCE = new SpeakManager();
    
    private final SafeArrayList<Speak> speaks = new SafeArrayList<Speak>(Speak.class);
    
    private SpeakManager() {
        super(0);
    }
    
    public static SpeakManager getInstance() {
        return INSTANCE;
    }

    @Override
    protected void logicInit(Game game) {}

    @Override
    protected void logicUpdate(float tpf) {
        if (speaks.isEmpty())
            return;
        
        for (Speak s : speaks.getArray()) {
            if (s.isEnd()) {
                speaks.remove(s);
            } else {
                s.update(tpf);
            }
        }
    }

    @Override
    public void cleanup() {
        for (Speak s : speaks) {
            if (!s.isEnd()) {
                s.cleanup();
            }
        }
        speaks.clear();
        super.cleanup();
    }

    /**
     * 立即让角色说话
     * @param actor
     * @param mess 
     * @param useTime 
     */
    public void doSpeak(Entity actor, String mess, float useTime) {
        if (!isInitialized()) {
            return;
        }
        
        // 首先清除指定的角色旧的说话内容，否则内容可能重叠在一起
        for (Speak s : speaks) {
            if (s.getActor() == actor) {
                s.cleanup();
            }
        }
        
        // 开始新的说话内容
        Speak speak = new SpeakImpl(actor, mess, useTime);
        speak.start();
        speaks.add(speak);
    }


    
}
