import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


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
 *     <li>The request_path - the complete path the request was set too</li>
 *     <li>The arguments passed</li>
 *     <li>private variable headers, and public Object Body.</li>
 * </ul>
 * <p>
 * HTTPFormat provides a bunch of Functions:
 *      {@link #readHeaders() readHeaders} method,
 */
public class HTTPFormat {
    private BufferedReader reader;
    private HashMap<String, String> headers;
    public String[] head_first_line;
    public String request_path;
    public Http_Format_Type http_format_type;
    public String http_version;
    public Http_Method http_method;
    public HashMap<String,String> args;
    public Body body;
    private int status;
    private String errorMessage;

    public HTTPFormat(Socket s) throws IOException {
        this.reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
        this.head_first_line = readFirstLine();
        if(head_first_line != null) {
            this.http_format_type = readFormat();
            if(http_format_type == Http_Format_Type.RESPONSE){
                //FOR INCOMING RESPONSES
                if(!head_first_line[0].startsWith("HTTP/1")) setStatus(505, "HTTP Version Not Supported: This api only responding to HTTP/1.x");
                this.http_version = head_first_line[0];
                this.http_method = null;
                this.request_path = null;
                this.args = null;
            }else{
                //FOR INCOMING REQUESTS
                this.http_version = head_first_line[2];
                this.http_method = getHttpMethod(head_first_line[0]);
                if(setPath(head_first_line[1]))
                    setArgs(head_first_line[1]);
            }
            if((this.headers = readHeaders()) != null)
                this.body = readBody();
        }
        System.out.println("HTTPRequest is brought into format.");
    }

    /**
     * If http-Request was correct, returns in String Array format: [0]- Method, [1]- path?parmas, [2]- httpVersion
     * @return String
     * @throws IOException
     */
    private String[] readFirstLine() throws IOException {
        System.out.println("Inside readFirstLine() - Formatting request...");
        if(!reader.ready()){
            setStatus(400, "Bad Request - Header request seems to lead to endless read action.");
        }
        String req = reader.readLine();
        if(req == null) {
            setStatus(400, "Bad Request - No request found");
            return null;
        }
        String[] request = req.split(" ");
        if(request.length != 3){
            setStatus(400, "Bad Request - Header must contain 3 parts separated by a space. Encountered: " + request.length);
            return null;
        }
        return request;
    }

    private Http_Format_Type readFormat() {
        for(Http_Method m: Http_Method.values())
            if(head_first_line[0].equals(m.toString()))
                return Http_Format_Type.REQUEST;
        return Http_Format_Type.RESPONSE;
    }

    private Http_Method getHttpMethod(String s) {
        System.out.println("Inside setHttpMethod() - Setting Request Method...");
        switch (s){
            case "GET": return Http_Method.GET;
            case "POST": return Http_Method.POST;
            case "PATCH": return Http_Method.PATCH;
            case "PUT": return Http_Method.PUT;
            case "UPDATE": return Http_Method.UPDATE;
            case "DELETE": return Http_Method.DELETE;
        }
        setStatus(501, "Not Implemented - The Http method is not implemented.");
        return null;
    }

    private boolean setPath(String s) {
        System.out.println("Inside setPathAndArgs() - Setting path and args...");
        System.out.println("This is the income of the String s inside setPath: " + s);
        if((s.indexOf('/') == -1 && s.indexOf('\\') == -1)|| s.length() == 1){
            System.out.println("Inside if");
            this.request_path = "\\index.html";
            return s.indexOf('?') != -1;
        }
        if(s.indexOf('/') != -1) s = s.replace("/","\\");
        if(s.indexOf('?') == -1){
            System.out.println("No arguments detected");
            this.request_path = s;
            return false;
        }
        this.request_path = s.substring(0,s.indexOf('?'));
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

    private HashMap<String,String> readHeaders() throws IOException {
        System.out.println("Inside readHeaders() - Formatting Headers...");
        String head = reader.readLine();
        if(head == null || head.isEmpty()){
            setStatus(400, "Bad Request - No headers detected.");
            return null;
        }
        HashMap<String,String> res = new HashMap<String, String>();
        while (!head.isEmpty()){
            res.put(head.substring(0,head.indexOf(":")).trim(),head.substring(head.indexOf(":")+1).trim());
            head = reader.readLine();
        }
        return res;
    }

    private Body readBody() throws IOException {
        String t = "";
        String res = "";
        int read_chars = 0;
        int cl = 0;
        System.out.println("Inside readBody() - perfoming Body format...");
        if(!reader.ready()){
            System.out.println("NextLine is not readable");
            return null;
        }else{
            if(getHeaderValueByName("Content-Length").isEmpty() || getHeaderValueByName("Content-Length") == null){
                System.out.println("Problem with content length");
                setStatus(411, "Length Required: Please add Content-Length to the headers");
                return null;
            }
            try{
                System.out.println("Trying to parse contentlength into int");
                System.out.println("GetHeaderValueByName: " + getHeaderValueByName("Content-Length"));
                cl = Integer.parseInt(getHeaderValueByName("Content-Length"));
            }catch (NumberFormatException e){
                System.out.println("Problem with parsing process");
                setStatus(412, String.format("Precondition Failed: %s",e.toString()));
                return null;
            }
        }
        System.out.println("Infront of readLine");
        while((t = reader.readLine()) != null){
            res += t + "\n";
            read_chars += t.length()+1; //plus one because line break isn't count as length but is counted inside Content-Length
            System.out.println("String processing: " + t);
            System.out.println(read_chars +" - " + cl);
            if(read_chars >= cl) return new Body(res,getHeaderValueByName("Content-Type"));
        }
        return null;
    }

    public String getHeaderValueByName(String name){
        Iterator it = this.headers.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry pair = (Map.Entry)it.next();
            if(pair.getKey().toString().equals(name)) return pair.getValue().toString();
            it.remove(); // avoids a ConcurrentModificationException
        }
        return null;
    }

    public String getArgumentByName(String name) {
        Iterator it = this.args.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry pair = (Map.Entry)it.next();
            if(pair.getKey().toString().equals(name)) return pair.getValue().toString();
            it.remove(); // avoids a ConcurrentModificationException
        }
        return null;
    }

    public boolean isFormatCorrect(){return this.status < 300 && this.status >= 200;}
    public boolean hasBody(){return this.body != null;}
    public int getStatus(){return this.status;}
    public String getErrorMessage(){return this.errorMessage;}


    enum Http_Method {
        GET, POST, PATCH, PUT, UPDATE, DELETE
    }
    enum Http_Format_Type{
        REQUEST, RESPONSE
    }

    /**
     * Body is a nested class to format the Body in a specific format depending on the MIME-TYPE.
     * <p>
     * It provides multiple function which allow the User of this class to work with the body by expected content
     * instead of splitting it up all by hand.
     *
     */
    class Body{
        private final String mimeType;
        private final String body_string;

        /**
         *
         * @param body
         * @param mimeType
         */
        public Body(String body,String mimeType) {
            this.mimeType = mimeType == null || mimeType.isEmpty()? "text/plain":mimeType;
            //Considering plain text:
            this.body_string = body;
        }

        public String toString(){
            return body_string;
        }
    }

    private void setStatus(int i, String e) {
        this.status = i;
        this.errorMessage = e.toString();
    }

    @Override
    public String toString() {
        return "HTTPFormat{" +
                ", headers=" + headers +
                ", head_first_line=" + Arrays.toString(head_first_line) +
                ", request_path='" + request_path + '\'' +
                ", http_format_type=" + http_format_type +
                ", http_version='" + http_version + '\'' +
                ", http_method=" + http_method +
                ", args=" + args +
                ", body=" + body.toString() +
                ", status=" + status +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}
