import java.io.*;
import java.net.*;
import java.util.*;

public class ServerRunnable implements Runnable {
    private Thread t;
    private int processID;
    private int port;
    private int clock;
    private ServerSocket socketServer;
    private Integer sentProcessID0;
    private Integer sentProcessID1;
    private Integer sentPort0;
    private Integer sentPort1;
    private int[] receivePort;
    private String command;
    private PriorityQueue<int[]> q;
    private HashMap<Integer, Integer> map;

    ServerRunnable(int processID, int port) throws Exception{
        this.clock = 0;
        this.processID = processID;
        this.port = port;
        this.socketServer = new ServerSocket(port);
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
        this.q = new PriorityQueue<int[]>(
            new Comparator<int[]>(){
                public int compare(int[] i1, int[] i2){
                    if (i1[0] == i2[0]) return i1[1] - i2[1];
                    return i1[0] - i2[0];
                }
            });
    }

    public void start(){
        if (t == null){
            t = new Thread(this, String.valueOf(processID) + "ServerRunnable");
            t.start();
        }
    }

    public void run() {
        System.out.printf("serverSocket of process %d started\n", processID);
        try{
            while (true) {
                Socket socket = socketServer.accept();
                DataInputStream inputStream = new DataInputStream(socket.getInputStream());
                int sentProcessID = inputStream.readInt();
                int sentClock = inputStream.readInt();
                int messageType = inputStream.readInt();

                clock = Math.max(sentClock, clock) + 1;
                if (messageType == 0){
                    System.out.printf("Receive request from process %d, clock in process %d is %d\n", sentProcessID, processID, clock);
                    q.offer(new int[]{sentClock, sentProcessID});
                    clock += 1;
                    TCPRunnable r = new TCPRunnable(processID, sentProcessID, map.get(sentProcessID), clock, 1);
                    r.start();
                    System.out.printf("Send response to process %d, clock in process %d is %d\n", sentProcessID, processID, clock);
                }
                else if (messageType == 1){
                    System.out.printf("Receive response from process %d, clock in process %d is %d\n", sentProcessID, processID, clock);
                    receivePort[sentProcessID - 1] = 1;
                }
                else if (messageType == 2){
                    System.out.printf("Receive release from process %d, clock in process %d is %d\n", sentProcessID, processID, clock);
                    q.poll();
                }
                if (receivePort[0] + receivePort[1] + receivePort[2] == 2 && q.peek()[1] == processID){
                    q.poll();
                    enterCriticalSection(command);
                    receivePort = new int[3];
                    send(2, command);
                }
            }
        }
        catch (Exception e){
            System.out.println(e);
        }
    }

    public void enterCriticalSection(String command) throws Exception{
        if (command.toLowerCase().equals("like")){
            System.out.println("----");
            BufferedReader br = new BufferedReader(new FileReader("criticalSection/numOfLikes.txt"));
            int numOfLikes = Integer.valueOf(br.readLine());
            br.close();

            numOfLikes += 1;
            PrintWriter writer = new PrintWriter("criticalSection/numOfLikes.txt");
            writer.print(numOfLikes);
            writer.close();
            System.out.printf("Process %d likes post, current numOfLikes: %d\n", processID, numOfLikes);
            System.out.println("----");
        }
        else{
            System.out.println("----");
            BufferedReader br = new BufferedReader(new FileReader("criticalSection/post.txt"));
            String post = br.readLine();
            br.close();

            br = new BufferedReader(new FileReader("criticalSection/numOfLikes.txt"));
            int numOfLikes = Integer.valueOf(br.readLine());
            br.close();
            System.out.print("\"" + post + "\" Like: ");
            System.out.println(numOfLikes);
            System.out.println("----");
        }
    }

    public void send(int messageType, String command){
        this.command = command;
        clock += 1;
        if (messageType == 0) q.offer(new int[]{clock, processID});
        TCPRunnable r0 = new TCPRunnable(processID, sentProcessID0, sentPort0, clock, messageType);
        TCPRunnable r1 = new TCPRunnable(processID, sentProcessID1, sentPort1, clock, messageType);

        r0.start();
        r1.start();
    }
}