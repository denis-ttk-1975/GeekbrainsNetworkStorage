package model;

import java.time.LocalDateTime;

public class ChatUnitMessage implements Message {

    private String senderName;
    private final String messageContent;
    private final LocalDateTime sendAt;

    public ChatUnitMessage(String senderName,
                           String messageContent,
                           LocalDateTime sendAt) {
        this.senderName = senderName;
        this.messageContent = messageContent;
        this.sendAt = sendAt;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public LocalDateTime getSendAt() {
        return sendAt;
    }

    @Override
    public String toString() {
        return String.format("[%d-%d-%d %d:%d] %s: %s",
                sendAt.getYear(), sendAt.getMonthValue(), sendAt.getDayOfMonth(),
                sendAt.getHour(), sendAt.getMinute(), senderName, messageContent);
    }
}
