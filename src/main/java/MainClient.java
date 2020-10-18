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
             SimpleBufferedWriter writer = new SimpleBufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {

            System.out.println("cli: " + reader.readLine());
            System.out.println("cli: " + reader.readLine());
            String input = null;
            System.out.print("cli: ");
            while (!"quit".equals(input = consoleReader.readLine())) {
                writer.write(input);
                System.out.print("cli: ");
            }
            writer.write(false, input);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("close client");

    }
}
