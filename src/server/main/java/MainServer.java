import bif3.swe.if20b211.api.HandleRequest;
import bif3.swe.if20b211.api.Message;
import bif3.swe.if20b211.http.Format;

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
            Socket s = _listener.accept();
            while (true) {
                String client_string = getClientString(s);
                if(!(client_string == null)){
                    Format request = new Format(client_string);

                    Format.Body body = request.getBody();
                    try{
                        Message m = body.toObjectExpectingJson(Message.class);
                        System.out.println("Parsing is done. Got following: Message id: " + m.getId() + " Sender: " + m.getSender() + " Message: " + m.getMessage());
                    }catch (NullPointerException e){
                        System.out.println("Body is null or not in Format Message");
                    }
                    String response = fullfill(request);
                    write(response,s);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void write(String response, Socket s) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
        writer.write(response);
        writer.newLine();
        writer.flush();
    }

    private static String fullfill(Format request) throws IOException {

        switch (request.getMethod()){
            case GET: return HandleRequest.GET(request).BARE_STRING;
            case POST: return HandleRequest.POST(request).BARE_STRING;
            case PATCH: return HandleRequest.PATCH(request).BARE_STRING;
            case PUT:
                break;
            case DELETE: return HandleRequest.DELETE(request).BARE_STRING;
            case HEAD:
                break;
            case CONNECT:
                break;
            case OPTION:
                break;
            case TRACE:
                break;
        }
        System.err.println("No httpmethod");
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
        while (!next.isEmpty()){
            res += next+"\n";
            if(!reader.ready()) break;
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
