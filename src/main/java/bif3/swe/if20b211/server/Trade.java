package bif3.swe.if20b211.server;

public class Trade {
    private Offer a;
    private Offer b;

    public Trade(Offer a, Offer b){
        this.a = a;
        this.b = b;
    }

    public void submitTrade(Offer a, Offer b){
        check_for_Trade_possible();
    }
    public void deleteTrade(){

    }
    public void removeOffer(Offer removeable){

    }
    public boolean check_for_Trade_possible(){
        boolean doable = false;
        //TODO Check if it's stack or Deck
        Card[] avaible = a.user.getDeck();
        Card[] offer = a.getCards();
        for(int i = 0; i < 2; i++){
            for(Card o_card:offer){
                for(Card a_card:avaible){
                    if(a_card.equals(o_card)){
                        doable = true;
                        break;
                    }
                }
                if(!doable) return doable;
                doable = !doable;
            }
            avaible = b.user.getDeck();
            offer = b.getCards();
        }
        return true;
    }
}
