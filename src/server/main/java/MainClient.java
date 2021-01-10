import bif3.swe.if20b211.http.Format;
import bif3.swe.if20b211.mctg.ClientConsoleHandler;

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
            Format request = null;
            while (true){
                if(!cch.isUserLoggedIn()){
                    System.out.println("Not logged in");
                    cch.logInOrRegister();
                    request = new Format(cch.getMethod(),HOST,cch.getRequestPath(), cch.getBody(), cch.getMimeType());
                }else{
                    boolean skip = false;
                    System.out.println("Logged in");
                    String userCommand = cch.requestInput();
                    if(userCommand.startsWith("buy")){
                        cch.acquirePackage(userCommand);
                    }else if(userCommand.startsWith("deck")){
                        cch.manageDeck(userCommand);
                    }else if(userCommand.equals("battle")){
                        cch.startBattel();
                    }else if(userCommand.equals("quit")){
                        break;
                    }else {
                        cch.wrongInput("Unknown command");
                        skip = true;
                    }
                    if(!skip || !cch.sendAbel()){
                        request = new Format(cch.getMethod(),HOST,cch.getRequestPath(), cch.getBody(), cch.getMimeType());
                        if(cch.getArgs() != null)
                            for (String arg:cch.getArgs())
                                request.addArgument(arg.split("=")[0],arg.split("=")[1]);
                        request.addHeader("token", cch.getToken());
                    }
                }
                request.buildFormat();
                write(request.BARE_STRING,s);
                cch.handleResponse(new Format(read(s)));
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
