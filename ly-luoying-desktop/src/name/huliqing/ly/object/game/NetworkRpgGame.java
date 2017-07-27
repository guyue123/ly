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
package name.huliqing.ly.object.game;

import com.jme3.app.Application;
import com.jme3.math.Vector3f;
import name.huliqing.luoying.LuoYing;
import name.huliqing.luoying.data.EntityData;
import name.huliqing.luoying.network.LanGame;
import name.huliqing.luoying.network.Network;
import name.huliqing.luoying.object.anim.AnimationControl;
import name.huliqing.luoying.object.anim.ScaleAnim;
import name.huliqing.luoying.object.entity.impl.ChaseCameraEntity;
import name.huliqing.luoying.ui.Icon;
import name.huliqing.luoying.ui.UI;
import name.huliqing.luoying.ui.state.UIState;
import name.huliqing.ly.view.ActorSelectView;
import name.huliqing.ly.view.ClientsWin;

/**
 *
 * @author huliqing
 */
public abstract class NetworkRpgGame extends SimpleRpgGame implements LanGame {
    
    protected final Network network = Network.getInstance();
    
    // 选择角色用的界面
    protected ActorSelectView actorPanel;
    
    // 客户端列表界面
    protected ClientsWin clientsWin;
    protected ScaleAnim clientsWinAnim;
    protected AnimationControl winAnimControl;
    protected Icon lanBtn;
    
    public NetworkRpgGame() {}
    
    @Override
    public void initialize(Application app) {
        super.initialize(app); 
        // 初始化Network
        network.initialize(app);
        // 创建LanUI界面
        createLanUI();
    }
    
    @Override
    protected void simpleUpdate(float tpf) {
        super.simpleUpdate(tpf);
        network.update(tpf);
    }
    
    @Override
    public void cleanup() {
        network.cleanup();
        super.cleanup(); 
    }
    
     /**
     * 显示角色选择面板
     */
    public final void showSelectPanel() {
        if (actorPanel == null) {
            actorPanel = new ActorSelectView(LuoYing.getSettings().getWidth(), LuoYing.getSettings().getHeight(), this.app.getRootNode());
            actorPanel.setSelectedListener(new ActorSelectView.SelectedListener() {
                @Override
                public void onSelected(EntityData entityData) {
                    onSelectPlayer(entityData);
                }
            });
        }
        actorPanel.setModels(data.getAvailableActors());
        
        UIState.getInstance().addUI(actorPanel);
        
        ChaseCameraEntity cc = getChaseCamera();  
        if (cc != null) {
            cc.setEnabled(true);
            cc.setChase(actorPanel.getActorView());
        }
    }
    
    @Override
    public boolean isServer() {
        return network.isServer();
    }
    
    /**
     * 刷新客户端界面列表
     */
    @Override
    public void onClientListUpdated() {
        if (clientsWin.isVisible()) {
            clientsWin.setClients(getClients());
        }
    }

    private void createLanUI() {
         // ---- 联网状态按钮,用于打开局域网客户端列表面板
        float fullWidth = LuoYing.getSettings().getWidth();
        float fullHeight = LuoYing.getSettings().getHeight();
        clientsWin = new ClientsWin(this, fullWidth * 0.75f, fullHeight * 0.8f);
        clientsWin.setToCorner(UI.Corner.CC);
        clientsWin.setCloseable(true);
        clientsWin.setDragEnabled(true);
        clientsWinAnim = new ScaleAnim();
        clientsWinAnim.setStartScale(0.5f);
        clientsWinAnim.setEndScale(1f);
        clientsWinAnim.setRestore(true);
        clientsWinAnim.setUseTime(0.4f);
        clientsWinAnim.setLocalScaleOffset(new Vector3f(clientsWin.getWidth() * 0.5f
                    , clientsWin.getHeight() * 0.5f
                    , 0));
        clientsWinAnim.setTarget(clientsWin);
        
        winAnimControl = new AnimationControl(clientsWinAnim);
        app.getGuiNode().addControl(winAnimControl);
                
        lanBtn = new Icon("Interface/icon/link.png");
        lanBtn.setUseAlpha(true);
        lanBtn.addClickListener(new name.huliqing.luoying.ui.UI.Listener() {
            @Override
            public void onClick(UI ui, boolean isPress) {
                if (isPress) return;
                displayLanPanel();
            }
        });
        // 把按钮添加到工具栏
        getMenuTool().addMenu(lanBtn, 0);
    }
    
    private void displayLanPanel() {
        if (clientsWin.getParent() == null) {
            UIState.getInstance().addUI(clientsWin);
            clientsWin.setClients(getClients());
            clientsWinAnim.start();
        } else {
            clientsWin.removeFromParent();
        }
    } 
    
    /**
     * 当玩家通过角色选择面板选择了一个角色后该方法被调用，该方法主要需要处理以下事情：<br>
     * 1.对于服务端来说: 需要载入指定的角色，作为玩家的控制角色，并广播到所有客户端。<br>
     * 2.对于客户端来说: 需要把指定的角色id及名称发送到服务端，由服务端确认，并载入角色, 然后把结束通知客户端，
     * 让客户端知道选择的角色成功与否，如果成功则客户端将指定角色转为客户端玩家本地控制的角色。
     * @param entityData
     */
    protected abstract void onSelectPlayer(EntityData entityData);
}
