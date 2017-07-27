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
package name.huliqing.luoying.network.discover;

import java.io.UnsupportedEncodingException;

/**
 * @deprecated 不再使用
 * 注：所有code的长度必须统一为4个字节长度的整数
 * @author huliqing
 */
public class PacketParse {
    
    // 消息头
    private final String HEAD = "ly3d";
    
    /**
     * 将要发送的信息进行编码
     * @param code
     * @param content
     * @return 
     * @throws java.io.UnsupportedEncodingException 
     */
    public byte[] encode(String code, String content) throws UnsupportedEncodingException {
        //return (HEAD + code + (content != null ? content : "")).getBytes();
        return (HEAD + code + (content != null ? content : "")).getBytes("UTF-8");
    }
    
    /**
     * 解码接收到的信息，注：该方法会检查头部信息，如果头部信息不匹配，则返
     * 回null,因为这可能是其它机器发送的广播信息。
     * @param data
     * @return 
     * @throws java.io.UnsupportedEncodingException 
     */
    public String[] decode(byte[] data) throws UnsupportedEncodingException {
//        Logger.getLogger(PacketParse.class.getName()).log(Level.INFO, "Raw message before decode:{0}", new String(data));
        if (data == null || data.length <= 0) {
            return null;
        }
//        String fullStr = new String(data, 0, data.length);
        String fullStr = new String(data, 0, data.length, "UTF-8");
        if (!checkHead(fullStr)) {
            return null;
        }
        // code为固定４个字符长度
        int codeLen = 4;
        int contentIndex = HEAD.length() + codeLen;
        String code = fullStr.substring(HEAD.length(), contentIndex);
        String content = fullStr.substring(contentIndex);
        return new String[] {code, content};
    }

    /**
     * 检查头部信息是否匹配，如果不匹配，则不作任何处理
     * @param data
     * @return 
     */
    private boolean checkHead(String data) {
        String head = data.substring(0, HEAD.length());
        return head.equals(HEAD);
    }
    
}
