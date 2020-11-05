import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashMap;


/**
 * HTTPFormat reads an incoming HTTP Request or Response and splits the information into several parts:
 * <p>
 * <ul>
 *     <li>First part is the Headline of the HEAD - head_first_Line[] - containing either</li>
 *     <ul>
 *        <li>Request: [0] - http_method, [1] - requestDomain(requestedPath)(additionalArguments), [2] - http_version</li>
 *        <li>Response: [0] - http_verison, [1] statusCode, [2] - String("OK"||"ERR");</li>
 *     </ul>
 *     <li>The Http_Format_Type - Either Enum Http_Format_Type.REQUEST or Enum Http_Format_Type.RESPONSE</li>
 *     <li>The http_version - String version (ex. HTTP/1.1)</li>
 *     <li>The request_method - Either one of Enum Http_Method enums or null;</li>
 *     <li>private variable headers, and public Object Body.</li>
 * </ul>
 * <p>
 * HTTPFormat provides a bunch of Functions:
 *      {@link #readHeaders() readHeaders} method,
 */
public class HTTPRequest {
    private BufferedReader reader;
    private HashMap<String, String> headers;
    private String[] body; //JSON
    public String[] head_first_line;
    public String request_path;
    public Http_Format_Type http_format_type;
    public String http_version;
    public Http_Method http_method;
    public String path;
    public HashMap<String,String> args;


    public HTTPRequest(Socket s) throws IOException {
        this.reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
        this.head_first_line = readFirstLine();
        if(head_first_line != null) {
            this.http_format_type = readFormat();
            if(http_format_type == Http_Format_Type.RESPONSE){
                //FOR INCOMING RESPONSES
                this.http_version = head_first_line[0];
                this.http_method = null;
                this.path = null;
                this.args = null;
            }else{
                //FOR INCOMING REQUESTS
                this.http_version = head_first_line[2];
                this.http_method = getHttpMethod(head_first_line[0]);
                if(setPath(head_first_line[1]))
                    setArgs(head_first_line[1]);
            }

        }
        this.headers = readHeaders(); //TODO Rewrite to Read Header and Body
        System.out.println("HTTPRequest is brought into format.");
    }

    private boolean setPath(String s) {
        System.out.println("Inside setPathAndArgs() - Setting path and args...");
        if(s.indexOf('/') != -1) s = s.replace("/","\\");
        if(s.indexOf('?') == -1){
            System.out.println("No arguments detected");
            this.path = s;
            return false;
        }
        this.path = s.substring(0,s.indexOf('?'));
        return true;
    }
    private void setArgs(String s) {
        if(!s.contains("&")) {
            String param = s.substring(s.indexOf('?'));
            this.args.put(param.substring(0,param.indexOf("=")),param.substring(param.indexOf("=")));
        }else{
            String params[] = s.substring(s.indexOf('?')).split("&");
            for(String param : params)
                this.args.put(param.substring(0,param.indexOf("=")),param.substring(param.indexOf("=")));
        }
    }

    private Http_Format_Type readFormat() {
        for(Http_Method m: Http_Method.values())
            if(head_first_line[0].equals(m.toString()))
                return Http_Format_Type.REQUEST;
        return Http_Format_Type.RESPONSE;
    }

    /**
     * If http-Request was correct, returns in String Array format: [0]- Method, [1]- path?parmas, [2]- httpVersion
     * @return String
     * @throws IOException
     */
    private String[] readFirstLine() throws IOException {
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

    private Http_Method getHttpMethod(String s) {
        System.out.println("Inside setHttpMethod() - Setting Request Method...");
        switch (s){
            case "GET":
                return Http_Method.GET;
            case "POST":
                return Http_Method.POST;
            case "PATCH":
                return Http_Method.PATCH;
            case "PUT":
                return Http_Method.PUT;
            case "UPDATE":
                return Http_Method.UPDATE;
            case "DELETE":
                return Http_Method.DELETE;
        }
        return null;
    }

    enum Http_Method {
        GET, POST, PATCH, PUT, UPDATE, DELETE
    }
    enum Http_Format_Type{
        REQUEST, RESPONSE
    }
}
