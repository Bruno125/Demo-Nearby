package pe.upc.nearby;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import pe.upc.nearby.model.ChatMessage;
import pe.upc.nearby.model.Status;
import pe.upc.nearby.model.UserType;

public class ChatListAdapter extends BaseAdapter {

    private ArrayList<ChatMessage> chatMessages;
    private final Context context;
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("HH:mm:ss.SSS");

    public ChatListAdapter(ArrayList<ChatMessage> chatMessages, Context context) {
        this.chatMessages = chatMessages;
        this.context = context;

    }


    @Override
    public int getCount() {
        return chatMessages.size();
    }

    @Override
    public Object getItem(int position) {
        return chatMessages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = null;
        ChatMessage message = chatMessages.get(position);
        ViewHolder1 holder1;
        ViewHolder2 holder2;

        if (message.getUserType() == UserType.SELF) {
            if (convertView == null) {
                v = LayoutInflater.from(context).inflate(R.layout.chat_user1_item, null, false);
                holder1 = new ViewHolder1();


                holder1.messageTextView = (TextView) v.findViewById(R.id.message_text);
                holder1.timeTextView = (TextView) v.findViewById(R.id.time_text);
                holder1.author = (TextView) v.findViewById(R.id.chat_company_reply_author);

                v.setTag(holder1);
            } else {
                v = convertView;
                holder1 = (ViewHolder1) v.getTag();

            }

            holder1.messageTextView.setText((message.getMessageText()));
            Date now = new Date();
            String format = SIMPLE_DATE_FORMAT.format(message.getMessageTime());
            Date send = new Date(message.getMessageTime());
            long diff = now.getTime() - message.getMessageTime();
            Log.i("ChatList","Diff|" + now.getTime() + "|" + message.getMessageTime()+"|"+diff);

            holder1.timeTextView.setText(diff + "");
            holder1.author.setText(message.getmMessageAuthor());

        } else if (message.getUserType() == UserType.OTHER) {

            if (convertView == null) {
                v = LayoutInflater.from(context).inflate(R.layout.chat_user2_item, null, false);

                holder2 = new ViewHolder2();


                holder2.messageTextView = (TextView) v.findViewById(R.id.message_text);
                holder2.timeTextView = (TextView) v.findViewById(R.id.time_text);
                holder2.messageStatus = (ImageView) v.findViewById(R.id.user_reply_status);
                v.setTag(holder2);

            } else {
                v = convertView;
                holder2 = (ViewHolder2) v.getTag();

            }

            Date now = new Date();
            String format = SIMPLE_DATE_FORMAT.format(message.getMessageTime());
            Date send = new Date(message.getMessageTime());
            long diff = now.getTime() - message.getMessageTime();
            holder2.messageTextView.setText(message.getMessageText());
            //holder2.messageTextView.setText(message.getMessageText());
            Log.i("ChatList","Diff|" + now.getTime() + "|" + message.getMessageTime()+"|"+diff);
            holder2.timeTextView.setText(diff+"");

            if (message.getMessageStatus() == Status.DELIVERED) {
                holder2.messageStatus.setImageDrawable(ContextCompat.getDrawable(this.context,R.drawable.ic_double_tick));
            } else if (message.getMessageStatus() == Status.SENT) {
                holder2.messageStatus.setImageDrawable(ContextCompat.getDrawable(this.context,R.drawable.ic_single_tick));
            }
        }

        return v;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = chatMessages.get(position);
        return message.getUserType().ordinal();
    }

    private class ViewHolder1 {
        public TextView messageTextView;
        public TextView timeTextView;
        public TextView author;


    }

    private class ViewHolder2 {
        public ImageView messageStatus;
        public TextView messageTextView;
        public TextView timeTextView;

    }
}
