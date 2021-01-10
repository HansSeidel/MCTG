package bif3.swe.if20b211.mctg.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.json.JSONPropertyIgnore;

public class Card {
    private String cardname;
    private int damage;
    private String cardType;
    private String is_a;
    private String occurance;

    @JsonIgnore
    public Card(String cardname, int damage, String cardType, String is_a, int occurance) {
        this.cardname = cardname;
        this.damage = damage;
        setCardType(cardType);
        setIs_a(is_a);
        setKind(occurance);
    }
    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public Card(@JsonProperty("cardname")String cardname,
                @JsonProperty("damage") int damage,
                @JsonProperty("cardType") String cardType,
                @JsonProperty("is_a") String is_a,
                @JsonProperty("occurance") String occurance) {
        this.cardname = cardname;
        this.damage = damage;
        setCardType(cardType);
        setIs_a(is_a);
        setOccurance(occurance);

    }

    public String getCardname() {
        return cardname;
    }

    public void setCardname(String cardname) {
        this.cardname = cardname;
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public String getCardType() { return cardType; }

    public void setCardType(String cardType) {
        if(cardType.equals("f")){
            this.cardType = "fire";
        }else if(cardType.equals("w")){
            this.cardType = "water";
        }else if(cardType.equals("n")){
            this.cardType = "normal";
        }else{
            this.cardType = cardType;
        }
    }

    public String getIs_a() {return is_a;}

    public void setIs_a(String is_a) {
        this.is_a = is_a.equals("m")?"monster":"spell";
    }

    public String getOccurance() {
        return occurance;
    }

    public void setOccurance(String occurance){
        this.occurance = occurance;
    }
    @JsonIgnore
    public void setKind(int occurance) {
        this.occurance = "legendary";
        if(occurance >= 5) this.occurance = "epic";
        if(occurance >= 10) this.occurance = "rare";
        if(occurance >= 30) this.occurance = "seldom";
        if(occurance >= 50) this.occurance = "normal";
    }
    @JsonIgnore
    public int isStronger(Card opponent_card) {
        Card att_final = new Card("NONE",0,"n","m",0);
        Card opp_final = new Card("NONE",0,"n","m",0);
        //Keep in mind that the order of the ifs is important.
        //Define goblin rule -------------------------------------
        if(this.getCardname().equals("goblin") || this.getCardname().equals("giant"))
            if(opponent_card.getCardname().equals("goblin")||opponent_card.getCardname().equals("giant"))
                return 0;
        if(this.getCardname().equals("goblin")) return -1;
        if(opponent_card.getCardname().equals("goblin")) return 1;

        //Define Knight rules -------------------------------------
        if(this.getCardname().equals("knight") && opponent_card.getIs_a().equals("spell")){
            if(opponent_card.getCardType().equals("water"))
                return -1; //Waterspell always wins against knight
            if(opponent_card.getCardType().equals("fire"))
                return this.getDamage() - (opponent_card.getDamage()/4);
        }
        if(opponent_card.getCardname().equals("knight") && this.getIs_a().equals("spell")){
            if(this.getCardType().equals("water"))
                return 1; //Waterspell always wins against knight
            if(opponent_card.getCardType().equals("fire"))
                return (this.getDamage()/4) - opponent_card.getDamage();
        }
        if(this.getCardname().equals("knight") && opponent_card.getCardname().equals("water_ork"))
            return this.getDamage()/4 - opponent_card.getDamage();
        if(this.getCardname().equals("knight") && opponent_card.getCardname().equals("fire_ork"))
            return this.getDamage() - opponent_card.getDamage()/4;
        if(opponent_card.getCardname().equals("knight") && this.getCardname().equals("water_ork"))
            return this.getDamage() - opponent_card.getDamage()/4;
        if(opponent_card.getCardname().equals("knight") && this.getCardname().equals("fire_ork"))
            return this.getDamage()/4 - opponent_card.getDamage();
        //Define ork rules (Monster vs Monster -------------------------------------
        //if((waterork or fireork) against normal monster
        if((this.getCardname().equals("fire_ork") || this.getCardname().equals("water_ork"))
                && opponent_card.getIs_a().equals("monster") && opponent_card.getCardType().equals("normal")){
            return (this.getDamage()+(opponent_card.getDamage()/2))-opponent_card.getDamage();
        }
        //if(Normal monster against (waterork or fireork))
        if((opponent_card.getCardname().equals("fire_ork") || opponent_card.getCardname().equals("water_ork"))
                && this.getIs_a().equals("monster") && this.getCardType().equals("normal")){
            return this.getDamage()-(opponent_card.getDamage()+(this.getDamage()/2));
        }
        //if fireork vs waterork
        if(this.getCardname().equals("fire_ork") && opponent_card.getCardname().equals("water_ork"))
            return ((this.getDamage()/2)-(opponent_card.getDamage()*2));
        //if waterork vs fireork
        if(this.getCardname().equals("water_ork") && opponent_card.getCardname().equals("fire_ork"))
            return ((this.getDamage()*2)-(opponent_card.getDamage()/2));

        if(this.getCardname().equals("kraken") && opponent_card.getIs_a().equals("spell"))
            return 1;
        if(opponent_card.getCardname().equals("kraken") && this.getIs_a().equals("spell"))
            return -1;

        //Definew wizard rules ----------------------------------
        //Define wizards normal
        if(this.getCardname().equals("wizard") && opponent_card.getIs_a().equals("spell")
                && opponent_card.getCardType().equals("normal"))
            return (Math.random()*100) <= 40? 1:this.getDamage()-opponent_card.getDamage()*2;
        if(this.getCardname().equals("wizard") && opponent_card.getIs_a().equals("spell"))
            return (Math.random()*100) <= 5? 1:this.getDamage()-opponent_card.getDamage()*2;
        //Define wizards fire
        if(this.getCardname().equals("fire_wizard") && opponent_card.getIs_a().equals("spell")
                && opponent_card.getCardType().equals("fire"))
            return (Math.random()*100) <= 40? 1:this.getDamage()-opponent_card.getDamage()*2;
        if(this.getCardname().equals("fire_wizard") && opponent_card.getIs_a().equals("spell"))
            return (Math.random()*100) <= 5? 1:this.getDamage()-opponent_card.getDamage();
        //Define wizards normal
        if(this.getCardname().equals("water_wizard") && opponent_card.getIs_a().equals("spell")
                && opponent_card.getCardType().equals("water"))
            return (Math.random()*100) <= 40? 1:this.getDamage()-opponent_card.getDamage()*2;
        if(this.getCardname().equals("water_wizard") && opponent_card.getIs_a().equals("spell"))
            return (Math.random()*100) <= 5? 1:this.getDamage()-opponent_card.getDamage()*2;

        //Define wizards normal
        if(opponent_card.getCardname().equals("wizard") && this.getIs_a().equals("spell")
                && this.getCardType().equals("normal"))
            return (Math.random()*100) <= 40? -1:this.getDamage()*2-opponent_card.getDamage();
        if(opponent_card.getCardname().equals("wizard") && this.getIs_a().equals("spell"))
            return (Math.random()*100) <= 5? -1:this.getDamage()*2-opponent_card.getDamage();
        //Define wizards fire
        if(opponent_card.getCardname().equals("fire_wizard") && this.getIs_a().equals("spell")
                && this.getCardType().equals("fire"))
            return (Math.random()*100) <= 40? -1:this.getDamage()*2-opponent_card.getDamage();
        if(opponent_card.getCardname().equals("fire_wizard") && this.getIs_a().equals("spell"))
            return (Math.random()*100) <= 5? -1:this.getDamage()*2-opponent_card.getDamage();
        //Define wizards normal
        if(opponent_card.getCardname().equals("water_wizard") && this.getIs_a().equals("spell")
                && this.getCardType().equals("water"))
            return (Math.random()*100) <= 40? -1:this.getDamage()*2-opponent_card.getDamage();
        if(opponent_card.getCardname().equals("water_wizard") && this.getIs_a().equals("spell"))
            return (Math.random()*100) <= 5? -1:this.getDamage()*2-opponent_card.getDamage();

        //Define werwolf rules -----------------------------------
        if(this.getCardname().equals("werwolf") && opponent_card.getCardname().equals("giant"))
            return -1;
        if(this.getCardname().equals("giant") && opponent_card.getCardname().equals("werwolf"))
            return 1;

        //Define witch rules --------------------------------------
        if(this.getCardname().equals("witch")){
            int rand = (int) Math.random()*100;
            if(rand >= 50){
                att_final.setDamage(att_final.getDamage() + this.getDamage()- 30);
            }else {
                if(rand <=40){
                    att_final.setDamage(att_final.getDamage() + this.getDamage() - ((int)(opponent_card.getDamage()/100*20)));
                }else {
                    att_final.setDamage(att_final.getDamage() + this.getDamage()*2);
                }
            }
        }
        if(opponent_card.getCardname().equals("witch")){
            int rand = (int) Math.random()*100;
            if(rand >= 50){
                opp_final.setDamage(opp_final.getDamage() + opponent_card.getDamage()- 30);
            }else {
                if(rand <=40){
                    opp_final.setDamage(opp_final.getDamage() + opponent_card.getDamage() - ((int)(this.getDamage()/100*20)));
                }else {
                    opp_final.setDamage(opp_final.getDamage() + opponent_card.getDamage()*2);
                }
            }
        }

        //Define Monster vs Spell -------------------------------------
        if(this.getCardType().equals("fire") && opponent_card.getCardType().equals("water")){
            if(this.getIs_a().equals("monster") && opponent_card.getIs_a().equals("spell")){
                att_final.setDamage(att_final.getDamage() + this.getDamage()/2);
                opp_final.setDamage(opp_final.getDamage() + opp_final.getDamage()*2);
            }else if (this.getIs_a().equals("spell") && opponent_card.getIs_a().equals("monster")){
                att_final.setDamage(att_final.getDamage() + this.getDamage()/2);
                opp_final.setDamage(opp_final.getDamage() + opp_final.getDamage()*2);
            }else {
                att_final.setDamage(att_final.getDamage() + this.getDamage());
                opp_final.setDamage(opp_final.getDamage() + opp_final.getDamage());
            }
        }
        //Define Spell vs Monster
        if(this.getCardType().equals("water") && opponent_card.getCardType().equals("fire")){
            if(this.getIs_a().equals("monster") && opponent_card.getIs_a().equals("spell")){
                att_final.setDamage(att_final.getDamage() + this.getDamage()*2);
                opp_final.setDamage(opp_final.getDamage() + opp_final.getDamage()/2);
            }else if (this.getIs_a().equals("spell") && opponent_card.getIs_a().equals("monster")){
                att_final.setDamage(att_final.getDamage() + this.getDamage()*2);
                opp_final.setDamage(opp_final.getDamage() + opp_final.getDamage()/2);
            }else {
                att_final.setDamage(att_final.getDamage() + this.getDamage());
                opp_final.setDamage(opp_final.getDamage() + opp_final.getDamage());
            }
        }
        return att_final.getDamage() - opp_final.getDamage();
    }
}
