import java.io.*;
import java.net.*;
import java.util.*;

public class Server {

    public static void main(String[] args) {
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
        int[] money = new int[]{1000};
        ServerRunnable sr = new ServerRunnable(processID, port, money);
        sr.start();
        RandomRunnable rr = new RandomRunnable(processID, money);
        rr.start();
        try{
            while (true){
                Scanner scanner = new Scanner(System.in);
                String command = scanner.nextLine();
                if (command.equals("s")){
                    sr.snapshot();
                }
            }
        }
        catch (Exception e){
            e.printStackTrace(System.out);
        }
    }
}