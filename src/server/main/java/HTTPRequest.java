import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashMap;

public class HTTPRequest {
    private Socket clientSocket;
    private BufferedReader reader;
    private HashMap<String, String> headers;
    private String[] body; //JSON
    public String http_version;
    public Http_Method http_method;
    public String path;
    public HashMap<String,String> args;


    public HTTPRequest(Socket s) throws IOException {
        this.clientSocket = s;
        reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        String request[] = readRequest();
        this.headers = readHeaders(); //TODO Rewrite to Read Header and Body
        if(request == null){
            setHttpMethod(null);
            setPathAndArgs(null);
        }else{
            setHttpMethod(request[0]);
            setPathAndArgs(request[1]);
        }
        this.http_version = request == null? null:request[2];
        System.out.println("HTTPRequest is brought into format.");
    }

    /**
     * If http-Request was correct, returns in String Array format: [0]- Method, [1]- path?parmas, [2]- httpVersion
     * @return String
     * @throws IOException
     */
    private String[] readRequest() throws IOException {
        System.out.println("Inside readRequest() - Formatting request...");
        String req = reader.readLine();
        if(req == null) return null;
        String[] request = req.split(" ");
        return request;
    }

    private HashMap<String,String> readHeaders() throws IOException {
        System.out.println("Inside readHeaders() - Formatting Headers...");
        String head = reader.readLine();
        if(head == null)return null;
        HashMap<String,String> res = new HashMap<String, String>();
        while (!head.isEmpty()){
            res.put(head.substring(0,head.indexOf(":")),head.substring(head.indexOf(":")));
            head = reader.readLine();
        }
        return res;
    }

    private void setHttpMethod(String s) {
        System.out.println("Inside setHttpMethod() - Setting Request Method...");
        if(s == null) return;
        switch (s){
            case "GET":
                this.http_method = Http_Method.GET;
                break;
            case "POST":
                this.http_method = Http_Method.POST;
                break;
            case "PATCH":
                this.http_method = Http_Method.PATCH;
                break;
            case "PUT":
                this.http_method = Http_Method.PUT;
                break;
            case "UPDATE":
                this.http_method = Http_Method.UPDATE;
                break;
            case "DELETE":
                this.http_method = Http_Method.DELETE;
                break;
        }
    }

    private void setPathAndArgs(String request) {
        System.out.println("Inside setPathAndArgs() - Setting path and args...");
        if(request == null)return;
        if(request.indexOf('/') != -1) request = request.replace("/","\\");
        if(request.indexOf('?') == -1){
            System.out.println("No arguments detected");
            this.path = request;
            return;
        }
        this.path = request.substring(0,request.indexOf('?'));
        //Get the substring from the position of ? to the end and split it by &
        String params[] = request.substring(request.indexOf('?')).split("&");
        for(String param : params)
            this.args.put(param.substring(0,param.indexOf("=")),param.substring(param.indexOf("=")));
    }

    enum Http_Method {
        GET, POST, PATCH, PUT, UPDATE, DELETE
    }
}
