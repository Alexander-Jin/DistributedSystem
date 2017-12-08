import java.io.*;
import java.net.*;

public class SetSimulation {
    public static void main(String[] args) throws IOException {
        File criticalSection = new File("criticalSection");
        if (!criticalSection.exists()) criticalSection.mkdir();

        PrintWriter writer = new PrintWriter("criticalSection/post.txt");
        writer.print("TESTCASE CONTENT");
        writer.close();

        writer = new PrintWriter("criticalSection/numOfLikes.txt");
        writer.print(0);
        writer.close();
    }
}