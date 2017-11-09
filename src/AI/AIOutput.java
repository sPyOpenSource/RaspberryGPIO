package AI;

/**
 * This is the output of AI.
 * 
 * @author X. Wang
 * @version 1.0
 */
public class AIOutput extends AIBaseOutput
{
    /**
     * Constructor for objects of class AIOutput
     * @param mem
     */
    public AIOutput(AIMemory mem)
    {
	super(mem);
    }
    
    /*private void Send2Arduino(){
        String message = mem.dequeFirst("outgoingMessages2Arduino");
        if (message!=null){
            mem.getSerial().write(message);
            mem.getSerial().flush();
        }
    }*/

    @Override
    public void run() {
        while(true){
            //Send2Arduino();
            AILogic.Wait(dt);
        }
    }
}