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

import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import name.huliqing.editor.constants.AssetConstants;
import name.huliqing.editor.constants.StyleConstants;
import name.huliqing.editor.ui.utils.JfxUtils;

/**
 *
 * @author huliqing
 */
public class ComponentZone extends VBox{
    
    private final VBox componentsPanel = new VBox();
    
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

	public void init() {       
		ComponentsForm cv = new ComponentsForm(componentTypes);
        // componentsPanel.setContent(cv);
		componentsPanel.getChildren().add(cv);
        //componentsPanel.setText(Manager.getRes(ResConstants.FORM_COMPONENTS_TITLE));
        
		getChildren().addAll(searchPane(cv), componentsPanel);
        getStyleClass().add(StyleConstants.CLASS_HVBOX);
        setPadding(Insets.EMPTY);
	}
	
	private Pane searchPane(final ComponentsForm cf) {
        StackPane filterPane = new StackPane();
        HBox imageView = new HBox(JfxUtils.createIcon(AssetConstants.INTERFACE_ICON_SEARCH));
        TextField inputFilter = new TextField();
		
        imageView.setPadding(new Insets(0, 0, 0, 10));
        imageView.setMinWidth(16);
        imageView.setMaxWidth(16);
        imageView.prefHeightProperty().bind(filterPane.heightProperty());
        imageView.setAlignment(Pos.CENTER);
        inputFilter.prefWidthProperty().bind(filterPane.widthProperty());
        inputFilter.prefHeightProperty().bind(filterPane.heightProperty());
        inputFilter.setPadding(new Insets(0, 0, 0, 25));
        filterPane.setMinHeight(25);
        filterPane.setAlignment(Pos.CENTER_LEFT);
        
        filterPane.getChildren().add(inputFilter);
        filterPane.getChildren().add(imageView);
       // getChildren().add(filterPane);
        
        inputFilter.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
        	cf.updateList(newValue);
        });
        inputFilter.setStyle(StyleConstants.CSS_CORNER_ROUND_TOP);
        
        return filterPane;
	}
}
