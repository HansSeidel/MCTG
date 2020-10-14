import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class MainServer implements Runnable {

    private static ServerSocket _listener = null;

    public static void main(String[] args) {
        System.out.println("start server");

        try {
            _listener = new ServerSocket(8000, 5);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        Runtime.getRuntime().addShutdownHook(new Thread(new MainServer()));

        try {
            while (true) {
                Socket s = _listener.accept();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
                System.out.println("srv: sending welcome message");
                writer.write("Welcome to myserver!");
                writer.newLine();
                writer.write("Please enter your commands...");
                writer.newLine();
                writer.flush();

                BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
                String message;
                do {
                    message = reader.readLine();
                    System.out.println("srv: received: " + message);
                } while (!"quit".equals(message));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            _listener.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        _listener = null;
        System.out.println("close server");
    }
}
