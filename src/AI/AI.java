package AI;

/**
 * This is a class initialize an artificial intelligence.

 * 
 * @author Xuyi Wang
 * @version 1.0
 */

public final class AI
{
    // instance variables
    private final AIMemory mem = new AIMemory();
    private final AIInput inp;
    private final AILogic log;
    private final AIOutput oup;
    private final Thread logThread,inpThread,oupThread;
    
    /**
     * Constructor for objects of class AI
     */
    public AI()
    {
        // Initialize instance variables
        inp = new AIInput(mem);
        log = new AILogic(mem);
        oup = new AIOutput(mem);
	logThread = new Thread(log);
        inpThread = new Thread(inp);
        oupThread = new Thread(oup);
    }
    
    public static void main(String[] args) {
        System.load("/home/spy/opencv/build/lib/libopencv_java320.so");
        AI instance = new AI();
        instance.start();
    }
    
    public void start()
    {
	logThread.start();
        inpThread.start(); 
        oupThread.start();
    }
}