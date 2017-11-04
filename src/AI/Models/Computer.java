/*
 * Copyright (C) 2016 X. Wang
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package AI.Models;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author X. Wang
 */
public class Computer {
    private final SocketAddress client;
    private final DatagramSocket socket;
    private final Socket webSocket;
    private InputStream in;
    private boolean start = false;
    public Computer(SocketAddress client, DatagramSocket socket){
        this.client = client;
        this.socket = socket;
        this.webSocket = null;
        this.in = null;
    }
    public Computer(Socket webSocket){
        this.webSocket = webSocket;
        this.client = null;
        this.socket = null;
        try {
            this.in = webSocket.getInputStream();
        } catch (IOException ex) {
            Logger.getLogger(Computer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public boolean isOnline(){
        if(client==null&&socket==null&&webSocket==null)
            return false;
        try {
            if(webSocket==null){
                if(client==null||socket==null)
                    return false;
                socket.bind(client);
                return true;
            } else {
                return webSocket.isConnected();
            }
        } catch (SocketException ex) {
            Logger.getLogger(Computer.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    public void send(Info info){
        if(webSocket!=null){
            if(webSocket.isConnected()){
                try {
                    OutputStream out = webSocket.getOutputStream();
                    byte[] response = info.getPayload().getBytes("UTF-8");
                    byte[] message = new byte[2];
                    message[0] = (byte)129;
                    message[1] = (byte)response.length;
                    out.write(message, 0, 2);
                    out.write(response, 0, response.length);
                } catch (IOException ex) {
                    Logger.getLogger(WebsocketServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } else if(client!=null&&socket!=null){
            byte[] outMessage = info.getPayload().getBytes();
            DatagramPacket outPacket = new DatagramPacket(outMessage, outMessage.length, client);
            try {
                socket.send(outPacket);
            } catch (IOException ex) {
                Logger.getLogger(Computer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    public boolean isStart(){
        return start;
    }
    public String getInput(){
        start = true;
        if(in==null){
            return null;
        }
        try {
            byte[] buffer = new byte[50];
            in.read(buffer, 0, 50);
            byte[] key = Arrays.copyOfRange(buffer, 2, 6);
            int length = (buffer[1] & 0xff) - 128;
            byte[] decode = new byte[length];
            for(int i = 0; i < length; i++){
                decode[i] = (byte)(buffer[i + 6] ^ key[i & 0x3]);
            }
            return new String(decode);
        } catch (IOException ex) {
            Logger.getLogger(WebsocketServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}