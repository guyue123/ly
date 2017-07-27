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
package name.huliqing.ly.view.actor;

import name.huliqing.luoying.ui.tiles.ColumnBody;
import name.huliqing.luoying.ui.tiles.ColumnText;
import name.huliqing.luoying.ui.tiles.ColumnIcon;
import name.huliqing.ly.constants.InterfaceConstants;
import name.huliqing.ly.manager.ResourceManager;
import name.huliqing.luoying.object.skill.Skill;
import name.huliqing.luoying.ui.UIFactory;
import name.huliqing.luoying.ui.Row;

/**
 *
 * @author huliqing
 */
public class SkillRow extends Row<Skill> {
    
    private Skill data;
    
    // 物品
    private final ColumnIcon icon;
    private final ColumnBody body;
    private final ColumnText num;
    private final ColumnIcon shortcut;
    
    public SkillRow() {
        super();
        this.setLayout(Layout.horizontal);
        icon = new ColumnIcon(height, height, InterfaceConstants.UI_MISS);
        body = new ColumnBody(height, height, "", "");
        num = new ColumnText(height, height, "");
        shortcut = new ColumnIcon(height, height, InterfaceConstants.UI_OPER);
        addView(icon);
        addView(body);
        addView(num);
        addView(shortcut);
        
        setBackground(UIFactory.getUIConfig().getBackground(), true);
        setBackgroundColor(UIFactory.getUIConfig().getActiveColor(), true);
        setBackgroundVisible(false);
    }
    
    @Override
    public void updateViewChildren() {
        super.updateViewChildren();
        float iconSize = height;

        icon.setWidth(iconSize);
        icon.setHeight(iconSize);

        num.setWidth(iconSize);
        num.setHeight(iconSize);

        shortcut.setWidth(iconSize);
        shortcut.setHeight(iconSize);
        shortcut.setPreventEvent(true);

        body.setWidth(width - iconSize * 3);
        body.setHeight(iconSize);
    }

    @Override
    public final void displayRow(Skill data) {
        this.data = data;
        display(this.data);
    }
    
    public Skill getData() {
        return this.data;
    }
    
    public void setRowClickListener(Listener listener) {
        addClickListener(listener);
    }
    
    public void setShortcutListener(Listener listener) {
        shortcut.addClickListener(listener);
    }

    @Override
    protected void clickEffect(boolean isPress) {
        if (isPress) {
            this.setBackgroundColor(UIFactory.getUIConfig().getActiveColor(), true);
        }
        setBackgroundVisible(isPress);
    }

    @Override
    public void onRelease() {
        setBackgroundVisible(false);
    }
    
    protected void display(Skill skill) {
        icon.setIcon(skill.getData().getIcon());
        
        body.setDesText(ResourceManager.getObjectDes(skill.getData().getId()));
        num.setText(skill.getData().getLevel() + "/" + skill.getData().getMaxLevel());
        
        if (skill.isCooldown() 
                || !skill.isPlayableByElCheck() 
//                || !skill.isPlayableByWeapon()
//                || !skill.isPlayableByAttributeLimit() 
                ) {
            body.setDisabled(true);
        } else {
            body.setDisabled(false);
        }
        body.setNameText(ResourceManager.getObjectName(skill.getData()));
    }
}
