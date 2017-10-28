package pe.upc.nearby;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.nearby.messages.Message;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.UUID;

import pe.upc.nearby.model.ChatMessage;
import pe.upc.nearby.model.NearbyChat;
import pe.upc.nearby.model.Status;
import pe.upc.nearby.model.UserType;


public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        NearbyChat.OnFoundMessageListener,
        NearbyChat.OnLostMessageListener,
        NearbyChat.OnSubscribeSucceededListener,
        NearbyChat.OnSubscribeFailedListener {
    private ListView chatListView;
    private EditText chatEditText1;
    private ArrayList<ChatMessage> chatMessages;
    private ImageView enterChatView1;
    private ChatListAdapter listAdapter;
    private NearbyChat nearby;


    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int TTL_IN_SECONDS = 10 * 60; // Three minutes.

    // Key used in writing to and reading from SharedPreferences.
    private static final String KEY_UUID = "key_uuid";

    /**
     * Creates a UUID and saves it to {@link SharedPreferences}. The UUID is added to the published
     * message to avoid it being undelivered due to de-duplication. See {@link DeviceMessage} for
     * details.
     */
    private static String getUUID(SharedPreferences sharedPreferences) {
        String uuid = sharedPreferences.getString(KEY_UUID, "");
        if (TextUtils.isEmpty(uuid)) {
            uuid = UUID.randomUUID().toString();
            sharedPreferences.edit().putString(KEY_UUID, uuid).apply();
        }
        return uuid;
    }


    //////////////////////////////////////////////


    private final EditText.OnKeyListener keyListener = new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {

            // If the event is a key-down event on the "enter" button
            if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                    (keyCode == KeyEvent.KEYCODE_ENTER)) {
                // Perform action on key press

                EditText editText = (EditText) v;

                if (v == chatEditText1) {
                    sendMessage(editText.getText().toString());
                }

                chatEditText1.setText("");

                return true;
            }
            return false;

        }
    };

    private final ImageView.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (v == enterChatView1) {
                sendMessage(chatEditText1.getText().toString());
                //Scroll down a la pantalla en nuevo mensaje
                chatListView.setSelection(listAdapter.getCount() - 1);
            }

            chatEditText1.setText("");

        }
    };

    private final TextWatcher watcher1 = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            if (chatEditText1.getText().toString().equals("")) {
                return;

            } else {
                enterChatView1.setImageResource(R.drawable.ic_chat_send);

            }
        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (editable.length() == 0) {
                enterChatView1.setImageResource(R.drawable.ic_chat_send);
            } else {
                enterChatView1.setImageResource(R.drawable.ic_chat_send_active);
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        chatMessages = new ArrayList<>();
        listAdapter = new ChatListAdapter(chatMessages, this);

        nearby = new NearbyChat(this, this, this, this, this, this);


        chatListView = (ListView) findViewById(R.id.chat_list_view);
        chatEditText1 = (EditText) findViewById(R.id.chat_edit_text1);
        enterChatView1 = (ImageView) findViewById(R.id.enter_chat1);

        chatListView.setAdapter(listAdapter);
        chatEditText1.setOnKeyListener(keyListener);
        enterChatView1.setOnClickListener(clickListener);
        chatEditText1.addTextChangedListener(watcher1);

        chatEditText1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Scroll down a la pantalla en nuevo mensaje
                chatListView.setSelection(listAdapter.getCount() - 1);
            }
        });

    }

    private void sendMessage(final String messageText) {
        if (messageText.trim().length() == 0)
            return;

        final Message mPubMessage = ChatMessage.newNearbyMessage(
                UserType.OTHER,
                Status.SENT,
                getUUID(getSharedPreferences(getApplicationContext().getPackageName(), Context.MODE_PRIVATE)),
                Build.MODEL, messageText, new Date().getTime());

        final ChatMessage chatMessage = ChatMessage.fromNearbyMessage(mPubMessage);
        chatMessages.add(chatMessage);

        nearby.publish(mPubMessage, chatMessage, new NearbyChat.OnPublishSucceededListener() {
                    @Override
                    public void onPublishSucceeded(Message mPubMessage) {
                        Log.i(TAG, "Published successfully.");
                        chatMessage.setMessageStatus(Status.DELIVERED);

                        listAdapter.notifyDataSetChanged();

                    }
                },
                new NearbyChat.OnPublishFailedListener() {

                    @Override
                    public void onPublishFailed(com.google.android.gms.common.api.Status status) {
                        logAndShowSnackbar("Could not publish, status = " + status);
                    }
                });

        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();

            //Scroll down a la pantalla en nuevo mensaje
            chatListView.setSelection(listAdapter.getCount() - 1);
        }
    }

    private void receiveMessage(Message message) {
        final ChatMessage chatMessage = ChatMessage.fromNearbyMessage(message);
        chatMessage.setUserType(UserType.SELF);
        chatMessages.add(chatMessage);
        Log.i(TAG, "MENSAJE RECIBIDO");

        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();

            //Scroll down a la pantalla en nuevo mensaje
            chatListView.setSelection(listAdapter.getCount() - 1);
        }


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        logAndShowSnackbar("Exception while connecting to Google Play services: " +
                connectionResult.getErrorMessage());
    }

    @Override
    public void onConnectionSuspended(int i) {
        logAndShowSnackbar("Connection suspended. Error code: " + i);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "GoogleApiClient connected");
    }

    private void logAndShowSnackbar(final String text) {
        Log.w(TAG, text);
        View container = findViewById(R.id.chat_list_view);
        if (container != null) {
            Snackbar.make(container, text, Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void OnFoundMessage(Message message) {
        receiveMessage(message);
    }

    @Override
    public void OnLostMessage(final Message message) {
        Iterator<ChatMessage> i = chatMessages.iterator();

        while (i.hasNext()) {
            ChatMessage chatMessage = ChatMessage.fromNearbyMessage(message);
            ChatMessage iterator = i.next();
            if (iterator.getUUID().equalsIgnoreCase(chatMessage.getUUID())) {
                i.remove();
                Log.i(TAG, "Removiendo mensaje");
                break;
            }
        }

        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }

    }

    @Override
    public void onSubscribeSucceeded() {
        Log.i(TAG, "Subscribed Successfully");
    }

    @Override
    public void onSubscribeFailed(com.google.android.gms.common.api.Status status) {
        logAndShowSnackbar("Could not subscribe, status = " + status);
    }
}
