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

        String httpServer = "postman-echo.com";
        String httpRequest = "GET /headers HTTP/1.1";
        String hostHeader = "Host: " + httpServer;
        //String accept = "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9";
        //String userAgent = "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.111 Safari/537.36";
        String connection = "Connection: Close";


        try (Socket socket = new Socket(httpServer, 80);
            SimpleBufferedWriter writer = new SimpleBufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {

            writer.write(false,httpRequest,hostHeader,connection);
            writer.newLine();
            writer.flush();

            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String t;
            while((t = br.readLine()) != null) System.out.println(t);
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("close client");

    }
}
