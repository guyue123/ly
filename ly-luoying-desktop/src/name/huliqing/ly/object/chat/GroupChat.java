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
package name.huliqing.ly.object.chat;

import com.jme3.font.BitmapFont;
import com.jme3.math.ColorRGBA;
import java.util.ArrayList;
import java.util.List;
import name.huliqing.luoying.Factory;
import name.huliqing.luoying.data.EntityData;
import name.huliqing.ly.constants.InterfaceConstants;
import name.huliqing.luoying.object.Loader;
import name.huliqing.ly.data.ChatData;
import name.huliqing.ly.manager.ResourceManager;
import name.huliqing.luoying.object.entity.Entity;
import name.huliqing.luoying.object.scene.Scene;
import name.huliqing.luoying.ui.AbstractUI;
import name.huliqing.luoying.ui.Icon;
import name.huliqing.luoying.ui.LinearLayout;
import name.huliqing.luoying.ui.ListView;
import name.huliqing.luoying.ui.Row;
import name.huliqing.luoying.ui.Text;
import name.huliqing.luoying.ui.UI;
import name.huliqing.luoying.ui.UI.Listener;
import name.huliqing.luoying.ui.UIFactory;
import name.huliqing.ly.layer.service.GameService;
import name.huliqing.ly.view.SimpleRow;

/**
 * 对话组
 * @author huliqing
 */
public class GroupChat extends Chat {
    private final GameService gameService = Factory.get(GameService.class);
    
    private List<Chat> chats; 
    
    // ---- inner
    private LinearLayout group;
    private TitlePanel title;
    private Icon separate;
    private ChatList chatList;

    @Override
    public void setData(EntityData data) {
        super.setData(data);
        String[] tempChats = data.getAsArray("chats");
        chats = new ArrayList<Chat>(tempChats.length);
        for (int i = 0; i < tempChats.length; i++) {
            Chat chat = Loader.load(tempChats[i]);
            chat.parent = this;
            chats.add(chat);
        }
    }

    @Override
    protected UI createChatUI(float width, float height) {
        group = new LinearLayout(width, height);
        group.setBackground("Interface/ui/bg_chat.png", true);
        group.setBackgroundColor(new ColorRGBA(0.2f, 0.2f, 0.2f, 0.25f), true);
        group.setPadding(10, 10, 10, 10);
        group.setToCorner(UI.Corner.CC);
        
        float cw = group.getContentWidth();
        float titleHeight = UIFactory.getUIConfig().getTitleHeight();
        float iconHeight = 2;
        float margin = 10;
        float listHeight = group.getContentHeight() - titleHeight - iconHeight - margin;
        
        title = new TitlePanel(ResourceManager.getObjectName(data), cw, titleHeight);
        
        separate = new Icon(InterfaceConstants.UI_LINE_H);
        separate.setWidth(cw);
        separate.setHeight(iconHeight);
        separate.setMargin(0, 0, 0, margin);
        
        chatList = new ChatList(cw, listHeight);
        
        group.addView(title);
        group.addView(separate);
        group.addView(chatList);
        // 添加一个空的事件阻止事件穿透
        group.addClickListener(AbstractUI.EMPTY_LISTENER);
        group.setEffectEnabled(false);

        return group;
    }
   

    @Override
    public void initEntity() {
        super.initEntity();
    }

    @Override
    public void onInitScene(Scene scene) {
        super.onInitScene(scene); 
         
        // 更新title
        title.setTitle(getChatName() + "-" + gameService.getName(actor));
        // 列表要刷新一下，因为一些Chat可能需要动态过滤以确定是否要对当前player显示
        chatList.refreshPageData();
        
        // 如果只有一个子对话框则直接弹出子对话框
        if (chats.size() == 1) {
            if (chats.get(0).isVisibleForPlayer()) {
                displayChat(chats.get(0));
            }
            // 注：因为closeParent参数的存在，在displayChat(chats.get(0))的时候, 
            // 子Chat可能已经关闭并清除了父Chat(当前Chat)。所以这里必须判断一下，避免重复调用cleanup造成异常
            if (isInitialized()) {
                cleanup();
            }
        }
    }

    @Override
    public void setActor(Entity actor) {
        super.setActor(actor);
        if (!chats.isEmpty()) {
            for (Chat c : chats) {
                c.setActor(actor);
            }
        }
    }
    
    private void displayChat(Chat chat) {
        chat.setActor(actor);
        scene.addEntity(chat);
    }

    // ---- 列表显示所有子chat
    private class ChatList extends ListView<Chat> {

        public ChatList(float width, float height) {
            super(width, height);
            setPageSize(5);
        }
        
        @Override
        protected Row createEmptyRow() {
            return new ChatRow(this);
        }

        @Override
        public List getDatas() {
            return chats;
        }

        @Override
        protected boolean filter(Chat chat) {
            // 如果子chat对玩家不可见则要过滤掉，不显示
            return !chat.isVisibleForPlayer();
        }
        
    }
    
    private class ChatRow extends SimpleRow<Chat> implements Listener {
        private final Text text;
        private Chat chat;
        
        public ChatRow(ListView parentView) {
            super(parentView);
            text = new Text("");
            text.setVerticalAlignment(BitmapFont.VAlign.Center);
            addView(text);
            addClickListener(this);
        }
        
        @Override
        public void displayRow(Chat data) {
            this.chat = data;
            text.setText(chat.getChatName());
        }
        
        @Override
        public void onClick(UI view, boolean isPressed) {
            if (isPressed) return;
            displayChat(chat);
        }
        
        @Override
        protected void updateViewChildren() {
            super.updateViewChildren();
            text.setWidth(width);
            text.setHeight(height);
        }
    }
    
    private class TitlePanel extends LinearLayout {

        private Text title;
        private Icon btn;
        
        public TitlePanel(String titleText, float width, float height) {
            super(width, height);
            float btnHeight = height * 0.75f;
            
            title = new Text(titleText);
            title.setFontSize(UIFactory.getUIConfig().getTitleSize());
            title.setWidth(width - btnHeight);
            title.setHeight(height);
            
            btn = new Icon(UIFactory.getUIConfig().getButtonClose());
            btn.setWidth(btnHeight);
            btn.setHeight(btnHeight);
            btn.addClickListener(new Listener() {

                @Override
                public void onClick(UI view, boolean isPressed) {
                    if (isPressed) return;
                    scene.removeEntity(GroupChat.this);
                }
            });
            
            setLayout(Layout.horizontal);
            addView(title);
            addView(btn);
        }
        
        void setTitle(String titleText) {
            title.setText(titleText);
            title.setFontSize(UIFactory.getUIConfig().getTitleSize());
        }
    }
}
