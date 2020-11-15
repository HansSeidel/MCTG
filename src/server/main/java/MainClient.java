import bif3.swe.if20b211.api.MyHttpHandler;
import bif3.swe.if20b211.api.SimpleBufferedWriter;
import bif3.swe.if20b211.http.Format;

import javax.net.ssl.HostnameVerifier;
import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.IllegalFormatException;

public class MainClient {
    public static void main(String[] args) {
        System.out.println("start client");
        final String HOST = "localhost";
        final int PORT = 8000;
        try{
            Socket s = new Socket(HOST,PORT);
            while (true){
                //TODO Write following use cases:
                String command[] = null;
                do{
                    command = getCommandFormatFromConsole();
                }while (command == null);
                if(command[0].equals("quit")) break;

                Format.Http_Method method = Format.Http_Method.GET;
                if(command[0].equals("struct") || command[0].equals("list"))method= Format.Http_Method.GET;
                if(command[0].equals("send"))method= Format.Http_Method.POST;
                if(command[0].equals("update"))method= Format.Http_Method.PATCH;
                if(command[0].equals("delete"))method= Format.Http_Method.DELETE;
                System.out.println("command equals: " + Arrays.toString(command));
                //Method,path,host,port
                Format request;
                try{
                    if(command.length == 2){
                        System.out.println("No Body");
                        request = new Format(method, HOST,command[1],null,null);
                    }else if(command.length == 3){
                        request = new Format(method,HOST,command[1],command[2],"application/json");
                    }else {
                        System.err.println("IndexRange is wrong:");
                        //Force unhandled error
                        command[3] = null;
                        request = null;
                    }
                }catch (IllegalArgumentException e){
                    System.out.println("Unkwon requets");
                    return;
                }
                request.buildFormat();
                String final_request = request.BARE_STRING;
                System.out.println("request is: ----------- " + final_request);
                write(final_request,s);
                System.out.println("Response: " + read(s));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("close client");

    }

    private static String read(Socket s) throws IOException {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            return null;
        }
        String res = "";
        String next = "";
        boolean body = true;
        BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
        if(!reader.ready()){
            return null;
        }
        next = reader.readLine();
        System.out.println("Still Reading");
        //First loop is reading until first blank line occurs
        if(next == null) return null;
        while (!next.isEmpty()){
            res += next+"\n";
            System.out.println("next: " + next);
            if(!reader.ready()) break;
            next = reader.readLine();
        }
        System.out.println("Still Reading");

        //Ready is waiting a short period of time and checking if there is some readable text behind the blank line.
        res += "\n";
        if(!reader.ready()){
            body = false;
        }
        System.out.println("Still Reading");

        //Reading out the rest of the incoming message
        if(body){
            while((next = reader.readLine()) != null){
                res += next + "\n";
                System.out.println("next is : "+next);
                if(!reader.ready()){
                    break;
                }
            }
        }

        System.out.println("Res: " + res.trim());
        return res.trim();
    }

    private static void write(String response, Socket s) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
        System.out.println("Writing message");
        writer.write(response);
        writer.newLine();
        writer.flush();
    }

    /**
     *
     * @return String[] in Format [0]-command, [1]-path, [2]-body||null;
     * @throws IOException
     */
    private static String[] getCommandFormatFromConsole() throws IOException {
        String message_path = "/messages/";
        String api_structure_path = "/api/structure";

        System.out.println("Enter your command: <struct, list, send, list x, update x, delete x>");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String command = br.readLine().trim().toLowerCase();
        if(command.equals("struct")){
            return new String[]{"list",api_structure_path};
        }else if(command.startsWith("list")){
            //Check if user wants to list all messages
            boolean testings[] = testCommands(command,4,"list",true,true,true);
            if(testings[0]){
                System.out.println(String.format("Returning the following: {%s},{%s}",command,message_path));
                return new String[] {command,message_path};
            }
            if(!testings[1]||!testings[2]) {
                System.out.println("Returning null");
                return null;
            }
            System.out.println("Returning the following: " + message_path + command.split(" ")[1]);
            return new String[] {command.split(" ")[0],(message_path + command.split(" ")[1])};
        }else if (command.equals("send")){
            System.out.println("Enter senders name(max chars. 255):");
            String name = br.readLine().trim();
            if(name.isEmpty() || name == null || name.length() > 255){
                System.out.println("You have either entered nothing or wrote to many characters. Command aborted");
                return null;
            }
            System.out.println("Enter your message (For multiline bif3.swe.if20b211.api.Messages enter \"mlm\"):");
            String msg = br.readLine().trim();
            if(msg.equalsIgnoreCase("mlm")){
                msg = "";
                System.out.println("You activated multiline input. You may type your message line by line but it won't be used with line breaks.");
                boolean finished = false;
                do {
                    System.out.println("Enter your line or finish (lines get trimmed):");
                    String tmp_msg = br.readLine().trim().replace("\n","").replace("\t","");
                    if(tmp_msg.equals("finish")) break;
                    msg += tmp_msg;
                } while (true);
            }
            if(msg.isEmpty()) {
                System.out.println("Seems like you haven't entered a messages. Command aborted");
                return null;
            }
            return new String[] {command,message_path,String.format("{\n\t\"sender\":\"%s\",\n\t\"message\": \"%s\"\n}",name,msg)};
        }else if (command.startsWith("update")){
            boolean updateName;
            boolean updateMessage;
            boolean testings[] = testCommands(command,6,"update",true,true,true);

            if(testings[0]||!testings[1]||!testings[2]) return null;

            String[] command_number = command.split(" ");

            System.out.println("Enter senders name(max chars. 255) or null or blank enter if you don't want to update the name:");
            String name = br.readLine().trim();
            if(name.length() > 255){
                System.out.println("You have wrote to many characters. Command aborted");
                return null;
            }
            updateName = !(name.isEmpty() || name.equalsIgnoreCase("null"));

            System.out.println("Enter your message or null or blank if you don't want to update the message (For multiline bif3.swe.if20b211.api.Messages enter \"mlm\"):");
            String msg = br.readLine().trim();
            updateMessage = !(msg.isEmpty() || msg.equals("null"));
            if(updateMessage){
                if(msg.equalsIgnoreCase("mlm")){
                    msg = "";
                    System.out.println("You activated multiline input. You may type your message line by line but it won't be used with line breaks.");
                    boolean finished = false;
                    do {
                        System.out.println("Enter your line or finish (lines get trimmed):");
                        String tmp_msg = br.readLine().trim().replace("\n","").replace("\t","");
                        if(tmp_msg.equals("finish")) break;
                        msg += tmp_msg;
                    } while (true);
                }
                if(msg.isEmpty()) {
                    System.out.println("Seems like you haven't entered a messages. Command aborted");
                    return null;
                }
            }
            if(!updateMessage && !updateName){
                System.out.println("Seems like you won't change anything. Discarding command.");
                return null;
            }
            return new String[] {command.split(" ")[0],(message_path+command_number[1]),"{" +
                    (updateName?String.format("\n\t\"sender\":\"%s\"%s",name,updateMessage?",":""):"") +
                    (updateMessage?String.format("\n\t\"message\": \"%s\"",msg):"") +
                    "\n}"};
        }else if(command.startsWith("delete")){
            boolean testings[] = testCommands(command,6,"delete",true,true,true);
            if(testings[0]||!testings[1]||!testings[2]) return null;

            return new String[]{command.split(" ")[0],message_path+command.split(" ")[1]};
        }else if(command.equals("quit")){
            br.close();
            return new String[]{"quit"};
        }else{
            System.out.println("Unknown command. Write quit to exit connection");
            return null;
        }
    }


    /**
     * Returning three bool parameters:<p></p>
     *      First bool returns true if the input String is the exact same length as the expected_length.<p>
     *          If first bool is true, second and third bool aren't checked. They will return false without errormessage.
     *      Second bool returns true if the input String consists of the format: "input number"<p>
     *      Third bool returns true if the expected number is parseable.<p>
     *      Using correct_command to write the error messages.
     *      Decide on each test [0]-[2] if you want to print the message (... print_messages)
     * @param input
     * @param expected_length
     * @param correct_command also prints out errorhandling to the console. If you leave this empty, it won't print anything
     * @param print_messages indicates which test shell print an errormessage.
     * @return
     */
    private static boolean[] testCommands(String input, int expected_length, String correct_command, boolean ... print_messages) {
        boolean testings[] = new boolean[]{false,true,true};
        if(print_messages.length > 3) {
            System.err.println("Internal error");
            throw new RuntimeException();
        }
        if(input.length() == expected_length) {
            if(print_messages[0])System.out.println("Seems you've written the command wrong. Similar commands: <"+correct_command+" n>");
            testings[0] = true;
            testings[1] = false;
            testings[2] = false;
        }else{
            String[] command_number = input.split(" ");
            if(command_number.length != 2){
                if(print_messages[1])System.out.println("Seems you've written the command wrong. Similar commands: <"+correct_command+"> <"+correct_command+" n>");
                testings[1] = false;
            }
            try{
                Integer.parseInt(command_number[1]);
            }catch(NumberFormatException e){
                if(print_messages[2])System.err.println("Expected <"+correct_command+" x> x to be type of Integer. Got another type.");
                testings[2] = false;
            }
        }
        return testings;
    }

    /**
     * Returning three bool parameters:<p></p>
     *      First bool returns true if the input String is the exact same length as the expected_length.<p>
     *      Second bool returns true if the input String consists of the format: "input number"<p>
     *      Third bool returns true if the expected number is parseable.<p>
     * @param input
     * @param expected_length
     * @return
     */
    private static boolean[] testCommands(String input, int expected_length){
        return testCommands(input, expected_length,null,false,false,false);
    }
}
