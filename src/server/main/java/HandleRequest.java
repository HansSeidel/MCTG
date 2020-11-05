import java.io.*;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;

public class HandleRequest {
    private HTTPRequest request;
    private int status = 200;
    private String errorMessage = "No error or unknown error";

    public HandleRequest(Socket s) throws IOException {
        this.request = new HTTPRequest(s);
        evaluateStatus();
    }

    private void evaluateStatus() {
        //TODO Evalute if HTTPRequest is in proper format.
        if(request.http_method == null) setStatus(400,String.format("Bad Request: Unkwon http_method: %s", request.http_version));
        if(request.path == null || request.path.isEmpty()) setStatus(404,String.format("Not Found: Path not found: %s", request.http_version));
        try {
            if (request.http_version.charAt(5) != '1')
                setStatus(301, "Moved Permanently: This api only responding to HTTP/1.x");
        }catch (IndexOutOfBoundsException e) {
            setStatus(400,"Bad Request: Unable to read http_method_version: This api only responding to HTTP/1.x");
        }
    }

    public String fullFill(){
        String result = null;
        String mime_type = getParameterIfExists(request.args,"Content-Type");
        switch (request.http_method){
            case GET:
                result = GET(request.path, request.args);
        }
        return result;
    }

    private String getParameterIfExists(HashMap<String, String> args, String s) {
        return null;
    }

    private String GET(String path, HashMap<String,String> args) {
        if(status != 200) return errorMessage;
        //TODO implenet args as well
        //May change status
        String body = getContentOf(path, args);
        //May change status
        String head = buildHead(body.length());

        return head+body + "\\u001a";
    }

    private String buildHead(int length) {
        String response = String.format("%s %s %s\n",request.http_version,status,status < 300 && status >= 200? "OK":"ERR");
        String head = String.format("Date: %s\n" +
                "Server: localhost\n" +
                "Content-Length: %d\n", new Date().toString(),length);

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
            File f = new File((System.getProperty("user.dir") + path));//TODO Define File or directory
            Scanner myReader = new Scanner(f);
            while(myReader.hasNext())
                resp += myReader.nextLine() + "\n";
        }catch (FileNotFoundException e){
            setStatus(404, e.toString());
            return String.format("{errorMessage:{%s}}",this.errorMessage);
        }
        return resp;
    }

    private void setStatus(int i, String e) {
        this.status = i;
        this.errorMessage = e.toString();
    }
    public String getStatusString(){
        return String.format("Status Code is: %d; Status Message is: %s",this.status, this.errorMessage);
    }
    public boolean correctFormat() {
        return status >= 200 && status < 300;
    }
}
