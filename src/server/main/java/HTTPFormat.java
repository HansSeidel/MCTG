import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
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

    private boolean setPath(String s) {
        System.out.println("Inside setPathAndArgs() - Setting path and args...");
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
        if(head == null || head.isEmpty())return null;
        HashMap<String,String> res = new HashMap<String, String>();
        while (!head.isEmpty()){
            res.put(head.substring(0,head.indexOf(":")),head.substring(head.indexOf(":")));
            head = reader.readLine();
        }
        return res;
    }

    private Body readBody() throws IOException {
        String t = "";
        String res = "";
        int read_chars = 0;
        int cl = 0;
        if(!reader.ready()){
            return null;
        }else{
            if(getHeaderValueByName("Content-Length").isEmpty() || getHeaderValueByName("Content-Length") == null){
                setStatus(411, "Length Required: Please add Content-Length to the headers");
                return null;
            }
            try{
                cl = Integer.parseInt(getHeaderValueByName("Content-Length"));
            }catch (NumberFormatException e){
                setStatus(412, String.format("Precondition Failed: %s",e.toString()));
                return null;
            }
        }
        while((t = reader.readLine()) != null){
            t += t + "\n";
            read_chars += t.length()+1; //plus one because line break isn't count as length but is counted inside Content-Length
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
            this.mimeType = mimeType.isEmpty() || mimeType == null? "text/plain":mimeType;
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
}