import java.io.*;
import java.net.*;
import java.util.*;

public class Server {

    public static void main(String[] args) throws Exception {
        if (args.length < 1){
            System.out.println("Please provide processID");
            return;
        }
        HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
        map.put(1, 5101);
        map.put(2, 5102);
        map.put(3, 5103);

        int processID = Integer.valueOf(args[0]);
        int port = map.get(processID);
        ServerRunnable sr = new ServerRunnable(processID, port);
        sr.start();

        while (true){
            Scanner scanner = new Scanner(System.in);
            String command = scanner.nextLine();

            sr.send(0, command);
        }
    }
}