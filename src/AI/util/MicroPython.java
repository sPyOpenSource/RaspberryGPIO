/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package AI.util;

import com.pi4j.io.serial.Serial;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author X. Wang
 */
public class MicroPython {
    private final Serial serial;
    
    public MicroPython(Serial serial){
        this.serial = serial;
    }
    
    public void send(String cmd){
        serial.writeln(cmd);
    }
    
    public void sendFile(String path){
        try (BufferedReader memory = new BufferedReader(new FileReader(path))){
            String line;
            while((line = memory.readLine()) != null){
                send(line);
            }
        } catch (IOException ex) {
            Logger.getLogger(MicroPython.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
