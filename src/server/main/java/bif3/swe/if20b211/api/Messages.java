package bif3.swe.if20b211.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Messages {
    private List<Message> messages;

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    @JsonIgnore
    public List<Message> getMessagesLimitBy(int n) {
        return messages.stream().limit(n).collect(Collectors.toList());
    }

    @JsonIgnore
    public List<Message> getMessagesBySender(String sender){
        return messages.stream().filter(e -> e.getSender().equals(sender)).collect(Collectors.toList());
    }
    @JsonIgnore
    public List<Message> getMessagesBySenderLimitBy(String sender, int n){
        return getMessagesBySender(sender).stream().limit(n).collect(Collectors.toList());
    }

    @JsonIgnore
    public Message getMessagesById(int id) {
        return messages.stream().filter(message -> message.getId()==id).findFirst().orElse(null);
    }

    @JsonIgnore
    public int getNextId() {
        return messages.stream().max(Comparator.comparing(Message::getId)).orElseGet(() -> {
            Message m = new Message();
            m.setId(1);
            return m;
        }).getId()+1;
    }

    @JsonIgnore
    public void addMessage(Message m) {
        messages.add(m);
    }
}
