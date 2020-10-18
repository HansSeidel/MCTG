package bif3.swe.if20b211.server;

import java.util.Date;

public class Battle {
    private final Date MATCH_BEGIN;
    private User a;
    private User b;
    private int move_time = 0;
    private int healthA;
    private int healthB;

    public Battle(User a, User b){
        this.a = a;
        this.b = b;
        this.MATCH_BEGIN = new Date();
    }

    //This is not the actually coed for this method. It's just som pseudocode to remember the idea
    public void action(User playing, Card c){
        boolean actionFromA = (playing == a);
        if(actionFromA){
            healthB -= c.getDamage();
            if (healthB <= 0) game_finished(b,a);
        }else{
            healthA -= c.getDamage();
            if (healthA <= 0) game_finished(b,a);
        }
    }

    private void game_finished(User winner, User looser){

    }

    private void safe_match(){

    }

}
