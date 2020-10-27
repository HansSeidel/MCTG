package bif3.swe.if20b211.api;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * TODO Write documentation
 * Handles the CRUD function for a specified server. Connections are established and closed by each CRUD Method self.
 */
public class MyHttpHandler {
    //This Method should be clientSided and normally is build with JavaScript

    //private static <Type> metadata;
    //CREATE OBJECT BUILDER!!
    //Socket variable
    private Socket socket;

    //Necessary Variables
    private String host;
    private int port;

    //Header default information
    //Should be automatically took by the program. For now, they are just included by an default set and can be overwritten.
    private HashMap<String,String> defaultHeader = new HashMap<String, String>();
    String[][] defaultHeaderStringArray = {
            {"Accept: ","text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9"},
            {"Accept-Encoding: ","gzip, deflate"},
            {"Accept-Language: ","de-DE,de;q=0.9,en-US;q=0.8,en;q=0.7"},
            {"Cache-Control: ","max-age=0"},
            {"Connection: ", "Close"},
            {"Upgrade-Insecure-Requests: ", "1"},
            {"User-Agent: ", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.111 Safari/537.36"}
    };

    private HashMap<String,String> optionalHeader = new HashMap<String,String>();

    /**
     *
     * This method fills this.defaultHeader with either the default values, defined by defaultHeaderStringArray or
     * with the passed Values. The passed values are expected to be in format HashMap<String,String>.
     * For parameters not mathing the default Keys, it will be passed as optional parameters.
     * @param dh
     */
    private void fillDefaultHeaderInformation(HashMap<String,String> dh){
        for (String[] defaultHeader : defaultHeaderStringArray){
            if(!dh.isEmpty() && dh.containsKey(defaultHeader[0])){
                this.defaultHeader.put(defaultHeader[0],dh.get(defaultHeader[0]));
                dh.remove(defaultHeader[0]);
            }else{
                this.defaultHeader.put(defaultHeader[0],defaultHeader[1]);
            }
            if(!dh.isEmpty()) addOptionalHeader(dh);
        }
    }

    public void setOptionalHeader(HashMap<String ,String> optionalHeader){
        this.optionalHeader = optionalHeader;
    }

    public void addOptionalHeader(HashMap<String,String> optionalHeader){
        Iterator it = optionalHeader.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry pair = (Map.Entry)it.next();
            this.optionalHeader.put(pair.getKey().toString(),pair.getValue().toString());
            it.remove(); // avoids a ConcurrentModificationException
        }
    }

    //TODO Add error handling for to many Arguments in String Array
    public void addOptionalHeader(String[] ... optionalHeaders) {
        for(String[] header : optionalHeaders){
            this.optionalHeader.put(header[0],header[1]);
        }
    }

    public HashMap<String, String> getOptionalHeader(){
        return optionalHeader;
    }

    public MyHttpHandler(String host, int port){
        this.host = host;
        this.port = port;
        fillDefaultHeaderInformation(this.defaultHeader);
    }

    public MyHttpHandler(String host, int port, HashMap<String,String> defaultHeader){
        this.host = host;
        this.port = port;
        fillDefaultHeaderInformation(defaultHeader);
    }

    //TODO Think about really going this way and not putting host and port into the arguments
    public String GET(String path) throws IOException {
        String response = "";
        socket = new Socket(host,port);
        socket.setSoTimeout(2000); //TODO Get settimeout out of this function
        SimpleBufferedWriter writer = new SimpleBufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        String hostHeader = "Host: " + host;
        String httpRequest = String.format("GET %s HTTP/1.1",path);
        writer.write(false,httpRequest,hostHeader);

        HashMap<String,String> allHeaders = new HashMap<String, String>();
        allHeaders.putAll(defaultHeader);
        allHeaders.putAll(optionalHeader);
        Iterator it = allHeaders.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry pair = (Map.Entry)it.next();
            writer.write(false,(pair.getKey().toString() + pair.getValue().toString()));
            it.remove(); // avoids a ConcurrentModificationException
        }

        writer.newLine();
        writer.flush();
        String t;
        while((t = br.readLine()) != null) response += t + "\n";
        br.close();
        writer.close();
        System.out.println("Finished GET-Request");
        return response;
    }

}
