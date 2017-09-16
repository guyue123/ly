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
import java.util.logging.Logger;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.util.Callback;
import name.huliqing.editor.component.ComponentDefine;
import name.huliqing.editor.constants.ComponentConstants;
import name.huliqing.editor.constants.ConfigConstants;
import name.huliqing.editor.constants.DataFormatConstants;
import name.huliqing.editor.manager.ComponentManager;
import name.huliqing.editor.manager.ConfigManager.ConfigChangedListener;
import name.huliqing.editor.manager.Manager;
import name.huliqing.fxswing.Jfx;
import name.huliqing.luoying.xml.DataFactory;
import name.huliqing.luoying.xml.Proto;

/**
 *
 * @author huliqing
 */
public class ComponentsForm extends TableView<ComponentDefine> implements ConfigChangedListener{
    private static final Logger LOG = Logger.getLogger(ComponentsForm.class.getName());
    
    private List<String> componentTypes;
    
    public ComponentsForm() {
        init();
    }
    
    public ComponentsForm(List<String> types) {
    	componentTypes = types;
        init();
    }

	private void init() {
/*		setCellValueFactory((TableView<ComponentDefine> param) -> new ListCell<ComponentDefine>() {
            @Override
            protected void updateItem(ComponentDefine item, boolean empty) {
                super.updateItem(item, empty);
                String name = null;
                Node icon = null;
                if (item != null && !empty) {
                    name = item.getName();
                }
                setText(name);
                setGraphic(icon);
            }
        });*/
		initTable(this);
		
        setOnDragDetected(this::doDragDetected);
        setOnDragDone(this::doDragDone);
        
        updateAassetDir();
        
        // 切换资源目录的时候要重置组件面板
        Manager.getConfigManager().addListener(this);
	}
	
    /**
     * 设置参数，响应
     * @param tableView
     */
	@SuppressWarnings("unchecked")
	private void initTable(TableView<ComponentDefine> tableView) {
		// 列表
        //listView.setCellFactory(new CellInner());
		TableColumn<ComponentDefine, String> iconCol = new TableColumn<>("图片");
        iconCol.setMinWidth(60);
        iconCol.setMaxWidth(60);
        iconCol.setCellValueFactory(new CellInner("icon"));
 
        TableColumn<ComponentDefine, String> idCol = new TableColumn<>("编号");
        idCol.setMinWidth(90);
        idCol.setCellValueFactory(new CellInner("id"));
 
        TableColumn<ComponentDefine, String> nameCol = new TableColumn<>("名称");
        nameCol.setMinWidth(160);
        nameCol.setCellValueFactory(new CellInner("name"));
        
       /* TableColumn<ComponentDefine, String> catCol = new TableColumn<>("分类");
        catCol.setMinWidth(60);
        catCol.setCellValueFactory(new CellInner("catName"));*/
        
        tableView.setEditable(false);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        //listView.setItems(data);
        tableView.getColumns().addAll(iconCol, idCol, nameCol);
	}
	
    private class CellInner implements Callback<CellDataFeatures<ComponentDefine, String>, ObservableValue<String>> {
    	private String cellName;
    	
    	public CellInner(String fieldName) {
    		this.cellName = fieldName;
    	}
    	
        @Override
        public ObservableValue<String> call(CellDataFeatures<ComponentDefine, String> param) {
        	//ComponentDefine item = param.getValue();
        	String nameStr = "";
        	ComponentDefine cd = param.getValue();//ComponentManager.getComponentsById(item.getId());
            if ("id".equals(cellName)) {
            	nameStr = cd.getId();
            } else if ("name".equals(cellName)) {
	            nameStr = cd.getName();
            } else if ("catName".equals(cellName)) {
            	nameStr = cd.getCname();
            } else if ("icon".equals(cellName)) {
            	nameStr = cd.getIcon();
            }
        	
            return new SimpleStringProperty(nameStr);
        }
    }
	
	/**
	 * 过滤
	 * @param items
	 * @return
	 */
	private List<ComponentDefine> filtComponent(List<ComponentDefine> items) {
		if (componentTypes == null || componentTypes.isEmpty()) {
			return items;
		}
		
		List<ComponentDefine> selectedItems = new ArrayList<>();
		for (ComponentDefine item : items) {
			if (componentTypes != null && !componentTypes.isEmpty() && item != null) {
	    		Proto p = DataFactory.getProto(item.getId());
	    		if (p != null) {
	    			if (componentTypes.contains(p.getTagName())) {
	    				selectedItems.add(item);
	    			}
	    		}
	    	}
		}
		
		return selectedItems;
	}
    
    @Override
    public void onConfigChanged(String key) {
        if (key.equals(ConfigConstants.KEY_MAIN_ASSETS)) {
            Jfx.runOnJfx(() -> updateAassetDir());
        }
    }
    
    private void updateAassetDir() {
        getItems().clear();
        List<ComponentDefine> cds = ComponentManager.getComponentsByType(ComponentConstants.ENTITY);
        if (cds != null) {
            getItems().addAll(filtComponent(cds));
        }
        List<ComponentDefine> cdsFilter = ComponentManager.getComponentsByType(ComponentConstants.ENTITY_FILTER);
        if (cdsFilter != null) {
            getItems().addAll(filtComponent(cdsFilter));
        }
    }
    
	
    public void updateList(String filterText) {
    	List<ComponentDefine> tempList = new ArrayList<>();
    	List<ComponentDefine> cdList = new ArrayList<>();
    	
        List<ComponentDefine> cds = ComponentManager.getComponentsByType(ComponentConstants.ENTITY);
        if (cds != null) {
        	cdList.addAll(filtComponent(cds));
        }
        List<ComponentDefine> cdsFilter = ComponentManager.getComponentsByType(ComponentConstants.ENTITY_FILTER);
        if (cdsFilter != null) {
        	cdList.addAll(filtComponent(cdsFilter));
        }
        
        tempList.clear();
        getItems().clear();
        
        if (cdsFilter == null || cdsFilter.isEmpty()) {
        	return;
        }
        
        // 无任何过滤
        if (filterText == null || filterText.isEmpty()) {
            for (ComponentDefine e : cdList) {
                tempList.add(e);
            }
            getItems().addAll(tempList);
            return;
        }
        
        // 允许通过半角","号分隔多个过滤项
        String[] filters = filterText.split(",");
        String entityStr;
        for (ComponentDefine e : cdList) {
            if (filters.length <= 0) {
                tempList.add(e);
                continue;
            }
            entityStr = e.getId();
            if (e.getName() != null) {
                entityStr += "(" + e.getName() + ")";
            }
            
            for (String filter : filters) {
                if (!filter.isEmpty() && entityStr.toLowerCase().contains(filter)) {
                    tempList.add(e);
                }
            }
        }
        getItems().addAll(tempList);
    }
    

    private ComponentDefine getMainSelectItem() {
        ObservableList<ComponentDefine> items = getSelectionModel().getSelectedItems();
        if (items.isEmpty()) {
            return null;
        }
        for (ComponentDefine c : items) {
            if (c != null) {
                return c;
            }
        }
        return null;
    }

    private void doDragDetected(MouseEvent e) {
//        LOG.log(Level.INFO, "EntityComponents: doDragDetected.");
        ComponentDefine selected = getMainSelectItem();
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
//        LOG.log(Level.INFO, "EntityComponents: doDragDone.");
        e.consume();
    }

}
