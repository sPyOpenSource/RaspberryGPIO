package AI;

import AI.Models.Info;
import gnu.io.SerialPort;
import java.io.IOException;
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
    private final SerialPort serial;

    /**
     * Constructor for objects of class AIOutput
     * @param mem
     */
    public AIOutput(AIMemory mem)
    {
        super(mem);
        serial = mem.getSerial();
    }
    
    private void Send(){
        Info message = (Info)mem.dequeFirst("outgoingMessages2Serial");
        if (message != null){
            try {
                serial.getOutputStream().write(message.getPayload().getBytes());
                serial.getOutputStream().flush();
            } catch (IOException ex) {
                Logger.getLogger(AIOutput.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    protected void Thread() {
        Send();
    }
}