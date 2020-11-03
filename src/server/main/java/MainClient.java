import bif3.swe.if20b211.api.MyHttpHandler;
import bif3.swe.if20b211.api.SimpleBufferedWriter;

import java.io.*;
import java.net.Socket;

public class MainClient {
    public static void main(String[] args) {
        System.out.println("start client");

        /*
         * Aim code:
         * messager.write(to,message);
         * inside messager:
         *
        connection.get("/messages/{id}");
        connection.post("/message/10","MyMessage");
        connection.put("message/1","MyNewMessage");
        connection.delet("message/1","Deletede");

        try {
            MyHttpCRUD httpHandler = new MyHttpCRUD("localhost","https://postman-echo.com");
            StringBuffer response = httpHandler.GET("GET","foo1=bar1","foo2=bar2");
            System.out.println(response.toString());
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
        */




        try{
            //TODO Write many tests for this method
            MyHttpHandler handler = new MyHttpHandler("postman-echo.com",80);
            //handler.addOptionalHeader(new String[]{"Connection: ", "Keep-Alive"}); Chrashes programm
            String response = handler.GET("/get","foo=bar","foo2=bar2");
            System.out.println(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("close client");

    }
}
