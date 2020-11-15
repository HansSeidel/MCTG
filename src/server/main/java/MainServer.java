import bif3.swe.if20b211.Json_form;
import bif3.swe.if20b211.api.Message;
import bif3.swe.if20b211.api.Messages;
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
/*
        try {
            JsonNode node = Json_form.parse(new File(System.getProperty("user.dir") + "/messages/testMessage.json"));
            Message messages = Json_form.fromJson(node,Message.class);

            System.out.println("message id: " + messages.getId());
            System.out.println("sender id: " + messages.getSender());
            System.out.println("message id: " + messages.getMessage());

            messages.setSender("Hans");
            System.out.println("Processing writing: " + Json_form.write(System.getProperty("user.dir") + "/messages/testMessage.json",Json_form.toJson(messages)));

        } catch (IOException e) {
            e.printStackTrace();
        }
        */
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
                //Was soll nun alles getestet werden:
                String client_string = getClientString(s);
                if(!(client_string == null)){
                    Format request = new Format(client_string);
                    System.out.println("Format.toString() - " + request.toString());

                    Format.Body body = request.getBody();
                    try{
                        Message m = body.toObjectExpectingJson(Message.class);
                        System.out.println("Parsing is done. Got following: Message id: " + m.getId() + " Sender: " + m.getSender() + " Message: " + m.getMessage());
                    }catch (NullPointerException e){
                        System.out.println("Body is null");
                    }
                    String response = fullfill(request);
                    System.out.println("response is: ----------- " + response);
                    write(response,s);
                }

                //client_http_format.debug(); --> !!!Crashes the programm (on purpose)

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

    private static void write(String response, Socket s) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
        writer.write(response);
        writer.flush();
    }

    private static String fullfill(Format request) throws IOException {

        switch (request.getMethod()){
            case GET: return HandleRequest.GET(request).BARE_STRING;
            case POST:
                break;
            case PATCH:
                break;
            case PUT:
                break;
            case DELETE:
                break;
            case HEAD:
                break;
            case CONNECT:
                break;
            case OPTION:
                break;
            case TRACE:
                break;
        }
        System.out.println("No httpmethod");
        return null;
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
        if(next == null) return null;
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
