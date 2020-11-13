package bif3.swe.if20b211.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.URLEncoder;
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
    private int sTimeout = 2000;

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
    private String customHostForNextRequest = null;

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

    /**
     * This function adds optional HEADER information:
     *   - format_example: optionalHeader({"Connection: ","keep-alive"},{"auth-token: ", "dh2k3l4kjjasdf8894ngfk"})
     * @param optionalHeaders
     */
    public void addOptionalHeader(String[] ... optionalHeaders) {
        for(String[] header : optionalHeaders){
            this.optionalHeader.put(header[0],header[1]);
        }
    }

    public HashMap<String, String> getOptionalHeaders(){
        return this.optionalHeader;
    }
    public HashMap<String,String> getDefaultHeaders(){
        return this.defaultHeader;
    }

    /**
     * Gives all Headers in JSON Format as String
     * @return String
     */
    public String getAllHeaders(){
        String result = "{\"headers: \": {";
        HashMap<String ,String> allHeaders = new HashMap<String,String>();
        allHeaders.putAll(this.defaultHeader);
        allHeaders.putAll(this.optionalHeader);
        Iterator it = allHeaders.entrySet().iterator();
        for(Map.Entry pair = (Map.Entry)it.next();it.hasNext();pair = (Map.Entry)it.next()){
            result += String.format("\"%s\":\"%s\"",pair.getKey().toString(), pair.getValue().toString());
            result += it.hasNext()?",":"}}";
            it.remove();
        }
        return result;
    }

    /**
     * Initializes a http Handler for a sepcific Website or Server, on a specific port.
     * Optional add a HasMap<String, String> for HeaderOptions of each Request.
     * If you not add headerOption, a default Header is generated (Hardcoded).
     * You may get Header Options with getHeader functions:
     *   - getAllHeaders(); -returns String
     *   - getDefaultHeaders(); -returns HasMap<String, String>
     *   - getOptionalHeaders(); -returns HasMap<String, String>
     *
     * @param host
     * @param port
     */
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

    /**
     * Proceeds a HTTTP/1.1 GET_Request on the objects defined host.
     * Optional parameter path directs to a specific entry. Path will be to the domain. <www.example.api.com><path>
     * Optional parameters args will be appended to the path as arguments <www.exampl.api.com><path>?<args>
     * GET(); - Returns response in form ot String of the root directory of the defined HOST
     * GET("/"); - Same as GET();
     * GET("/sub"); - Returns the response of the get request on the subdirectory (http://www.example.api.com/sub)
     * GET("/sub","foo=bar","foo2=bar2"); - Returns the response on the subd. with the parameters foo and bar set (http://www.example.api.com/sub?foo=bar&foo2=bar2)
     *
     * @param path
     * @param args
     * @return String
     * @throws IOException
     */
    public String GET(String path, String ... args) throws IOException {
        String response = "";
        socket = new Socket(host,port);
        socket.setSoTimeout(sTimeout); //TODO Get settimeout out of this function
        SimpleBufferedWriter writer = new SimpleBufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        //Define Request
        String hostHeader = "Host: " + (customHostForNextRequest == null? host:customHostForNextRequest);
        customHostForNextRequest = null;
        //Add params if defined
        String arguments = "";
        for(String arg : args) arguments += arg + "&";
        //Define complete Request
        String httpRequest = String.format("GET %s%s HTTP/1.1",path,args.length > 0? ("?"+arguments.substring(0,arguments.length()-1)):arguments);
        writer.write(false,httpRequest,hostHeader);

        //Write Head data
        HashMap<String,String> allHeaders = new HashMap<String, String>();
        allHeaders.putAll(defaultHeader);
        allHeaders.putAll(optionalHeader);
        Iterator it = allHeaders.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry pair = (Map.Entry)it.next();
            writer.write(false,(pair.getKey().toString() + pair.getValue().toString()));
            it.remove(); // avoids a ConcurrentModificationException
        }

        //Declare end of HEAD
        writer.newLine();
        //Add body if defined

        writer.flush();

        //Read headers
        String t = br.readLine();
        int cl = 0;
        while(!t.isEmpty()){
            response += t + "\n";
            //Directly save Content-Length
            if(t.indexOf(":") != -1)
                if(t.substring(0,t.indexOf(":")).equals("Content-Length")) cl = Integer.parseInt(t.substring(t.indexOf(":")+1).trim());
            t = br.readLine();
        }
        //Adding seperater between HEAD and BODY
        response += "\n";
        //Read body
        //first part of condition skips the line break. t goes out of the upper loop with the value: \n
        //Then going on to the body until the body is null (If now body, it will skip.
        /*
        char[] buf = new char[cl];
        System.out.println(br.read(buf));
        for(int i = 0; i < cl; i++)
            response += buf[i];
            buf = null;
        */
        int read_chars = 0;
        while((t = br.readLine()) != null){
            response += t + "\n";
            read_chars += t.length()+1; //plus one because line break isn't count as length but is counted inside Content-Length
            System.out.println(read_chars +" - " + cl);
            if(read_chars >= cl) break;
        }
        br.close();
        writer.close();

        System.out.println("Finished GET-Request");
        return response;
    }
    public String GET() throws IOException {
        return GET("/");
    }



    public int getSocketTimeout() {
        return sTimeout;
    }
    public void setSocketTimeout(int ms) {
        sTimeout=ms;
    }

    public void setHost(String host){
        this.host = host;
    }
    public void setCustomHostForNextRequest(String customHostForNextRequest){
        this.customHostForNextRequest = customHostForNextRequest;
    }
}
