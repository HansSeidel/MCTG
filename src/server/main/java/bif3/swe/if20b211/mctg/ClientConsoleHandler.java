package bif3.swe.if20b211.mctg;

import bif3.swe.if20b211.mctg.models.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ClientConsoleHandler {
    private BufferedReader clientBR;

    public ClientConsoleHandler (){
        this.clientBR = new BufferedReader(new InputStreamReader(System.in));
    }

    public String getInput() throws IOException {
        return this.clientBR.readLine();
    }

    public void printColoredMessage(String colorCode, String message){
        System.out.println(String.format("%s%s",colorCode,message));
    }

    public User register(){
        return null;
    }
}
