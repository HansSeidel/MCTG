import bif3.swe.if20b211.api.Message;
import bif3.swe.if20b211.api.Messages;
import bif3.swe.if20b211.colores.ConsoleColors;
import bif3.swe.if20b211.http.Format;
import bif3.swe.if20b211.http.Json_form;
import bif3.swe.if20b211.mctg.ClientConsoleHandler;
import com.fasterxml.jackson.databind.JsonNode;
import org.json.JSONException;

import java.io.*;
import java.net.Socket;

public class MainClient {
    public static void main(String[] args) {
        System.out.println("start client");
        final String HOST = "localhost";
        final int PORT = 8000;
        try{
            Socket s = new Socket(HOST,PORT);
            ClientConsoleHandler cch = new ClientConsoleHandler();
            cch.welcomeMessage();
            while (true){
                Format request;
                if(!cch.isUserLoggedIn()){
                    System.out.println("Not logged in");
                    cch.logInOrRegister();
                    request = new Format(cch.getMethod(),HOST,cch.getRequestPath(), cch.getBody(), cch.getMimeType(),cch.getArgs());
                    request.buildFormat();
                    write(request.BARE_STRING,s);
                    cch.handleResponse(new Format(read(s)));
                }else{
                    System.out.println("Logged in");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("close client");
    }

    private static String read(Socket s) throws IOException {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            return null;
        }
        String res = "";
        String next = "";
        boolean body = true;
        BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
        if(!reader.ready()){
            return null;
        }
        next = reader.readLine();
        //First loop is reading until first blank line occurs
        if(next == null) return null;
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

    private static void write(String response, Socket s) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
        writer.write(response);
        writer.newLine();
        writer.flush();
    }
}
