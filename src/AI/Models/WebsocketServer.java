package AI.Models;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.bind.DatatypeConverter;

/**
 *
 * @author X. Wang
 */
public class WebsocketServer {
    private InputStream in;
    private OutputStream out;
    private Socket client;
    private final ServerSocket server;
    private boolean isConnected = false;
    
    public WebsocketServer(ServerSocket server){
        this.server = server;
    }
    
    public void WaitOnConnection(){
        try {
            client = server.accept();
            isConnected = true;
            System.out.println("A client connected.");
            in = client.getInputStream();
            out = client.getOutputStream();
            String data = new Scanner(in,"UTF-8").useDelimiter("\\r\\n\\r\\n").next();
            Matcher get = Pattern.compile("^GET").matcher(data);
            if (get.find()) {
                Matcher match = Pattern.compile("Sec-WebSocket-Key: (.*)").matcher(data);
                match.find();
                byte[] response = ("HTTP/1.1 101 Switching Protocols\r\n"
                        + "Connection: Upgrade\r\n"
                        + "Upgrade: websocket\r\n"
                        + "Sec-WebSocket-Accept: "
                        + DatatypeConverter
                        .printBase64Binary(
                                MessageDigest
                                .getInstance("SHA-1")
                                .digest((match.group(1) + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11")
                                        .getBytes("UTF-8")))
                        + "\r\n\r\n")
                        .getBytes("UTF-8");
                out.write(response, 0, response.length);
            }        
        } catch (IOException | NoSuchAlgorithmException ex) {
            Logger.getLogger(WebsocketServer.class.getName()).log(Level.SEVERE, null, ex);
        }  
    }
    
    public String getInput(){
        try {
            byte[] buffer = new byte[50];
            in.read(buffer, 0, 50);
            byte[] key = {buffer[2],buffer[3],buffer[4],buffer[5]};
            int length = (buffer[1] & 0xff) - 128;
            byte[] decode = new byte[length];
            for(int i = 0; i < length; i++){
                decode[i] = (byte)(buffer[i+6] ^ key[i & 0x3]);
            }
            return new String(decode);
        } catch (IOException ex) {
            Logger.getLogger(WebsocketServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public void send(String info){ 
        try {
            byte[] response = info.getBytes("UTF-8");
            byte[] message = new byte[2];
            message[0] = (byte)129;
            message[1] = (byte)response.length;
            out.write(message, 0, 2);
            out.write(response, 0, response.length);
        } catch (IOException ex) {
            Logger.getLogger(WebsocketServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean isConnect() {
        return isConnected;
    }
}
