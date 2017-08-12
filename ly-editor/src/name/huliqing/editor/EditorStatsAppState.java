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
package name.huliqing.editor;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.renderer.Statistics;

import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import name.huliqing.editor.constants.AssetConstants;
import name.huliqing.editor.constants.ResConstants;
import name.huliqing.editor.manager.Manager;
import name.huliqing.editor.ui.utils.JfxUtils;
import name.huliqing.fxswing.Jfx;

/**
 * @deprecated 不再使用
 * @author huliqing
 */
public class EditorStatsAppState extends AbstractAppState {

    private Statistics stat;
    private String[] labels;
    private int[] datas;
    private final StringBuilder sb = new StringBuilder();
    
    private final Label label = new Label();
    private ToggleButton statisticsIcon;
    
    public EditorStatsAppState() {
        setEnabled(false);
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        stat = app.getRenderer().getStatistics();
        Jfx.runOnJfx(() -> {
            label.setVisible(false);
            statisticsIcon = new ToggleButton();
            statisticsIcon.setTooltip(new Tooltip(Manager.getRes(ResConstants.COMMON_STATISTICS)));
            statisticsIcon.prefWidth(20);
            statisticsIcon.prefHeight(20);
            statisticsIcon.setPadding(Insets.EMPTY);
            statisticsIcon.setGraphic(JfxUtils.createIcon(AssetConstants.INTERFACE_ICON_STATISTICS));
            statisticsIcon.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                label.setVisible(newValue);
                setEnabled(newValue);
            });
        });
        labels = stat.getLabels();
        datas = new int[labels.length];
        stat.setEnabled(isEnabled());
    }

    @Override
    public void cleanup() {
        Jfx.runOnJfx(() -> {
           // UIManager.ZONE_STATUS.getChildren().removeAll(statisticsIcon, label);
        });
        super.cleanup();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled); 
        if (stat != null) {
            stat.setEnabled(enabled);
        }
    }
    
    @Override
    public void update(float tpf) {
        super.update(tpf);
        stat.getData(datas);
        sb.setLength(0);

        for (int i = 0; i < labels.length; i++) {
            sb.append(labels[i]).append("=").append(datas[i]).append(" | ");
        }
        final String str = sb.toString();
        Jfx.runOnJfx(() -> label.setText(str));
        
    }
    
    

}
