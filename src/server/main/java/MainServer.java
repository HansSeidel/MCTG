import bif3.swe.if20b211.api.HandleRequest;
import bif3.swe.if20b211.api.Message;
import bif3.swe.if20b211.colores.ConsoleColors;
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
            System.out.println("Connection established");
            while (true) {
                String client_string = getClientString(s);
                if(!(client_string == null)){
                    //Bring request into workable format:
                    System.out.println(ConsoleColors.GREEN+"Formatting request..."+ConsoleColors.RESET);
                    Format request = new Format(client_string);
                    Format.Body body = request.getBody();
                    try{
                        Message m = body.toObjectExpectingJson(Message.class);
                    }catch (NullPointerException e){
                        System.out.println(ConsoleColors.RED_BRIGHT + "Body is null or not in correct Format" + ConsoleColors.RESET);
                    }

                    System.out.println(String.format("%sRetrieved a request: %s\nPath to fullfill the action is: %s\n%sExecuting...",ConsoleColors.GREEN_BRIGHT,request.getMethod(),request.getPath(),ConsoleColors.GREEN) + ConsoleColors.RESET);

                    //fullfill the request, build a new Format and return the new Format.
                    Format response = fullfill(request);
                    assert response != null;
                    System.out.println("Processed response with status: " +
                            (response.getStatus() >= 200 && response.getStatus() < 300?ConsoleColors.GREEN_BOLD+response.getStatus():ConsoleColors.RED_BOLD+response.getStatus()));
                    System.out.println(ConsoleColors.GREEN+"Sending response..." + ConsoleColors.RESET);
                    write(response.BARE_STRING,s);
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

    private static Format fullfill(Format request) throws IOException {

        switch (request.getMethod()){
            case GET: return HandleRequest.GET(request);
            case POST: return HandleRequest.POST(request);
            case PATCH: return HandleRequest.PATCH(request);
            case PUT: return HandleRequest.PUT(request);
            case DELETE: return HandleRequest.DELETE(request);
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
