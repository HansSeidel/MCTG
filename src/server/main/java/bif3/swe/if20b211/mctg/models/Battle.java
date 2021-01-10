package bif3.swe.if20b211.mctg.models;

import java.util.Collections;
import java.util.List;

public class Battle {
    private final List<Card> deck_attacker;
    private final List<Card> deck_opponent;
    private final User attacker;
    private final User opponent;
    private int rounds;
    private String log = "";
    private User winner = null;

    public Battle(User attacker, User opponent, List<Card> deck_attacker, List<Card> deck_opponent) {
        this.attacker = attacker;
        this.opponent = opponent;
        this.deck_attacker = deck_attacker;
        this.deck_opponent = deck_opponent;
        this.fight();
    }

    private void fight() {
        log(String.format("\t%s\t\tvs\t\t%s\n",attacker.getUsername(),opponent.getUsername()));
        System.out.print("Username of opponent: " + opponent.getUsername());
        if(this.deck_attacker.size() > this.deck_opponent.size()){
            int i = 0;
            for (Card card:deck_attacker) {
                if(deck_opponent.size() > i){
                    log(String.format("\t-%s\t\t\t\t-%s\n", card.getCardname(),deck_opponent.get(i).getCardname()));
                }else {
                    log(String.format("\t-%s\t\t\t\t-%s\n", card.getCardname(),""));
                }
                i++;
            }
        }else{
            int i = 0;
            for (Card card:deck_opponent) {
                if(deck_attacker.size() > i){
                    log(String.format("\t-%s\t\t\t\t-%s\n", card.getCardname(),deck_attacker.get(i).getCardname()));
                }else {
                    log(String.format("\t-%s\t\t\t\t-%s\n", card.getCardname(),""));
                }
                i++;
            }
        }
        //Start rounds
        for(int i = 0; i < 100;i++){
            //get a random Card of each deck.
            //Already remove it from the deck.
            Card attacker_card = getRandomCard(deck_attacker);
            Card opponent_card = getRandomCard(deck_opponent);
            //Define a winnerand add both cards to winner deck
            if(attacker_card.isStronger(opponent_card) > 0){
                deck_attacker.add(attacker_card);
                deck_attacker.add(opponent_card);
                log(String.format("Round %d\t-%s\t\tvs\t\t-%s\t\t%s takes control\n",
                        i,attacker_card.getCardname(),opponent_card.getCardname(),attacker.getUsername()));
            }else if (attacker_card.isStronger(opponent_card) < 0){
                deck_opponent.add(attacker_card);
                deck_opponent.add(opponent_card);
                log(String.format("Round %d\t-%s\t\tvs\t\t-%s\t\t%s takes control\n",
                        i,attacker_card.getCardname(),opponent_card.getCardname(),opponent.getUsername()));
            }else {
                deck_attacker.add(attacker_card);
                deck_opponent.add(opponent_card);
                log(String.format("Round %d\t-%s\t\tvs\t\t-%s\t\tDRAW\n",
                        i,attacker_card.getCardname(),opponent_card.getCardname()));
            }
            if(gameFinished()){
                defineWinner();
                log(String.format("\t%s wins!",getWinnerName()));
                return;
            }
        }
        setDraw();
        log("\n\n\tDRAW");
    }

    private void setDraw() {
        this.winner = new User("DRAW","XXXXX");
    }

    private void log(String toLog) {
        this.log += toLog;
    }

    private void defineWinner() {
        this.winner = deck_attacker.isEmpty()? opponent:attacker;
    }

    private boolean gameFinished() {
        if(deck_opponent.isEmpty() || deck_attacker.isEmpty()) return true;
        return false;
    }

    private Card getRandomCard(List<Card> deck) {
        Collections.shuffle(deck);
        Card result = deck.get(0);
        deck.remove(result);
        return result;
    }

    public String getLog(){
        return this.log;
    }

    public String getWinnerName() {
        return this.winner.getUsername();
    }

    public User getWinner(){
        return this.winner;
    }
}
