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
            this.cardType = "water";
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
}
