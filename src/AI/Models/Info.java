package AI.Models;

import org.opencv.core.Mat;

/**
 *
 * @author X. Wang
 */
public class Info {
    private final String name;
    private final Mat image;
    private final Computer computer;
    public Info(String name){
        this.name = name;
        image = null;
        computer = null;
    }
    public Info(Mat image){
        this.image = image;
        name = null;
        computer = null;
    }
    public Info(Computer computer){
        this.computer = computer;
        name = null;
        image = null;
    }
    public String getPayload(){
        return name;
    }
    public Mat getImage(){
        return image;
    }
    public boolean isOnline(){
        if (computer!=null){
            return computer.isOnline();
        }
        return false;
    }
    public boolean isStart(){
        if (computer!=null){
            return computer.isStart();
        }
        return false;
    }
    public void send(Info info){
        if(computer!=null)
            computer.send(info);
    }
    public String Receive(){
        if(computer!=null)
            return computer.getInput();
        else
            return null;
    }
}
