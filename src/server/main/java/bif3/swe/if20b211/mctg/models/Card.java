package bif3.swe.if20b211.mctg.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Card {
    private String cardname;
    private int damage;
    private String cardType;
    private String is_a;
    private String kind;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public Card(@JsonProperty("cardname")String cardname,
                @JsonProperty("damage") int damage,
                @JsonProperty("type") String type,
                @JsonProperty("is_a") String is_a,
                @JsonProperty("occurance") int occurance) {
        this.cardname = cardname;
        this.damage = damage;
        this.cardType = type;
        this.is_a = is_a;
        setKind(occurance);

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
        if(cardType.equals("f")) this.cardType = "fire";
        if(cardType.equals("w")) this.cardType = "water";
        this.cardType = "normal";
    }

    public String getIs_a() {return is_a;}

    public void setIs_a(String is_a) {
        this.is_a = is_a.equals("m")?"monster":"spell";
    }

    public String getKind() {
        return kind;
    }

    public void setKind(int occurance) {
        this.kind = "legendary";
        if(occurance >= 5) this.kind = "epic";
        if(occurance >= 10) this.kind = "rare";
        if(occurance >= 30) this.kind = "seldom";
        if(occurance >= 50) this.kind = "normal";
    }
}
