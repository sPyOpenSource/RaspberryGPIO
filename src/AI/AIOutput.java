package AI;

import AI.Models.Info;
import com.pi4j.io.serial.Serial;

/**
 * This is the output of AI.
 * 
 * @author X. Wang
 * @version 1.0
 */
public class AIOutput extends AIBaseOutput
{
    private final Serial serial;

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
        Info message = mem.dequeFirst("outgoingMessages2Arduino");
        if (message != null){
            serial.write(message.getPayload());
            serial.flush();
        }
    }

    @Override
    protected void Thread() {
        //Send2Arduino();
    }
}