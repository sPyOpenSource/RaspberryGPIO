package AI.util;

import AI.Models.Vector3D;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import java.io.IOException;

/*
 * Accelerometer + Magnetometer
 */
public class LSM303
{
  // Minimal constants carried over from Arduino library
  /*
  Prompt> sudo i2cdetect -y 1
       0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f
  00:          -- -- -- -- -- -- -- -- -- -- -- -- --
  10: -- -- -- -- -- -- -- -- -- 19 -- -- -- -- 1e --
  20: -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
  30: -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
  40: -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
  50: -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
  60: -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
  70: -- -- -- -- -- -- -- --
   */
  // Those 2 next addresses are returned by "sudo i2cdetect -y 1", see above.
  public final static int LSM303_ADDRESS_ACCEL = 0x1d;//(0x32 >> 1); // 0011001x, 0x19
  public final static int LSM303_ADDRESS_MAG   = 0x1d;//(0x3C >> 1); // 0011110x, 0x1E
                                                                    // Default    Type
  public final static int LSM303_REGISTER_ACCEL_CTRL_REG1_A = 0x20; // 00000111   rw
  public final static int LSM303_REGISTER_ACCEL_CTRL_REG4_A = 0x23; // 00000000   rw
  public final static int LSM303_REGISTER_ACCEL_OUT_X_L_A   = 0x28;
  public final static int LSM303_REGISTER_MAG_CRB_REG_M     = 0x01;
  public final static int LSM303_REGISTER_MAG_MR_REG_M      = 0x02;
  public final static int LSM303_REGISTER_MAG_OUT_X_H_M     = 0x03;
  public final static int D_OUT_X_L_M       = 0x08;
  public final static int CTRL1             = 0x20; // D
  public final static int CTRL2             = 0x21; // D
  public final static int CTRL3             = 0x22; // D
  public final static int CTRL4             = 0x23; // D
  public final static int CTRL5             = 0x24; // D
  public final static int CTRL6             = 0x25; // D
  public final static int CTRL7             = 0x26; // D
  // Gain settings for setMagGain()
  public final static int LSM303_MAGGAIN_1_3 = 0x20; // +/- 1.3
  public final static int LSM303_MAGGAIN_1_9 = 0x40; // +/- 1.9
  public final static int LSM303_MAGGAIN_2_5 = 0x60; // +/- 2.5
  public final static int LSM303_MAGGAIN_4_0 = 0x80; // +/- 4.0
  public final static int LSM303_MAGGAIN_4_7 = 0xA0; // +/- 4.7
  public final static int LSM303_MAGGAIN_5_6 = 0xC0; // +/- 5.6
  public final static int LSM303_MAGGAIN_8_1 = 0xE0; // +/- 8.1

  private I2CBus bus;
  private I2CDevice accelerometer, magnetometer;
  private byte[] accelData, magData;
  
  private static final boolean verbose = false;
  
  public LSM303()
  {
    if (verbose)
      System.out.println("Starting sensors reading:");
    try
    {
      // Get i2c bus
      bus = I2CFactory.getInstance(I2CBus.BUS_1); // Depends onthe RasPI version
      if (verbose)
        System.out.println("Connected to bus. OK.");

      // Get device itself
      accelerometer = bus.getDevice(LSM303_ADDRESS_ACCEL);
      magnetometer  = bus.getDevice(LSM303_ADDRESS_MAG);
      if (verbose)
        System.out.println("Connected to devices. OK.");

      /*
       * Start sensing
       */
      // Enable accelerometer
      /*accelerometer.write(LSM303_REGISTER_ACCEL_CTRL_REG1_A, (byte)0x27); // 00100111
      accelerometer.write(LSM303_REGISTER_ACCEL_CTRL_REG4_A, (byte)0x00);*/
      accelerometer.write(CTRL2, (byte)0x00);

      // 0x57 = 0b01010111
      // AODR = 0101 (50 Hz ODR); AZEN = AYEN = AXEN = 1 (all axes enabled)
      accelerometer.write(CTRL1, (byte)0x97);
      if (verbose)
        System.out.println("Accelerometer OK.");

      // Enable magnetometer
      /*magnetometer.write(LSM303_REGISTER_MAG_MR_REG_M, (byte)0x00);
      int gain = LSM303_MAGGAIN_1_3;
      magnetometer.write(LSM303_REGISTER_MAG_CRB_REG_M, (byte)gain);*/
      magnetometer.write(CTRL5, (byte)0x64);

      // 0x20 = 0b00100000
      // MFS = 01 (+/- 4 gauss full scale)
      magnetometer.write(CTRL6, (byte)0x20);

      // 0x00 = 0b00000000
      // MLP = 0 (low power mode off); MD = 00 (continuous-conversion mode)
      magnetometer.write(CTRL7, (byte)0x00);
      if (verbose)
        System.out.println("Magnetometer OK.");
    }
    catch (IOException e)
    {
      System.err.println(e.getMessage());
    }
  }

  public Vector3D readingAcc()
    throws IOException
  {
      accelData = new byte[6];

      int r = accelerometer.read(LSM303_REGISTER_ACCEL_OUT_X_L_A | 0x80, accelData, 0, 6);
      if (r != 6)
        System.out.println("Error reading accel data, < 6 bytes");
      
      int accelX = accel12(accelData, 0);
      int accelY = accel12(accelData, 2);
      int accelZ = accel12(accelData, 4);
      
      return new Vector3D(accelX*1.0, accelY*1.0, accelZ*1.0);
  }
  
  public Vector3D readingMag() 
    throws IOException
  {
      magData   = new byte[6];

      // Reading magnetometer measurements.
      //r = magnetometer.read(LSM303_REGISTER_MAG_OUT_X_H_M, magData, 0, 6);
      int r = magnetometer.read(D_OUT_X_L_M|(1<<7), magData, 0, 6);
      if (r != 6)
        System.out.println("Error reading mag data, < 6 bytes");

      int magX = mag16(magData, 0);
      int magY = mag16(magData, 2);
      int magZ = mag16(magData, 4);

      float heading = (float)Math.toDegrees(Math.atan2(magY, magX));
      while (heading < 0)
        heading += 360f;
      /*float pitch = (float)Math.toDegrees(Math.atan2(magX, magZ)); // TODO -180, 180
      float roll  = (float)Math.toDegrees(Math.atan2(magY, magZ)); // TODO -180, 180


      // Bonus : CPU Temperature
      float cpuTemp = Float.MIN_VALUE;
      float cpuVoltage = Float.MIN_VALUE;*/
      /*try
      {
        cpuTemp    = SystemInfo.getCpuTemperature();
        cpuVoltage = SystemInfo.getCpuVoltage();
      }
      catch (InterruptedException | IOException ie)
      {
        ie.printStackTrace();
      }*/

      /*String test = "accel (X: " + accelX + 
                                ", Y: " + accelY + 
                                ", Z: " + accelZ + 
                           ") mag (X: " + magX + 
                                ", Y: " + magY + 
                                ", Z: " + magZ + 
                                ", heading: " + Z_FMT.format(heading) +
                                ", pitch: " + Z_FMT.format(pitch) +
                                ", roll: " + Z_FMT.format(roll) + ")" +
                           (cpuTemp != Float.MIN_VALUE?" Cpu Temp:" + cpuTemp:"") + 
                           (cpuVoltage != Float.MIN_VALUE?" Cpu Volt:" + cpuVoltage:"");*/
      //System.out.println(test);
      return new Vector3D(magX*1.0, magY*1.0, magZ*1.0);
  }

  private static int accel12(byte[] list, int idx)
  {
    int n = (list[idx] & 0xFF) | ((list[idx+1] & 0xFF) << 8); // Low, high bytes
    if (n > 32767) 
      n -= 65536;                           // 2's complement signed
    return n >> 4;                          // 12-bit resolution
  }
  
  private static int mag16(byte[] list, int idx)
  {
    int n = ((list[idx] & 0xFF) << 8) | (list[idx+1] & 0xFF);   // High, low bytes
    return (n < 32768 ? n : n - 65536);       // 2's complement signed
  }
}
