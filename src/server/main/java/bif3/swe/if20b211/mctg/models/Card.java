package bif3.swe.if20b211.mctg.models;

public class Card {
    private String cardname;
    private int damage;
    private cardType cardType;
    private is_a is_a;
    private String kind;

    public Card(String cardname, int damage, String type, String is_a, int occurance) {
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

    public Card.cardType getCardType() {
        return cardType;
    }

    public void setCardType(Card.cardType cardType) {
        this.cardType = cardType;
    }

    public Card.is_a getIs_a() {
        return is_a;
    }

    public void setIs_a(Card.is_a is_a) {
        this.is_a = is_a;
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

    private enum cardType {
        FIRE, WATER, NORMAL
    }
    private enum is_a{
        MONSTER, SPELL
    }
}
