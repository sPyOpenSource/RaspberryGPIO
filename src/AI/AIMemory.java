package AI;

import AI.Model.Computer;
import AI.Model.Info;
import AI.Model.WebsocketServer;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

        
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
    private final ConcurrentHashMap<String, List<Info>> dict = new ConcurrentHashMap<>();
    private DatagramSocket socket;
    private SerialPort serialPort;
    private final String log;
    private WebsocketServer server;
    
    /**
     * Constructor for objects of class AIMemory
     */
    public AIMemory()
    {
        // Initialize instance variables
        shortterm = new String[20];
        emotions = new double[4];
        try {
            serialPort = (SerialPort)CommPortIdentifier.getPortIdentifier("/dev/ttyACM0").open(this.getClass().getName(),2000);
            serialPort.setSerialPortParams(115200,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE); 
        } catch (NoSuchPortException | PortInUseException | UnsupportedCommOperationException ex) {
            Logger.getLogger(AIMemory.class.getName()).log(Level.SEVERE, null, ex);
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
        log ="/home/spy/AI/log.txt";
    }
    
    public void SaveShort(String x, int n)
    {
        shortterm[n%shortterm.length] = x;    
    }

    public String GetShortMemory(int j)
    {
        if (shortterm.length>j&&j>0)
            return shortterm[j];
        else
            return null;
    }
    
    public int getShortLength()
    {
        return shortterm.length;
    }
    
    public void addInfo(Info info, String key){
        if(info!=null&&key!=null)
            search(key).add(info);
    }
    
    public Info dequeFirst(String key){
        List<Info> messages = search(key);
        if (messages.size()>0)
            return messages.remove(0);
        else 
            return null;
    }
    
    public Info dequeLast(String key){
        List<Info> messages = search(key);
        int length = messages.size();
        if (length>0)
            return messages.remove(length-1);
        else
            return null; 
    }
    
    public Info getLast(String key){
        List<Info> messages = search(key);
        int length = messages.size();
        if (length>0)
            return messages.get(length-1);
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
    
    public List<Info> search(String key)
    {
        if (dict.containsKey(key))
            return dict.get(key);
        else
        {
            List<Info> temp = new ArrayList<>();
            dict.put(key,temp);
            return temp;
        }
    }
    
    public void removeAll(String key){
        if (dict.containsKey(key)){
            dict.get(key).clear();
        }
    }
    
    public Enumeration<String> getKeys(){
        return dict.keys();
    }
    
    public SerialPort getSerialPort(){
        return serialPort;
    }
    
    public void ReceiveFromNetwork(int bufferSize){
        byte[] buffer = new byte[bufferSize];
        DatagramPacket inPacket = new DatagramPacket(buffer, bufferSize);
        try {
            socket.receive(inPacket);
        } catch (IOException ex) {
            Logger.getLogger(AIMemory.class.getName()).log(Level.SEVERE, null, ex);
        }
        String[] info = new String(inPacket.getData()).split(";");
        if("start".equals(info[0]))
            addInfo(new Info(new Computer(inPacket.getSocketAddress(),socket)),"networkClients");
        else
            addInfo(new Info(info[0]),"incomingMessages");
    } 
    
    public String getLogPath(){
        return log;
    }
    
    public void ReceiveFromWebsocket(){
        if(!server.isConnect())
            server.WaitOnConnection();
        System.out.println(server.getInput());
    }
    
    public void Send(Info info){
        if (server.isConnect())
            server.send(info);
    }
}