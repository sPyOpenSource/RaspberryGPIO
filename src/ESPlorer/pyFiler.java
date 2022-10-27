/**
 *
 * @author 4refr0nt
 */
package ESPlorer;

import static ESPlorer.ESPlorer.sendBuf;
import java.util.ArrayList;

public class pyFiler {

    private static final String dir = "";

    public static final int OK = 0;
    public static final int ERROR_COMMUNICATION = 1;

    public String ListDir() {
        return "";
    }

    public boolean Put(String ft, String[] s) {

        boolean success = true;
        sendBuf = new ArrayList<>();

        sendBuf.add("f=open('" + escape(ft) + "','w')");
        for (String subs : s) {
            sendBuf.add("f.write('" + escape(subs) + "\\n')");
        }
        sendBuf.add("f.close()");

        return success;
    }

    public boolean Get() {
        return false;
    }

    public boolean Rename() {
        return false;
    }

    public int Length() {
        return 0;
    }

    public String cd() {
        return dir;
    }

    public String pwd() {
        return dir;
    }

    public String GetParent() {
        return "";
    }

    public boolean isExist() {
        return false;
    }
    
private byte[] concatArray(byte[] a, byte[] b) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        byte[] r = new byte[a.length + b.length];
        System.arraycopy(a, 0, r, 0, a.length);
        System.arraycopy(b, 0, r, a.length, b.length);
        return r;
    }

    private byte[] copyPartArray(byte[] a, int start, int len) {
        if (a == null) {
            return null;
        }
        if (start > a.length) {
            return null;
        }
        byte[] r = new byte[len];
        try {
            System.arraycopy(a, start, r, 0, len);
        } catch (Exception e) {
            /*log(e.toString());
            log("copyPartArray exception");
            log("size a=" + Integer.toString(a.length));
            log("start =" + Integer.toString(start));
            log("len   =" + Integer.toString(len));*/
        }
        return r;
    }
    
     private void FileDownloadFinisher(boolean success) {
        /*try {
            serialPort.removeEventListener();
        } catch (SerialPortException e) {
            log(e.toString());
        }
        try {
            serialPort.addEventListener(new ESPlorer.PortReader(), portMask);
        } catch (SerialPortException e) {
            log("Downloader: Can't Add OldEventListener.");
        }
        //SendUnLock();
        StopSend();
        if (success) {
            TerminalAdd("Success.\r\n");
            if (DownloadCommand.startsWith("EDIT")) {
                FileNew(rcvFile);
            } else if (DownloadCommand.startsWith("DOWNLOAD")) {
                SaveDownloadedFile();
            } else {
                // nothing, reserved
            }
        } else {
            TerminalAdd("FAIL.\r\n");
        }*/
    }
    
    public String escape(String str) {
        char ch;
        StringBuilder buf = new StringBuilder(str.length() * 2);

        for (int i = 0, l = str.length(); i < l; ++i) {
            ch = str.charAt(i);
            if (ch == '"') {
                buf.append("\\");
            } else if (ch == '\'') {
                buf.append("\\");
            }
            buf.append(ch);
        }
        return buf.toString();
    } // escape
} // pyFiler
