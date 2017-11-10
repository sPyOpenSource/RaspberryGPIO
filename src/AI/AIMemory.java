package AI;

import com.pi4j.io.serial.Serial;
import java.util.logging.Level;
import java.util.logging.Logger;
        
/**
 * This is the memory class of AI.
 * 
 * @author X. Wang 
 * @version 1.0
 */
public class AIMemory extends AIBaseMemory
{
    // instance variables
    private Serial serial;

    /**
     * Constructor for objects of class Memory
     */
    public AIMemory()
    {
        // Initialize instance variables
        try {
            serial.open("/dev/ttyACM0",115200); 
        } catch(NullPointerException ex) {
            Logger.getLogger(AIMemory.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /*public Serial getSerial(){
        return serial;
    }*/
}