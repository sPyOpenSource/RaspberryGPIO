package AI.util;

import AI.Models.Vector3D;

import java.io.IOException;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class L3GD20
{
  public final static int L3GD20ADDRESS = 0x6b; 

  public final static int L3GD20_REG_R_WHO_AM_I            = 0x0f; // Device identification register
  public final static int L3GD20_REG_RW_CTRL_REG1          = 0x20; // Control register 1
  public final static int L3GD20_REG_RW_CTRL_REG2          = 0x21; // Control register 2
  public final static int L3GD20_REG_RW_CTRL_REG3          = 0x22; // Control register 3
  public final static int L3GD20_REG_RW_CTRL_REG4          = 0x23; // Control register 4
  public final static int L3GD20_REG_RW_CTRL_REG5          = 0x24; // Control register 5
  public final static int L3GD20_REG_RW_REFERENCE          = 0x25; // Reference value for interrupt generation
  public final static int L3GD20_REG_R_OUT_TEMP            = 0x26; // Output temperature
  public final static int L3GD20_REG_R_STATUS_REG          = 0x27; // Status register
  public final static int L3GD20_REG_R_OUT_X_L             = 0x28; // X-axis angular data rate LSB
  public final static int L3GD20_REG_R_OUT_X_H             = 0x29; // X-axis angular data rate MSB
  public final static int L3GD20_REG_R_OUT_Y_L             = 0x2a; // Y-axis angular data rate LSB
  public final static int L3GD20_REG_R_OUT_Y_H             = 0x2b; // Y-axis angular data rate MSB
  public final static int L3GD20_REG_R_OUT_Z_L             = 0x2c; // Z-axis angular data rate LSB
  public final static int L3GD20_REG_R_OUT_Z_H             = 0x2d; // Z-axis angular data rate MSB
  public final static int L3GD20_REG_RW_FIFO_CTRL_REG      = 0x2e; // Fifo control register
  public final static int L3GD20_REG_R_FIFO_SRC_REG        = 0x2f; // Fifo src register
  public final static int L3GD20_REG_RW_INT1_CFG_REG       = 0x30; // Interrupt 1 configuration register
  public final static int L3GD20_REG_R_INT1_SRC_REG        = 0x31; // Interrupt source register
  public final static int L3GD20_REG_RW_INT1_THS_XH        = 0x32; // Interrupt 1 threshold level X MSB register
  public final static int L3GD20_REG_RW_INT1_THS_XL        = 0x33; // Interrupt 1 threshold level X LSB register
  public final static int L3GD20_REG_RW_INT1_THS_YH        = 0x34; // Interrupt 1 threshold level Y MSB register
  public final static int L3GD20_REG_RW_INT1_THS_YL        = 0x35; // Interrupt 1 threshold level Y LSB register
  public final static int L3GD20_REG_RW_INT1_THS_ZH        = 0x36; // Interrupt 1 threshold level Z MSB register
  public final static int L3GD20_REG_RW_INT1_THS_ZL        = 0x37; // Interrupt 1 threshold level Z LSB register
  public final static int L3GD20_REG_RW_INT1_DURATION      = 0x38; // Interrupt 1 duration register
  
  public final static int L3GD20_MASK_CTRL_REG1_X_EN       = 0x01; // X enable
  public final static int L3GD20_MASK_CTRL_REG1_Y_EN       = 0x02; // Y enable
  public final static int L3GD20_MASK_CTRL_REG1_Z_EN       = 0x04; // Z enable
  public final static int L3GD20_MASK_CTRL_REG1_PD         = 0x08; // Power-down
  public final static int L3GD20_MASK_CTRL_REG1_BW         = 0x30; // Bandwidth
  public final static int L3GD20_MASK_CTRL_REG1_DR         = 0xc0; // Output data rate
  public final static int L3GD20_MASK_CTRL_REG2_HPCF       = 0x0f; // High pass filter cutoff frequency
  public final static int L3GD20_MASK_CTRL_REG2_HPM        = 0x30; // High pass filter mode selection
  public final static int L3GD20_MASK_CTRL_REG3_I2_EMPTY   = 0x01; // FIFO empty interrupt on DRDY/INT2
  public final static int L3GD20_MASK_CTRL_REG3_I2_ORUN    = 0x02; // FIFO overrun interrupt on DRDY/INT2
  public final static int L3GD20_MASK_CTRL_REG3_I2_WTM     = 0x04; // FIFO watermark interrupt on DRDY/INT2
  public final static int L3GD20_MASK_CTRL_REG3_I2_DRDY    = 0x08; // Date-ready on DRDY/INT2
  public final static int L3GD20_MASK_CTRL_REG3_PP_OD      = 0x10; // Push-pull / Open-drain
  public final static int L3GD20_MASK_CTRL_REG3_H_LACTIVE  = 0x20; // Interrupt active configuration on INT1
  public final static int L3GD20_MASK_CTRL_REG3_I1_BOOT    = 0x40; // Boot status available on INT1
  public final static int L3GD20_MASK_CTRL_REG3_I1_INT1    = 0x80; // Interrupt enabled on INT1
  public final static int L3GD20_MASK_CTRL_REG4_SIM        = 0x01; // SPI Serial interface selection
  public final static int L3GD20_MASK_CTRL_REG4_FS         = 0x30; // Full scale selection
  public final static int L3GD20_MASK_CTRL_REG4_BLE        = 0x40; // Big/little endian selection
  public final static int L3GD20_MASK_CTRL_REG4_BDU        = 0x80; // Block data update
  public final static int L3GD20_MASK_CTRL_REG5_OUT_SEL    = 0x03; // Out selection configuration
  public final static int L3GD20_MASK_CTRL_REG5_INT_SEL    = 0xc0; // INT1 selection configuration
  public final static int L3GD20_MASK_CTRL_REG5_HPEN       = 0x10; // High-pass filter enable
  public final static int L3GD20_MASK_CTRL_REG5_FIFO_EN    = 0x40; // Fifo enable
  public final static int L3GD20_MASK_CTRL_REG5_BOOT       = 0x80; // Reboot memory content
  public final static int L3GD20_MASK_STATUS_REG_ZYXOR     = 0x80; // Z, Y, X axis overrun
  public final static int L3GD20_MASK_STATUS_REG_ZOR       = 0x40; // Z axis overrun
  public final static int L3GD20_MASK_STATUS_REG_YOR       = 0x20; // Y axis overrun
  public final static int L3GD20_MASK_STATUS_REG_XOR       = 0x10; // X axis overrun
  public final static int L3GD20_MASK_STATUS_REG_ZYXDA     = 0x08; // Z, Y, X data available
  public final static int L3GD20_MASK_STATUS_REG_ZDA       = 0x04; // Z data available
  public final static int L3GD20_MASK_STATUS_REG_YDA       = 0x02; // Y data available
  public final static int L3GD20_MASK_STATUS_REG_XDA       = 0x01; // X data available
  public final static int L3GD20_MASK_FIFO_CTRL_REG_FM     = 0xe0; // Fifo mode selection
  public final static int L3GD20_MASK_FIFO_CTRL_REG_WTM    = 0x1f; // Fifo treshold - watermark level
  public final static int L3GD20_MASK_FIFO_SRC_REG_FSS     = 0x1f; // Fifo stored data level
  public final static int L3GD20_MASK_FIFO_SRC_REG_EMPTY   = 0x20; // Fifo empty bit
  public final static int L3GD20_MASK_FIFO_SRC_REG_OVRN    = 0x40; // Overrun status
  public final static int L3GD20_MASK_FIFO_SRC_REG_WTM     = 0x80; // Watermark status
  public final static int L3GD20_MASK_INT1_CFG_ANDOR       = 0x80; // And/Or configuration of interrupt events 
  public final static int L3GD20_MASK_INT1_CFG_LIR         = 0x40; // Latch interrupt request
  public final static int L3GD20_MASK_INT1_CFG_ZHIE        = 0x20; // Enable interrupt generation on Z high
  public final static int L3GD20_MASK_INT1_CFG_ZLIE        = 0x10; // Enable interrupt generation on Z low
  public final static int L3GD20_MASK_INT1_CFG_YHIE        = 0x08; // Enable interrupt generation on Y high
  public final static int L3GD20_MASK_INT1_CFG_YLIE        = 0x04; // Enable interrupt generation on Y low
  public final static int L3GD20_MASK_INT1_CFG_XHIE        = 0x02; // Enable interrupt generation on X high
  public final static int L3GD20_MASK_INT1_CFG_XLIE        = 0x01; // Enable interrupt generation on X low
  public final static int L3GD20_MASK_INT1_SRC_IA          = 0x40; // Int1 active
  public final static int L3GD20_MASK_INT1_SRC_ZH          = 0x20; // Int1 source Z high
  public final static int L3GD20_MASK_INT1_SRC_ZL          = 0x10; // Int1 source Z low
  public final static int L3GD20_MASK_INT1_SRC_YH          = 0x08; // Int1 source Y high
  public final static int L3GD20_MASK_INT1_SRC_YL          = 0x04; // Int1 source Y low
  public final static int L3GD20_MASK_INT1_SRC_XH          = 0x02; // Int1 source X high
  public final static int L3GD20_MASK_INT1_SRC_XL          = 0x01; // Int1 source X low  
  public final static int L3GD20_MASK_INT1_THS_H           = 0x7f; // MSB
  public final static int L3GD20_MASK_INT1_THS_L           = 0xff; // LSB
  public final static int L3GD20_MASK_INT1_DURATION_WAIT   = 0x80; // Wait number of samples or not
  public final static int L3GD20_MASK_INT1_DURATION_D      = 0x7f; // Duration of int1 to be recognized

  private static boolean verbose = false;
  
  //private I2CBus bus;
  //private I2CDevice l3dg20;
  private double gain = 1D;
  
  public L3GD20()
  {
    this(L3GD20ADDRESS);
  }
  
  public L3GD20(int address)
  {
    /*try
    {
      // Get i2c bus
      //bus = I2CFactory.getInstance(I2CBus.BUS_1); // Depends onthe RasPI version
      if (verbose)
        System.out.println("Connected to bus. OK.");

      // Get device itself
      //l3dg20 = bus.getDevice(address);
      if (verbose)
        System.out.println("Connected to device. OK.");
    } catch (IOException e) {
      Logger.getLogger(L3GD20.class.getName()).log(Level.SEVERE, null, e);
    }*/
  }
  
  public void writeToRegister(int register, int mask, int value) throws Exception
  {
    int current = readU8(register);
    int newValue = BitOps.setValueUnderMask(value, current, mask);
    //this.l3dg20.write(register, (byte)newValue);
  }
  
  public int readFromRegister(int register, int mask) throws Exception
  {
    int current = readU8(register);
    return BitOps.getValueUnderMask(current, mask);
  }
  
  private String readFromRegisterWithDictionaryMatch(int register, int mask, Map<String, Byte> dictionary) throws Exception
  {    
    int current = this.readFromRegister(register, mask);
    for (String key : dictionary.keySet())
    {
      if (dictionary.get(key) == (byte)current)
        return key;
    }
    return null;
  }
                
  private void writeToRegisterWithDictionaryCheck(int register, int mask, String value, Map<String, Byte> dictionary, String dictName) throws Exception
  {    
    if (!dictionary.containsKey(value))
      throw new RuntimeException("Value [" + value + "] not in range of " + dictName);
    this.writeToRegister(register, mask, dictionary.get(value));
  }
    
  /*
   * To be called after configuration, before measuring
   */
  public void init() throws Exception
  {
      writeU8(L3GD20_REG_RW_CTRL_REG1, 0xFF);
      switch (L3GD20Dictionaries._250_DPS) {
          case L3GD20Dictionaries._250_DPS:
              this.gain = 0.00875*3.14/180;
              break;
          case L3GD20Dictionaries._500_DPS:
              this.gain = 0.0175*3.14/180;
              break;
          case L3GD20Dictionaries._2000_DPS:
              this.gain = 0.07*3.14/180;
              break;
          default:
              break;
      }
  }

  public int[] getAxisOverrunValue() throws Exception
  {
    int zor = 0;
    int yor = 0;
    int xor = 0;
    if (this.readFromRegister(L3GD20_REG_R_STATUS_REG, L3GD20_MASK_STATUS_REG_ZYXOR) == 0x01){
      zor = this.readFromRegister(L3GD20_REG_R_STATUS_REG, L3GD20_MASK_STATUS_REG_ZOR);
      yor = this.readFromRegister(L3GD20_REG_R_STATUS_REG, L3GD20_MASK_STATUS_REG_YOR);
      xor = this.readFromRegister(L3GD20_REG_R_STATUS_REG, L3GD20_MASK_STATUS_REG_XOR);
    }
    return new int[] { xor, yor, zor };
  }

  public int[] getAxisDataAvailableValue() throws Exception
  {
    int zda = 0;
    int yda = 0;
    int xda = 0;
    if (this.readFromRegister(L3GD20_REG_R_STATUS_REG, L3GD20_MASK_STATUS_REG_ZYXDA) == 0x01)
    {
      zda = this.readFromRegister(L3GD20_REG_R_STATUS_REG, L3GD20_MASK_STATUS_REG_ZDA);
      yda = this.readFromRegister(L3GD20_REG_R_STATUS_REG, L3GD20_MASK_STATUS_REG_YDA);
      xda = this.readFromRegister(L3GD20_REG_R_STATUS_REG, L3GD20_MASK_STATUS_REG_XDA);
    }
    return new int[] { xda, yda, zda };
  }
  
  public Vector3D getRawOutValues() throws Exception
  {     
    
    byte[] gyroData = new byte[6];
    int r = 0;//l3dg20.read(L3GD20_REG_R_OUT_X_L|0x80, gyroData, 0, 6);
    if (r != 6)
        Logger.getLogger(L3GD20.class.getName()).log(Level.SEVERE, "Error reading data, < 6 bytes");

    byte ylo = gyroData[0];
    byte yhi = gyroData[1];
    byte xlo = gyroData[2];
    byte xhi = gyroData[3];
    byte zlo = gyroData[4];
    byte zhi = gyroData[5];
    return new Vector3D (-(int)(xlo | (xhi << 8))*this.gain, (int)(ylo | (yhi << 8))*this.gain, (int)(zlo | (zhi << 8))*this.gain);
  }

  /*
   * All getters and setters
   */
  public String getFullScaleValue() throws Exception
  {
    return this.readFromRegisterWithDictionaryMatch(L3GD20_REG_RW_CTRL_REG4, L3GD20_MASK_CTRL_REG4_FS, L3GD20Dictionaries.FullScaleMap);
  }

  public void setFullScaleValue(String value) throws Exception
  {
    this.writeToRegisterWithDictionaryCheck(L3GD20_REG_RW_CTRL_REG4, L3GD20_MASK_CTRL_REG4_FS, value, L3GD20Dictionaries.FullScaleMap, "FullScaleMap") ;
  }
  
  public int getDeviceId() throws Exception
  {
    return this.readFromRegister(L3GD20_REG_R_WHO_AM_I, 0xff);
  }
  
  /*
   * Read an unsigned byte from the I2C device
   */
  private int readU8(int reg) throws Exception
  {
    int result = 0;//this.l3dg20.read(reg);
    if (verbose)
        System.out.println("(U8) I2C: Device " + toHex(L3GD20ADDRESS) + " returned " + toHex(result) + " from reg " + toHex(reg));
    return result;
  }
  
  private void writeU8(int adress, int reg){
      /*try {
          this.l3dg20.write(adress, (byte)reg);
      } catch (IOException ex) {
          Logger.getLogger(L3GD20.class.getName()).log(Level.SEVERE, null, ex);
      }*/
  }
  
  private static String toHex(int i)
  {
    String s = Integer.toString(i, 16).toUpperCase();
    while (s.length() % 2 != 0)
      s = "0" + s;
    return "0x" + s;
  }
}
