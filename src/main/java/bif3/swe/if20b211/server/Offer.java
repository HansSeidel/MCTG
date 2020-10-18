package bif3.swe.if20b211.server;

public class Offer {
    public User user;
    private Card[] cards;

    public Offer(User a, Card ... cards){
        this.user = a;
        this.cards = cards;
    }

    public Card[] getCards() {
        return cards;
    }

    public void setCards(Card[] cards) {
    }

    public void addCardToCards(Card ... card) {
    }

    public void removeCardFromCards(Card ... card){

    }

    public void changeCards(Card a, Card b){
        removeCardFromCards(a);
        addCardToCards(b);
    }

    public void removeOffer(){
    }
}
