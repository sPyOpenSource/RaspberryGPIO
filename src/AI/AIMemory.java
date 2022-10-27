package AI;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

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
    private SerialPort serial;

    /**
     * Constructor for objects of class Memory
     */
    public AIMemory()
    {
        // Initialize instance variables
        try {
            serial = (SerialPort)CommPortIdentifier.getPortIdentifier("/dev/ttyACM0").open(this.getClass().getName(), 2000);
            serial.setSerialPortParams(115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
        } catch(NullPointerException | NoSuchPortException | PortInUseException | UnsupportedCommOperationException ex) {
            Logger.getLogger(AIMemory.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public SerialPort getSerial(){
        return serial;
    }
}