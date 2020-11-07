

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
        if(request.getStatus() != 200)
        switch (request.http_method){
            case GET: return GET(request.request_path, request.args);
            case PUT:
            case POST: return PUT_POST(request.request_path,request.body.toString(),request.args);
            case DELETE: return DELETE(request.request_path,request.args);
        }
        return null;
    }

    private String DELETE(String path, HashMap<String, String> args) {
        String head;
        if(status != 200) return request.getErrorMessage();
        //you only may post to everything underlying of messages/
        //TODO handle following posts:
        //messages/3
        //messages/3.json
        if(!path.startsWith("\\messages")){
            setStatus(403,"Forbidden - You may delete inside /messages/. Everything else is restricted, unless you have administrator rights");
            return this.errorMessage;
        }else{
            File file = new File(System.getProperty("user.dir") + path);
            if(file.isDirectory()){
                setStatus(403,"Forbidden - You may delete inside /messages/. You are not allowed to delete all messages, unless you have administrator rights");
                return this.errorMessage;
            }else{
                //Check for extension and add if not existing
                if(!file.getName().endsWith(".json")){
                    file = new File(String.format("%s%s.%s",System.getProperty("user.dir"), path,"json"));
                }
                try {
                    if(!file.delete()) throw new IOException();
                    setStatus(200,"OK - Message deleted");
                    return this.errorMessage;
                }catch (SecurityException e){
                    setStatus(500, String.format("Internal Server Error - Please contact the administrator: %s", e.toString()));
                }catch (IOException e){
                    setStatus(404,String.format("File not Found - %s",e.toString()));
                }
            }
            return this.errorMessage;
        }
    }

    private String PUT_POST(String path, String body, HashMap<String, String> args) {
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
                    if (request.http_method.equals(HTTPFormat.Http_Method.PUT)){
                        setStatus(405,"Method Not Allowed - Trying to PUT into a directory instead of a specific message. Try POST instead");
                        lock.unlock();
                        return this.errorMessage;
                    }
                    //Getting highest number and adding the message to the next number
                    for(File f : Objects.requireNonNull(file.listFiles())){
                        System.out.println("Name of the currecnt file: " + f.getName());
                        System.out.println("charAt'.' of the currecnt filename: " + f.getName().indexOf('.'));
                         int tmpMN = Integer.parseInt(f.getName().substring(0,f.getName().indexOf('.')));
                         messageNumber = tmpMN >= messageNumber? tmpMN+1:messageNumber;
                    }
                }else {
                    getContentOf(path,args);
                    if(status != 404 && request.http_method.equals(HTTPFormat.Http_Method.PUT)){
                        System.out.println("Inside restricted area");
                        setStatus(405,"Method Not Allowed - Trying to overwrite existing message. Use PATCH/PUT instead.");
                        lock.unlock();
                        return this.errorMessage;
                    }
                    //Checking if the post was send with file extension
                    //TODO make short
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
                lock.unlock();
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
        //TODO: REWRITE THESE METHOD. The following line is everything else then well coded
        boolean isIndex = path.equals("\\index.html");
        try{
            File f_d = new File((System.getProperty("user.dir") + path));
            resp = isIndex? "":"{";
            if(f_d.isDirectory()){
                System.out.println("Right bevore Loop");
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
                System.out.println("Right after Loop");
            }else{
                //Now only writing for JSON response and index.html
                if(!f_d.getName().substring(f_d.getName().lastIndexOf('.')+1).equals("json") && !isIndex)
                    f_d = new File(String.format("%s.%s",f_d.getPath(),"json"));
                resp += isIndex? "":String.format("\n\"%s\": \n\t",f_d.getName().substring(0,f_d.getName().indexOf('.')));
                Scanner myReader = new Scanner(f_d);
                while(myReader.hasNext())
                    resp += myReader.nextLine() + (myReader.hasNext()? "\n\t":"\n");
            }
            resp += isIndex?"":"}\n";
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
