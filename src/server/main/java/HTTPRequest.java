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
        this.headers = readHeaders();
        setHttpMethod(request[0]);
        setPathAndArgs(request[1]);
        this.http_version = request[2];
    }

    private void setPathAndArgs(String request) {
        if(request == null)return;
        this.path = request.substring(0,request.indexOf('?'));

        String params[] = request.substring(request.indexOf('?')).split("&");
        for(String param : params)
            this.args.put(param.substring(0,param.indexOf("=")),param.substring(param.indexOf("=")));
    }

    /**
     * If http-Request was correct, returns in String Array format: [0]- Method, [1]- path?parmas, [2]- httpVersion
     * @return String
     * @throws IOException
     */
    private String[] readRequest() throws IOException {
        String req = reader.readLine();
        if(req == null) return null;
        String[] request = req.split(" ");

        return request;
    }

    private HashMap<String,String> readHeaders() throws IOException {
        HashMap<String,String> res = null;
        for(String head = reader.readLine(); head != null || !head.isEmpty() || head.equals("");head = reader.readLine())
            res.put(head.split(":")[0],head.split(":")[1]);
        return res;
    }

    private void setHttpMethod(String s) {
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

    enum Http_Method {
        GET, POST, PATCH, PUT, UPDATE, DELETE
    }
}
