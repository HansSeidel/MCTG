package bif3.swe.if20b211.api;

import bif3.swe.if20b211.http.Json_form;
import bif3.swe.if20b211.http.Format;
import bif3.swe.if20b211.mctg.models.Card;
import bif3.swe.if20b211.mctg.models.User;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.*;
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
        } else {
            return new Format(403, "Forbidden - You are not allowed to get any data from your requested resource.", null);
        }
    }

    private static Format GET_api(String path, Format request, DBConnector _dbConnector) {
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
        if (request.getPath().equals("\\api\\mctg\\deck")){
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
}
