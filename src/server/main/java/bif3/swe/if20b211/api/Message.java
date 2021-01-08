package bif3.swe.if20b211.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

public class Message {
    private boolean gone = false;
    private int id;
    private String sender;
    private String message;

    public boolean isGone() {
        return gone;
    }

    @JsonIgnore
    public boolean isNotGone(){
        return !gone;
    }

    public void setGone(boolean gone) {
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
