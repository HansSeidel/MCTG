import bif3.swe.if20b211.api.SimpleBufferedWriter;
import bif3.swe.if20b211.http.Format;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

//TODO REWRITE SO JSON IS UNDERSTABLE (For Server and for client (also for google Client)
//TODO Fix Bug, that after a specific period of time an Exception occures (Try with Connection-Header: Close)
//TODO WRITE POST AND UPDATE AND ETC.
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
                String client_string = getClientString(s);
                Format client_http_format = new Format(client_string);
                System.out.println("Headers: " + client_http_format.getHeaders());
                System.out.println("Connection type: " + client_http_format.getHeaderValueByName("Connection"));
                //System.out.println("client_http_format to string: " +client_http_format.toString());
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

    /**
     * This method takes care of the communication. It extracts all the incoming data and returns it as a trimmed String.
     * @param s - Socket
     * @return
     * @throws IOException
     */
    private static String getClientString(Socket s) throws IOException {
        String res = "";
        String next = "";
        boolean body = true;
        BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
        next = reader.readLine();
        //First loop is reading until first blank line occurs
        while (!next.isEmpty()){
            res += next+"\n";
            next = reader.readLine();
        }
        //Ready is waiting a short period of time and checking if there is some readable text behind the blank line.
        res += "\n";
        if(!reader.ready()){
            body = false;
        }
        //Reading out the rest of the incoming message
        if(body){
            while((next = reader.readLine()) != null){
                res += next + "\n";
                if(!reader.ready()){
                    break;
                }
            }
        }
        //System.out.println("Res: " + res.trim());
        return res.trim();
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
