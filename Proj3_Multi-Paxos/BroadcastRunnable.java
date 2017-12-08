import java.io.*;
import java.net.*;
import java.util.*;

public class BroadcastRunnable implements Runnable {
    private Thread t;
    private int processID;
    private Message message;
    private Config config;

    BroadcastRunnable(int processID, Config config, Message message){
        this.processID = processID;
        this.config = config;
        this.message = message;
    }

    public void start(){
        if (t == null){
            t = new Thread(this, String.valueOf(processID) + "BroadcastRunnable");
            t.start();
        }
    }

    public void run(){
        if (message.messageType != -1){
            System.out.printf("Broadcast message in type %d\n", message.messageType);
        }
        try {
            List<SendRunnable> l = new ArrayList<SendRunnable>();
            for (int sendProcessID: config.hostMap.keySet()){
                if (sendProcessID != processID){
                    l.add(new SendRunnable(processID, config.hostMap.get(sendProcessID), config.portMap.get(sendProcessID), message));
                }
            }
            for (SendRunnable sr: l) sr.start();
        }
        catch (Exception e){
            e.printStackTrace(System.out);
        }
    }
}