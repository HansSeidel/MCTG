

import bif3.swe.if20b211.Json_form;
import bif3.swe.if20b211.api.Message;
import bif3.swe.if20b211.api.Messages;
import bif3.swe.if20b211.http.Format;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.*;
import java.net.Socket;
import java.text.Normalizer;
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
    public static Format GET(Format request) {
        String path = request.getPath();
        System.out.println("Get recognised, path: " + path);
        if (!path.contains("\\")) {
            return new Format(404,"Not Found - the link you try to address does not exist",null);
        } else if (path.startsWith("\\api")) {
            return GET_api(path);
        } else if (path.startsWith("\\messages")){
            return GET_messages(path,request);
        } else if(path.startsWith("\\index") || path.equals("\\")){
            return GET_index(path);
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

    private static Format GET_index(String path) {
        Format response = new Format(Format.Http_Format_Type.RESPONSE);
        try {
            File myObj = new File(System.getProperty("user.dir") + "/index.html");
            Scanner myReader = new Scanner(myObj);
            String data = "";
            while (myReader.hasNextLine()) {
                data += myReader.nextLine();
            }
            myReader.close();
            response.setBody(data,"text/html");
            return response;
        } catch (FileNotFoundException e) {
            return new Format(503,"Service Unavailable - structure can't be processed. Please contact the server administrator",null);
        }
    }

    private static Format GET_messages(String path, Format request) {
        System.out.println("Seachring messages with path: " + path);
        Format response = new Format(Format.Http_Format_Type.RESPONSE);
        Messages messages;
        try {
            messages = Json_form.fromJson(Json_form.parse(new File(System.getProperty("user.dir") + "\\messages\\allMessages.json")), Messages.class);
        } catch (IOException e) {
            return new Format(503,"Service Unavailable - structure can't be processed. Please contact the server administrator",null);
        }

        if(path.equals("\\messages") || path.equals("\\messages\\")){
            String limit = request.getValueOfStringHashMap(request.getArguments(), "limit");
            String sender = request.getValueOfStringHashMap(request.getArguments(), "sender");
            Messages resultMessages = new Messages();
            try {
                if(limit != null && sender == null)resultMessages.setMessages(messages.getMessagesLimitBy(Integer.parseInt(limit)));
                if(limit == null && sender != null)resultMessages.setMessages(messages.getMessagesBySender(sender));
                if(limit != null && sender != null)resultMessages.setMessages(messages.getMessagesBySenderLimitBy(sender,Integer.parseInt(limit)));
                if(limit == null && sender == null)resultMessages.setMessages(messages.getMessages());
                System.out.println("Got following result: ");
                resultMessages.getMessages().stream().forEach(message -> {
                    System.out.println("isGone: " + message.isGone());
                    System.out.println("id: " + message.getId());
                    System.out.println("sender: " + message.getSender());
                    System.out.println("msg: " + message.getMessage());
                });
                response.setBody(Json_form.stringify(Json_form.toJson(resultMessages)),"application/json");
                System.out.println("Body: " + response.getBody());
            }catch (NumberFormatException e){
                return new Format(422, "Unprocessable Entity - The arguments can't be used. Use either limit or sender as argument",null);
            }catch (JsonProcessingException e) {
                return new Format(422, "Unprocessable Entity - The arguments can't be used. Use either limit or sender as argument",null);
            }
        }else {
            //split string into the path and into the id
            String[] path_splitted = path.split("\\\\");
            int id;
            try{
                id = Integer.parseInt(path_splitted[2].trim());
                Message resultMessage = messages.getMessagesById(id);
                if(resultMessage == null) return new Format(404, "Not Found - The message seems not to exists",null);
                if(resultMessage.isGone()) return new Format(410,"Gone - The message has been deleted",null);
                response.setBody(Json_form.stringify(Json_form.toJson(resultMessage)),"application/json");
            }catch (NumberFormatException e){
                return new Format(406, "Not Acceptable - Expected a number to search for a message.",null);
            }catch (JsonProcessingException e) {
                return new Format(422, "Unprocessable Entity - The arguments can't be used. Use either limit or sender as argument",null);
            }
        }
        System.out.println("Returning body with: " + response.BARE_STRING);
        return response;
    }

    private static Format GET_api(String path) {
        System.out.println("Seachring api with path: " + path);
        if(path.equals("\\api") || path.equals("\\api\\") || path.equals("\\api\\structure") || path.equals("\\api\\structure.json")){
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

    public static Format POST(Format request) {
        if(request.getPath().equals("\\messages")||request.getPath().equals("\\messages\\")){
            System.out.println("Putting messages with path: " + request.getPath());
            Format response = new Format(Format.Http_Format_Type.RESPONSE);
            Messages messages;
            Message m;
            try {
                messages = Json_form.fromJson(Json_form.parse(new File(System.getProperty("user.dir") + "\\messages\\allMessages.json")), Messages.class);
                m = request.getBody().fromJsonToObject(request.getBody().getJson_format(),Message.class);
            } catch (IOException e) {
                return new Format(503,"Service Unavailable - structure can't be processed. It could also be, that your body wasn't send correctly",null);
            }
            if(m.getSender() == null || m.getMessage() == null){
                return new Format(400, "Bad Format - You may only POST if you have values for sender and message. Otherwise use Patch.",null);
            }
            m.setId(messages.getNextId());
            messages.addMessage(m);
            if(Json_form.write(System.getProperty("user.dir") + "/messages/allMessages.json",Json_form.toJson(messages)) == -1){
                return new Format(500,"Internal Server Error - Struggling  writing new data. Please call the administrator.",null);
            }else{
                try {
                    response.setBody(Json_form.stringify(Json_form.toJson(m)),"application/json");
                    response.setStatus(201);
                } catch (JsonProcessingException e) {
                    return new Format(500,"Internal Server Error - Struggling  writing new data. Please call the administrator.",null);
                }
            }
            return response;
        }else {
            return new Format(403,"Forbidden - You are not allowed to get any data from your requested resource.",null);
        }
    }
}
