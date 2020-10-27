package bif3.swe.if20b211.api;

import netscape.javascript.JSObject;
import org.json.JSONObject;

import java.net.URI;

public class MyHttpCRUD {
    //This Method should be clientSided and normally is build with JavaScript
    //Pseudocode
    private static String client;
    private static String auth_token;
    private static URI server;
    private JSObject jsObject;

    //private static <Type> metadata;
    //CREATE OBJECT BUILDER!!

    MyHttpCRUD(String client, URI server){
        this.client = client;
        this.server = server;
    }
    public static String getAuth_token() {
        return auth_token;
    }

    public static void setAuth_token(String auth_token) {
        MyHttpCRUD.auth_token = auth_token;
    }

    public static URI getServer() {
        return server;
    }

    public static void setServer(URI server) {
        MyHttpCRUD.server = server;
    }

    public static JSONObject GET(String path, String ... params){

        return null;
    }
    public static JSONObject GET(URI otherUri, String path, String ... params){

        return null;
    }

}
