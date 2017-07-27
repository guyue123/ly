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
import name.huliqing.luoying.object.game.Game;
import name.huliqing.luoying.object.gamelogic.AbstractGameLogic;

/**
 * 说话，谈话管理,该类主要管理谈话逻辑，每个逻辑都包装成Talk，每个Talk在执
 * 行完之后都会被移除。如果需要循环执行一个talk，则需要判断talk是否结束，再
 * 将talk添加到当前管理器中{@link #addTalk(name.huliqing.fighter.game.manager.talk.Talk) }
 * ,Talk不增加循环设置主要是为了避免无意的操作，在添加循环talk之后即忘记移除的情况发生，造成资源浪费。
 * @author huliqing
 */
public class TalkManager extends AbstractGameLogic {
    
    private final static TalkManager INSTANCE = new TalkManager();
    private final SafeArrayList<Talk> talks = new SafeArrayList<Talk>(Talk.class);
    
    private TalkManager() {}

    public static TalkManager getInstance() {
        return INSTANCE;
    }

    @Override
    protected void logicInit(Game game) {}
    
    @Override
    protected void logicUpdate(float tpf) {
        if (talks.isEmpty())
            return;
        
        for (Talk t : talks.getArray()) {
            if (t.isEnd()) {
                talks.remove(t);
            } else {
                t.update(tpf);
            }
        }
    }

    @Override
    public void cleanup() {
        for (Talk talk : talks) {
            if (!talk.isEnd()) {
                talk.cleanup();
            }
        }
        talks.clear();
        super.cleanup();
    }

    /**
     * 执行一段谈话，talk是一个说话或对话序列，该talk在执行完成之后将被立即
     * 移除。
     * @param talk 
     */
    public void startTalk(Talk talk) {
        if (!isInitialized()) {
            return;
        }
        talk.start();
        talks.add(talk);
    }


    
}
