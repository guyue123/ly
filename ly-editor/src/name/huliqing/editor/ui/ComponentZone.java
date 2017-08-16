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
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import name.huliqing.editor.constants.ResConstants;
import name.huliqing.editor.constants.StyleConstants;
import name.huliqing.editor.manager.Manager;

/**
 *
 * @author huliqing
 */
public class ComponentZone extends VBox{
    
    private final TitledPane componentsPanel = new TitledPane();
    
    /**
     * 过滤组件
     */
    private List<String> componentTypes;
    
    public ComponentZone() {
        super();
        
        init();
    }
    
    public ComponentZone(String componentType) {
        super();
        
        if (componentTypes == null) {
        	componentTypes = new ArrayList<String>();
        }
        componentTypes.add(componentType);
        init();
    }
    
    public ComponentZone(List<String> types) {
        super();
        
        if (componentTypes == null) {
        	componentTypes = new ArrayList<String>();
        }
        
        if (types != null) {
        	componentTypes.addAll(types);
        }
        
        init();
    }
    
    public ComponentZone(String... types) {
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
		ComponentsForm cv = new ComponentsForm(componentTypes);
        componentsPanel.setContent(cv);
        componentsPanel.setText(Manager.getRes(ResConstants.FORM_COMPONENTS_TITLE));
        
        getChildren().add(componentsPanel);
        getStyleClass().add(StyleConstants.CLASS_HVBOX);
        setPadding(Insets.EMPTY);
	}
    
}
