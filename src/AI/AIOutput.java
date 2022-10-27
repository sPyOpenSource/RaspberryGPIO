package AI;

import AI.Models.Info;
import AI.util.MicroPython;
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
    private final MicroPython microPython;

    /**
     * Constructor for objects of class AIOutput
     * @param mem
     */
    public AIOutput(AIMemory mem)
    {
	super(mem);
        serial = mem.getSerial();
        microPython = new MicroPython(serial);
    }
    
    private void Send(){
        Info message = (Info)mem.dequeFirst("outgoingMessages2Serial");
        if (message != null){
            serial.write(message.getPayload());
            serial.flush();
        }
    }

    @Override
    protected void Thread() {
        Send();
    }
    
    public void sendFile(String path){
        microPython.sendFile(path);
    }
}