package bif3.swe.if20b211.http;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Format {
    //Public variables
    public final String BARE_STRING;

    //Private varibales
    private int status = 200;
    private String error_message = "NoError";
    private int error_counter = 0;
    private Http_Format_Type type;
    private Http_Method method;
    private HashMap<String,String> headers = new HashMap<>();
    //private Body body;

    //constructors
    public Format(String s){
        BARE_STRING = s;
        if(s.isEmpty() || s == null) {
            setStatus(400, "Bad Request - No Content");
            return;
        }
        //Split request into [] first line, [] headers, [] body
        String[] request_splitted = splitIncome(s);
        if(getStatus() != 200) return;
        type = getTypeAndCheckHTTPVersion(request_splitted[0]);
        if(type.equals(Http_Format_Type.REQUEST)){
            method = getHttpMethod(request_splitted[0]);
        }
        headers = setHeadersInitial(request_splitted[1]);

        /*
        if(request_splitted[2] != null){
            body = new Body(request_splitted[2],getHeaderByName("Content-Type"));
        }
        */
    }

    /**
     * Splits the headers into a key value map and returns this map.
     * @param s
     * @return
     */
    private HashMap<String, String> setHeadersInitial(String s) {
        HashMap<String,String> res = new HashMap<>();
        Arrays.stream(s.split("\\n")).distinct().forEach(line -> res.put(line.split(":")[0].trim(),line.split(":")[1].trim()));
        return res;
    }

    /**
     * Checks if the http method is known and if the http method is accepted by the server
     * The second part should be removed, if this method should be used for more than only these cases.
     * @param s
     * @return
     */
    private Http_Method getHttpMethod(String s) {
        switch (s.split(" ")[2].toUpperCase()){
            case "GET":return Http_Method.GET;
            case "POST":return Http_Method.POST;
            case "PUT":return Http_Method.PUT;
            case "DELETE":return Http_Method.DELETE;
            case "PATCH":return Http_Method.PATCH;
            case "HEAD":
            case "CONNECT":
            case "OPTIONS":
            case "TRACE":
                setStatus(405, "Method Not Allowed - This api does not suppored the requested method");
                return null;
            default:
                setStatus(405, "Method Not Allowed - Method unknown");
                return null;
        }
    }

    /**
     * Checks the type and the http version of the request or response.
     * Changes the state if it is the wrong http version
     * @param s
     * @return
     */
    private Http_Format_Type getTypeAndCheckHTTPVersion(String s) {
        String[] toCheck = s.split(" ");
        //Check if the last index is the HTTP version. If yes, it is a request. If not it is either a response or the wrong HTTPVersion
        boolean is_request = toCheck[2].startsWith("HTTP/1");
        //If HTTP/1 wasn't found, check if it is inside the first index. If it is, it is a response. If it isn't it is the wrong HTTPVersion.
        if(!is_request) if(!toCheck[0].startsWith("HTTP/1")) setStatus(505, "HTTP Version Not Supported: This api only responding to HTTP/1.x");
        if(getStatus() == 505) return null;
        return is_request? Http_Format_Type.REQUEST:Http_Format_Type.RESPONSE;
    }

    /**
     * This method checks, if the incoming stream consists of a request, a head and a body. <p></p>
     * It sets the status on an error, if the head or the request line is missing.
     * @param s
     * @return
     */
    private String[] splitIncome(String s) {
        //Split into head and body
        String[] tmp = s.split("\\n\\n");
        String[] res = new String[3];

        //Check for correct format
        if(tmp.length > 2) setStatus(400, "Bad Request - Too many line blank lines.");

        //Get first line and safe it into res[0]
        res[0] = Arrays.stream(tmp[0].split("\\n")).findFirst().get();

        //Get all headers and safe it into res[1]
        //Define res[1] to be not null. Otherwise it would be nullHeader:header
        res[1] = "";
        Arrays.stream(tmp[0].split("\\n")).skip(1).forEach(line -> {
            res[1] += line.trim()+"\n";
        });

        //Check if body exists and safe it if so in res[2]
        res[2] = tmp.length == 2? tmp[1].trim():null;

        //Return all results as Array
        return res;
    }

    //SetStatus function
    private void setStatus(int i, String s) {
        if(status != 200){
            this.error_message += String.format("There have been %d more errors which are not represented by this response.",error_counter);
        }else{
            this.status = i;
            this.error_message = s;
        }
        error_counter++;
    }
    //Getter / Setter
    public int getStatus(){ return status;}
    public String getErrorMessage(){return error_message;}
    public String getHeaders(){ return headers.toString(); }

    //Specific getters
    /**
     * Returns the given value to the key <name> or null, if not exists.
     * @param name
     * @return
     */
    public String getHeaderValueByName(String name){
        return this.headers.entrySet().stream().filter(e -> name.equalsIgnoreCase(e.getKey())).map(Map.Entry::getValue).findFirst().orElse(null);
    }

    @Override
    public String toString() {
        return "Format{" +
                "BARE_STRING='" + BARE_STRING + '\'' +
                ", status=" + status +
                ", error_message='" + error_message + '\'' +
                ", type=" + type +
                '}';
    }

    enum Http_Format_Type{
        REQUEST, RESPONSE
    }
    enum Http_Method {
        GET, POST, PATCH, PUT, DELETE, HEAD, CONNECT, OPTION, TRACE
    }
}
