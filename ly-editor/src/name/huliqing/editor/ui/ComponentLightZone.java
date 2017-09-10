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
package name.huliqing.editor.ui;

import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import name.huliqing.editor.component.ComponentDefine;
import name.huliqing.editor.constants.DataFormatConstants;
import name.huliqing.editor.constants.EntityConstants;
import name.huliqing.editor.constants.StyleConstants;
import name.huliqing.editor.manager.ComponentManager;
import name.huliqing.editor.ui.utils.JfxUtils;

/**
 *
 * @author huliqing
 */
public class ComponentLightZone extends VBox{
    
    private final BorderPane componentsPanel = new BorderPane();
    
    /**
     * 过滤组件
     */
    private List<String> componentTypes;
    
    public ComponentLightZone() {
        super();
        
        init();
    }
    
    public ComponentLightZone(String componentType) {
        super();
        
        if (componentTypes == null) {
        	componentTypes = new ArrayList<String>();
        }
        componentTypes.add(componentType);
        init();
    }
    
    public ComponentLightZone(List<String> types) {
        super();
        
        if (componentTypes == null) {
        	componentTypes = new ArrayList<String>();
        }
        
        if (types != null) {
        	componentTypes.addAll(types);
        }
        
        init();
    }
    
    public ComponentLightZone(String... types) {
        super();
        
        if (componentTypes == null) {
        	componentTypes = new ArrayList<String>();
        }
        
        if (types != null) {
        	for (String t : types) {
        		componentTypes.add(t);
        	}
        }
        
        init();
    }

	private void init() {
		//ComponentsForm cv = new ComponentsForm(componentTypes);
		//componentsPanel.getChildren().add(cv);
		//EntityConstants.ENTITY_DIRECTIONAL_LIGHT, EntityConstants.ENTITY_POINT_LIGHT
		
		// 日光
		ComponentDefine cdDL = ComponentManager.getComponentsById(EntityConstants.ENTITY_DIRECTIONAL_LIGHT);
		
		Label labelDL = new Label(cdDL.getName());
		labelDL.setGraphic(JfxUtils.createImage(cdDL.getIcon(), 60, 60));
		labelDL.setTextFill(Color.web("#fff666"));
		labelDL.setContentDisplay(ContentDisplay.TOP);
		labelDL.setGraphicTextGap(10);
		
		labelDL.setOnDragDetected(this::doDragDetected);
		labelDL.setOnDragDone(this::doDragDone);
		labelDL.setUserData(cdDL);
		
		// 灯光
		ComponentDefine cdPL = ComponentManager.getComponentsById(EntityConstants.ENTITY_POINT_LIGHT);
		
		Label labelPL = new Label(cdPL.getName());
		labelPL.setGraphic(JfxUtils.createImage(cdPL.getIcon(), 60, 60));
		labelPL.setTextFill(Color.web("#fff666"));
		labelPL.setContentDisplay(ContentDisplay.TOP);
		labelPL.setGraphicTextGap(10);
		labelPL.setUserData(cdPL);
		
		labelPL.setOnDragDetected(this::doDragDetected);
		labelPL.setOnDragDone(this::doDragDone);
		
		componentsPanel.setMaxWidth(230);
		componentsPanel.setRight(labelPL);
		componentsPanel.setLeft(labelDL);
		
        getChildren().add(componentsPanel);
        this.setAlignment(Pos.CENTER);
        this.setMargin(componentsPanel, new Insets(10, 10, 10, 10));
        getStyleClass().add(StyleConstants.CLASS_HVBOX);
        setPadding(new Insets(10, 10, 10, 10));
	}
    
	/**
	 * 拖动
	 * @param e
	 */
    private void doDragDetected(MouseEvent e) {
    	Label lab = (Label)e.getSource();
    	ComponentDefine selected = (ComponentDefine)lab.getUserData();
		if (selected == null) {
			e.consume();
		    return;
		}
		Dragboard db = startDragAndDrop(TransferMode.ANY);
		ClipboardContent clipboardContent = new ClipboardContent();
		clipboardContent.put(DataFormatConstants.COMPONENT_ENTITY, selected);
		db.setContent(clipboardContent);
		e.consume();
    }

  private void doDragDone(DragEvent e) {
      e.consume();
  }
}
