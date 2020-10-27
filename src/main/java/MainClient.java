import bif3.swe.if20b211.api.MyHttpCRUD;
import org.json.JSONObject;

import java.io.*;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;

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
            StringBuffer response = httpHandler.GET("get","foo1=bar1","foo2=bar2");
            System.out.println(response.toString());
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
        */


        try (Socket socket = new Socket("stackoverflow.com", 80);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
             SimpleBufferedWriter writer = new SimpleBufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {

            String httpServer = "stackoverflow.com";
            int serverPort = 80;
            int timeoutMillis = 5000;
            String httpRequest = "Get /questions/10673684/send-http-request-manually-via-socket HTTP/1.1";
            String hostHeader = "Host: " + httpServer + "\r\n";

            socket.setSoTimeout(timeoutMillis);
            //writer.write(httpRequest,hostHeader,"");
            writer.write(false,httpRequest,hostHeader);
            writer.newLine();
            writer.flush();

            String str;
            while ((str = reader.readLine()) != null)
                System.out.println(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("close client");

    }
}
