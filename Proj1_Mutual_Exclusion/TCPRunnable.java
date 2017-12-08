import java.io.*;
import java.net.*;
import java.util.*;

public class TCPRunnable implements Runnable {
    private Thread t;
    private int sentProcessID;
    private int sentPort;
    private int clock;
    private int processID;
    private int messageType;

    TCPRunnable(int processID, int sentProcessID, int sentPort, int clock, int messageType){
        this.sentProcessID = sentProcessID;
        this.clock = clock;
        this.processID = processID;
        this.sentPort = sentPort;
        this.messageType = messageType;
    }

    public void start(){
        if (t == null){
            t = new Thread(this, String.valueOf(sentProcessID) + "TCPRunnable");
            t.start();
        }
    }

    public void run(){
        try {
            Socket socket = new Socket("localhost", sentPort);
            if (messageType == 0) System.out.printf("process %d sends request to %d.\n", processID, sentProcessID);
            else if (messageType == 2) System.out.printf("process %d sends release to %d.\n", processID, sentProcessID);

            Thread.sleep(5000);
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            outputStream.writeInt(processID);
            outputStream.writeInt(clock);
            outputStream.writeInt(messageType);
            outputStream.flush();
        }
        catch (Exception e){
            System.out.println(e);
            System.out.println("Thread " +  sentProcessID + " interrupted.");
        }
    }
}