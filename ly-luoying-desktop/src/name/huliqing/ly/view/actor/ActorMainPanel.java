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

import java.util.List;
import name.huliqing.luoying.Factory;
import name.huliqing.luoying.data.ItemData;
import name.huliqing.luoying.data.SkinData;
import name.huliqing.luoying.data.TalentData;
import name.huliqing.luoying.data.TaskData;
import name.huliqing.ly.constants.InterfaceConstants;
import name.huliqing.ly.manager.ResourceManager;
import name.huliqing.luoying.layer.service.SkinService;
import name.huliqing.luoying.layer.service.TalentService;
import name.huliqing.luoying.layer.service.TaskService;
import name.huliqing.luoying.object.entity.Entity;
import name.huliqing.luoying.object.module.TalentListener;
import name.huliqing.luoying.object.module.TaskListener;
import name.huliqing.luoying.object.talent.Talent;
import name.huliqing.luoying.object.task.Task;
import name.huliqing.luoying.ui.UIFactory;
import name.huliqing.luoying.ui.FrameLayout;
import name.huliqing.luoying.ui.Icon;
import name.huliqing.luoying.ui.LinearLayout;
import name.huliqing.luoying.ui.UI;
import name.huliqing.luoying.ui.Window;
import name.huliqing.luoying.xml.ObjectData;
import name.huliqing.ly.constants.ResConstants;
import name.huliqing.ly.layer.service.GameService;
import name.huliqing.luoying.object.entity.DataListener;

/**
 * 角色主面板，这个面板包含角色所有的“属性","武器","装备","天赋"...等面板
 * @author huliqing
 */
public class ActorMainPanel extends Window implements DataListener, TalentListener, TaskListener {
    private final SkinService skinService = Factory.get(SkinService.class);
    private final TalentService talentService = Factory.get(TalentService.class);
    private final TaskService taskService = Factory.get(TaskService.class);
    private final GameService gameService = Factory.get(GameService.class);
    
    private Entity actor;
    
    private final LinearLayout tabPanel;
    private final TabButton btnAttr;  // 人物属性面板
    private final TabButton btnTalent; 
    private final TabButton btnResist;
    private final TabButton btnWeapon;
    private final TabButton btnArmor;
    private final TabButton btnSkill;
    private final TabButton btnItem;
    private final TabButton btnTask;
    
    private final LinearLayout bodyPanel;
    private final AttributePanel attrPanel;
    private final TalentPanel talentPanel;
    private final ResistPanel resistPanel;
    private final WeaponPanel weaponPanel;
    private final ArmorPanel armorPanel;
    private final SkillPanel skillPanel;
    private final ItemPanel itemPanel;
    private final TaskPanel taskPanel;
    
    private final int globalPageSize = 7;
    
    // 当前激活的tab和激活的ActorView
    private int index;
    private ActorPanel indexPanel;

    public ActorMainPanel(float width, float height) {
        super(width, height);
        setTitle(ResourceManager.get(ResConstants.COMMON_ACTOR_PANEL));
        setLayout(Layout.horizontal);
        
        tabPanel = new LinearLayout();
        bodyPanel = new LinearLayout();
        addView(tabPanel);
        addView(bodyPanel);
        
        float cw = getContentWidth();
        float ch = getContentHeight();
        
        // body panel 
        attrPanel = new AttributePanel(cw, ch);
        attrPanel.setVisible(false);
        attrPanel.setPageSize(10);

        armorPanel = new ArmorPanel(cw, ch);
        armorPanel.setPageSize(globalPageSize);
        armorPanel.setVisible(false);
        
        skillPanel = new SkillPanel(cw, ch);
        skillPanel.setPageSize(globalPageSize);
        skillPanel.setVisible(false);
        
        itemPanel = new ItemPanel(cw, ch);
        itemPanel.setPageSize(globalPageSize);
        itemPanel.setVisible(false);
        
        weaponPanel = new WeaponPanel(cw, ch);
        weaponPanel.setPageSize(globalPageSize);
        weaponPanel.setVisible(false);
        
        talentPanel = new TalentPanel(cw, ch);
        talentPanel.setPageSize(globalPageSize);
        talentPanel.setVisible(false);
        
        resistPanel = new ResistPanel(cw, ch);
        resistPanel.setPageSize(globalPageSize);
        resistPanel.setVisible(false);
        
        taskPanel = new TaskPanel(cw, ch);
        taskPanel.setPageSize(globalPageSize);
        taskPanel.setVisible(false);
        
        bodyPanel.addView(attrPanel);
        bodyPanel.addView(talentPanel);
        bodyPanel.addView(resistPanel);
        bodyPanel.addView(weaponPanel);
        bodyPanel.addView(armorPanel);
        bodyPanel.addView(skillPanel);
        bodyPanel.addView(itemPanel);
        bodyPanel.addView(taskPanel);
        
        // tab button
        btnAttr = new TabButton(InterfaceConstants.UI_ITEM_ATTR, attrPanel);
        btnTalent = new TabButton(InterfaceConstants.UI_ITEM_TALENT, talentPanel);
        btnResist = new TabButton(InterfaceConstants.UI_ITEM_RESIST, resistPanel);
        btnArmor = new TabButton(InterfaceConstants.UI_ITEM_ARMOR, armorPanel);
        btnSkill = new TabButton(InterfaceConstants.UI_ITEM_SKILL, skillPanel);
        btnItem = new TabButton(InterfaceConstants.UI_ITEM_ITEM, itemPanel);
        btnWeapon = new TabButton(InterfaceConstants.UI_ITEM_WEAPON, weaponPanel);
        btnTask = new TabButton(InterfaceConstants.UI_ITEM_TASK, taskPanel);
        tabPanel.addView(btnAttr);
        tabPanel.addView(btnTalent);
        tabPanel.addView(btnResist);
        tabPanel.addView(btnTask);
        tabPanel.addView(btnSkill);
        tabPanel.addView(btnWeapon);
        tabPanel.addView(btnArmor);
        tabPanel.addView(btnItem);
        
        // 默认显示最后一个tab
        index = tabPanel.getViews().size() - 1;
        
    }

    @Override
    protected void updateViewChildren() {
        super.updateViewChildren();
        float tabWidth = getContentWidth() * 0.2f;
        tabPanel.setWidth(tabWidth);
        tabPanel.setHeight(getContentHeight());
        
        List<UI> tabBtns = tabPanel.getViews();
        float tabHeight = getContentHeight() / tabBtns.size();
        for (UI btn : tabBtns) {
            btn.setMargin(0, 0, 0, 0);
            btn.setWidth(tabWidth);
            btn.setHeight(tabHeight);
        }
        
        bodyPanel.setWidth(getContentWidth() * 0.8f);
        bodyPanel.setHeight(getContentHeight());
        bodyPanel.setMargin(0, 0, 0, 0);
        List<UI> bodyChildren = bodyPanel.getViews();
        for (UI child : bodyChildren) {
            child.setWidth(bodyPanel.getWidth());
            child.setHeight(bodyPanel.getHeight());
        }
    }
    
    public void setActor(Entity newActor) {
        if (newActor == null) {
            return;
        }
        
        // 1.先清理上一个角色的侦听
        if (actor != null) {
            actor.removeDataListener(this);
            talentService.removeTalentListener(actor, this);
            taskService.removeTaskListener(actor, this);
        }
        
        // 2.更新角色并更新面板内容
        actor = newActor;
        setTitle(ResourceManager.get(ResConstants.COMMON_ACTOR_PANEL) + "-" + gameService.getName(newActor));
        
        // 4.显示指定的tab
        showTab(index);
        
        // 5.为新的角色添加侦听器以便实时更新面板内容
        actor.addDataListener(this);
        talentService.addTalentListener(actor, this);
        taskService.addTaskListener(actor, this);
    }
    
    public void cleanup() {
        if (actor != null) {
            actor.removeDataListener(this);
            talentService.removeTalentListener(actor, this);
            taskService.removeTaskListener(actor, this);
        }
    }
    
    private void showTab(int index) {
        showTab((TabButton) tabPanel.getViews().get(index));
    }
    
    private void showTab(TabButton activeTab) {
        List<UI> tabButtons = tabPanel.getViews();
        for (UI child : tabButtons) {
            TabButton temp = (TabButton) child;
            if (temp != activeTab) {
                temp.setActive(false);
            }
        }
        activeTab.setActive(true);
        index = tabButtons.indexOf(activeTab);
        indexPanel = activeTab.actorPanel;
    }
    
    // 更新指定的面板
    private void updatePanel(ActorPanel... actorPanels) {
        if (!isVisible()) {
            return;
        }
        for (ActorPanel ap : actorPanels) {
            if (ap == indexPanel) {
                ap.setPanelUpdate(actor);
                break;
            }
        }
    }
    
    // 物口添加或减少的时候要更新指定面板信息
    @Override
    public void onDataAdded(ObjectData data, int amount) {
        if (data instanceof ItemData) {
            updatePanel(itemPanel);
            return;
        }
        if (data instanceof SkinData) {
            updatePanel(armorPanel, weaponPanel, attrPanel);
            return;
        }
        if (data instanceof TaskData) {
            updatePanel(taskPanel);
            return;
        }
        if (data instanceof TalentData) {
            updatePanel(talentPanel, attrPanel);
        }
    }

    @Override
    public void onDataRemoved(ObjectData data, int amount) {
        if (data instanceof ItemData) {
            updatePanel(itemPanel);
            return;
        }
        if (data instanceof SkinData) {
            updatePanel(armorPanel, weaponPanel, attrPanel);
            return;
        }
        if (data instanceof TaskData) {
            updatePanel(taskPanel);
            return;
        }
        if (data instanceof TalentData) {
            updatePanel(talentPanel, attrPanel);
        }
    }

    @Override
    public void onDataUsed(ObjectData data) {
        if (data instanceof ItemData) {
            updatePanel(itemPanel);
            return;
        }
        if (data instanceof SkinData) {
            updatePanel(armorPanel, weaponPanel, attrPanel);
        }
    }

    @Override
    public void onTalentPointsChanged(Entity actor, Talent talent, int pointsAdded) {
        updatePanel(talentPanel, attrPanel);
    }

    @Override
    public void onTaskCompleted(Entity actor, Task task) {
        updatePanel(taskPanel);
    }

    private class TabButton extends FrameLayout {
        
        private final Icon tabIcon;
        private ActorPanel actorPanel;
        
        public TabButton(String icon, ActorPanel actorPanel) {
            super();
            this.actorPanel = actorPanel;
            this.tabIcon = new Icon(icon);
            addView(tabIcon);
            
            setBackground(UIFactory.getUIConfig().getBackground(), true);
            setBackgroundColor(UIFactory.getUIConfig().getBodyBgColor(), true);
            
            addClickListener(new Listener() {
                @Override
                public void onClick(UI ui, boolean isPress) {
                    if (!isPress) {
                        showTab(TabButton.this);
                    }
                }
            });
        }

        @Override
        protected void updateViewChildren() {
            super.updateViewChildren();
            tabIcon.setWidth(height * 0.75f);
            tabIcon.setHeight(tabIcon.getWidth());
        }

        @Override
        protected void updateViewLayout() {
            super.updateViewLayout(); 
            tabIcon.setToCorner(Corner.CC);
        }
        
        /**
         * 激活当前面板
         * @param active 
         */
        public void setActive(boolean active) {
            if (active) {
                setBackgroundColor(UIFactory.getUIConfig().getActiveColor(), true);
            } else {
                setBackgroundColor(UIFactory.getUIConfig().getBodyBgColor(), true);
            }
            actorPanel.setPanelVisible(active);
            actorPanel.setPanelUpdate(actor);
        }
        
        @Override
        protected void clickEffect(boolean isPress) {
           // ignore
        }
    }
}
