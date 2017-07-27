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

import java.io.File;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.util.Callback;

/**
 * 目标树组件
 * @author huliqing
 */
public class FileTree extends TreeView<File> {

    private final Callback<TreeView<File>, TreeCell<File>> cellCallback = new CellCallback();
    
    public FileTree() {}
    
    public void setRootDir(File rootDir) {
        setCellFactory(cellCallback);
        TreeItem<File> root = createNode(rootDir);
        setRoot(root);
    }
    
    public void refreshItem(TreeItem<File> treeItem) {
        if (treeItem instanceof FileTreeItem) {
            ((FileTreeItem) treeItem).refresh();
        }
    }
    
    private class CellCallback implements Callback<TreeView<File>, TreeCell<File>> {

        @Override
        public TreeCell<File> call(TreeView<File> param) {
            TreeCell<File> cell = new TreeCell<File>() {
                @Override
                protected void updateItem(File item, boolean empty) {
                    super.updateItem(item, empty);
                    if (!empty) {
                        setText(item.getName());
                    } else {
                        setText(null);
                        setGraphic(null);
                    }
                }
            };
            cell.setOnDragOver(e -> {
                File dirFile = cell.getItem();
                if (dirFile != null && dirFile.isDirectory() && e.getDragboard().hasFiles()) {
                    e.acceptTransferModes(TransferMode.MOVE);
                }
                e.consume();
            });
            cell.setOnDragDropped(e -> {
                File dirFile = cell.getItem();
                Dragboard db = e.getDragboard();
                if (dirFile == null || !dirFile.isDirectory() || !db.hasFiles()) {
                    e.consume();
                    return;
                }
                db.getFiles().forEach(t -> {
                    File newFile = new File(dirFile, t.getName());
                    if (!newFile.exists()) {
                        t.renameTo(newFile);
                    } else {
                        t.renameTo(checkNewFile(dirFile, t.getName(), 1));
                    }
                });
                
                // 当树节点被移除之后要刷新
                refreshItem(getRoot());
                
                e.setDropCompleted(true);
                e.consume();
            });
            return cell;
        }
        
        private File checkNewFile(File dir, String filename, int count) {
            File file = new File(dir, filename + "(" + count + ")");
            if (file.exists()) {
                count++;
                return checkNewFile(dir, filename, count);
            }
            return file;
        }

    }
    
    private TreeItem<File> createNode(final File f) {
        return new FileTreeItem(f);
    }
    
    private class FileTreeItem extends TreeItem<File> {
        
        private boolean isLeaf;
        private boolean isFirstTimeChildren = true;
        private boolean isFirstTimeLeaf = true;
        
        public FileTreeItem(File file) {
            super(file);
        }

        public void refresh() {
            isFirstTimeChildren = true;
            isFirstTimeLeaf = true;
            super.getChildren().setAll(buildChildren(this));
        }
        
        @Override
        public ObservableList<TreeItem<File>> getChildren() {
            if (isFirstTimeChildren) {
                isFirstTimeChildren = false;
                super.getChildren().setAll(buildChildren(this));
            }
            return super.getChildren();
        }

        @Override
        public boolean isLeaf() {
            if (isFirstTimeLeaf) {
                isFirstTimeLeaf = false;
                File f = (File) getValue();
                File[] children = f.listFiles();
                isLeaf = f.isFile() || children == null || children.length == 0;
            }
            return isLeaf;
        }

        private ObservableList<TreeItem<File>> buildChildren(TreeItem<File> TreeItem) {
            File f = TreeItem.getValue();
            if (f != null && f.isDirectory()) {
                File[] files = f.listFiles();
                if (files != null) {
                    ObservableList<TreeItem<File>> children = FXCollections.observableArrayList();
                    for (File childFile : files) {
                        children.add(createNode(childFile));
                    }
                    return children;
                }
            }
            return FXCollections.emptyObservableList();
        }
    }
}
