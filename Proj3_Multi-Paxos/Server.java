import java.io.*;
import java.net.*;
import java.util.*;

public class Server {

    public static void main(String[] args) {
        if (args.length < 2){
            System.out.println("Please provide processID, hostName, and port");
            return;
        }

        int processID = Integer.valueOf(args[0]);
        String hostName = args[1];
        int port = Integer.valueOf(args[2]);

        try{
            ServerRunnable sr = new ServerRunnable(processID, hostName, port);
            sr.start();
            while (true){
                Scanner scanner = new Scanner(System.in);
                String command = scanner.nextLine();

                if (command.length() < 4) System.out.println("Command not recognized.");
                else if (command.substring(0,3).equals("buy")){
                    int numOfTickets = Integer.valueOf(command.split("[ ]+")[1]);
                    command = "buy";
                    sr.process(command, numOfTickets);
                }
                else if (command.equals("show")){
                    sr.process("show", 0);
                }
                else if (command.equals("remove")){
                    sr.process("remove", 0);
                }
                else if (command.equals("leader")){
                    sr.process("leader", 0);
                }
                else System.out.println("Command not recognized.");
            }
        }
        catch (Exception e){
            e.printStackTrace(System.out);
        }
    }
}