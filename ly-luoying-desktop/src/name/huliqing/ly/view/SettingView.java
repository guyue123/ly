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
package name.huliqing.ly.view;

import name.huliqing.ly.view.system.SystemMainPanel;
import java.util.List;
import name.huliqing.luoying.Factory;
import name.huliqing.luoying.layer.service.PlayService;
import name.huliqing.ly.manager.ResourceManager;
import name.huliqing.luoying.ui.Button;
import name.huliqing.luoying.ui.UI;
import name.huliqing.luoying.ui.Window;
import name.huliqing.luoying.ui.state.UIState;
import name.huliqing.ly.constants.ResConstants;
import name.huliqing.ly.layer.service.GameService;

/**
 * 设置面板
 * @author huliqing
 */
public class SettingView extends Window {
    private final PlayService playService = Factory.get(PlayService.class);
    private final GameService gameService = Factory.get(GameService.class);
    
    private Button system;
    private Button about;
    private Button back;
    
    // “系统”设置：包含声音、快捷方式、游戏难度、效果等等设置
    private SystemMainPanel systemPanel;
    // “关于”设置：关于游戏的一些说明
    private AboutView aboutPanel;
    
    public SettingView(float width, float height) {
        super(width, height);
        setTitle(ResourceManager.get(ResConstants.SETTING_TITLE));
        init();
    }
    
    private void init() {
        float sw = playService.getScreenWidth();
        float sh = playService.getScreenHeight();
        float psw = sw * 1f;
        float psh = sh * 1f;
        
        // ==== button
        system = new Button(ResourceManager.get(ResConstants.SETTING_SYSTEM));
        about = new Button(ResourceManager.get(ResConstants.SETTING_ABOUT));
        back = new Button(ResourceManager.get(ResConstants.SETTING_RETURN));
        
        this.addView(system);
        this.addView(about);
        this.addView(back);
        
        // ==== Panel
        aboutPanel = new AboutView(psw, psh);
        systemPanel = new SystemMainPanel(psw, psh);
        systemPanel.setToCorner(Corner.CC);
        systemPanel.setCloseable(true);
        
        // ==== Binding listener
        // back panel
        back.addClickListener(new Listener() {
            @Override
            public void onClick(UI ui, boolean isPress) {
                if (!isPress) {
                    gameService.exitGame();
                }
            }
        });
        
        // 系统设置
        system.addClickListener(new Listener() {
            @Override
            public void onClick(UI ui, boolean isPress) {
                if (isPress) return;
                SettingView.this.removeFromParent();
                UIState.getInstance().addUI(systemPanel);
            }
        });
        
        // about
        about.addClickListener(new Listener() {
            @Override
            public void onClick(UI ui, boolean isPress) {
                if (isPress) return;
                SettingView.this.removeFromParent();
                UIState.getInstance().addUI(aboutPanel);
            }
        });
        
    }

    @Override
    protected void updateViewChildren() {
        super.updateViewChildren();
        List<UI> cuis = getViews();
        float btnWidth = getContentWidth();
        float btnHeight = getContentHeight() / cuis.size();
        for (UI ui : cuis) {
            ui.setWidth(btnWidth);
            ui.setHeight(btnHeight);
        }
    }
}
