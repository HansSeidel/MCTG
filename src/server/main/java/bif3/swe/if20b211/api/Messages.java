package bif3.swe.if20b211.api;

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

    public List<Message> getMessagesLimitBy(int n) {
        return messages.stream().limit(n).collect(Collectors.toList());
    }

    public List<Message> getMessagesBySender(String sender){
        return messages.stream().filter(e -> e.getSender().equals(sender)).collect(Collectors.toList());
    }

    public List<Message> getMessagesBySenderLimitBy(String sender, int n){
        return getMessagesBySender(sender).stream().limit(n).collect(Collectors.toList());
    }

    public Message getMessagesById(int id) {
        return messages.stream().filter(message -> message.getId()==id).findFirst().orElse(null);
    }
}
