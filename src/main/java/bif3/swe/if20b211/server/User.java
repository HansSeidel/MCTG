package bif3.swe.if20b211.server;

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
    }

    public void addCardToStack(Card ... card) {
    }
    public void removeCardFromStack(Card ... card){

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
