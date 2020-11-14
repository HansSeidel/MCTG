

import bif3.swe.if20b211.Json_form;
import bif3.swe.if20b211.http.Format;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class HandleRequest {

    /**
     * Get reqeust which get answerde:
     * api/structure.json
     * api/structure
     * api
     * api/
     * messages
     * messages/
     * messages/1
     *
     * demo
     * demo/
     * @return
     */
    public static Format GET(String path) {
        System.out.println("Get recognised, path: " + path);
        if (!path.contains("\\")) {
            return new Format(404,"Not Found - the link you try to address does not exist",null);
        } else if (path.startsWith("\\api")) {
            return GET_api(path);
        } else if (path.startsWith("\\messages")){
            //return GET_messages(path);
        } else if(path.startsWith("\\index")){
            //return GET_index(path);
        } else if (path.startsWith("\\demo")){
            //return GET_demo(path);
        }else{
            return new Format(403,"Forbidden - You are not allowed to get any data from your requested resource.",null);
        }
        return new Format(600,"In development - You reached handling, which is not handled by the developer yet.",null);
/*
        if(splitted_path.length < 2 && splitted_path_tmp.startsWith("messages")){
            result = messages;
        }else if (splitted_path.length == 2 && splitted_path_tmp.startsWith("messages")){
            try{
                result = messages.getMessage(Integer.parseInt(splitted_path[1]));
            }catch (NumberFormatException e){
                error_message = "Not Acceptable - Expected a number to search for a message.";
                errorCode = 406;
            }
        }else if(request.getPath().equals("\\index.html")){
            //TODO
        }else if(splitted_path_tmp.startsWith("api")){
            //TODO
        }
        else {
            error_message = splitted_path_tmp.startsWith("messages")? "Not Found - The link you try to address does not exist":
                    ;
        }

 */
    }

    private static Format GET_api(String path) {
        System.out.println("Seachring api with path: " + path);
        if(path.equals("\\api\\") || path.equals("\\api\\structure") || path.equals("\\api\\structure.json")){
            try {
                JsonNode node = Json_form.parse(new File(System.getProperty("user.dir") + "\\api\\structure.json"));
                return new Format(200,Json_form.stringify(node),"application/json");
            } catch (IOException e) {
                return new Format(503,"Service Unavailable - structure can't be processed. Please contact the server administrator",null);
            }
        }else {
            return new Format(404, "File Not Found - Btw. this should be unreachable code",null);
        }
    }
}
