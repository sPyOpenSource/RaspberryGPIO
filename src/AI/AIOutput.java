package AI;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is the output of AI.
 * 
 * @author X. Wang
 * @version 1.0
 */
public class AIOutput extends AIBaseOutput
{
    private Writer w;
 
    /**
     * Constructor for objects of class AIOutput
     * @param mem
     */
    public AIOutput(AIMemory mem)
    {
	super(mem);
        try {
            w = new OutputStreamWriter(mem.getSerialPort().getOutputStream(),"UTF-8");
        } catch (IOException | NullPointerException ex) {
            Logger.getLogger(AIOutput.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /*private void Send(String message){
        Info info = mem.dequeFirst(message);
        if(info==null)
            return;
        try {
            w.write(info.getPayload());
            w.flush();
        } catch (IOException ex) {
            Logger.getLogger(AIOutput.class.getName()).log(Level.SEVERE, null, ex);
        }
    }*/
    


    @Override
    public void run() {
        while(true){
            //Send("outgoingMessages2Arduino");
            AILogic.Wait(dt);
        }
    }
}