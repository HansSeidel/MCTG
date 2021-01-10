package bif3.swe.if20b211.mctg;

import bif3.swe.if20b211.colores.ConsoleColors;
import bif3.swe.if20b211.http.Format;
import bif3.swe.if20b211.http.Json_form;
import bif3.swe.if20b211.mctg.models.Card;
import bif3.swe.if20b211.mctg.models.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ClientConsoleHandler {
    private boolean sendAbel;
    private BufferedReader clientBR;
    private User user = null;
    private String mctgPath = "/api/mctg/";
    private Format.Http_Method method;
    private String requestPath;
    private String body;
    private String mimeType;
    private String[] args;
    private String token;

    /**
     * Handles all interaction between the user and the console according to the mctg-project.
     * With an Instance of this class you may also prepare a http-request (For the Format-Class) and
     * read the response properly.
     */
    public ClientConsoleHandler (){
        this.clientBR = new BufferedReader(new InputStreamReader(System.in));
    }

    private void printColoredMessageLn(String colorCode, String message){System.out.println(String.format("%s%s",colorCode,message)); }
    private void colorReset() {
        System.out.printf("%s",ConsoleColors.RESET);
    }
    private String getInput() throws IOException {return this.clientBR.readLine(); }
    public void wrongInput(String message) {
        this.sendAbel = false;
        printColoredMessageLn(ConsoleColors.RED, String.format("ERROR APPEARED: %s\n%s",message,
                "Enter help to see all commands"));
        this.colorReset();
    }



    /**
     * This simply request a input of the user
     * @return a String from Console-Input
     * @throws IOException
     */
    public String requestInput() throws IOException {
        printColoredMessageLn(ConsoleColors.BLUE,"Enter a command or \"quit\" to exit");
        colorReset();
        return this.getInput().trim();
    }

    /**
     * Delivers a welcome Message to the user.
     */
    public void welcomeMessage() {
        printColoredMessageLn(ConsoleColors.GREEN,"Welcome to the Monstercard Trading Game");
        colorReset();
    }

    /**
     * Checks if the user token is set or not.
     * @return True - If the user token is set. Otherwise it will return false.
     */
    public boolean isUserLoggedIn() {
        if(this.user == null) return false; //To prevent NullPointerException
        return this.user.isLoggedIn();
    }

    /**
     * Manages the login or register procedure of the user.
     * @throws IOException
     */
    public void logInOrRegister() throws IOException {
        printColoredMessageLn(ConsoleColors.BLUE, "Please enter either login or register.");
        this.colorReset();
        String[] credentials = this.getInput().trim().split(" ");
        if(credentials.length > 4){
            this.wrongInput("Too many input Parameter.");
            this.colorReset();
            this.sendAbel = false;
            return;
        }
        if(!(credentials[0].toLowerCase().equals("register") || credentials[0].toLowerCase().equals("login"))) {
            this.wrongInput("You have to be logged to use this application.");
            this.colorReset();
            this.sendAbel = false;
            return;
        }
        if(credentials.length < 2){
            printColoredMessageLn(ConsoleColors.BLUE,"Enter your username");
            credentials = new String[]{credentials[0], this.getInput()};
        }
        if(credentials.length < 3){
            printColoredMessageLn(ConsoleColors.BLUE,"Please enter your password");
            credentials = new String[]{credentials[0],credentials[1], this.getInput()};
        }
        this.user = new User(credentials[1],credentials[2]);

        if(credentials.length < 4 && credentials[0].toLowerCase().equals("register")){
            printColoredMessageLn(ConsoleColors.BLUE,"Please verify you password");
            if(!getInput().equals(credentials[2])){
                wrongInput("The verification failed.");
                this.colorReset();
                this.sendAbel = false;
                return;
            }
        }
        this.prepareStatement(Format.Http_Method.GET,
                credentials[0].toLowerCase(),
                String.format("{\"username\":\"%s\",\"password\":\"%s\"}",
                this.user.getUsername(),this.user.getPassword()),null);
        this.user.clearPassword();
        this.colorReset();
    }

    public void acquirePackage(String userCommand) throws IOException {
        int amount = 0;
        if(userCommand.equals("buy package")){
            amount = 1;
            //With trim() and the space at the end of "buy package ",
            //IndexOutOfBoundsException is prevented.
        }else if(userCommand.trim().startsWith("buy package ")){
            try{
                amount = Integer.parseInt(userCommand.split(" ")[2]);
            }catch (NumberFormatException e){
                wrongInput("Expected a Number as last parameter.");
                this.colorReset();
                this.sendAbel = false;
                return;
            }
        }
        printColoredMessageLn(ConsoleColors.BLUE,
                String.format("Do you want to buy %d package[s]? Enter ['y'/'n']",amount));
        if(this.getInput().equals("y")){
            this.prepareStatement(Format.Http_Method.GET,"order",null,
                    String.format("amount=%d",amount));
        }else {
            this.sendAbel = false;
            this.printColoredMessageLn(ConsoleColors.YELLOW,"Canceled order");
        }
        this.colorReset();
    }

    public void manageDeck(String userCommand) {
        String command;
        String cardname;
        if(userCommand.trim().equals("deck show")){
            this.prepareStatement(Format.Http_Method.GET,"deck",null,"action=show");
            return;
        }
        if(userCommand.trim().startsWith("deck add ") || userCommand.trim().startsWith("deck remove ")){
            cardname = userCommand.split( " ")[2];
        }else {
            printColoredMessageLn(ConsoleColors.BLUE,"Please enter a cards name");
            try{
                cardname = getInput();
            }catch (IOException e){
                wrongInput("Too many Parameters.");
                colorReset();
                this.sendAbel = false;
                return;
            }
        }
        this.prepareStatement(Format.Http_Method.POST,"deck", null,
                String.format("cardname=%s",cardname),String.format("action=%s",userCommand.split(" ")[1]));
    }

    public void startBattle() throws IOException {
        printColoredMessageLn(ConsoleColors.BLUE,"You about to start a battle. Press [y] to start.");
        if(getInput().equals("y")){
            prepareStatement(Format.Http_Method.GET,"battle",null,null);
        }else{
            colorReset();
            this.sendAbel = false;
        }
    }

    /**
     * This function prepares all properties to be ready for http-transmission
     * @param method Format.Http_Method
     * @param command String
     * @param body - String body in Json format.
     * @param args - in Format: "name=argument","name=argument",...
     */
    private void prepareStatement(Format.Http_Method method, String command,String body, String ... args) {
        this.method = method;
        this.requestPath = String.format("%s%s",this.mctgPath, command);
        this.body = body;
        this.mimeType = "application/Json";
        this.args = args;
        if(this.isUserLoggedIn()) this.token = this.user.getToken();
    }

    /**
     * This handles all incomes, relevant for the mctg-project.
     * It also clears all arguments of (this)
     * @param read
     */
    public void handleResponse(Format read) throws InterruptedException {
        this.setArgs(null);
        boolean isNoError = read.getStatus() >= 200 && read.getStatus() < 300;
        String model = read.getValueOfStringHashMap(read.getHeaders(),"model");
        if(!isNoError || model == null){
            try {
                printColoredMessageLn(ConsoleColors.RED,
                        String.format("Received error message: %s%n",
                                read.getBody().getJson_format().get("error_message")));
            } catch (IOException e) {
                printColoredMessageLn(ConsoleColors.RED,
                        String.format("ERROR-MESSAGE IS NOT READABLE: %s",e.toString()));
            } finally {
                this.colorReset();
                return;
            }
        }
        if(model.equals("user")) {
            this.user.setToken(read.getValueOfStringHashMap(read.getHeaders(),"token"));
            if(read.getStatus() == 200) printColoredMessageLn(ConsoleColors.BLUE,"Logged in");
            if(read.getStatus() == 201) printColoredMessageLn(ConsoleColors.BLUE,"Registered and Logged in");
            if(read.getStatus() == 205) printColoredMessageLn(ConsoleColors.BLUE, "Logged out");
        }
        if(model.equals("cards") || model.equals(("deck"))) {
            try {
                if(model.equals("cards")){
                    printColoredMessageLn(ConsoleColors.BLUE,"You recieved the following cards:");
                    if(read.getBody().toString().equals("{}")){
                        printColoredMessageLn(ConsoleColors.RED,"Sorry - can't show this cards correctly");
                        colorReset();
                        return;
                    }
                }else {
                    printColoredMessageLn(ConsoleColors.BLUE,"Your deck contains the following cards:");
                    if(read.getBody().toString().equals("{}")){
                        printColoredMessageLn(ConsoleColors.BLUE,"You have no cards in your deck.");
                        colorReset();
                        return;
                    }
                }
                JsonNode node = read.getBody().getJson_format();
                node.elements().forEachRemaining(el -> {
                    Card card = null;
                    try {
                        card = Json_form.fromJson(el,Card.class);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    String color = ConsoleColors.BLUE_BRIGHT;
                    if(card.getOccurance().equals("legendary")) color = ConsoleColors.YELLOW_BOLD_BRIGHT;
                    if(card.getOccurance().equals("epic")) color = ConsoleColors.PURPLE;
                    printColoredMessageLn(color,String.format("Cardname: %s, damage: %d, Card is a %s from type %s, Occurrence: %s",
                            card.getCardname(),card.getDamage(),
                            card.getIs_a(),card.getCardType(),
                            card.getOccurance()));
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
            colorReset();
        }
        if(model.equals("battle")){
            String winner = read.getValueOfStringHashMap(read.getHeaders(),"winner");
            boolean won = user.getUsername().equals(winner);
            if(won){
                printColoredMessageLn(ConsoleColors.GREEN_BOLD,String.format("%s won!",winner));
            }else {
                if (winner.equals("DRAW")){
                    printColoredMessageLn(ConsoleColors.BLUE,"DRAW");
                }else {
                    printColoredMessageLn(ConsoleColors.RED,String.format("%s won :/",winner));
                }
            }
            printColoredMessageLn(ConsoleColors.BLUE,"Recieving log...");
            printColoredMessageLn(ConsoleColors.BLUE_BOLD,read.getBody().toString());
        }
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

    private void setArgs(String[] args) {
        this.args = args;
    }

    public String[] getArgs() {
        return args;
    }

    public String getToken() {
        return token;
    }

    public boolean sendAbel() {
        return this.sendAbel;
    }
    public void resetSendable(){
        this.sendAbel = true;
    }

}
