package bif3.swe.if20b211.http;

import java.util.Arrays;
import java.util.Optional;

public class Format {
    //Public variables
    public final String BARE_STRING;

    //Private varibales
    private int status = 200;
    private String error_message = "NoError";
    private Http_Format_Type type;


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
        for (String st: request_splitted) {
            System.out.println(String.format("Curr. process: %s",st));
        }
    }

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
            System.out.println("line: " + line);
        });

        //Check if body exists and safe it if so in res[2]
        res[2] = tmp.length == 2? tmp[1].trim():null;

        //Return all results as Array
        return res;
    }

    //SetStatus function
    private void setStatus(int i, String s) {
        this.status = i;
        this.error_message = s;
    }
    //Getter / Setter
    public int getStatus(){ return status;}
    public String getErrorMessage(){return error_message;}

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
}
