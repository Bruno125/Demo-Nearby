package pe.upc.nearby.model;

import android.util.Log;

import com.google.android.gms.nearby.messages.Message;
import com.google.gson.Gson;

import java.nio.charset.Charset;

public class ChatMessage {


    private static final Gson gson = new Gson();

    private UserType userType;
    private Status messageStatus;
    private final String mUUID;
    private final String mMessageAuthor;
    private String mMessageBody;
    private final long messageTime;

    public ChatMessage(UserType userType, Status messageStatus, String mUUID, String mMessageAuthor, String mMessageBody, long messageTime) {
        this.mUUID = mUUID;
        this.userType = userType;
        this.messageStatus = messageStatus;
        this.mMessageAuthor = mMessageAuthor;
        this.mMessageBody = mMessageBody;
        this.messageTime = messageTime;
    }

    public long getMessageTime() {
        return messageTime;
    }

    public static ChatMessage fromNearbyMessage(Message message) {
        String nearbyMessageString = new String(message.getContent()).trim();
        try {
            return gson.fromJson(
                    (new String(nearbyMessageString.getBytes(Charset.forName("UTF-8")))),
                    ChatMessage.class);
        }
        catch (Exception ex) {
            return new ChatMessage(UserType.SELF,Status.SENT,"0","Error","Ocurrió un error en el mensaje",0);
        }
    }


    public static Message newNearbyMessage(UserType userType,Status messageStatus,String mUUID,String mMessageAuthor,String mMessageBody,long messageTime) {
        ChatMessage deviceMessage = new ChatMessage(userType,messageStatus,mUUID,mMessageAuthor,mMessageBody,messageTime);
        return new Message(gson.toJson(deviceMessage).getBytes(Charset.forName("UTF-8")));
    }

    public String getMessageText() {
        return mMessageBody;
    }

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) { this.userType = userType; }

    public Status getMessageStatus() {
        return messageStatus;
    }

    public String getUUID() {
        return mUUID;
    }

    public void setMessageBody(String mMessageBody) { this.mMessageBody = mMessageBody; }

    public String getmMessageAuthor() { return mMessageAuthor; }

    public void setMessageStatus(Status messageStatus) {
        this.messageStatus = messageStatus;
    }
}
