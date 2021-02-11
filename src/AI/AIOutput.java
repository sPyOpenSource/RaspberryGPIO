package AI;

import AI.Models.Info;
import org.opencv.core.Mat;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
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
    
    private void Send(){
        Info info = (Info)mem.dequeFirst("outgoingMessages2Serial");
        if(info == null)
            return;
        try {
            w.write(info.getPayload());
            w.flush();
        } catch (IOException ex) {
            Logger.getLogger(AIOutput.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
public BufferedImage getBufferedImage(String camera){
    //source: http://answers.opencv.org/question/10344/opencv-java-load-image-to-gui/
    //Fastest code
    //The output can be assigned either to a BufferedImage or to an Image

     int type = BufferedImage.TYPE_BYTE_GRAY;
     Mat temp = ((Info)mem.dequeFirst(camera)).getImage();
     if (temp == null){
         return null;
     }
     if (temp .channels() > 1 ) {
         type = BufferedImage.TYPE_3BYTE_BGR;
     }
     int bufferSize = temp.channels() * temp.cols() * temp.rows();
     byte [] b = new byte[bufferSize];
     temp.get(0, 0, b); // get all the pixels
     BufferedImage image = new BufferedImage(temp.cols(), temp.rows(), type);
     final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
     System.arraycopy(b, 0, targetPixels, 0, b.length);  
     temp.release();
     return image;
    }
    
    @Override
    protected void Thread() {
        Send();
    }
}