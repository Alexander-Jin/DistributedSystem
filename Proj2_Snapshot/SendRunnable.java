import java.io.*;
import java.net.*;
import java.util.*;

public class SendRunnable implements Runnable {
    private Thread t;
    private int sentID;
    private int sentPort;
    private int moneySent;
    private boolean isMarker;
    private int processID;
    private double snapCode;

    SendRunnable(int processID, int sentID, int sentPort, boolean isMarker, int moneySent, double snapCode){
        this.sentID = sentID;
        this.processID = processID;
        this.sentPort = sentPort;
        this.isMarker = isMarker;
        this.moneySent = moneySent;
        this.snapCode = snapCode;
    }

    public void start(){
        if (t == null){
            t = new Thread(this, String.valueOf(sentID) + "TCPRunnable");
            t.start();
        }
    }

    public void run(){
        try {
            Socket socket = new Socket("localhost", sentPort);

            Thread.sleep(5000);
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            outputStream.writeInt(processID);
            outputStream.writeBoolean(isMarker);
            outputStream.writeInt(moneySent);
            outputStream.writeDouble(snapCode);
            outputStream.flush();
        }
        catch (Exception e){
            e.printStackTrace(System.out);
        }
    }
}