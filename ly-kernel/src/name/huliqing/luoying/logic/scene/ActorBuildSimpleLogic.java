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
package name.huliqing.luoying.logic.scene;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import name.huliqing.luoying.LuoYing;
import name.huliqing.luoying.Factory;
import name.huliqing.luoying.layer.network.PlayNetwork;
import name.huliqing.luoying.layer.service.ActorService;
import name.huliqing.luoying.layer.service.SceneService;
import name.huliqing.luoying.object.Loader;
import name.huliqing.luoying.object.entity.Entity;
import name.huliqing.luoying.object.game.Game;
import name.huliqing.luoying.object.gamelogic.AbstractGameLogic;
import name.huliqing.luoying.utils.ThreadHelper;

/**
 * 简单的刷怪功能,可指定要刷新的角色id,位置及间隔刷新时间。
 * 当目标角色死亡并被移除出场景后，在达到指定间隔时间之后该角色会重新在指定
 * 地点刷新。
 * @author huliqing
 */
public class ActorBuildSimpleLogic extends AbstractGameLogic {

    private static final Logger LOG = Logger.getLogger(ActorBuildSimpleLogic.class.getName());
    
    private final PlayNetwork playNetwork = Factory.get(PlayNetwork.class);
    private final ActorService actorService = Factory.get(ActorService.class);
    private final SceneService sceneService = Factory.get(SceneService.class);
    
    public interface Callback {
        /**
         * 当角色载入完成时调用,该方法的调用在角色已经载入内存，并准备放入场
         * 景之前。
         * @param actor 
         */
        void onload(Entity actor);
    }
    
    private final List<ActorBuilder> datas = new ArrayList<ActorBuilder>();
    
    public ActorBuildSimpleLogic() {
        interval = 3;
    }

    /**
     * @param position
     * @param actorIds
     * @param interval
     */
    public void addBuilder(Vector3f position, String[] actorIds, float interval) {
        datas.add(new ActorBuilder(position, actorIds, interval, null));
    }
    
    /**
     * 添加一个角色刷新地点。
     * @param position 要刷新角色的地点
     * @param actorIds 可用于刷新的id列表，刷新时将会从这个列表中随机获取ID进行载入
     * @param interval 刷新的间隔，单位秒，是指角色死亡并被移出场景后一定时间。
     * @param callback 回调函数，该回调方法会在角色载入内存，并在添加到场景之前调用。
     */
    public void addBuilder(Vector3f position, String[] actorIds, float interval, Callback callback) {
        datas.add(new ActorBuilder(position, actorIds, interval, callback));
    }

    @Override
    protected void logicInit(Game game) {}

    @Override
    protected void logicUpdate(float tpf) {
        for (ActorBuilder abData : datas) {
            if (abData.actor != null) {
                if (abData.actor.getScene() != null) {
                    // 角色还在场景中，不管是否死亡都不应该考虑刷新
                    continue;
                } else if (abData.deadTime <= 0) {
                    // 如果不在场景中，并且未标记过角色死亡，则标记死亡时间
                    abData.deadTime = LuoYing.getGameTime();
                }
            }
            
            // 如果正在载入角色(刷新),则检查并处理
            if (abData.isbuilding()) {
                
                abData.waitAndCheckBuilding();
            
            // 以下两种情况需要刷新载入角色
            // 1.角色未载入过;
            // 2.死亡时间已经超过刷新时间
            } else if (abData.actor == null 
                    || (abData.deadTime > 0 && LuoYing.getGameTime() - abData.deadTime >= abData.interval * 1000)){
                
                abData.rebuild();
                
            }
        }
    }
        
    // --
    private class ActorBuilder {

        // 刷新地点
        public Vector3f position;
        
        // 可用于刷新的角色id列且
        public String[] actorIds;
        
        // 生成角色的时间间隔,单位秒
        public float interval;
        
        // 刷新的角色
        public Entity actor;
        
        // 最近角色的死亡时间,0表示未死亡
        public long deadTime;
        
        private Future<Entity> future;
        
        private final Callback callback;
        
        public ActorBuilder(Vector3f position, String[] actorIds, float interval, Callback callback) {
            this.position = position;
            this.actorIds = actorIds;
            this.interval = interval;
            this.callback = callback;
        }
        
        public void rebuild() {
            future = ThreadHelper.submit(new Callable<Entity>() {
                @Override
                public Entity call() throws Exception {
                    return Loader.load(actorIds[FastMath.nextRandomInt(0, actorIds.length - 1)]);
                }
            });
        }
        
        /**
         * 是否正在载入角色中
         * @return 
         */
        public boolean isbuilding() {
            return future != null;
        }
        
        /**
         * 检查并等待角色载入.
         */
        public void waitAndCheckBuilding() {
            if (future.isDone()) {
                try {
                    actor = future.get();
                    Vector3f locWithFixedHeight = sceneService.getSceneHeight(position.x, position.z);
                    if (locWithFixedHeight != null) {
                        actorService.setLocation(actor, locWithFixedHeight);
                    } else {
                        actorService.setLocation(actor, position);
                    }
                    if (callback != null) {
                        callback.onload(actor);
                    }
                    playNetwork.addEntity(actor);
                    deadTime = 0;
                    future = null;
                } catch (Exception e) {
                    // 重要：这个异常必须处理。
                    if (future != null) {
                        future.cancel(true);
                    }
                    future = null;
                    LOG.log(Level.SEVERE, e.getMessage());
                }
            } else if (future.isCancelled()) {
                future = null;
            }
        }
        
    }

}
