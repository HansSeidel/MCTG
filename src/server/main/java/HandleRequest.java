import java.io.*;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.Scanner;

public class HandleRequest {
    private HTTPFormat request;
    private int status = 200;
    private String errorMessage = "No error or unknown error";

    public HandleRequest(Socket s) throws IOException {
        this.request = new HTTPFormat(s);
        evaluateStatus();
    }

    private void evaluateStatus() {
        //TODO Evalute if HTTPRequest is in proper format.
        if(request.http_method == null) setStatus(400,String.format("Bad Request: Unkwon http_method: %s", request.http_version));
        if(request.request_path == null || request.request_path.isEmpty()) setStatus(404,String.format("Not Found: Path not found: %s", request.http_version));
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
                result = GET(request.request_path, request.args);
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
         localhost:8080/messages -> leads to all messages
         localhost:8080/messages/ -> leads to all messages
         ...
         */
        String resp = "";
        try{
            File f_d = new File((System.getProperty("user.dir") + path));//TODO Define File or directory
            if(f_d.isDirectory()){
                System.out.println("Right bevore Loop");
                resp = "{";
                for (File f: Objects.requireNonNull(f_d.listFiles())){
                    if(!f.isDirectory()){
                        Scanner newScanner;
                        //Checking if it is a json file (path/filename.json)
                        if (f.getPath().lastIndexOf(".") > 0 && f.getPath().substring(f.getPath().lastIndexOf(".")+1).equals("json")){
                            System.out.println("inside last conditional check");
                            newScanner = new Scanner(f);
                            resp += (resp.charAt(resp.length()-1)) == '}'? ",":"\n";
                            resp += String.format("\"%s\": ",f.getName().substring(0,f.getName().indexOf('.')));
                            while (newScanner.hasNext()){
                                //adding fileName as parameter
                                resp += newScanner.nextLine() + (newScanner.hasNext()? "\n\t":"\n");
                            }
                            /*
                                Example process:
                                    {
                                        1:{
                                        ...
                                        },
                                        2: {
                                        ...
                                        }
                                     }
                             */
                        }
                    }
                }
                resp += "}\n";
                System.out.println("Right after Loop");
            }else{
                //Now only writing for JSON response
                if(!f_d.getName().substring(f_d.getName().lastIndexOf('.')+1).equals("json"))
                    f_d = new File(String.format("%s.%s",f_d.getPath(),"json"));
                Scanner myReader = new Scanner(f_d);
                while(myReader.hasNext())
                    resp += myReader.nextLine() + "\n";
            }
        }catch (FileNotFoundException e){
            setStatus(404, e.toString());
            return String.format("{errorMessage:{%s}}",this.errorMessage);
        }catch (NullPointerException e){
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
