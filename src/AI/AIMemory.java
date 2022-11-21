
package AI;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortInvalidPortException;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is the memory class of AI.
 * Everything including storages
 * 
 * @author X. Wang 
 * @version 1.0
 */
public class AIMemory extends AIBaseMemory
{
    // instance variables
    private SerialPort serialPort;
    
    /**
     * Constructor for objects of class AIMemory
     */
    public AIMemory()
    {
        // Initialize instance variables
        try {
            serialPort = SerialPort.getCommPort("/dev/ttyACM0");
            serialPort.setBaudRate(115200);
            serialPort.setNumDataBits(8);
            serialPort.setNumStopBits(SerialPort.ONE_STOP_BIT);
            serialPort.setParity(SerialPort.NO_PARITY);
            serialPort.openPort();
        } catch (SerialPortInvalidPortException ex) {
            Logger.getLogger(AIMemory.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public SerialPort getSerialPort(){
        return serialPort;
    }
}