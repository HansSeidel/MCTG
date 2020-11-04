import bif3.swe.if20b211.api.SimpleBufferedWriter;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

//TODO REWRITE SO JSON IS UNDERSTABLE (For Server and for client (also for google Client)
//TODO Fix Bug, that after a specific period of time an Exception occure (Try with Connection-Header: Close)
//TODO WRITER POST AND UPDATE AND ETC.
//TODO WRITE TOO MANY REQUEST Handling <optional>
//TODO Implement TOKEN and LDAP connection <optional>

public class MainServer implements Runnable {

    private static ServerSocket _listener = null;

    /*
     * TODO Handle Request so structure.json is the response (If the request was propper formatted)
     * Pseudocode to aim for:
     *
     */

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
                //HandleRequest is called after a message is recieved by the listener.
                //Inside HandleRequest the format is proofed.
                HandleRequest request = new HandleRequest(s);
                //With correctFormat you'll find out if the request was correct.
                if(request.correctFormat()) {

                    //SendResponse prepares a response object (Maybe MyHTTPHandler)
                    SendResponse response = new SendResponse(s);
                    //request.fullFill() does the server-specific actions (Maybe by another Class).
                    //Afterwards it returns an HTTP Response in format (String/JSON). With Status-Code;
                    response.message = request.fullFill();
                    System.out.println("Response message: " + response.message);
                    //response.send() sends the message;
                    response.send();
                }

                //SimpleBufferedWriter writer = new SimpleBufferedWriter(new OutputStreamWriter(s.getOutputStream()));

                //System.out.println("srv: sending welcome message");
                //writer.write("Welcome to myserver!","Please enter your commands...");

                //BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
                //String message;
                //do {
                //    message = reader.readLine();
                //    System.out.println("srv: received: " + message);
                //} while (!"quit".equals(message));
            }
        } catch (IOException e) {
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
