

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class HandleRequest {
    private HTTPFormat request;
    private int status = 200;
    private String errorMessage = "No error or unknown error";
    private Lock lock = new ReentrantLock();

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
        String mime_type = getParameterIfExists(request.args,"Content-Type");
        switch (request.http_method){
            case GET: return GET(request.request_path, request.args);
            case PUT: return PUT(request.request_path,request.body.toString(),request.args);
        }
        return null;
    }

    private String PUT(String path, String body, HashMap<String, String> args) {
        String head;
        System.out.println("Body inside the PUT clause: " + body);
        if(status != 200) return request.getErrorMessage();
        //you only may post to everything underlying of messages/
        //TODO handle following posts:
            //messages
            //messages/
            //messages/3
            //messages/3.json
        if(!path.startsWith("\\messages")){
            setStatus(403,"Forbidden - You may post to /messages. Everything else is restricted, unless you have administrator rights");
            return this.errorMessage;
        }else{
            lock.lock();
            try {
                File file = new File(System.getProperty("user.dir") + path);
                int messageNumber = 1;
                if(file.isDirectory()){
                    //Getting highest number and adding the message to the next number
                    for(File f : Objects.requireNonNull(file.listFiles())){
                        System.out.println("Name of the currecnt file: " + f.getName());
                        System.out.println("charAt'.' of the currecnt filename: " + f.getName().indexOf('.'));
                         int tmpMN = Integer.parseInt(f.getName().substring(0,f.getName().indexOf('.')));
                         messageNumber = tmpMN >= messageNumber? tmpMN+1:messageNumber;
                    }
                }else {
                    getContentOf(path,args);
                    if(status != 404){
                        System.out.println("Inside restricted area");
                        setStatus(405,"Method Not Allowed - Trying to overwrite existing message. Use PATCH instead.");
                        return this.errorMessage;
                    }
                    //Checking if the post was send with file extension
                    //TODO make short
                    System.out.println("Path bevore checking File Extension: " + path);
                    System.out.println("Condition to be checked with -1: " + (path.substring(path.lastIndexOf('\\')+1)).indexOf('.'));
                    if((path.substring(path.lastIndexOf('\\')+1)).indexOf('.') == -1){
                        //Without file extension
                        System.out.println("String that should be parsed into int: " + path.substring(path.lastIndexOf('\\')+1));
                        messageNumber = Integer.parseInt(path.substring(path.lastIndexOf('\\')+1));
                    }else{
                        //With file extension.
                        messageNumber = Integer.parseInt(path.substring(path.lastIndexOf('\\')+1,path.lastIndexOf('.')));
                    }
                }
                FileWriter writer = new FileWriter(String.format("%s\\messages\\%d.json",System.getProperty("user.dir"),messageNumber));
                for (String line : body.split("\\n")) {
                    writer.write(line + "\n");
                }
                writer.close();
                body = getContentOf(String.format("\\messages\\%d.json",messageNumber),args);
                setStatus(201, "Created - Successfully created message with the id: " + messageNumber);
            } catch (IOException e){
                setStatus(404, e.toString());
                return String.format("{errorMessage:{%s}}",this.errorMessage);
            } finally {
                lock.unlock();
            }
            head = buildHead(body.length());
            return head+body + "\\u001a";
        }
    }

    private String getParameterIfExists(HashMap<String, String> args, String s) {
        return null;
    }

    private String GET(String path, HashMap<String,String> args) {
        if(status != 200) return request.getErrorMessage();
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
        String resp = "";
        try{
            File f_d = new File((System.getProperty("user.dir") + path));
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
                //Now only writing for JSON response and index.html

                if(!f_d.getName().substring(f_d.getName().lastIndexOf('.')+1).equals("json") && !path.equals("\\index.html"))
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
