package AI;

import AI.Models.MatEx;
import AI.Models.WebsocketServer;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.pi4j.io.serial.Serial;
import java.io.IOException;
import java.net.ServerSocket;
        
/**
 * This is the memory class of AI.
 * 
 * @author X. Wang 
 * @version 1.0
 */
public class AIMemory
{
    // instance variables
    private final String[] shortterm;
    private final double[] emotions;
    private final ConcurrentHashMap<String, List<String>> dict = new ConcurrentHashMap<>();
    private final List<MatEx> inpImages = new ArrayList<>();
    private DatagramSocket socket;
    private Serial serial;
    private final String log;
    private WebsocketServer server;

    public double percentage;

    /**
     * Constructor for objects of class Memory
     */
    public AIMemory()
    {
        // Initialize instance variables
        shortterm = new String[20];
        emotions = new double[4];
        percentage = 0;
        try {
            serial.open("/dev/ttyACM0",115200); 
        } catch (Exception e) {
            System.out.println(e);
        }
        try {
            socket = new DatagramSocket(8000);
        } catch (SocketException ex) {
            Logger.getLogger(AIMemory.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            server = new WebsocketServer(new ServerSocket(9000));
        } catch (IOException ex) {
            Logger.getLogger(AIMemory.class.getName()).log(Level.SEVERE, null, ex);
        }
        log = "log.txt";
    }

    public String GetShortMemory(int j)
    {
        if (shortterm.length>j&&j>0)
            return shortterm[j];
        else
            return null;
    }
    
    public String GetLongMemory(int j)
    {
        List<String> longterm = search("longterm");
        if (longterm.size()>j&&j>0){
            return longterm.get(j);
        } else {
            return null;
        }
    }
    
    public int getLength()
    {
        return shortterm.length;
    }
    
    public void addInfo(String info, String key){
        if(info!=null&&key!=null)
            search(key).add(info);
    }
    
    public void addInpImage(MatEx image){
        if(image!=null){
            inpImages.add(image);
        }
    }
    
    public MatEx dequeFirstImage(){
        if (inpImages.size()>0){
            return inpImages.remove(0);
        } else {
            return null;
        }
    }
    
    public String dequeFirst(String key){
        List<String> incomingMessages = search(key);
        if (incomingMessages.size()>0)
            return incomingMessages.remove(0);
        else 
            return null;
    }
    
    public String dequeLast(String key){
        List<String> incomingMessages = search(key);
        int length = incomingMessages.size();
        if (length>0)
            return incomingMessages.remove(length-1);
        else
            return null; 
    }
    
    public String getLastConfigure(){
        List<String> configures = search("configures");
        int length = configures.size();
        if (length>0)
            return configures.get(length-1);
        else 
            return null;
    }

    public void setEmotions(int j, double y)
    {
        emotions[j] = y;
    }
    
    public double getEmotions(int j){
        return emotions[j];
    }
    
    private List<String> search(String key)
    {
        if (dict.containsKey(key))
            return dict.get(key);
        else
        {
            List<String> temp = new ArrayList<>();
            dict.put(key,temp);
            return temp;
        }
    }
    
    public Enumeration<String> getKeys(){
        return dict.keys();
    }
    
    /*public Serial getSerial(){
        return serial;
    }*/
    
    public DatagramSocket getSocket(){
        return socket;
    } 
    
    public String getLogPath(){
        return log;
    }
    
    public void ReceiveFromWebsocket(){
        if(!server.isConnect())
            server.WaitOnConnection();
        System.out.println(server.getInput());
    }
    
    public void Send(String info){
        if (server.isConnect())
            server.send(info);
    }
}