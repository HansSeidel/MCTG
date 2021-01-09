package bif3.swe.if20b211.mctg;

import bif3.swe.if20b211.colores.ConsoleColors;
import bif3.swe.if20b211.http.Format;
import bif3.swe.if20b211.mctg.models.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.HashMap;

public class ClientConsoleHandler {
    private BufferedReader clientBR;
    private User user = null;
    private String mctgPath = "/api/mctg/";
    private Format.Http_Method method;
    private String requestPath;
    private String body;
    private String mimeType;
    private String[] args;

    public ClientConsoleHandler (){
        this.clientBR = new BufferedReader(new InputStreamReader(System.in));
    }

    public String getInput() throws IOException {
        return this.clientBR.readLine();
    }

    public void printColoredMessageLn(String colorCode, String message){
        System.out.println(String.format("%s%s",colorCode,message));
    }

    public User register(){
        return null;
    }

    public void welcomeMessage() {
        printColoredMessageLn(ConsoleColors.GREEN,"Welcome to the Monstercard Trading Game");
        colorReset();
    }

    public boolean isUserLoggedIn() {
        if(this.user == null) return false; //To prevent NullPointerException
        return this.user.isLoggedIn();
    }

    public void logInOrRegister() throws IOException {

        printColoredMessageLn(ConsoleColors.BLUE, "Please enter either login or register.");
        this.colorReset();
        String credintials[] = this.getInput().trim().split(" ");
        if(credintials.length > 4){
            this.wrongInput("Too many input Parameter.");
            logInOrRegister(); //Recursive call if input has been wrong.
            return;
        }
        if(!(credintials[0].toLowerCase().equals("register") || credintials[0].toLowerCase().equals("login"))) {
            this.wrongInput("You have to be logged to use this application.");
            logInOrRegister(); //Recursive call if input has been wrong.
            return;
        }
        if(credintials.length < 2){
            printColoredMessageLn(ConsoleColors.BLUE,"Enter your username");
            credintials = new String[]{credintials[0], this.getInput()};
        }
        if(credintials.length < 3){
            printColoredMessageLn(ConsoleColors.BLUE,"Please enter your password");
            credintials = new String[]{credintials[0],credintials[1], this.getInput()};
        }
        this.user = new User(credintials[1],credintials[2]);

        if(credintials.length < 4 && credintials[0].toLowerCase().equals("register")){
            printColoredMessageLn(ConsoleColors.BLUE,"Please verify you password");
            if(!getInput().equals(credintials[2])){
                wrongInput("The verification failed.");
                logInOrRegister(); //Recursive call if input has been wrong.
                return;
            }
        }
        this.prepareStatement(Format.Http_Method.GET,credintials[0].toLowerCase(),null,
                "un",this.user.getUsername(),this.user.getPassword());
    }

    private void prepareStatement(Format.Http_Method method, String command,String body, String ... args) {
        this.method = method;
        this.requestPath = String.format("%s%s",this.mctgPath, command);
        this.body = body;
        this.mimeType = "application/Json";
        this.args = args;
    }

    private void colorReset() {
        System.out.printf("%s",ConsoleColors.RESET);
    }

    private void wrongInput(String message) {
        printColoredMessageLn(ConsoleColors.RED, String.format("ERROR APPEARED: %s\n%s",message,
                "Enter help to see all commands"));
        this.colorReset();
    }

    //Getter and Setters.
    public Format.Http_Method getMethod() {
        return method;
    }

    public String getRequestPath() {
        return requestPath;
    }

    public String getBody() {
        return body;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String[] getArgs() {
        return args;
    }
}
