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
package name.huliqing.ly.view.system;

import java.util.ArrayList;
import java.util.List;
import name.huliqing.ly.manager.ResourceManager;
import name.huliqing.luoying.ui.ListView;
import name.huliqing.luoying.ui.Row;
import name.huliqing.luoying.ui.UI;
import name.huliqing.luoying.ui.state.UIState;
import name.huliqing.ly.LyConfig;
import name.huliqing.ly.view.shortcut.ShortcutManager;

/**
 *
 * @author huliqing
 */
public class SystemUIPanel extends ListView<SystemData> {
    private final List<SystemData> datas = new ArrayList<SystemData>(2); 
    
    // 快捷方式锁定
    private RowCheckbox shortcutLock;
    // 快捷方式大小
    private RowSimple shortcutSize;
    private ShortcutSizeOper shortcutSizeOper;
    // 快捷方式清理
    private RowSimple shortcutClear;
    // 谈话速度调节
    private RowSimple talkSpeed;
    private TalkSpeedOper talkSpeedOper;
    

    public SystemUIPanel(float width, float height) {
        super(width, height);
        
        datas.add(new SystemData(get("system.ui.shortcutLock"), get("system.ui.shortcutLock.des")));
        datas.add(new SystemData(get("system.ui.shortcutSize"), get("system.ui.shortcutSize.des")));
        datas.add(new SystemData(get("system.ui.shortcutClear"), get("system.ui.shortcutClear.des")));
        datas.add(new SystemData(get("system.ui.talkSpeed"), get("system.ui.talkSpeed.des")));
        
        // 快捷方式锁定
        shortcutLock = new RowCheckbox(datas.get(0).getName(), datas.get(0).getDes(), LyConfig.isShortcutLocked());
        shortcutLock.addClickListener(new Listener() {
            @Override
            public void onClick(UI ui, boolean isPress) {
                if (isPress) return;
                LyConfig.setShortcutLocked(shortcutLock.isChecked());
            }
        });
        
        // 快捷方式大小调节
        shortcutSize = new RowSimple(this, datas.get(1).getName(), datas.get(1).getDes());
        shortcutSize.addClickListener(new Listener() {
            @Override
            public void onClick(UI ui, boolean isPress) {
                if (isPress) return;
                UIState.getInstance().addUI(shortcutSizeOper);
            }
        });
        shortcutSizeOper = new ShortcutSizeOper(width * 0.5f, height * 0.5f, shortcutSize);
        
        // 快捷清理
        shortcutClear = new RowSimple(this, datas.get(2).getName(), datas.get(2).getDes());
        shortcutClear.addClickListener(new Listener() {
            @Override
            public void onClick(UI ui, boolean isPress) {
                if (isPress) return;
                ShortcutManager.cleanup();
            }
        });
        
        // 谈话速度调节
        talkSpeed = new RowSimple(this, datas.get(3).getName(), datas.get(3).getDes());
        talkSpeed.addClickListener(new Listener() {
            @Override
            public void onClick(UI ui, boolean isPress) {
                if (isPress) return;
//                playService.addObject(talkSpeedOper, true);
                UIState.getInstance().addUI(talkSpeedOper);
            }
        });
        talkSpeedOper = new TalkSpeedOper(width * 0.5f, height * 0.5f, talkSpeed);
        
        rows.add(shortcutLock);
        rows.add(shortcutSize);
        rows.add(shortcutClear);
        rows.add(talkSpeed);
        pageSize = datas.size();
        for (int i = 0; i < rows.size(); i++) {
            attachChild(rows.get(i));
        }
    }
    
    @Override
    protected Row createEmptyRow() {
        return new RowSimple(this, "", "");
    }

    @Override
    public List getDatas() {
        return datas;
    }
    
     /**
     * 获取所有数据占据的行数
     * @return 
     */
    @Override
    public int getRowTotal() {
        return rows.size();
    }
    
    @Override
    public void addItem(SystemData data) {
        // ignore
    }
    
    @Override
    public boolean removeItem(SystemData data) {
        return false;
    }
    
    private String get(String resKey) {
        return ResourceManager.get(resKey);
    }
}
