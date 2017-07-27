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
package name.huliqing.luoying.object.channel;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.LoopMode;
import name.huliqing.luoying.data.ChannelData;
import name.huliqing.luoying.object.module.ChannelModule;

/**
 * 用于封装JME3的动画通道
 * @author huliqing
 * @param <T>
 */
public abstract class AbstractChannel <T extends ChannelData> implements Channel <T> {
    
    protected T data;
    protected String[] fromRootBones;
    protected String[] toRootBones;
    protected String[] bones;
    
    // 动画通道是否被锁定
    protected boolean locked;
    // 动画控制器
    protected AnimControl animControl;
    // JME的原始动画通道
    protected AnimChannel animChannel;
    
    @Override
    public void setData(T data) {
        this.data = data;
        fromRootBones = data.getAsArray("fromRootBones");
        toRootBones = data.getAsArray("toRootBones");
        bones = data.getAsArray("bones");
        locked = data.getAsBoolean("locked", locked);
    }

    @Override
    public T getData() {
        return data;
    }

    @Override
    public void updateDatas() {
        String animName = animChannel.getAnimationName();
        // 如果animName不存在，则角色可能刚载入，loop,speed,time等都不需要再获取.
        if (animName == null) 
            return;
        
        LoopMode loop = animChannel.getLoopMode();
        float speed = animChannel.getSpeed();
        float time = animChannel.getTime();
        data.setAttribute("animName", animName);
        data.setAttribute("loop", loopModeToInt(loop));
        data.setAttribute("speed", speed);
        data.setAttribute("time", time);
        data.setAttribute("locked", locked);
    }
    
    @Override
    public void initialize(ChannelModule cm, AnimControl animControl) {
        this.animControl = animControl;
        this.animChannel = createAnimChannel(this.animControl);
        
        // 恢复动画
        String animName = data.getAsString("animName");
        if (animName != null) {
            // 有可能animName为扩展动画。
            cm.checkAndLoadAnim(animName);
            
            LoopMode loop = intToLoopMode(data.getAsInteger("loop", 1));
            float speed = data.getAsFloat("speed", 1.0f);
            float time = data.getAsFloat("time", 0);
            // 恢复动画时如果角色已经锁住则应该临时解开。
            boolean tempLocked = locked;
            if (locked) {
                locked = false;
            }
            playAnim(animName, loop, speed, time, 0);
            locked = tempLocked;
        }
    }

    @Override
    public void playAnim(String animName, LoopMode loop, float speed, float time, float blendTime) {
        if (locked) {
            return;
        }
        
        // 如果当前正在播放的是循环动画，并且与给定的要播放的动画一致，则不要重新从头播放起，
        // 只要更新循环模式和速度就可以。
        // 否则循环动画看起来会有一种被不停切断的感觉
        if (animChannel.getLoopMode() == LoopMode.Loop && animName.equals(animChannel.getAnimationName())) {
            animChannel.setLoopMode(loop);
            animChannel.setSpeed(speed);
            return;
        }
        
        // 注意：setAnim(xx) 必须放在最前面，否则其它参数会被覆盖掉。
        animChannel.setAnim(animName, blendTime);
        animChannel.setLoopMode(loop);
        animChannel.setSpeed(speed);
        animChannel.setTime(time);
    }

    @Override
    public void updateLoopMode(LoopMode loop) {
        animChannel.setLoopMode(loop);
    }

    @Override
    public void updateSpeed(float speed) {
        animChannel.setSpeed(speed);
    }

    @Override
    public String getId() {
        return data.getId();
    }

    @Override
    public String getAnimationName() {
        return animChannel.getAnimationName();
    }

    @Override
    public void reset() {
        // 不要使用该reset操作，该方法可能会让动画速度出现异常。
        // 该问题出现在没有“死亡”动画的蜘蛛身上，使用该方法reset后，
        // 通过recycleManager 回收再利用的时候有一定机率出现"走路”动画不正常
        // animChannel.reset(true);
        
        // 使用speed=0.0001来代替reset更合理.
        // 注意不要把speed设置为0，这会导致除0出错。使用一个接近0的值就可以。
        animChannel.setSpeed(0.0001f);
        animChannel.setTime(0);
    }

    @Override
    public void resetToAnimationTime(String anim, float timeInter) {
        animChannel.setAnim(anim);
        animChannel.setSpeed(0);
        animChannel.setTime(animChannel.getAnimMaxTime() * timeInter);
    }

    @Override
    public void setLock(boolean locked) {
        this.locked = locked;
    }

    @Override
    public boolean isLocked() {
        return locked;
    }

    @Override
    public AnimChannel getAnimChannel() {
        return animChannel;
    }

    /**
     * 创建一个AnimChannel
     * @param ac
     * @return 
     */
    private AnimChannel createAnimChannel(AnimControl ac) {
        AnimChannel channel = ac.createChannel();
        
        // 如果未配置其它任何参数，则默认创建一个完全通道
        if (fromRootBones == null && toRootBones == null && bones == null) {
            return channel;
        }
        
        // From root bones
        if (fromRootBones != null) {
            for (String bone : fromRootBones) {
                channel.addFromRootBone(bone);
            }
        }
        
        // To root bones
        if (toRootBones != null) {
            for (String bone : toRootBones) {
                channel.addToRootBone(bone);
            }
        }
        
        // Other single bones.
        if (bones != null) {
            for (String bone : bones) {
                channel.addBone(bone);
            }
        }
        
        return channel;
    }
    
    private int loopModeToInt(LoopMode lm) {
        switch (lm) {
            case Cycle:
                return 0;
            case DontLoop:
                return 1;
            case Loop:
                return 2;
        }
        throw new RuntimeException("Unsupported LoomMode: lm=" + lm);
    }
    
    private LoopMode intToLoopMode(int lm) {
        switch (lm) {
            case 0:
                return LoopMode.Cycle;
            case 1:
                return LoopMode.DontLoop;
            case 2:
                return LoopMode.Loop;
        }
        throw new RuntimeException("Unsupported int to LoopMode,lm=" + lm);
    }
}
