package bif3.swe.if20b211.http;

import bif3.swe.if20b211.Json_form;
import bif3.swe.if20b211.api.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.*;

public class Format {
    //Public variables
    public String BARE_STRING;

    //Private varibales
    private int status = 200;
    private String error_message = "NoError";
    private int error_counter = 0;
    private Http_Format_Type type;

    private Http_Method method;
    private HashMap<String,String> headers = new HashMap<>();

    private Body body;
    private String path;
    private HashMap<String,String> arguments = new HashMap<>();

    //constructors

    /**
     * If you use this method, you have to run buildFormat(), bevore using the object.
     * @param response
     */
    public Format(Http_Format_Type response) {
        this.type = response;
        this.method = null;
        this.path = null;
        this.arguments = null;
        addDefaultHeaders();
    }

    public Format(int i, String s,String mime_type){
        this.status = i;
        boolean isError = i < 200 || i >= 300;
        this.error_message = isError?s:this.error_message;
        this.error_counter = isError?-1:this.error_counter;
        this.type = Http_Format_Type.RESPONSE;
        this.method = null;
        addDefaultHeaders();
        if(isError){
            this.body = new Body(String.format("{\"error_message\":\"%s\"}",s),mime_type != null?mime_type:"application/json");
        }else {
            this.body = new Body(s,mime_type != null?mime_type:"text/plain");
        }

        this.path = null;
        this.arguments = null;
        this.BARE_STRING = buildFormat();
    }

    public Format(Http_Method request, String host, String path, String body, String mime_type, String ... args) {
        //Define Request
        type = Http_Format_Type.REQUEST;
        this.method = request;
        this.path = path;
        addHeader("Host",host);
        for(String arg : args) {
            try {
                addArgument(arg.split("=")[0], arg.split("=")[1]);
            } catch (IndexOutOfBoundsException e) {
                setStatus(400, "Bad Format - Parameters can't be processed correctly");
            }
        }
        this.body = body == null? null:new Body(body,mime_type);
        buildFormat();
        addDefaultHeaders();
    }

    public String buildFormat(){
        if(body != null) overwriteHeader("Content-Length",Integer.toString(body.getLength()));
        overwriteHeader("Date", new Date().toString());
        String res = null;
        if(this.type.equals(Http_Format_Type.RESPONSE)){
            overwriteHeader("Status",Integer.toString(getStatus()));
            overwriteHeader("Server","localhost");
            String status = this.status >= 200 && this.status < 300? "OK":"ERR";
            res = String.format("HTTP/1.1 %d %s\n",this.status,status);
        }else {
            String args = "?";
            for(Map.Entry<String,String> entry:this.arguments.entrySet())
                args += String.format("%s=%s&",entry.getKey(),entry.getValue());
            //Removing last & or resetting args to be ""
            args = args.length() > 1? args.substring(0,args.length()-1):"";
            res = String.format("%s %s%s HTTP/1.1\n",this.method.toString(),path,args);
        }
        for (Map.Entry<String, String> entry:this.headers.entrySet())
            res += String.format("%s: %s\n", entry.getKey(), entry.getValue());
        if(body != null){
            res += "\n";
            res += body.toString() + "\u001a";
        }
        this.BARE_STRING = res;
        return res;
    }

    private void addDefaultHeaders() {
        addHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
        addHeader("Accept-Language","de-DE,de;q=0.9,en-US;q=0.8,en;q=0.7");
        addHeader("Accept-Encoding","gzip, deflate");
        if(Http_Format_Type.REQUEST.equals(this.type)){
            addHeader("Connection","Close");
        }
    }

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

            String[] path_args = getPathAndArgumentsSplitted(request_splitted[0]);
            if(path_args.length > 2){
                setStatus(400, "Bad Request - Too many argument indicators");
                return;
            }
            path = path_args[0];
            arguments = path_args.length < 2? null:generateStringKeyValueMapByDelimiters(path_args[1],"&","=");
        }
        headers = generateStringKeyValueMapByDelimiters(request_splitted[1],"\\n",":");

        if(request_splitted[2] != null){
            body = new Body(request_splitted[2], getValueOfStringHashMap(this.headers,"Content-Type"));
        }
    }

    /**
     * Generates a HashMap<String,String> out of a String. It the entries by delimiter1 and the key/value pairs by delimiter2
     * @param s
     * @param delimiter1
     * @param delimiter2
     * @return
     */
    private HashMap<String,String> generateStringKeyValueMapByDelimiters(String s, String delimiter1, String delimiter2){
        HashMap<String,String > res = new HashMap<>();
        Arrays.stream(s.split(delimiter1)).distinct().forEach(line -> res.put(line.split(delimiter2)[0].trim(),line.split(delimiter2)[1].trim()));
        return res;
    }

    private String[] getPathAndArgumentsSplitted(String s) {
        //Taking the path position  s.split(" ")[1]
        //Change format to backslashes
        String res = s.indexOf('/') != -1?  s.split(" ")[1].replace('/','\\').trim(): s.split(" ")[1].trim();
        //Returning index.html if request was / or empty
        if((s.indexOf('/') == -1 && s.indexOf('\\') == -1)|| s.length() == 1)return new String[]{"index.html"};
        return res.split("\\?");
    }

    /**
     * Checks if the http method is known and if the http method is accepted by the server
     * The second part should be removed, if this method should be used for more than only these cases.
     * @param s
     * @return
     */
    private Http_Method getHttpMethod(String s) {
        switch (s.split(" ")[0].toUpperCase()){
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
        if(status >= 200 && status < 300){
            this.error_message += String.format("There have been %d more errors which are not represented by this response.",error_counter);
        }else{
            this.status = i;
            this.error_message = s;
        }
        error_counter++;
    }
    //Getter / Setter
    public int getStatus(){ return status;}
    public String getPath() { return path;}
    public String getErrorMessage(){return error_message;}
    public HashMap<String, String> getHeaders(){ return headers; }
    public HashMap<String, String> getArguments () {return arguments;}
    public void addHeader(String header, String value){this.headers.put(header,value);}
    public void addArgument(String argument, String value){this.arguments.put(argument,value);}
    public void overwriteHeader(String header, String value){
        this.headers.remove(header);
        this.addHeader(header,value);
    }
    public String getHeadersToString(){ return headers.toString(); }
    public Http_Method getMethod() {return method;}
    //Specific getters
    /**
     * Returns the given value to the key <name> or null, if not exists.
     * @param name
     * @return
     */
    public String getValueOfStringHashMap(HashMap<String,String> map, String name){
        return map == null?null:map.entrySet().stream().filter(e -> name.equalsIgnoreCase(e.getKey())).map(Map.Entry::getValue).findFirst().orElse(null);
    }

    @Override
    public String toString() {
        if(status == 200){
            return "Format{" +
                    "BARE_STRING='" + BARE_STRING + '\'' +
                    ", status=" + status +
                    ", error_message='" + error_message + '\'' +
                    ", error_counter=" + error_counter +
                    ", type=" + type +
                    ", method=" + method +
                    ", headers=" + headers.toString() +
                    ", body=" + (body == null?null:body.toString()) +
                    ", path='" + path + '\'' +
                    ", arguments=" + (arguments == null? null:arguments.toString()) +
                    '}';
        }else {
            return "Format is not well formed. " +
                    "Run debug() to see all values. " +
                    "Debug() - function will crash if you see this response from toString()" +
                    "Debug() - could also crash if you don't see this response";
        }
    }
    public void debug(){
        System.out.println("BARE_STRING='" + BARE_STRING + '\'');
        System.out.println("status= " + status);
        System.out.println("error_message= " + error_message);
        System.out.println("error_counter= " + error_counter);
        System.out.println("type= " + type);
        System.out.println("method= " + method);
        System.out.println("headers= " + headers.toString());
        System.out.println("path= " + path);
        System.out.println("arguments= " + arguments.toString());
        System.out.println("body= " + body.toString());
    }

    public void setStatus(int i) {
    }
    //Additional Types
    public enum Http_Format_Type{
        REQUEST, RESPONSE
    }
    public enum Http_Method {
        GET, POST, PATCH, PUT, DELETE, HEAD, CONNECT, OPTION, TRACE
    }

    public Body getBody() {
        return body;
    }

    /**
     * Also runs buildFormat()
     * @param body
     * @param mimeType
     */
    public void setBody(String body, String mimeType) {
        System.out.println("Inside set Body with string: " + body);
        this.body = new Body(body, mimeType);
        buildFormat();
    }

    /**
     * Body is a nested class to format the Body in a specific format depending on the MIME-TYPE.
     * <p>
     * It provides multiple function which allow the User of this class to work with the body by expected content
     * instead of splitting it up all by hand.
     *
     */
    public class Body{
        public final String mimeType;
        private final String bare_body;
        private Object json_format;
        
        /**
         *
         * @param body
         * @param mimeType
         */
        public Body(String body,String mimeType) {
            this.bare_body = body;
            if(mimeType == null || mimeType.isEmpty()){
                this.mimeType = "text/plain";
            }else{
                this.mimeType = mimeType;
            }
        }

        public String toString(){
            return bare_body;
        }

        /**
         * It is suggested to use this method with the control, if the mimeType is correct.
         * <p>Returning null, if body is null
         * @return
         * @throws IOException
         */
        public JsonNode getJson_format() throws IOException {
            return Json_form.parse(bare_body);
        }

        /**
         * It is suggested to use this method with the control, if the mimeType is correct.
         * <p>Returning null, if body is null
         * @param node
         * @param aClass
         * @param <A>
         * @return
         * @throws JsonProcessingException
         */
        public <A> A fromJsonToObject(JsonNode node, Class<A> aClass) throws JsonProcessingException {
            return Json_form.fromJson(node,aClass);
        }

        /**
         * It is suggested to use this method with the control, if the mimeType is correct.
         * <p>Returning null, if body is null
         * @param aClass
         * @param <A>
         * @return
         * @throws JsonProcessingException
         */
        public <A> A toObjectExpectingJson(Class<A> aClass) throws IOException {
            return Json_form.fromJson(Json_form.parse(bare_body),aClass);
        }

        public String getMimeType(){
            return mimeType;
        }

        public int getLength() {
            return bare_body.length();
        }
    }
}
