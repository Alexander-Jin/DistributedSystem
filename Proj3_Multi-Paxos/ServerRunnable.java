import java.io.*;
import java.net.*;
import java.util.*;

public class ServerRunnable implements Runnable {
    private Thread t;
    private int processID;
    private String hostName;
    private int port;

    private long lastHeartbeatTime;
    private Instance currentInstance;
    private Config currentConfig;

    private HashMap<Integer, Config> configLog;
    private HashMap<Integer, Instance> instanceLog;

    private ServerSocket socketServer;

    private int totalTicket;

    private int[] leaderProcessID;

    ServerRunnable(int processID, String hostName, int port) throws Exception{
        this.processID = processID;
        this.hostName = hostName;
        this.port = port;
        this.socketServer = new ServerSocket(port);
        socketServer.setSoTimeout(3000);
        this.lastHeartbeatTime = System.currentTimeMillis();

        configLog = new HashMap<Integer, Config>();
        instanceLog = new HashMap<Integer, Instance>();

        BufferedReader br = new BufferedReader(new FileReader("config.txt"));
        String line = br.readLine();
        HashMap<Integer, String> hostMap = new HashMap<Integer, String>();
        HashMap<Integer, Integer> portMap = new HashMap<Integer, Integer>();
        leaderProcessID = new int[]{-1};
        while (line != null){
            int sendProcessID = Integer.valueOf(line.split("[ ]+")[0]);
            String sendHost = line.split("[ ]+")[1];
            int sendPort = Integer.valueOf(line.split("[ ]+")[2]);
            line = br.readLine();
            hostMap.put(sendProcessID, sendHost);
            portMap.put(sendProcessID, sendPort);
        }
        br.close();

        currentConfig = new Config(hostMap, portMap, leaderProcessID[0]);
        currentInstance = new Instance(processID, 0, 0, -1, -1);
        currentInstance.setAddress(processID, hostName, port);

        totalTicket = 100;
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
            HeartbeatRunnable hbr = new HeartbeatRunnable(processID, currentConfig, currentInstance, leaderProcessID);
            hbr.start();
            BroadcastRunnable addRunnable = new BroadcastRunnable(processID, currentConfig, new Message(processID, 8, currentInstance));
            addRunnable.start();
            //HeartbeatListener hbl = new HeartbeatRunnable(processID, currentConfig, currentInstance, socketServer);
            //hbl.start();
            while (true) {
            	try{
            		if (leaderProcessID[0] == processID) socketServer.setSoTimeout(300000);
            		else socketServer.setSoTimeout(3000);
                	Socket socket = socketServer.accept();
                	ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
                	Message message = (Message) inputStream.readObject();
                	int receiveProcessID = message.processID;
                	int messageType = message.messageType;
                	Instance receiveInstance = message.instance;
                	int receiveLogID = receiveInstance.logID;
                	String receiveHost = receiveInstance.host;
                	int receivePort = receiveInstance.port;
                	int receiveValue = message.value;

                	/* messageType:
                -1: heartbeat from the leader
                0: request to elect a leader
                1: respond to elect a leader
                2: ask to accept a leader
                3: respond to accept a leader
                4: broadcast a new leader
                5: propose a value
                6: respond to a value
                7: broadcast a new value
                8: new server added to configuration
                9: the server removed from configuration
                10: ask leader to propose a value
                11: inform new server current value
                */

                	if (messageType == -1){
                    	lastHeartbeatTime = System.currentTimeMillis();
                    	currentInstance.logID = receiveLogID;
                	}
                	else if (messageType == 0){
                    	System.out.printf("Receive leader election request from process %d.\n", receiveProcessID);
                    	int receiveProposalID = receiveInstance.proposalID;
                    	if (leaderProcessID[0] != processID &&   		
                    		(receiveProposalID == currentInstance.proposalID && receiveProcessID > processID
                    		|| receiveProposalID > currentInstance.proposalID))
                    	{
                    		currentInstance.proposalID = receiveProposalID;
                        	SendRunnable r = new SendRunnable(processID, receiveHost, receivePort, new Message(processID, 1, currentInstance));
                        	r.start();
                        	currentInstance.respondedToElection = true;
                        	System.out.printf("Responded to election. \n");
                    	}
                	}
                	else if (messageType == 1){
                		System.out.printf("Receive election response from process %d.\n", receiveProcessID);
                    	currentInstance.candidateResponseReceived += 1;
                    	if (currentInstance.candidateResponseReceived >= currentConfig.majority){
                        	BroadcastRunnable br = new BroadcastRunnable(processID, currentConfig, new Message(processID, 2, currentInstance));
                        	br.start();
                    	}
                	}
                	else if (messageType == 2){
                		System.out.printf("Receive request to accept leader from process %d.\n", receiveProcessID);
                    	int receiveProposalID = receiveInstance.proposalID;
                    	if (receiveProposalID >= currentInstance.proposalID && leaderProcessID[0] != processID){
                        	currentInstance.proposalID = receiveProposalID;
                        	SendRunnable r = new SendRunnable(processID, receiveHost, receivePort, new Message(processID, 3, currentInstance));
                        	r.start();
                        	System.out.printf("Responded to new candidate. \n");
                    	}
                	}
                	else if (messageType == 3){
                		System.out.printf("Receive response to accept leader from process %d.\n", receiveProcessID);
                    	currentInstance.leaderResponseReceived += 1;
                    	if (currentInstance.leaderResponseReceived >= currentConfig.majority && leaderProcessID[0] != processID){
                        	currentInstance.elected = true;
                        	currentInstance.candidateResponseReceived = 0;
                        	currentInstance.leaderResponseReceived = 0;
                        	leaderProcessID[0] = processID;
                        	BroadcastRunnable br = new BroadcastRunnable(processID, currentConfig, new Message(processID, 4, currentInstance));
                        	br.start();
                        	System.out.printf("Announced to be the new Leader. \n");
                        	currentInstance.leaderResponseReceived = 0;
                    	}
                	}
                	else if (messageType == 4){
                		System.out.printf("Receive broadcast of new leader from process %d.\n", receiveProcessID);
                    	currentInstance.launchedElection = false;
                    	currentInstance.respondedToElection = false;
                    	leaderProcessID[0] = receiveProcessID;
                	}
                	else if (messageType == 5){
                		System.out.printf("Receive proposal of new value from process %d.\n", receiveProcessID);
                		Instance instance = new Instance(currentInstance);
                		if (instanceLog.containsKey(receiveLogID)) instance = instanceLog.get(receiveLogID);
                		else instanceLog.put(receiveLogID, instance);
                		instance.logID = receiveLogID;
                    	if (receiveInstance.proposalID >= instance.proposalID){
                        	instance.proposalID = receiveInstance.proposalID;
                        	SendRunnable r = new SendRunnable(processID, receiveHost, receivePort, new Message(processID, 6, instance, receiveValue));
                        	r.start();
                        	System.out.printf("Responded to new value proposal with logID %d. \n", receiveLogID);
                    	}
                	}
                	else if (messageType == 6){
                		System.out.printf("Received response for new value from server (%s, %d) with logID %d. \n", receiveInstance.host, receiveInstance.port, receiveLogID);
                		Instance instance = instanceLog.get(receiveLogID);
                    	Config config = configLog.get(receiveLogID);
                    	instance.proposalResponseReceived += 1;
                    	if (instance.proposalResponseReceived >= config.majority){
                    		if (!instance.valueBroadcasted){
                    			System.out.println("New leader elected.");
                    			System.out.print("Received response from majority, broadcast new value");
                    			totalTicket = receiveValue;
                        		BroadcastRunnable br = new BroadcastRunnable(processID, configLog.get(receiveLogID), new Message(processID, 7, instance, receiveValue));
                        		br.start();
                        		instance.valueBroadcasted = true;
                        	}
                    	}
                	}
                	else if (messageType == 7){
                		System.out.printf("Receive broadcase of new value from process %d.\n", receiveProcessID);
                    	totalTicket = receiveValue;
                	}
                	else if (messageType == 8){
                		System.out.printf("find new server process %d.\n", receiveProcessID);
                    	currentConfig.addServer(receiveProcessID, receiveInstance.host, receiveInstance.port);
                    	currentConfig.leaderProcessID = leaderProcessID[0];
                    	SendRunnable r = new SendRunnable(processID, receiveHost, receivePort, new Message(processID, 11, currentInstance, currentConfig, totalTicket));
                        r.start();
                	}
                	else if (messageType == 9){
                		System.out.printf("find server withdrawl from process %d.\n", receiveProcessID);
                    	currentConfig.removeServer(receiveProcessID);
                	}
                	else if (messageType == 10){
                		System.out.printf("Receive request to propose a new value from process %d.\n", receiveProcessID);
                    	Instance newInstance = new Instance(currentInstance);
                    	Config newConfig = new Config(currentConfig);
                    	newInstance.logID = receiveInstance.logID;
                    	instanceLog.put(newInstance.logID, newInstance);
                    	configLog.put(newInstance.logID, newConfig);

                    	BroadcastRunnable br = new BroadcastRunnable(processID, currentConfig, new Message(processID, 5, newInstance, receiveValue));
                    	br.start();
                	}
                	else if (messageType == 11){
                		currentConfig = message.config;
                		currentInstance.logID = receiveLogID;
                		totalTicket = message.value;
                		leaderProcessID[0] = currentConfig.leaderProcessID;
                		System.out.printf("Current number of tickets: %d\n", totalTicket);
                		System.out.println("Current config");
                		System.out.println(currentConfig.portMap);
                	}
                	if (System.currentTimeMillis() - lastHeartbeatTime > 4000 && leaderProcessID[0] != processID){
                    	if (!currentInstance.launchedElection && !currentInstance.respondedToElection){
                    		Random ran = new Random();
                    		int randomWait = ran.nextInt(2000);
                    		Thread.sleep(randomWait);
                        	BroadcastRunnable br = new BroadcastRunnable(processID, currentConfig, new Message(processID, 0, currentInstance));
                        	br.start();
                        	currentInstance.launchedElection = true;
                    	}
                	}
            	}
            	catch (SocketTimeoutException s) {
            		if (leaderProcessID[0] != processID && !currentInstance.launchedElection && !currentInstance.respondedToElection){
            			Random ran = new Random();
                    	BroadcastRunnable br = new BroadcastRunnable(processID, currentConfig, new Message(processID, 0, currentInstance));
                		br.start();
                		currentInstance.launchedElection = true;
            		}
            		continue;
        		}
        		catch (Exception e){
            		e.printStackTrace(System.out);
            		continue;
        		}
            }
        }
        catch (Exception e){
            e.printStackTrace(System.out);
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

    public void setLeader(){
        leaderProcessID[0] = processID;
    }

    public void process(String command, int value){
        if (command.equals("show")) System.out.println(totalTicket);
        else if (command.equals("buy")){
            if (value > totalTicket){
                System.out.println("Do not have enough tickets");
            }
            else{
                currentInstance.logID += 1;
                if (processID == leaderProcessID[0]){
                    Instance newInstance = new Instance(currentInstance);
                    Config newConfig = new Config(currentConfig);
                    instanceLog.put(newInstance.logID, newInstance);
                    configLog.put(newInstance.logID, newConfig);
                    BroadcastRunnable br = new BroadcastRunnable(processID, currentConfig, new Message(processID, 5, newInstance, totalTicket - value));
                    br.start();
                }
                else{
                	String leaderHost = currentConfig.hostMap.get(leaderProcessID[0]);
                    int leaderPort = currentConfig.portMap.get(leaderProcessID[0]);
                    Instance newInstance = new Instance(currentInstance);
                    Config newConfig = new Config(currentConfig);
                    instanceLog.put(newInstance.logID, newInstance);
                    configLog.put(newInstance.logID, newConfig);
                    SendRunnable r = new SendRunnable(processID, leaderHost, leaderPort, new Message(processID, 10, newInstance, totalTicket - value));
                    r.start();
                }
            }
        }
        else if (command.equals("leader")){
        	System.out.println(leaderProcessID[0] == processID);
        }
    }
}