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
package name.huliqing.luoying.loader;

import com.jme3.math.Vector3f;
import com.jme3.util.TempVars;
import java.util.ArrayList;
import java.util.List;
import name.huliqing.luoying.data.EntityData;
import name.huliqing.luoying.xml.Proto;
import name.huliqing.luoying.data.SceneData;
import name.huliqing.luoying.xml.DataFactory;
import name.huliqing.luoying.utils.ConvertUtils;
import name.huliqing.luoying.utils.MathUtils;

/**
 * 载入随机植被的场景
 * @author huliqing
 * @param <T>
 */
public class RandomSceneDataLoader<T extends SceneData> extends SceneDataLoader<T> {

    @Override
    public void load(Proto proto, T store) {
        // super loader
        super.load(proto, store);
        
         // EmptyZones
        String[] tempEZS = proto.getAsArray("emptyZones");
        List<EmptyZone> emptyZones = null;
        if (tempEZS != null && tempEZS.length > 0) {
            emptyZones = new ArrayList<EmptyZone>(tempEZS.length);
            String[] zone;
            for (String ez : tempEZS) {
                zone = ez.split("\\|");
                EmptyZone emptyZone = new EmptyZone(Float.parseFloat(zone[0])
                        ,Float.parseFloat(zone[1])
                        ,Float.parseFloat(zone[2]));
                emptyZones.add(emptyZone);
            }
        }
        
        // 载入随机的树木和花草
        TempVars tv = TempVars.get();
        float xExt = proto.getAsFloat("randomXExtent", 100);
        float zExt = proto.getAsFloat("randomZExtent", 100);

        // 随机环境物体ID，格式："envId|size, envId2|size, ..."
        // envId关联到环境物体(env.xml), size指定该随机物体的数量。
        String[] reArrs = proto.getAsArray("randomEntities");
        String envId;
        int size;
        String[] tempArr;
        
        if (reArrs != null && reArrs.length > 0) {
            List<EntityData> entityDatas = store.getEntityDatas();
            if (entityDatas == null) {
                entityDatas = new ArrayList<EntityData>(reArrs.length);
                store.setEntityDatas(entityDatas);
            }
            
            for (String reArr : reArrs) {
                tempArr = reArr.split("\\|");
                envId = tempArr[0];
                size = tempArr.length > 1 ? ConvertUtils.toInteger(tempArr[1], 1) : 1;
                for (int j = 0; j < size; j++) {
                    EntityData ed = DataFactory.createData(envId);
                    entityDatas.add(ed);
                    // 生成随机位置（但需要防止生成的位置与空白区域冲突）
                    Vector3f randomLoc = new Vector3f();
                    do {
                        MathUtils.getRandomPosition(xExt, zExt, randomLoc);
                    } while (checkInEmptyZone(randomLoc.x, randomLoc.z, emptyZones));

                    ed.setLocation(randomLoc);
                }
            }
        }
        
        tv.release();
    }
    
    // 检查目标点是否在空白区域内
    private boolean checkInEmptyZone(float otherX, float otherZ, List<EmptyZone> emptyZones) {
        if (emptyZones == null || emptyZones.isEmpty())
            return false;
        for (EmptyZone ez : emptyZones) {
            if (ez.checkInZone(otherX, otherZ)) {
                return true;
            }
        }
        return false;
    }
    
    // 注意不要把没有经过Serializer.registerClass注册的特殊类型作为类字段中去。
    // 即使是加了transient也不行，会导致联网异常
    private class EmptyZone {
        private float x;
        private float z;
        private float radiusSquare;
        
        public EmptyZone() {}
        public EmptyZone(float x, float z, float radius) {
            this.x = x;
            this.z = z;
            this.radiusSquare = radius * radius;
        }
        public boolean checkInZone(float ox, float oz) {
            double dx = x - ox;
            double dz = z - oz;
            double distanceSquare = (dx * dx + dz * dz);
            return distanceSquare < radiusSquare;
        }
    }
}
