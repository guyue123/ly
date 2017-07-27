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
package name.huliqing.luoying.data;

import name.huliqing.luoying.xml.ObjectData;
import com.jme3.network.serializing.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 定义游戏的数据
 * @author huliqing
 */
@Serializable
public class GameData extends ObjectData {
    
    /**
     * 获取游戏图标，如果没有设置则返回null.
     * @return 
     */
    public String getIcon() {
        return getAsString("icon");
    }
    
    public void setIcon(String icon) {
        setAttribute("icon", icon);
    }

    /**
     * 获取场景数据
     * @return 
     */
    public SceneData getSceneData() {
        return getAsObjectData("sceneData");
    }

    /**
     * 设置场景数据
     * @param sceneData 
     */
    public void setSceneData(SceneData sceneData) {
        setAttribute("sceneData", sceneData);
    }

    public SceneData getGuiSceneData() {
        return getAsObjectData("guiSceneData");
    }

    public void setGuiSceneData(SceneData guiSceneData) {
        setAttribute("guiSceneData", guiSceneData);
    }

    /**
     * 获取游戏逻辑数据列表
     * @return 
     */
    public List<GameLogicData> getGameLogicDatas() {
        return getAsObjectDataList("gameLogicDatas");
    }

    /**
     * 设置游戏逻辑数据列表
     * @param gameLogics 
     */
    public void setGameLogicDatas(List<GameLogicData> gameLogics) {
        setAttributeSavableList("gameLogicDatas", gameLogics);
    }
    
    /**
     * 添加一个游戏逻辑数据
     * @param gameLogicData 
     */
    public void addGameLogicData(GameLogicData gameLogicData) {
        List<GameLogicData> gameLogicDatas = getGameLogicDatas();
        if (gameLogicDatas == null) {
            gameLogicDatas = new ArrayList<GameLogicData>();
            setGameLogicDatas(gameLogicDatas);
        }
        // 这里要注意：不能把null添加到gameLogicData中，这会造成在网络序列化传送过程中报NPE
        if (!gameLogicDatas.contains(gameLogicData)) {
            gameLogicDatas.add(gameLogicData);
        }
    }
    
    /**
     * 移除一个游戏逻辑数据
     * @param gameLogicData
     * @return 
     */
    public boolean removeGameLogicData(GameLogicData gameLogicData) {
        List<GameLogicData> gameLogicDatas = getGameLogicDatas();
        return gameLogicDatas != null && gameLogicDatas.remove(gameLogicData);
    }

    /**
     * 获取游戏可选的角色id列表
     * @return 
     */
    public List<String> getAvailableActors() {
        return getAsStringList("availableActors");
    }

    /**
     * 设置游戏可选的角色id列表
     * @param availableActors 
     */
    public void setAvailableActors(List<String> availableActors) {
        setAttributeStringList("availableActors", availableActors);
    }

    @Override
    public ObjectData clone() {
        GameData clone = (GameData) super.clone();
        return clone;
    }
    
}
