import java.io.*;
import java.net.*;
import java.util.*;

public class SendRunnable implements Runnable {
    private Thread t;
    private int processID;
    private String sendHost;
    private int sendPort;
    private Message message;

    SendRunnable(int processID, String sendHost, int sendPort, Message message){
        this.processID = processID;
        this.sendHost = sendHost;
        this.sendPort = sendPort;
        this.message = message;
    }

    public void start(){
        if (t == null){
            t = new Thread(this, String.valueOf(processID) + "SendRunnable");
            t.start();
        }
    }

    public void run(){
        try {
            if (message.messageType != -1){
                System.out.printf("Send message in type %d to server (%s, %d)\n", message.messageType, sendHost, sendPort);
            }
            Socket socket = new Socket(sendHost, sendPort);
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.writeObject(message);
            outputStream.flush();
            if (message.messageType != -1){
                System.out.printf("Message sent\n", sendHost, sendPort);
            }
        }
        catch (Exception e){
            if (message.messageType != -1){
                System.out.printf("Send message in type %d to server (Host %s, Port %d) failed.\n", message.messageType, sendHost, sendPort);
            }
        }
    }
}