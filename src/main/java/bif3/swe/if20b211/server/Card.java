package bif3.swe.if20b211.server;

public class Card implements ICard{
    private String name;
    private int damage;
    public ECardType type;
    private int occurance;

    public Card(String name, int damage, ECardType type, int occurance) {
        this.name = name;
        this.damage = damage;
        this.type = type;
        this. occurance = occurance;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getDamage() {
        return damage;
    }

    @Override
    public int getOccurance() {
        return occurance;
    }

    @Override
    public boolean equals(Card card) {
        if(!this.name.equals(card.getName())) return false;
        if(this.damage != card.getDamage()) return false;
        if(this.type != card.type) return false;
        return this.occurance != card.getOccurance()? false:true;
    }
}
