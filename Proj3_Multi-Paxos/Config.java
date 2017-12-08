import java.util.*;
import java.io.Serializable;

public class Config implements Serializable {
    HashMap<Integer, String> hostMap;
    HashMap<Integer, Integer> portMap;
    int numModification;
    int majority;
    int leaderProcessID;

    Config(HashMap<Integer, String> hostMap, HashMap<Integer, Integer> portMap, int leaderProcessID){
        this.portMap = portMap;
        this.hostMap = hostMap;
        this.leaderProcessID = leaderProcessID;
        numModification = 0;
        majority = portMap.size() / 2;
    }

    Config(Config c){
        this.hostMap = c.hostMap;
        this.portMap = c.portMap;
        this.leaderProcessID = c.leaderProcessID;
        this.numModification = c.numModification;
        this.majority = c.majority;
    }

    public void addServer(int processID, String host, int port){
        hostMap.put(processID, host);
        portMap.put(processID, port);
        numModification += 1;
    }

    public void removeServer(int processID){
        hostMap.remove(processID);
        portMap.remove(processID);
        numModification += 1;
    }
}