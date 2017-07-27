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
package name.huliqing.editor.edit;

import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Insets;
import javafx.scene.input.DragEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import name.huliqing.editor.constants.StyleConstants;
import name.huliqing.editor.toolbar.Toolbar;
import name.huliqing.fxswing.Jfx;
import name.huliqing.editor.ui.toolbar.JfxToolbar;
import name.huliqing.editor.ui.toolbar.JfxToolbarFactory;

/**
 * @author huliqing
 * @param <T>
 */
public abstract class JfxSimpleEdit<T extends JmeEdit> extends JfxAbstractEdit<T> {
    // editPanel不能完全透明，完全透明则响应不了事件，在响应事件时还需要设置为visible=true
    private final static String STYLE_EDIT_PANEL = "-fx-background-color:rgba(0,0,0,0.01)";
    
    protected final BorderPane layout = new BorderPane();
    
    protected final HBox propertyZone = new HBox();
    protected final StackPane bodyZone = new StackPane();
    protected final Pane editPanelBinder = new HBox();
    protected final Pane editPanel = new HBox();
    
    protected final HBox jfxToolbarPanel = new HBox();
    protected final JfxExtToolbar jfxExtToolbarPanel = new JfxExtToolbar();
    
    // --
    protected JfxToolbar jfxToolbar;
    protected final List<JfxToolbar> jfxExtToolbars = new ArrayList();
    
    public JfxSimpleEdit() {
        layout.setLeft(propertyZone);
        layout.setCenter(bodyZone);
        //layout.setRight(jfxExtToolbarPanel);
        layout.setBottom(jfxToolbarPanel);
        layout.setBackground(Background.EMPTY);
        
        propertyZone.getStyleClass().add(StyleConstants.CLASS_HVBOX);
        
        bodyZone.setBackground(Background.EMPTY);
        bodyZone.getChildren().addAll(editPanel, editPanelBinder); // editPanelBinder要放上面，以响应拖放事件
        
        editPanelBinder.setVisible(false);
        editPanelBinder.setStyle(STYLE_EDIT_PANEL);
        editPanelBinder.setOnDragOver(this::onDragOver);
        editPanelBinder.setOnDragDropped(this::onDragDropped);
        
        jfxToolbarPanel.setPadding(Insets.EMPTY);
        jfxToolbarPanel.getStyleClass().add(StyleConstants.CLASS_HVBOX);
        
        // 只有在有扩展工具栏的时候才应该显示 
/*        jfxExtToolbarPanel.managedProperty().bind(jfxExtToolbarPanel.visibleProperty());
        jfxExtToolbarPanel.setVisible(false);
        jfxExtToolbarPanel.setPadding(Insets.EMPTY);*/
    }
    
    @Override
    protected void jfxInitialize() {
        editRoot.getChildren().add(layout);
        layout.prefWidthProperty().bind(editRoot.widthProperty());
        layout.prefHeightProperty().bind(editRoot.heightProperty());
        
        // 基本工具栏
        Toolbar baseToolbar = jmeEdit.getToolbar();
        if (baseToolbar != null) {
            JfxToolbar jfxToolPanel = JfxToolbarFactory.createJfxToolbar(baseToolbar);
            setJfxToolbar(jfxToolPanel);
        }
        
        // 扩展工具栏
        List<Toolbar> extToolbars = jmeEdit.getExtToolbars();
        if (extToolbars != null && !extToolbars.isEmpty()) {
            extToolbars.stream().map((t) -> JfxToolbarFactory.createJfxToolbar(t)).forEach((jtl) -> {
                addJfxExtToolbar(jtl);
            });
        }
        
        // 强制刷新一下UI，必须的，否则界面无法实时刷新(JFX嵌入Swing的一个BUG)
        Jfx.jfxForceUpdate();
        Jfx.jfxCanvasBind(editPanelBinder);
        
    }

    @Override
    protected void jfxCleanup() {
        if (jfxToolbar != null && jfxToolbar.isInitialized()) {
            jfxToolbar.cleanup();
            jfxToolbar = null;
        }
        if (jfxExtToolbars != null && !jfxExtToolbars.isEmpty()) {
            jfxExtToolbars.stream().filter((jtb) -> (jtb.isInitialized())).forEach((jtb) -> {
                jtb.cleanup();
            });
            jfxExtToolbars.clear();
        }
        layout.prefWidthProperty().unbind();
        layout.prefHeightProperty().unbind();
        editRoot.getChildren().remove(layout);
    }
    
    @Override
    public void jfxOnDragStarted() {
        super.jfxOnDragStarted();
        editPanelBinder.setVisible(true);
    }

    @Override
    public void jfxOnDragEnded() {
        super.jfxOnDragEnded(); 
        editPanelBinder.setVisible(false);
    }

    @Override
    public Pane getPropertyPanel() {
        return propertyZone;
    }

    @Override
    public Pane getEditPanel() {
        return editPanel;
    }
    
    /**
     * 设置扩展工具栏是否可见
     * @param visible 
     */
    public void setExtToolbarVisible(boolean visible) {
        jfxExtToolbarPanel.setToolbarVisible(visible);
    }
    
    /**
     * 设置基本工具栏
     * @param jfxToolbar 
     */
    protected void setJfxToolbar(JfxToolbar jfxToolbar) {
        jfxToolbarPanel.getChildren().clear();
        if (this.jfxToolbar != null && this.jfxToolbar.isInitialized()) {
            this.jfxToolbar.cleanup();
        }
        this.jfxToolbar = jfxToolbar;
        if (jfxToolbar != null) {
            if (!jfxToolbar.isInitialized()) {
                jfxToolbar.initialize();
            }
            jfxToolbarPanel.getChildren().add(jfxToolbar.getView());
        }
    }

    /**
     * 添加扩展工具栏
     * @param jfxToolbar
     */
    protected void addJfxExtToolbar(JfxToolbar jfxToolbar) {
        if (jfxExtToolbars != null && jfxExtToolbars.contains(jfxToolbar)) {
            return;
        }
        if (!jfxToolbar.isInitialized()) {
            jfxToolbar.initialize();
        }
        jfxExtToolbarPanel.addToolbar(jfxToolbar.getName(), jfxToolbar.getView());
        jfxExtToolbarPanel.setVisible(true);
        jfxExtToolbars.add(jfxToolbar);
    }

    protected boolean removeJfxExtToolbar(JfxToolbar jfxToolbar) {
        if (jfxExtToolbarPanel.removeToolbar(jfxToolbar.getName())) {
            jfxExtToolbars.remove(jfxToolbar);
            if (jfxToolbar.isInitialized()) {
                jfxToolbar.cleanup();
            }
            return true;
        }
        return false;
    }
    
    /**
     * 当鼠标拖放到编辑面板上面时该方法被调用
     * @param e 
     */
    protected abstract void onDragOver(DragEvent e);
    
    /**
     * 当鼠标从拖放到释放时该方法被调用。
     * @param e 
     */
    protected abstract void onDragDropped(DragEvent e);
}
