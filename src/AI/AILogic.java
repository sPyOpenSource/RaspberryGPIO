package AI;

import AI.Model.Info;
import AI.util.MotionDetection;
import java.util.List;
import java.util.Random;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is the logic class of AI.
 * 
 * @author X. Wang 
 * @version 1.0
 */
public class AILogic implements Runnable
{
    // instance variables
    private final AIMemory mem;
    private final Random random = new Random();
    private final int imax;
    private final double dt;
    private final double threshold,factor,zmincor,d,f,xmincor,percentage;
    private int index;
    private final MotionDetection left, right;
        
    /**
     * Constructor for objects of class AILogic
     * @param mem
     */
    public AILogic(AIMemory mem)
    {
        // Initialize instance variables        
	this.mem = mem;
        percentage = 0.5;
        threshold = 10000;
        left = new MotionDetection(0.5, threshold);
        right = new MotionDetection(0.5, threshold);
        imax = 150;
        index = 1;
        dt = 0.02;   // in s
        factor = 500/0.6;
        zmincor = 4.5*3.82/3; // 1m
        xmincor = 0.14/0.62*5; //-0.14 ipv 0.62, 
        d = 0.185;
        f = 1.90/zmincor;
    }

    /**
     * this is a wait method
     * 
     * @param t in seconds
     */
    public static void Wait(double t)
    {
        if(t>0){
            try {
                Thread.sleep((int)(t*1000));
            } catch (InterruptedException e) {
                Logger.getLogger(AIMemory.class.getName()).log(Level.SEVERE, null, e);
            }   
        }	
    }
    
    private void ProcessMessages(){
        while(true){
            Info info = mem.dequeFirst("incomingMessages");
            if(info==null)
                break;
            mem.SaveShort(info.getPayload(), Wish(mem.getShortLength()));
            Fuzzy(info);
            Info response = Induction(info);
            if (response!=null){
                mem.addInfo(response, "outgoingMessages2Web");
            } else {
                String message = Dream();
                if(message!=null)
                    mem.addInfo(new Info(message), "outgoingMessages2Web");
            }
        }   
    }

    private Info Induction(Info info)
    {
    	String key = info.getPayload();
        List<Info> messages = mem.search(key);
        int s = messages.size();
        if(s>0)
            return messages.get(Wish(s));
        else
            return null;
    }
    
    private void Fuzzy(Info info)
    {
    	double p = 0;
        int l = mem.getShortLength();
    	for (int i = 0; i < l; i++)
    	{
            if (info.getPayload().equals(mem.GetShortMemory(i)))
            {
                p=p+1.0/l;
            }
    	}
    	if(p>percentage)
            mem.addInfo(info, "longterm");
    }
    
    private String Dream()
    {
    	return mem.GetShortMemory(Wish(mem.getShortLength()));
    }
    
    private int Wish(int length)
    {
	return (int) (random.nextDouble()*length);
    }
    
    public void Love()
    {
        int l = mem.getShortLength();
        List<Info> messages = mem.search("longterm");
        int s = messages.size();
        if(s==0){
            return;
        }
    	for (int j = 0; j < l/2; j++)
    	{
            mem.SaveShort(messages.get(Wish(s)).getPayload(),Wish(l));
    	}
    }
    private void Clean(String key){
        List<Info> list = mem.search(key);
        for(int i=list.size()-1;i>=0;i--){
            if(!list.get(i).isOnline())
                list.remove(i);
        }
    }

    private void ProcessImages() {    
        Info imageLeft = mem.getLast("leftImages");
        if (imageLeft!=null){
            left.UpdatePosition(imageLeft);
            mem.removeAll("leftImages");
        }  
        Info imageRight = mem.getLast("rightImages");
        if (imageRight!=null){
            mem.removeAll("rightImages");
            right.UpdatePosition(imageRight);
        }
        double xLeft = left.getX()/factor;
        double yLeft = left.getY()/factor;
        double xRight = right.getX()/factor;
        double yRight = right.getY()/factor;
        double l = Math.sqrt(Math.pow(xLeft - xRight,2)+Math.pow(yLeft - yRight,2));
        if (l>0){
            double z = (d*f)/l;
            double x = -xLeft*z/f/xmincor;
            double y = -yLeft*z/f/xmincor ;
            System.out.println(String.format("%.4f, %.4f, %.4f",x,y,z));
        }
        if(index%imax==0){
            System.gc();
        }
        index++;
    }

    @Override
    public void run() {
        while(true){
            ProcessImages();
            ProcessMessages();
            Clean("networkClients");
            Wait(dt);
        }
    }
}