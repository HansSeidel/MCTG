package bif3.swe.if20b211.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(value = {"gone"})
public class Message {
    private boolean gone = false;
    private int id;
    private String sender;
    private String message;

    public boolean isGone() {
        return gone;
    }

    private void setGone(boolean gone) {
        this.gone = gone;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
