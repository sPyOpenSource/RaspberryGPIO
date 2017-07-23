package AI;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is the output of AI.
 * 
 * @author X. Wang
 * @version 1.0
 */
public class AIOutput implements Runnable
{
    private final AIMemory mem;
    private BufferedWriter memory;
    private InetAddress client;
    private final int client_port;
    private final double dt;
    private String filename;
 
    /**
     * Constructor for objects of class AIOutput
     * @param mem
     */
    public AIOutput(AIMemory mem)
    {
	this.mem = mem;
        filename = String.format("%s.txt", LocalDateTime.now());
        try {
            File file = new File(filename);
            file.createNewFile();
            memory = new BufferedWriter(new FileWriter(file));
        } catch (IOException ex) {
            Logger.getLogger(AIOutput.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            client = InetAddress.getByName("192.168.1.2");
        } catch (UnknownHostException ex) {
            Logger.getLogger(AIOutput.class.getName()).log(Level.SEVERE, null, ex);
        }
        client_port = 8080;
        dt = 0.01; // in s
    }
    
    /*private void Send2Arduino(){
        String message = mem.dequeFirst("outgoingMessages2Arduino");
        if (message!=null){
            mem.getSerial().write(message);
            mem.getSerial().flush();
        }
    }*/
    
    private void Send2Web(){
        String message = mem.dequeFirst("outgoingMessages2Web");
        mem.Send(message);
    }
    
    private void Send2Network(){
        String info = mem.dequeFirst("outgoingMessages2Network");
        if(info!=null){
            byte[] outMessage = info.getBytes();
            DatagramPacket outPacket = new DatagramPacket(outMessage, outMessage.length, client, client_port);
            try {
                mem.getSocket().send(outPacket);
            } catch (IOException ex) {
                Logger.getLogger(AIOutput.class.getName()).log(Level.SEVERE, null, ex);
            }   
        }
    }
    
    private void SaveMemory(){
        try {
            try (BufferedWriter log = new BufferedWriter(new FileWriter(mem.getLogPath()))) {
                log.write(filename+"\n");
            }
            Enumeration<String> keys = mem.getKeys();
            while(keys.hasMoreElements()){
                String key = (String) keys.nextElement();
                while(true){
                    String value = mem.dequeFirst(key);
                    if (value==null)
                        break;
                    memory.write(key+","+value+"\n");
                }
            }
            memory.close();
        } catch (IOException ex) {
            Logger.getLogger(AIOutput.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {
        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            @Override
            public void run()
            {
                SaveMemory();
            }
        });
        while(true){
            Send2Network();
            //Send2Arduino();
            Send2Web();
            AILogic.Wait(dt);
        }
    }
}