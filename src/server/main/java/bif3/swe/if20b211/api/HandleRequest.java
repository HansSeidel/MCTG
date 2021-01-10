package bif3.swe.if20b211.api;

import bif3.swe.if20b211.http.Json_form;
import bif3.swe.if20b211.api.Message;
import bif3.swe.if20b211.api.Messages;
import bif3.swe.if20b211.http.Format;
import bif3.swe.if20b211.mctg.models.Card;
import bif3.swe.if20b211.mctg.models.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class HandleRequest {
    private static User user = null;

    private static boolean checkAuth(String token) {
        if(user == null) return false;
        if(token == null) return false;
        if(!user.isLoggedIn()) return false;
        if(!user.getToken().equals(token)) return false;
        return true;
    }

    /**
     * Get reqeust which get answerde:
     * api/structure.json
     * api/structure
     * api
     * api/
     * messages
     * messages/
     * messages/1
     * <p>
     * demo
     * demo/
     *
     * @return
     */
    public static Format GET(Format request, DBConnector _dbConnector) {
        String path = request.getPath();
        if (!path.contains("\\")) {
            return new Format(404, "Not Found - the link you try to address does not exist", null);
        } else if (path.startsWith("\\api")) {
            return GET_api(path, request, _dbConnector);
        } else if (path.startsWith("\\messages")) {
            return GET_messages(path, request);
        } else if (path.startsWith("\\index") || path.equals("\\")) {
            return GET_index(path);
        } else if (path.startsWith("\\demo")) {
            //return GET_demo(path);
        } else {
            return new Format(403, "Forbidden - You are not allowed to get any data from your requested resource.", null);
        }
        return new Format(600, "In development - You reached handling, which is not handled by the developer yet.", null);
    }

    private static Format GET_index(String path) {
        Format response = new Format(Format.Http_Format_Type.RESPONSE);
        try {
            File myObj = new File(System.getProperty("user.dir") + "\\index.html");
            Scanner myReader = new Scanner(myObj);
            String data = "";
            while (myReader.hasNextLine()) {
                data += myReader.nextLine();
            }
            myReader.close();
            response.setBody(data, "text/html");
            return response;
        } catch (FileNotFoundException e) {
            return new Format(503, "Service Unavailable - structure can't be processed. Please contact the server administrator", null);
        }
    }

    private static Format GET_messages(String path, Format request) {
        Format response = new Format(Format.Http_Format_Type.RESPONSE);
        Messages messages;
        try {
            messages = Json_form.fromJson(Json_form.parse(new File(System.getProperty("user.dir") + "\\messages\\allMessages.json")), Messages.class);
        } catch (IOException e) {
            return new Format(503, "Service Unavailable - structure can't be processed. Please contact the server administrator", null);
        }

        if (path.equals("\\messages") || path.equals("\\messages\\")) {
            String limit = request.getValueOfStringHashMap(request.getArguments(), "limit");
            String sender = request.getValueOfStringHashMap(request.getArguments(), "sender");
            String gone = request.getValueOfStringHashMap(request.getArguments(), "showGone");
            Messages resultMessages = new Messages();
            try {
                if(!(gone != null && gone.equals("true")))
                    resultMessages.setMessages(messages.getMessagesWithoutGone());

                if (limit != null && sender == null)
                    resultMessages.setMessages(resultMessages.getMessagesLimitBy(Integer.parseInt(limit)));
                if (limit == null && sender != null) resultMessages.setMessages(resultMessages.getMessagesBySender(sender));
                if (limit != null && sender != null)
                    resultMessages.setMessages(resultMessages.getMessagesBySenderLimitBy(sender, Integer.parseInt(limit)));
                if (limit == null && sender == null) resultMessages.setMessages(resultMessages.getMessages());

                response.setBody(Json_form.stringify(Json_form.toJson(resultMessages)), "application/json");
            } catch (NumberFormatException e) {
                return new Format(422, "Unprocessable Entity - The arguments can't be used. Use either limit or sender as argument", null);
            } catch (JsonProcessingException e) {
                return new Format(422, "Unprocessable Entity - The arguments can't be used. Use either limit or sender as argument", null);
            }
        } else {
            //split string into the path and into the id
            String[] path_splitted = path.split("\\\\");
            int id;
            try {
                id = Integer.parseInt(path_splitted[2].trim());
                Message resultMessage = messages.getMessagesById(id);
                if (resultMessage == null) return new Format(404, "Not Found - The message seems not to exists", null);
                if (resultMessage.isGone()) return new Format(410, "Gone - The message has been deleted", null);
                response.setBody(Json_form.stringify(Json_form.toJson(resultMessage)), "application/json");
            } catch (NumberFormatException e) {
                return new Format(406, "Not Acceptable - Expected a number to search for a message.", null);
            } catch (JsonProcessingException e) {
                return new Format(422, "Unprocessable Entity - The arguments can't be used. Use either limit or sender as argument", null);
            }
        }
        return response;
    }

    private static Format GET_api(String path, Format request, DBConnector _dbConnector) {
        if (path.equals("\\api") || path.equals("\\api\\") || path.equals("\\api\\structure") || path.equals("\\api\\structure.json")) {
            try {
                JsonNode node = Json_form.parse(new File(System.getProperty("user.dir") + "\\api\\structure.json"));
                return new Format(200, Json_form.stringify(node), "application/json");
            } catch (IOException e) {
                return new Format(503, "Service Unavailable - structure can't be processed. Please contact the server administrator", null);
            }
        }
        Format response = new Format(Format.Http_Format_Type.RESPONSE);
        if(path.equals("\\api\\mctg\\register") || path.equals("\\api\\mctg\\login")){
            try {
                user = request.getBody().fromJsonToObject(request.getBody().getJson_format(), User.class);
                if(_dbConnector.userExists(user.getUsername())){
                    if(path.equals("\\api\\mctg\\login")){
                        if(!_dbConnector.checkPassword(user))
                            return new Format(406, "Not accepted - Wrong password", null);
                    }else{
                        return new Format(409, "Conflicht - Username already exists.",null);
                    }
                }else{
                    if(path.equals("\\api\\mctg\\login"))
                        return new Format(404, "Not Found - User not found",null);
                    if(_dbConnector.addUser(user) != 0) return new Format(500, "Internal Server Error - please contact the administrator",null);
                }
                user.setToken(UUID.randomUUID().toString());
                response.setStatus(201);
                if(path.equals("\\api\\mctg\\login")) response.setStatus(200);
                response.addHeader("model","user");
                response.addHeader("token", user.getToken());
                response.setBody(Json_form.stringify(Json_form.toJson(user)), "application/json");
            } catch (IOException | SQLException e) {
                e.printStackTrace();
            }
            return response;
        }
        if(path.equals("\\api\\mctg\\order")) {
            if(!checkAuth(request.getValueOfStringHashMap(request.getHeaders(),"token")))
                return new Format(401, "Unauthorized - You must be logged in for these commands",null);;
            try {
                int current_coins = _dbConnector.getCoins(user.getUsername());
                int amount = Integer.parseInt(request.getValueOfStringHashMap(request.getArguments(),"amount"));
                if(current_coins < 5*amount){
                    if(current_coins == -1) return new Format(500, "Internal Server Error - No coins at all",null);
                    return new Format(409, "Conflict - Not enough coins",null);
                }
                List<Card> cards = _dbConnector.acquirePackages(amount);
                if(cards == null) return new Format(409, "Conflict - You may only buy 10 packages at once",null);
                _dbConnector.updateCoins(user.getUsername(),amount);
                _dbConnector.addToStack(user.getUsername(),amount,cards);
                String finalBody = _dbConnector.createCardsBody(cards);
                response.addHeader("model","cards");
                response.setBody(finalBody,"application/json");
                return response;
            } catch (SQLException | JsonProcessingException throwables) {
                throwables.printStackTrace();
                return new Format(500, "Internal Server Error - Please contact the administrator",null);
            }
        }
        if (request.getPath().equals("\\api\\mctg\\deck")){
            String action = request.getValueOfStringHashMap(request.getArguments(),"action");
            if(action.equals("show")){
                try {
                    List<Card> cards = _dbConnector.getDeck(user.getUsername());
                    String finalBody = _dbConnector.createCardsBody(cards);
                    response.addHeader("model","deck");
                    response.setBody(finalBody,"application/json");
                    return response;
                } catch (SQLException | JsonProcessingException throwables) {
                    throwables.printStackTrace();
                    return new Format(500, "Internal Server Error - Please contact the administrator",null);
                }

            }else {
                return new Format(404, "Request Not Found - unknown action in Header field", null);
            }
        }
        return new Format(404, "Request Not Found", null);
    }

    public static Format POST(Format request, DBConnector _dbConnector) {
        Format response = new Format(Format.Http_Format_Type.RESPONSE);
        if (request.getPath().equals("\\messages") || request.getPath().equals("\\messages\\")) {
            Messages messages;
            Message m;
            try {
                messages = Json_form.fromJson(Json_form.parse(new File(System.getProperty("user.dir") + "\\messages\\allMessages.json")), Messages.class);
                m = request.getBody().fromJsonToObject(request.getBody().getJson_format(), Message.class);
            } catch (IOException e) {
                return new Format(503, "Service Unavailable - structure can't be processed. It could also be, that your body wasn't send correctly", null);
            }
            if (m.getSender() == null || m.getMessage() == null) {
                return new Format(400, "Bad Format - You may only POST if you have values for sender and message. Otherwise use Patch.", null);
            }
            m.setId(messages.getNextId());
            messages.addMessage(m);
            if (Json_form.write(System.getProperty("user.dir") + "/messages/allMessages.json", Json_form.toJson(messages)) == -1) {
                return new Format(500, "Internal Server Error - Struggling  writing new data. Please call the administrator.", null);
            } else {
                try {
                    response.setStatus(201);
                    response.setBody(Json_form.stringify(Json_form.toJson(m)), "application/json");
                } catch (JsonProcessingException e) {
                    return new Format(500, "Internal Server Error - Struggling  writing new data. Please call the administrator.", null);
                }
            }
            return response;
        } else if (request.getPath().equals("\\api\\mctg\\deck")){
            String cardname = request.getValueOfStringHashMap(request.getArguments(),"cardname");
            String action = request.getValueOfStringHashMap(request.getArguments(),"action");
            try {
                if(action.equals("add"))
                    if(_dbConnector.countDeck(user.getUsername()) >= 4)
                        return new Format(409,"Conflict - You can only have 4 cards in your deck.",null);
                List<Card> cards = _dbConnector.manageDeck(user.getUsername(),cardname,action);
                if(cards == null){
                    if(action.equals("add")) return new Format(409, "Conflict - This card does either not exists or isn't in your stack", null);
                    return new Format(409,"Conflict - You can't remove this card, it is not inside your deck.",null);
                }
                String finalBody = _dbConnector.createCardsBody(cards);
                response.addHeader("model","deck");
                response.setBody(finalBody,"application/json");
                return response;
            } catch (SQLException | JsonProcessingException throwables) {
                throwables.printStackTrace();
                return new Format(500, "Internal Server Error - Please contact the administrator",null);
            }

        } else {
            return new Format(403, "Forbidden - You are not allowed to get any data from your requested resource.", null);
        }
    }

    public static Format PATCH(Format request){
        return PUT_PATCH(request,false);
    }

    public static Format PUT(Format request) {
        return PUT_PATCH(request,true);
    }

    private static Format PUT_PATCH(Format request, boolean isPut) {
        if (request.getPath().startsWith("\\messages\\")) {
            Format response = new Format(Format.Http_Format_Type.RESPONSE);
            Messages messages;
            Message m;
            String[] path_splitted = request.getPath().split("\\\\");
            int id;
            try {
                messages = Json_form.fromJson(Json_form.parse(new File(System.getProperty("user.dir") + "\\messages\\allMessages.json")), Messages.class);
                id = Integer.parseInt(path_splitted[2].trim());
                m = request.getBody().fromJsonToObject(request.getBody().getJson_format(), Message.class);
                Message toPatch = messages.getMessagesById(id);
                if (m.getId() != 0)
                    return new Format(403, "Forbidden - You are not allowed to change the id of a message", null);
                if(toPatch == null)
                    return new Format(404,"Not Found - There is no message to update under the requested id",null);
                if(toPatch.isGone())
                    return new Format(410, "Gone - The message doesn't exist any longer", null);
                m.setId(toPatch.getId());
                if (m.getMessage() == null && m.getSender() == null) {
                    response = new Format(204, "No Content - Body does not contain any valid data", null);
                } else {
                    if(m.getSender() != null && m.getMessage() != null && !isPut)
                        return new Format(405, "Method Not Allowed - If you changing everything use PUT or create a new message", null);
                    m.setMessage(m.getMessage() != null ? m.getMessage() : toPatch.getMessage());
                    m.setSender(m.getSender() != null ? m.getSender() : toPatch.getSender());
                    messages.changeMessage(m);
                    messages.sort();
                }
                if (Json_form.write(System.getProperty("user.dir") + "/messages/allMessages.json", Json_form.toJson(messages)) == -1) {
                    return new Format(500, "Internal Server Error - Struggling  writing new data. Please call the administrator.", null);
                } else {
                    try {
                        if (response.getStatus() == 204) {
                            response.setBody(Json_form.stringify(Json_form.toJson(toPatch)), "application/json");
                        } else {
                            response.setStatus(202);
                            response.setBody(Json_form.stringify(Json_form.toJson(m)), "application/json");
                        }
                    } catch (JsonProcessingException e) {
                        return new Format(500, "Internal Server Error - Struggling  writing new data. Please call the administrator.", null);
                    }
                }
                return response;
            } catch (NumberFormatException e) {
                return new Format(406, "Not Acceptable - Expected a number to patch for a message.", null);
            } catch (JsonProcessingException e) {
                return new Format(422, "Unprocessable Entity - The arguments can't be used. Use either limit or sender as argument", null);
            } catch (IOException e) {
                return new Format(503, "Service Unavailable - structure can't be processed. It could also be, that your body wasn't send correctly", null);
            }
        } else {
            return new Format(405, "Method Not Allowed - You may only PATCH a specific message (/message/{id}) Otherwise use POST.", null);
        }
    }

    public static Format DELETE(Format request) {
        if (request.getPath().startsWith("\\messages\\")) {
            Format response = new Format(Format.Http_Format_Type.RESPONSE);
            Messages messages;
            Message m;
            String[] path_splitted = request.getPath().split("\\\\");
            int id;
            try {
                messages = Json_form.fromJson(Json_form.parse(new File(System.getProperty("user.dir") + "\\messages\\allMessages.json")), Messages.class);
                id = Integer.parseInt(path_splitted[2].trim());
                m = messages.getMessagesById(id);
                if (m == null) {
                    return new Format(404, "Not Found - There is no message to delete under the requested id", null);
                } else if (m.isGone()) {
                    return new Format(410, "Gone - The message has already been deleted", null);
                } else {
                    m.setMessage(null);
                    m.setSender(null);
                    m.setGone(true);
                    messages.changeMessage(m);
                    messages.sort();
                }
                if (Json_form.write(System.getProperty("user.dir") + "/messages/allMessages.json", Json_form.toJson(messages)) == -1) {
                    return new Format(500, "Internal Server Error - Struggling  writing new data. Please call the administrator.", null);
                } else {
                    response.setBody("{\"delete_response\":\"Message has been deleted\"}", "application/json");
                    response.setStatus(200);
                }
                return response;
            } catch (NumberFormatException e) {
                return new Format(406, "Not Acceptable - Expected a number to delete for a message.", null);
            } catch (JsonProcessingException e) {
                return new Format(422, "Unprocessable Entity - The arguments can't be used. Use either limit or sender as argument", null);
            } catch (IOException e) {
                return new Format(503, "Service Unavailable - structure can't be processed. It could also be, that your body wasn't send correctly", null);
            }
        }
        return new Format(500, "Internal Server Error - You reached unreachable code",null);
    }
}
