import java.io.Serializable;

public class Message implements Serializable {
    int processID;
    int messageType;
    int logID;
    Config config;
    Instance instance;
    int value;

    Message(int processID, int messageType, Instance instance, int value){
        this.processID = processID;
        this.messageType = messageType;
        this.instance = instance;
        this.logID = instance.logID;
        this.value = value;
    }

    Message(int processID, int messageType, Instance instance){
        this.processID = processID;
        this.messageType = messageType;
        this.instance = instance;
        this.logID = instance.logID;
    }

    Message(int processID, int messageType, int logID, Config config){
        this.processID = processID;
        this.messageType = messageType;
        this.logID = logID;
        this.config = config;
    }

    Message(int processID, int messageType, Instance instance, Config config, int value){
        this.processID = processID;
        this.messageType = messageType;
        this.instance = instance;
        this.config = config;
        this.value = value;
    }
}