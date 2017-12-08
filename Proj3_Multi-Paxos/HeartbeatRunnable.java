import java.io.*;
import java.net.*;
import java.util.*;

public class HeartbeatRunnable implements Runnable {
    private Thread t;
    private int processID;
    private Config config;
    private Instance instance;
    private int[] leaderProcessID;

    HeartbeatRunnable(int processID, Config config, Instance instance, int[] leaderProcessID){
        this.processID = processID;
        this.config = config;
        this.instance = instance;
        this.leaderProcessID = leaderProcessID;
    }

    public void start(){
        if (t == null){
            t = new Thread(this, String.valueOf(processID) + "HeartbeatRunnable");
            t.start();
        }
    }

    public void run(){
        try{
            while (true) {
                Thread.sleep(1000);
                if (instance.processID == leaderProcessID[0]){
                    BroadcastRunnable br = new BroadcastRunnable(processID, config, new Message(processID, -1, instance));
                    br.start();
                }
            }
        }
        catch (Exception e){
            e.printStackTrace(System.out);
        }
    }
}