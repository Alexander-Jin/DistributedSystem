import java.io.*;
import java.net.*;
import java.util.*;

public class ServerRunnable implements Runnable {
    private Thread t;
    private int processID;
    private int port;
    private int clock;
    private int[] money;
    private ServerSocket socketServer;
    private Integer sentProcessID0;
    private Integer sentProcessID1;
    private Integer sentPort0;
    private Integer sentPort1;
    private int[] receivePort;
    private String command;
    private HashMap<Integer, Integer> map;
    private HashMap<Double, int[]> inChannelRecord;
    private HashMap<Double, boolean[]> outChannelRecord;
    private HashMap<Double, int[]> messageRecord;
    private HashMap<Double, Integer> processRecord;
    private HashMap<Double, Boolean> snapFinished;

    ServerRunnable(int processID, int port, int[] money) {
        this.clock = 0;
        this.processID = processID;
        this.port = port;
        this.money = money;
        inChannelRecord = new HashMap<Double, int[]>();
        outChannelRecord = new HashMap<Double, boolean[]>();
        messageRecord = new HashMap<Double, int[]>();
        processRecord = new HashMap<Double, Integer>();
        snapFinished = new HashMap<Double, Boolean>();
        try{
            this.socketServer = new ServerSocket(port);
        }
        catch (Exception e){
            e.printStackTrace(System.out);
        }

        map = new HashMap<Integer, Integer>();
        map.put(1, 5101);
        map.put(2, 5102);
        map.put(3, 5103);
        for (int i = 1; i <= 3; i++){
            if (i != processID){
                if (sentProcessID0 == null) sentProcessID0 = i;
                else sentProcessID1 = i;
            }
        }
        this.sentPort0 = map.get(sentProcessID0);
        this.sentPort1 = map.get(sentProcessID1);
        this.receivePort = new int[3];
    }

    public void start(){
        if (t == null){
            t = new Thread(this, String.valueOf(processID) + "ServerRunnable");
            t.start();
        }
    }

    public void run() {
        System.out.printf("Process %d started with initial money 1000\n", processID);
        try{
            while (true) {
                Socket socket = socketServer.accept();
                DataInputStream inputStream = new DataInputStream(socket.getInputStream());
                int sentProcessID = inputStream.readInt();
                boolean isMarker = inputStream.readBoolean();
                int moneyValue = inputStream.readInt();
                double snapCode = inputStream.readDouble();

                if (isMarker && !snapFinished.containsKey(snapCode)){
                    processRecord.put(snapCode, money[0]);
                    snapFinished.put(snapCode, false);

                    int[] inChannelRecordElement = new int[4];
                    int[] messageRecordElement = new int[4];
                    boolean[] outChannelRecordElement = new boolean[4];
                    Arrays.fill(inChannelRecordElement, -1);

                    outChannelRecord.put(snapCode, outChannelRecordElement);
                    messageRecord.put(snapCode, messageRecordElement);
                    inChannelRecord.put(snapCode, inChannelRecordElement);

                    inChannelRecord.get(snapCode)[sentProcessID] = 0;

                    System.out.printf("Receive marker from process %d\n", sentProcessID);
                    System.out.print(String.format("*** snapshot %.2f of process %d, %d\n", snapCode, processID, processRecord.get(snapCode)));
                    System.out.print(String.format("*** snapshot %.2f of channel %d-%d, %d\n", snapCode, sentProcessID, processID, 0));

                    send(sentProcessID0, true, 0, snapCode);
                    send(sentProcessID1, true, 0, snapCode);
                    outChannelRecord.get(snapCode)[sentProcessID0] = true;
                    outChannelRecord.get(snapCode)[sentProcessID1] = true;
                    System.out.printf("Send marker to process %d\n", sentProcessID0);
                    System.out.printf("Send marker to process %d\n", sentProcessID1);
                }
                else if (isMarker && !snapFinished.get(snapCode)){
                    inChannelRecord.get(snapCode)[sentProcessID] = messageRecord.get(snapCode)[sentProcessID];
                    System.out.printf("Receive marker from process %d\n", sentProcessID);
                    System.out.print(String.format("*** snapshot %.2f of channel %d-%d, %d\n", snapCode, sentProcessID, processID, inChannelRecord.get(snapCode)[sentProcessID]));
                    int inChannelRecordCount = 0;
                    int inChannelSum = 0;
                    for (int i : inChannelRecord.get(snapCode)){
                        if (i >= 0){
                            inChannelRecordCount += 1;
                            inChannelSum += i;
                        }
                    }
                    if (inChannelRecordCount == 2){
                        snapFinished.put(snapCode, true);
                        System.out.printf("*** snapshot %.2f (in channels + process) for process %d, %d\n", 
                            snapCode, processID, inChannelSum + processRecord.get(snapCode));
                    }
                }
                else {
                    money[0] += moneyValue;
                    for (double code: messageRecord.keySet()){
                        if (!snapFinished.get(code)) messageRecord.get(code)[sentProcessID] += moneyValue;
                    }
                    System.out.printf("Receive money from process %d, ", sentProcessID);
                    System.out.printf("Current money: %d\n", money[0]);
                }
                
            }
        }
        catch (Exception e){
            e.printStackTrace(System.out);
        }
    }

    public void send(int sentID, boolean isMarker, int moneySent, double snapCode){
        SendRunnable r = new SendRunnable(this.processID, sentID, map.get(sentID), isMarker, moneySent, snapCode);
        r.start();
    }

    public void snapshot(){
        double snapCode = snapFinished.size() + processID / 10.0;
        System.out.print(String.format("*** Start snapshot %.2f\n", snapCode));
        processRecord.put(snapCode, (money[0]));
        snapFinished.put(snapCode, false);

        int[] inChannelRecordElement = new int[4];
        int[] messageRecordElement = new int[4];
        boolean[] outChannelRecordElement = new boolean[4];
        Arrays.fill(inChannelRecordElement, -1);

        outChannelRecord.put(snapCode, outChannelRecordElement);
        messageRecord.put(snapCode, messageRecordElement);
        inChannelRecord.put(snapCode, inChannelRecordElement);

        outChannelRecord.get(snapCode)[sentProcessID0] = true;
        outChannelRecord.get(snapCode)[sentProcessID1] = true;

        try{
            System.out.print(String.format("*** snapshot %.2f of process %d, %d\n", snapCode, processID, processRecord.get(snapCode)));
            send(sentProcessID0, true, 0, snapCode);
            send(sentProcessID1, true, 0, snapCode);
        }
        catch (Exception e){
            e.printStackTrace(System.out);
        }
    }
}