import java.util.*;
import java.io.Serializable;

public class Instance implements Serializable {
    int logID;
    int proposalID;
    int acceptedID;
    int acceptedValue;
    int processID;
    int candidateResponseReceived;
    int leaderResponseReceived;
    int proposalResponseReceived;
    boolean launchedElection;
    boolean valueBroadcasted;
    boolean respondedToElection;
    boolean elected;

    String host;
    int port;

    Instance(int processID, int logID, int proposalID, int acceptedID, int acceptedValue){
        this.processID = processID;
        this.logID = logID;
        this.proposalID = proposalID;
        this.acceptedID = acceptedID;
        this.acceptedValue = acceptedValue;
        candidateResponseReceived = 0;
        leaderResponseReceived = 0;
        proposalResponseReceived = 0;
        launchedElection = false;
        respondedToElection = false;
        elected = false;
        valueBroadcasted = false;
    }

    Instance(Instance instance){
        this.processID = instance.processID;
        this.logID = instance.logID;
        this.proposalID = instance.proposalID;
        this.acceptedID = instance.acceptedID;
        this.acceptedValue = instance.acceptedValue;
        candidateResponseReceived = instance.candidateResponseReceived;
        leaderResponseReceived = instance.leaderResponseReceived;
        proposalResponseReceived = instance.proposalResponseReceived;
        launchedElection = instance.launchedElection;
        respondedToElection = instance.respondedToElection;
        elected = instance.elected;
        this.host = instance.host;
        this.port = instance.port;
        valueBroadcasted = instance.valueBroadcasted;
    }

    public void setAddress(int processID, String host, int port){
        this.processID = processID;
        this.host = host;
        this.port = port;
    }
}