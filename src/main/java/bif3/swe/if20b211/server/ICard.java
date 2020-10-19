package bif3.swe.if20b211.server;

public interface ICard {
    public String getName();
    public int getDamage();
    public int getOccurance();
    public boolean equals(Card card);
}
