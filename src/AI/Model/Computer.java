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
package AI.Model;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author X. Wang
 */
public class Computer {
    private final SocketAddress client;
    private final DatagramSocket socket;
    public Computer(SocketAddress client, DatagramSocket socket){
        this.client = client;
        this.socket = socket;
    }
    public boolean isOnline(){
        if(client==null||socket==null)
            return false;
        try {
            socket.bind(client);
            return true;
        } catch (SocketException ex) {
            Logger.getLogger(Computer.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    public void send(Info info){
        if(client==null||socket==null)
            return;
        byte[] outMessage = info.getPayload().getBytes();
        DatagramPacket outPacket = new DatagramPacket(outMessage, outMessage.length, client);
        try {
            socket.send(outPacket);
        } catch (IOException ex) {
            Logger.getLogger(Computer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}