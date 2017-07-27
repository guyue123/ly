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
package name.huliqing.test.swing;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.OverlayLayout;
import javax.swing.SwingUtilities;

import name.huliqing.test.fxjme.JfxSetuper;

/**
 *
 * @author huliqing
 */
public class TestLayered {

    private int width = 640;
    private int height = 480;
    
    private JFrame jframe;
    private JWindow jfxWindow;

    private void createFrame() {
        jframe = new JFrame("Test");
        jframe.setName("MainWindow");
        jframe.setSize(width, height);
        jframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        jframe.setUndecorated(false);
//        jframe.setBackground(new Color(0,0,0,0f));
//        jframe.add(SwingUtils.createCanvas());
//        jfxWindow = createJfxWindow(jframe);

        OverlayLayout layout = new OverlayLayout(jframe);
        
        
        
        JPanel jp1 = new TranslucentPane();
        jp1.setSize(width, (int) (height * 0.5));
        jp1.setBackground(new Color(0,0,0,0));
        jp1.add(new JButton("1111"));
//        jp1.setOpaque(false);
        
        JPanel jp2 = new JPanel();
        jp2.setSize(width, height);
//        jp2.setBackground(new Color(0,0,0,0.1f));
        jp2.add(new JButton("22222222222222"));
        jp2.add(SwingUtils.createCanvas());
        
        
        jframe.add(jp1);
        jframe.add(jp2);

    }
    
//    private Component createJfxWindow() {
//        
//        
//        JFXPanel jfxPanel = new JFXPanel();
//        jfxPanel.setSize(width, height);
//        window.getContentPane().add(jfxPanel);
//        
//        Platform.runLater(() -> {
//            Button btn = new Button("Hello JFX!");
//            StackPane root = new StackPane();
//            root.setBackground(javafx.scene.layout.Background.EMPTY);
//            root.getChildren().add(btn);
//            Scene scene = new Scene(root);
//            scene.setFill(Color.TRANSPARENT);
//            jfxPanel.setScene(scene);
//        });
//        
//        SwingUtilities.invokeLater(() -> {
//            window.setLocation(500, 100);
//            window.setAlwaysOnTop(true);
//        });
//        SwingUtils.setDragable(window);
//        window.setVisible(true);
//        return window;
//    }
    
    private void start() {
        SwingUtilities.invokeLater(() -> {
            createFrame();
            jframe.setLocationRelativeTo(null);
            jframe.setVisible(true);
        });
    }
    
    public static void main(String[] args) {
    	JfxSetuper jfxSetuper = new JfxSetuper();
    	jfxSetuper.setupJavaFX();
    	
        new TestLayered().start();
    }
    
    public class TranslucentPane extends JPanel {

        public TranslucentPane() {
            setOpaque(false);
            setBackground(new java.awt.Color(0,0,0,0));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g); 

            Graphics2D g2d = (Graphics2D) g.create();
//            g2d.setComposite(AlphaComposite.SrcOver.derive(0.85f));
            g2d.setComposite(AlphaComposite.SrcOver.derive(0.01f));
            g2d.setColor(getBackground());
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }

    }
}
