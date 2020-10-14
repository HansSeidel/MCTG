import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class MainClient {
    public static void main(String[] args) {
        System.out.println("start client");

        try (Socket socket = new Socket("localhost", 8000);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {

            System.out.println("cli: " + reader.readLine());
            System.out.println("cli: " + reader.readLine());
            String input = null;
            System.out.print("cli: ");
            while (!"quit".equals(input = consoleReader.readLine())) {
                writer.write(input);
                writer.newLine();
                writer.flush();
                System.out.print("cli: ");
            }
            writer.write("quit");
            writer.newLine();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("close client");

    }
}
