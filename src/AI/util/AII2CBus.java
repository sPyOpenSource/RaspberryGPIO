/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package AI.util;

import AI.Models.Vector3D;
import static AI.util.L3GD20.L3GD20ADDRESS;
import static AI.util.L3GD20.L3GD20_REG_RW_CTRL_REG1;
import static AI.util.L3GD20.L3GD20_REG_R_OUT_X_L;
import static AI.util.LSM303.CTRL1;
import static AI.util.LSM303.CTRL2;
import static AI.util.LSM303.CTRL5;
import static AI.util.LSM303.CTRL6;
import static AI.util.LSM303.CTRL7;
import static AI.util.LSM303.D_OUT_X_L_M;
import static AI.util.LSM303.LSM303_ADDRESS_ACCEL;
import static AI.util.LSM303.LSM303_ADDRESS_MAG;
import static AI.util.LSM303.LSM303_REGISTER_ACCEL_OUT_X_L_A;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author spy
 */
public class AII2CBus
{
    //private I2CBus bus;
    private boolean verbose = false;
    //private I2CDevice accelerometer, magnetometer, l3dg20;
    private double gain;
    
    public AII2CBus(){
        /*try {
            // Get i2c bus
            //bus = I2CFactory.getInstance(I2CBus.BUS_1); // Depends onthe RasPI version
            if (verbose)
                System.out.println("Connected to bus. OK.");

            // Get device itself
            //l3dg20        = bus.getDevice(L3GD20ADDRESS);
            //accelerometer = bus.getDevice(LSM303_ADDRESS_ACCEL);
            //magnetometer  = bus.getDevice(LSM303_ADDRESS_MAG);
            if (verbose)
                System.out.println("Connected to devices. OK.");
        } catch (IOException ex) {
            Logger.getLogger(AII2CBus.class.getName()).log(Level.SEVERE, null, ex);
        }*/
    }
    
    public void init() throws Exception
    {
        /*
        * Start sensing
        */
        
        // Enable gyroscope
        //l3dg20.write(L3GD20_REG_RW_CTRL_REG1, (byte)0xFF);
        gain = 0.00875 * 3.14 / 180;      

        // Enable accelerometer
        //accelerometer.write(CTRL2, (byte)0x00);

        // 0x57 = 0b01010111
        // AODR = 0101 (50 Hz ODR); AZEN = AYEN = AXEN = 1 (all axes enabled)
        //accelerometer.write(CTRL1, (byte)0x97);
        if (verbose)
            System.out.println("Accelerometer OK.");

        // Enable magnetometer
        //magnetometer.write(CTRL5, (byte)0x64);

        // 0x20 = 0b00100000
        // MFS = 01 (+/- 4 gauss full scale)
        //magnetometer.write(CTRL6, (byte)0x20);

        // 0x00 = 0b00000000
        // MLP = 0 (low power mode off); MD = 00 (continuous-conversion mode)
        //magnetometer.write(CTRL7, (byte)0x00);
        if (verbose)
            System.out.println("Magnetometer OK.");
    }
    
    public Vector3D readingAcc() throws IOException
    {
        byte[] accelData = new byte[6];

        /*if (accelerometer.read(LSM303_REGISTER_ACCEL_OUT_X_L_A | 0x80, accelData, 0, 6) == 6){
            double accelX = accel12(accelData, 0);
            double accelY = accel12(accelData, 2);
            double accelZ = accel12(accelData, 4);

            return new Vector3D(accelX, accelY, accelZ);
        }*/
        throw new IOException("Error reading acc data, < 6 bytes");
    }
  
    public Vector3D readingMag() throws IOException
    {
        byte[] magData = new byte[6];

        // Reading magnetometer measurements.
        /*if (magnetometer.read(D_OUT_X_L_M | (1 << 7), magData, 0, 6) == 6){
            double magX = mag16(magData, 0);
            double magY = mag16(magData, 2);
            double magZ = mag16(magData, 4);

            return new Vector3D(magX, magY, magZ);
        }*/
        throw new IOException("Error reading mag data, < 6 bytes");
    }
    
    private static double accel12(byte[] list, int idx){
        int n = (list[idx] & 0xFF) | ((list[idx + 1] & 0xFF) << 8);     // Low, high bytes
        if (n > 32767) 
            n -= 65536;                                                 // 2's complement signed
        return n >> 4;                                                  // 12-bit resolution
    }

    private static double mag16(byte[] list, int idx){
        int n = ((list[idx] & 0xFF) << 8) | (list[idx + 1] & 0xFF);     // High, low bytes
        return (n < 32768 ? n : n - 65536);                             // 2's complement signed
    }
    
    public Vector3D readingGyr() throws IOException
    {     
        byte[] gyroData = new byte[6];
        
        /*if (l3dg20.read(L3GD20_REG_R_OUT_X_L | 0x80, gyroData, 0, 6) == 6){
            byte ylo = gyroData[0];
            byte yhi = gyroData[1];
            byte xlo = gyroData[2];
            byte xhi = gyroData[3];
            byte zlo = gyroData[4];
            byte zhi = gyroData[5];
            
            return new Vector3D (- (int)(xlo | (xhi << 8)) * this.gain, (int)(ylo | (yhi << 8)) * this.gain, (int)(zlo | (zhi << 8)) * this.gain);
        }*/
        throw new IOException("Error reading gyr data, < 6 bytes");
    }
}
