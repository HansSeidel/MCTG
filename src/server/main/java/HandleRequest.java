import java.io.*;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;

public class HandleRequest {
    private HTTPRequest request;
    private int status = 200;
    private String errorMessage = "Unkown Error";

    public HandleRequest(Socket s) throws IOException {
        this.request = new HTTPRequest(s);
        evaluateStatus();
    }

    private void evaluateStatus() {
        //TODO Evalute if HTTPRequest is in proper format.
        if(request.http_method == null) setStatus(400,String.format("Bad Request: Unkwon http_method: %s", request.http_version));
        if(request.path == null || request.path.isEmpty()) setStatus(404,String.format("Not Found: Path not found: %s", request.http_version));
        if(request.http_version.charAt(6) != '1') setStatus(301,"Moved Permanently: This api only responding to HTTP/1.x");
    }

    public String fullFill(){
        String result = null;
        switch (request.http_method){
            case GET:
                result = GET(request.path, request.args);
        }
        return result;
    }

    private String GET(String path, HashMap<String,String> args) {
        if(status != 200) return errorMessage;
        //TODO implenet args as well
        //May change status
        String body = getContentOf(path, args);
        //May change status
        String head = buildHead();

        if(status != 200) return errorMessage;
        return head+body;
    }

    private String buildHead() {
        String response = String.format("%s %s %s\n",request.http_version,status,status < 300 && status >= 200? "OK":"ERR");
        String head = String.format("Date: %s\nServer: localhost\n", new Date().toString());
        return response+head+"\n";
    }

    private String getContentOf(String path, HashMap<String, String> args) {
        /*
        TODO Handle following paths:
         localhost:8080/api -> leads to structure
         localhost:8080/api/ -> leads to structure
         localhost:8080/api/structure -> leads to structure
         localhost:8080/api/structure.json -> leads to structure
         localhost:8080/api/messages -> leads to all messages
         localhost:8080/api/messages/ -> leads to all messages
         ...
         */
        String resp = "";
        try{
            File f = new File(path);//Define File or directory
            Scanner myReader = new Scanner(f);
            while(myReader.hasNext())
                resp = myReader.nextLine();
        }catch (FileNotFoundException e){
            setStatus(404, e.toString());
        }
        return resp;
    }

    private void setStatus(int i, String e) {
        this.status = i;
        this.errorMessage = e.toString();
    }

    public boolean correctFormat() {
        return status >= 0 && status < 300? true:false;
    }
}
