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
package name.huliqing.ly;

import com.jme3.app.Application;
import com.jme3.export.Savable;
import com.jme3.network.serializing.Serializer;
import java.util.Map;
import java.util.Map.Entry;
import name.huliqing.luoying.Factory;
import name.huliqing.luoying.LuoYing;
import name.huliqing.luoying.LuoYingException;
import name.huliqing.luoying.data.ConfigData;
import name.huliqing.luoying.data.GameData;
import name.huliqing.luoying.data.GameLogicData;
import name.huliqing.luoying.data.ModuleData;
import name.huliqing.luoying.loader.GameDataLoader;
import name.huliqing.luoying.manager.ResManager;
import name.huliqing.luoying.object.Loader;
import name.huliqing.luoying.xml.DataFactory;
import name.huliqing.ly.data.ChatData;
import name.huliqing.ly.data.ViewData;
import name.huliqing.ly.layer.network.ChatNetwork;
import name.huliqing.ly.layer.network.ChatNetworkImpl;
import name.huliqing.ly.layer.service.ChatService;
import name.huliqing.ly.layer.service.ChatServiceImpl;
import name.huliqing.ly.loader.ChatDataLoader;
import name.huliqing.ly.loader.ViewDataLoader;
import name.huliqing.ly.mess.ActorSpeakMess;
import name.huliqing.ly.mess.ChatSendMess;
import name.huliqing.ly.mess.ChatShopMess;
import name.huliqing.ly.mess.MessageMess;
import name.huliqing.ly.object.chat.GroupChat;
import name.huliqing.ly.object.chat.SellChat;
import name.huliqing.ly.object.chat.SendChat;
import name.huliqing.ly.object.chat.ShopItemChat;
import name.huliqing.ly.object.chat.TaskChat;
import name.huliqing.ly.object.game.story.StoryGbGame;
import name.huliqing.ly.object.game.story.StoryGuardGame;
import name.huliqing.ly.object.game.story.StoryTreasureGame;
import name.huliqing.ly.object.game.lan.SurvivalGame;
import name.huliqing.ly.object.gamelogic.PlayerDeadCheckerGameLogic;
import name.huliqing.ly.object.module.ChatModule;
import name.huliqing.ly.object.view.TextPanelView;
import name.huliqing.ly.object.view.TextView;
import name.huliqing.ly.object.view.TimerView;
import name.huliqing.ly.layer.network.GameNetwork;
import name.huliqing.ly.layer.network.GameNetworkImpl;
import name.huliqing.ly.layer.service.GameService;
import name.huliqing.ly.layer.service.GameServiceImpl;
import name.huliqing.ly.mess.ActionRunMess;

/**
 * @author huliqing
 */
public class Init {
    
    /**
     * 初始化数据
     * @param app
     */
    public static void initialize(Application app) {
        // 落樱初始化
        LuoYing.initialize(app);
        
        // 落樱日志记录功能
        LuoYing.initializeLogManager();
        
        // 本地初始化
        registerService();
        registerData();
        registerProcessor();
        loadData();
        
        // 载入资源, 英文环境
        ResManager.loadResource("/data/font/en_US/resource",               "utf-8", "en_US");
        ResManager.loadResource("/data/font/en_US/resource_object",   "utf-8", "en_US");
        ResManager.loadResource("/data/font/en_US/story_gb",               "utf-8", "en_US");
        ResManager.loadResource("/data/font/en_US/story_guard",          "utf-8", "en_US");
        ResManager.loadResource("/data/font/en_US/story_treasure",      "utf-8", "en_US");
        
        // 载入资源,中文环境
        ResManager.loadResource("/data/font/zh_CN/resource",                "utf-8", "zh_CN");
        ResManager.loadResource("/data/font/zh_CN/resource_object",    "utf-8", "zh_CN");
        ResManager.loadResource("/data/font/zh_CN/story_gb",                "utf-8", "zh_CN");
        ResManager.loadResource("/data/font/zh_CN/story_guard",           "utf-8", "zh_CN");
        ResManager.loadResource("/data/font/zh_CN/story_treasure",       "utf-8", "zh_CN");
        
        // 载入配置
        ConfigData config = (ConfigData) Loader.loadData("config");
        ConfigData configSaved = Factory.get(GameService.class).loadConfig();
        if (configSaved != null && configSaved.getLocalData() != null) {
            Map<String, Savable> saveData = configSaved.getLocalData();
            for (Entry<String, Savable> es : saveData.entrySet()) {
                config.setAttribute(es.getKey(), es.getValue());
            }
        }
        LyConfig.setConfigData(config);
        
        // 设置默认环境
        ResManager.setLocale(LyConfig.getLocale());
        ResManager.setLocaleDefault("en_US");
    }
    
    private static void registerData() {
        Serializer.registerClass(ChatData.class);
        Serializer.registerClass(ViewData.class);
        
        Serializer.registerClass(ActionRunMess.class);
        Serializer.registerClass(ActorSpeakMess.class);
        Serializer.registerClass(ChatSendMess.class);
        Serializer.registerClass(ChatShopMess.class);
        Serializer.registerClass(MessageMess.class);
    }
    
    private static void registerService() {
        Factory.register(GameService.class, GameServiceImpl.class);
        Factory.register(ChatService.class, ChatServiceImpl.class);
        Factory.register(ChatNetwork.class, ChatNetworkImpl.class);
        
        Factory.register(GameNetwork.class, GameNetworkImpl.class);
    }

    private static void registerProcessor() {
        // 自定义的游戏类型
        DataFactory.register("gameStoryTreasure", GameData.class, GameDataLoader.class, StoryTreasureGame.class);
        DataFactory.register("gameStoryGb", GameData.class, GameDataLoader.class, StoryGbGame.class);
        DataFactory.register("gameStoryGuard", GameData.class, GameDataLoader.class, StoryGuardGame.class);
        DataFactory.register("gameSurvival", GameData.class, GameDataLoader.class, SurvivalGame.class);
        
        // 额外的游戏逻辑
        DataFactory.register("gameLogicPlayerDeadChecker", GameLogicData.class, null, PlayerDeadCheckerGameLogic.class);
        
        // Chat
        DataFactory.register("chatGroup",  ChatData.class, ChatDataLoader.class, GroupChat.class);
        DataFactory.register("chatSend",  ChatData.class, ChatDataLoader.class, SendChat.class);
        DataFactory.register("chatShopItem",  ChatData.class, ChatDataLoader.class, ShopItemChat.class);
        DataFactory.register("chatSell",  ChatData.class, ChatDataLoader.class, SellChat.class);
        DataFactory.register("chatTask",  ChatData.class, ChatDataLoader.class, TaskChat.class);
        
        // actor module
        DataFactory.register("moduleChat", ModuleData.class, null, ChatModule.class);
        
        // View
        DataFactory.register("viewText",  ViewData.class, ViewDataLoader.class, TextView.class);
        DataFactory.register("viewTextPanel",  ViewData.class, ViewDataLoader.class, TextPanelView.class);
        DataFactory.register("viewTimer",  ViewData.class, ViewDataLoader.class, TimerView.class);
    }
    
    private static void loadData() throws LuoYingException {
        LuoYing.loadData("/data/object/action.xml");
        LuoYing.loadData("/data/object/actor.xml");
        LuoYing.loadData("/data/object/actorAnim.xml");
        LuoYing.loadData("/data/object/anim.xml");
        LuoYing.loadData("/data/object/attribute.xml");
        LuoYing.loadData("/data/object/bullet.xml");
        LuoYing.loadData("/data/object/channel.xml");
        LuoYing.loadData("/data/object/chat.xml");
        LuoYing.loadData("/data/object/config.xml");
        LuoYing.loadData("/data/object/define.xml");
        LuoYing.loadData("/data/object/drop.xml");
        LuoYing.loadData("/data/object/effect.xml");
        LuoYing.loadData("/data/object/el.xml");
        LuoYing.loadData("/data/object/emitter.xml");
        LuoYing.loadData("/data/object/entity.xml");
        LuoYing.loadData("/data/object/game.xml");
        LuoYing.loadData("/data/object/gameLogic.xml");
        LuoYing.loadData("/data/object/hitChecker.xml");
        LuoYing.loadData("/data/object/item.xml");
        LuoYing.loadData("/data/object/logic.xml");
        LuoYing.loadData("/data/object/magic.xml");
        LuoYing.loadData("/data/object/module.xml");
        LuoYing.loadData("/data/object/position.xml");
        LuoYing.loadData("/data/object/resist.xml");
        LuoYing.loadData("/data/object/scene.xml");
        LuoYing.loadData("/data/object/shape.xml");

        // 技能
        LuoYing.loadData("/data/object/skill.xml");
        LuoYing.loadData("/data/object/skill_monster.xml");
        LuoYing.loadData("/data/object/skill_skin.xml");

        // 装备、武器
        LuoYing.loadData("/data/object/skin.xml");
        LuoYing.loadData("/data/object/skin_male.xml");
        LuoYing.loadData("/data/object/skin_weapon.xml");

        // 武器槽位配置
        LuoYing.loadData("/data/object/slot.xml");

        LuoYing.loadData("/data/object/sound.xml");
        LuoYing.loadData("/data/object/state.xml");
        LuoYing.loadData("/data/object/talent.xml");
        LuoYing.loadData("/data/object/task.xml");
        LuoYing.loadData("/data/object/view.xml");

    }
}
