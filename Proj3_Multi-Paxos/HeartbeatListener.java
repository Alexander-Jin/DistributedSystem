import java.io.*;
import java.net.*;
import java.util.*;

public class HeartbeatListener implements Runnable {
    private Thread t;
    private int processID;
    private Config config;
    private Instance instance;
    private ServerSocket socketServer;

    HeartbeatListener(int processID, Config config, Instance instance, ServerSocket socketServer){
        this.processID = processID;
        this.config = config;
        this.instance = instance;
        this.socketServer = socketServer;
    }

    public void start(){
        if (t == null){
            t = new Thread(this, String.valueOf(processID) + "HeartbeatListener");
            t.start();
        }
    }

    public void run(){
        try{
            while (true) {
                
                if (instance.processID == config.leaderProcessID){
                    System.out.printf("Process %d begins sending heartbeat.", processID);
                    BroadcastRunnable br = new BroadcastRunnable(processID, config, new Message(processID, -1, instance));
                    br.start();
                }
                Thread.sleep(200);
            }
        }
        catch (Exception e){
            e.printStackTrace(System.out);
        }
    }
}