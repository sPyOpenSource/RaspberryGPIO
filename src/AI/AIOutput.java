package AI;

import AI.Models.Info;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
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
    private Writer w;
    private final double dt = 0.01; // in s
    private String filename;
 
    /**
     * Constructor for objects of class AIOutput
     * @param mem
     */
    public AIOutput(AIMemory mem)
    {
	this.mem = mem;
        filename = String.format("/home/spy/AI/%s.txt", LocalDateTime.now());
        try {
            w = new OutputStreamWriter(mem.getSerialPort().getOutputStream(),"UTF-8");
        } catch (IOException | NullPointerException ex) {
            Logger.getLogger(AIOutput.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /*private void Send(String message){
        String[] temp = message.split(":");
        Info info = mem.dequeFirst(temp[0]);
        if(info==null)
            return;
        if(temp.length==2){
            mem.Send(info);
            mem.search(temp[1]).stream().forEach((session) -> {
                session.send(info);
            });                
        } else if (temp.length==1){
            try {
                w.write(info.getPayload());
                w.flush();
            } catch (IOException ex) {
                Logger.getLogger(AIOutput.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }*/
    
    private void SaveMemory(){
        try {
            try (BufferedWriter log = new BufferedWriter(new FileWriter(mem.getLogPath(),true))) {
                log.write(filename+"\n");
            }
            Enumeration<String> keys = mem.getKeys();           
            try (BufferedWriter memory = new BufferedWriter(new FileWriter(filename))) {
                while(keys.hasMoreElements()){
                    String key = (String) keys.nextElement();
                    Info value;
                    while((value = mem.dequeFirst(key))!=null){
                        String info = value.getPayload();
                        if (info!=null)
                            memory.write(key+","+info+"\n");
                    }
                }
            }
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
            //Send("outgoingMessages2Arduino");
            //Send("outgoingMessages2Network:networkClients");
            AILogic.Wait(dt);
        }
    }
}