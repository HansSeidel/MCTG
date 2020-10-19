package bif3.swe.if20b211.server;

import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.io.ObjectStreamField;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class User {
    private String username;
    private String password;
    private String auth_token;
    private Card[] stack;
    private Card[] deck;
    private int elo = 100;


    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setStack(Card[] cards) {
        stack = cards;
    }

    public void addCardToStack(Card ... cards) {
        int n = 0;
        Card[] result = new Card[stack.length+cards.length];
        for(Card card : stack) result[n++] = card;
        for(Card card : cards) result[n++] = card;
        stack = result;
    }

    public void removeCardFromStack(Card ... cards){
        for(Card card : cards){
            for(int i = 0; i < stack.length;i++){
                if(stack[i].equals(card)){
                    stack = removeIndexFromArray(stack,i);
                    break;
                }
            }
        }
    }

    private Card[] removeIndexFromArray(Card[] stack, int index) throws ArrayIndexOutOfBoundsException {
        //Check if input parameters are correct
        if (stack == null || index < 0 || index >= stack.length) return stack;

        //Create copy of array without deleted object
        Card[] result = new Card[stack.length-1];
        for(int i = 0;i < stack.length-1; i++) result[i] = stack[i<index?i:i+1];

        return result;
    }


    public void changeCard(Card a, Card b){
        removeCardFromStack(a);
        addCardToStack(b);
    }

    public Card[] getStack() {
        return stack;
    }

    public void setDeck(Card[] cards) {
    }

    public void addCardToDeck(Card ... card) {
    }
    public void removeCardFromDeck(Card ... card){

    }

    public void changeDeck(Card a, Card b){
        removeCardFromDeck(a);
        addCardToDeck(b);
    }

    public Card[] getDeck() {
        return deck;
    }
}
