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
package name.huliqing.luoying.object.sound;

import com.jme3.math.Vector3f;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * 用于统一管理声音的管理器
 * @author huliqing
 */
public class SoundManager {
     
    private final static SoundManager INSTANCE = new SoundManager();
    private SoundManager() {}
    public static SoundManager getInstance() {
        return INSTANCE;
    }
    
    // 声音的开启或关闭
    private boolean soundEnabled = true;
    private float volume = 1.0f;
    
    // 普通无循环声效,用于播放非常普通的声效
    private final SoundPlayer simplePlayer = new SoundPlayer();
    
    // 当前音效列表
    private final Set<Sound> sounds = new HashSet<Sound>();
    
    /**
     * 简单播放声效，非循环。
     * @param soundId 声效ID
     * @param position 声源位置
     */
    public void playSound(String soundId, Vector3f position) {
        if (!soundEnabled) return;
        simplePlayer.playSound(soundId, position, volume);
    }
    
    /**
     * 添加声音到列表中并立即播放，该方法会立即播放声音。当声音不再使用时要将声音从列表中移除。
     * @param sound 
     */
    public void addAndPlay(Sound sound) {
        if (!sound.isInitialized()) {
            sound.initialize();
        }
        // 如果全局声音未打开，则只要将“循环”类型的声音添加到列表即可，不需要执行播放，而”非循环“的声音直接丢弃，不
        // 需要播放也不需要添加到列表中。
        if (!soundEnabled) {
            if (sound.isLoop()) {
                sounds.add(sound);
            }
            return;
        }
        sounds.add(sound);
        sound.setVolume(volume * sound.getData().getVolume());
        sound.play();
    }
    
    /**
     * 停止声音循环，并将声音移除出列表，这样可以让声音自然停止。
     * @param sound 
     * @return  
     * @see #removeAndStopDirectly(Sound) 
     */
    public boolean removeAndStopLoop(Sound sound) {
        if (sound.isInitialized()) {
            sound.setLoop(false);
        }
        return sounds.remove(sound);
    }

    /**
     * 直接停止播放指定的声音，并将声音移出列表, 部分情况下这可能会让声音停止得太不自然。
     * @param sound 
     * @return  
     * @see #removeAndStopLoop(Sound) 
     */
    public boolean removeAndStopDirectly(Sound sound) {
        if (sound.isInitialized()) {
            sound.stop();
        }
        return sounds.remove(sound);
    }
    
    /**
     * 判断音效是否打开.
     * @return 
     */
    public boolean isSoundEnabled() {
        return soundEnabled;
    }
    
    /**
     * 开启或关闭全局声音，如果是关闭声音，则所有当前列表中的声音都将被立即停止播放，并且所有“非循环”类型的声音将
     * 会被移出列表中，只留下“循环”的, 以便当重新打开声音时，所有循环类型的声音可以重新播放。
     * @param enabled 
     */
    public void setSoundEnabled(boolean enabled) {
        // 值不改变则不需要处理多余的逻辑
        if (soundEnabled == enabled) 
            return;
        
        soundEnabled = enabled;
        if (soundEnabled) {
            restartSounds();
        } else {
            stopSounds();
        }
    }

    /**
     * 获取音量，0.0~1.0
     * @return 
     */
    public float getVolume() {
        return volume;
    }

    /**
     * 设置音量，0.0~1.0
     * @param volume 
     */
    public void setVolume(float volume) {
        this.volume = volume;
        updateSoundsVolume();
    }
    
    /**
     * 停止播放所有声音,并且将所有不是循环类型的声音移除出列表。
     */
    private void stopSounds() {
        Iterator<Sound> it = sounds.iterator();
        Sound temp;
        while (it.hasNext()) {
            temp = it.next();
            temp.stop();
            if (!temp.isLoop()) {
                it.remove();
            }
        }
    }
    
    /**
     * 重新打开声音,所有循环类型的声音会重新开始播放，而非循环的声音会被移除出列表。
     */
    private void restartSounds() {
        Iterator<Sound> it = sounds.iterator();
        Sound temp;
        while (it.hasNext()) {
            temp = it.next();
            if (temp.isLoop()) {
                temp.play();
            } else {
                it.remove();
            }
        }
    }
    
    /**
     * 重新打开声音,所有循环类型的声音会重新开始播放，而非循环的声音会被移除出列表。
     */
    private void updateSoundsVolume() {
        Iterator<Sound> it = sounds.iterator();
        Sound s;
        while (it.hasNext()) {
            s = it.next();
            s.setVolume(volume * s.getData().getVolume());
        }
    }

}
