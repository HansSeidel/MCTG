package bif3.swe.if20b211.api;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class MyHttpCRUD {
    //This Method should be clientSided and normally is build with JavaScript
    //Pseudocode
    private static String client;
    private static String auth_token;
    private static String server;

    //private static <Type> metadata;
    //CREATE OBJECT BUILDER!!

    public MyHttpCRUD(String client, String server){
        MyHttpCRUD.client = client;
        MyHttpCRUD.server = server;
    }
    public static String getAuth_token() {
        return auth_token;
    }

    public static void setAuth_token(String auth_token) {
        MyHttpCRUD.auth_token = auth_token;
    }

    public static String getServer() {
        return server;
    }

    public static void setServer(String server) {
        MyHttpCRUD.server = server;
    }

    public static StringBuffer GET(String path, String ... params) throws IOException {
        String url = server + "/" +path + (params[0]!=null?"?":"");
        for(String param : params)
            url += param + (param.equals(params[params.length-1])?"":"&");


        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        String line;
        StringBuffer responseContent = new StringBuffer();
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        while ((line = reader.readLine()) != null)
            responseContent.append(line);
        reader.close();

    return responseContent;
    }

    public static JSONObject GET(URI ownUri, String path, String ... params){

        return null;
    }

}
