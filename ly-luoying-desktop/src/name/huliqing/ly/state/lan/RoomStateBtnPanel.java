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
package name.huliqing.ly.state.lan;

import com.jme3.math.ColorRGBA;
import java.util.List;
import name.huliqing.ly.view.SimpleBtn;
import name.huliqing.ly.manager.ResourceManager;
import name.huliqing.luoying.ui.LinearLayout;
import name.huliqing.luoying.ui.UIFactory;
import name.huliqing.luoying.ui.UI;

/**
 * 服务端Room面板里面的控制按钮
 * @author huliqing
 */
public class RoomStateBtnPanel extends LinearLayout {
    private RoomState roomState;
    private SimpleBtn btnStart;     // 开始游戏
    private SimpleBtn btnKick;      // 踢掉指定客户端
    private SimpleBtn btnBack;      // 返回局域网房间列表
    
    public RoomStateBtnPanel(float width, float height, RoomState _roomState) {
        super();
        this.width = width;
        this.height = height;
        this.roomState = _roomState;
        setBackground(UIFactory.getUIConfig().getBackground(), true);
        setBackgroundColor(new ColorRGBA(0.5f, 0.5f, 0.5f, 0.5f), true);
        setLayout(Layout.horizontal);

        btnStart = new SimpleBtn(ResourceManager.get("lan.startGame"));
        btnKick = new SimpleBtn(ResourceManager.get("lan.kick"));
        btnBack = new SimpleBtn(ResourceManager.get("lan.back"));

        addView(btnBack);
        addView(btnKick);
        addView(btnStart);

        // 开始游戏
        btnStart.addClickListener(new Listener() {
            @Override
            public void onClick(UI ui, boolean isPress) {
                if (isPress) {return;}
                roomState.startGame();
            }
        });
        
        btnKick.addClickListener(new Listener() {
            @Override
            public void onClick(UI ui, boolean isPress) {
                if (isPress) {return;}
                roomState.kickClient();
            }
        });

        // 返回主界面
        btnBack.addClickListener(new Listener() {
            @Override
            public void onClick(UI ui, boolean isPress) {
                if (isPress) {return;}
                roomState.back();
            }
        });
    }

    @Override
    public void updateViewChildren() {
        super.updateViewChildren();
        float btnHeight = height;
        float mgTop = btnHeight * 0.1f;
        float mgLeft = 20;
        List<UI> cuis = getViews();

        for (UI ui : cuis) {
            ui.setHeight(btnHeight - mgTop * 2);
            ui.setMargin(mgLeft, mgTop, 0, 0);
        }
    }

    public SimpleBtn getBtnStart() {
        return btnStart;
    }

    public void setBtnStart(SimpleBtn btnStart) {
        this.btnStart = btnStart;
    }

    public SimpleBtn getBtnKick() {
        return btnKick;
    }

    public void setBtnKick(SimpleBtn btnKick) {
        this.btnKick = btnKick;
    }

    public SimpleBtn getBtnBack() {
        return btnBack;
    }

    public void setBtnBack(SimpleBtn btnBack) {
        this.btnBack = btnBack;
    }

    
}
