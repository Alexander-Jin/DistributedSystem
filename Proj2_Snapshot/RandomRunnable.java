import java.io.*;
import java.net.*;
import java.util.*;

public class RandomRunnable implements Runnable {
    private Thread t;
    private int processID;
    private int port;
    private int clock;
    private int[] money;
    private Integer sentProcessID0;
    private Integer sentProcessID1;
    private Integer sentPort0;
    private Integer sentPort1;
    private int[] receivePort;
    private String command;
    private HashMap<Integer, Integer> map;
    private boolean[] hasRecord;
    private Integer[] channelRecord;
    private Integer[] messageRecord;
    private Integer processRecord;

    RandomRunnable(int processID, int[] money) {
        this.clock = 0;
        this.processID = processID;
        this.money = money;
        this.hasRecord = new boolean[4];
        this.channelRecord = new Integer[4];
        this.messageRecord = new Integer[4];
        Arrays.fill(this.channelRecord, -1);

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
        Random random = new Random();
        List<Integer> sentPool = new ArrayList<Integer>();
        for (int i = 1; i <= 3; i++){
            if (processID != i) sentPool.add(i);
        }
        try{
            long timeStamp0 = System.currentTimeMillis();
            while (true){
                long timeStamp1 = System.currentTimeMillis();
                long timeDelta = timeStamp1 - timeStamp0;
                if (timeDelta >= 5000){
                    timeStamp0 = timeStamp1;
                    int i = random.nextInt(4);
                    if (i == 1){
                        int moneySent = random.nextInt(100);
                        int randomNumber = random.nextInt(2);
                        int sentID = sentPool.get(random.nextInt(2));
                        SendRunnable r = new SendRunnable(this.processID, sentID, map.get(sentID), false, moneySent, -1.0);
                        r.start();
                        money[0] -= moneySent;
                        System.out.printf("Send money %d to server %d, ", moneySent, sentID);
                        System.out.printf("Current money: %d\n", money[0]);
                    }
                }
            }
        }
        catch (Exception e){
            e.printStackTrace(System.out);
        }
    }
}