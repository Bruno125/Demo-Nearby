package pe.upc.nearby.model;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.*;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.android.gms.nearby.messages.PublishCallback;
import com.google.android.gms.nearby.messages.PublishOptions;
import com.google.android.gms.nearby.messages.Strategy;
import com.google.android.gms.nearby.messages.SubscribeCallback;
import com.google.android.gms.nearby.messages.SubscribeOptions;

import java.util.ArrayList;

import pe.upc.nearby.MainActivity;

/**
 * Created by jamil on 9/2/16.
 */
public class NearbyChat implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private static final int TTL_IN_SECONDS = 10 * 60; //

    private MessageListener mMessageListener;

    private static final Strategy PUB_SUB_STRATEGY = new Strategy.Builder()
            .setTtlSeconds(TTL_IN_SECONDS).build();

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String KEY_UUID = "key_uuid";
    private GoogleApiClient mGoogleApiClient;

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        this.subscribe(mMessageListener,onSubscribeSucceededListener,onSubscribeFailedListener);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public interface OnPublishSucceededListener {
         void onPublishSucceeded(Message mPubMessage);
    }

    public interface OnPublishFailedListener {
         void onPublishFailed(Status status);
    }

    public interface OnSubscribeSucceededListener {
         void onSubscribeSucceeded();
    }

    public interface OnSubscribeFailedListener {
         void onSubscribeFailed(Status status);
    }

    public interface OnFoundMessageListener {
         void OnFoundMessage(Message message);
    }

    public interface OnLostMessageListener {
         void OnLostMessage(Message message);
    }

    private final OnSubscribeFailedListener onSubscribeFailedListener;

    private final OnSubscribeSucceededListener onSubscribeSucceededListener;

    public NearbyChat(Context context,
                      FragmentActivity activity,
                      OnFoundMessageListener foundListener,
                      OnLostMessageListener lostListener,
                      OnSubscribeSucceededListener succeededListener,
                      OnSubscribeFailedListener failedListener) {
        buildGoogleApiClient(context, this, activity, this);
        buildMessageListener(foundListener, lostListener);

        this.onSubscribeSucceededListener = succeededListener;
        this.onSubscribeFailedListener = failedListener;

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                this.subscribe(mMessageListener,succeededListener,failedListener);
            }
        }


        // If GoogleApiClient is connected, perform pub actions in response to user action.
        // If it isn't connected, do nothing, and perform pub actions when it connects (see
        // onConnected()).

    private void buildMessageListener(final OnFoundMessageListener foundListener, final OnLostMessageListener lostListener) {
        mMessageListener = new MessageListener() {
            @Override
            public void onFound(final Message message) {
                // Called when a new message is found.
                foundListener.OnFoundMessage(message);
                //receiveMessage(message);
            }

            @Override
            public void onLost(final Message message) {
                // Se llama cuando un usuario ya no está cerca
                lostListener.OnLostMessage(message);
            }
        };
    }

    private void buildGoogleApiClient(Context context, GoogleApiClient.ConnectionCallbacks callbacks, FragmentActivity activity, GoogleApiClient.OnConnectionFailedListener listener) {
        if (mGoogleApiClient != null) {
            return;
        }
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(com.google.android.gms.nearby.Nearby.MESSAGES_API)
                .addConnectionCallbacks(callbacks)
                .enableAutoManage(activity, listener)
                .build();
    }

    /**
     * Subscribes to messages from nearby devices and updates the UI if the subscription either
     * fails or TTLs.
     */
    public void subscribe(MessageListener mMessageListener,
                          final OnSubscribeSucceededListener succeededlistener,
                          final OnSubscribeFailedListener failedListener) {

        Log.i(TAG, "Subscribing");
        SubscribeOptions options = new SubscribeOptions.Builder()
                .setStrategy(PUB_SUB_STRATEGY)
                .setCallback(new SubscribeCallback() {
                    @Override
                    public void onExpired() {
                        super.onExpired();
                        Log.i(TAG, "No longer subscribing");
                    }
                }).build();

        com.google.android.gms.nearby.Nearby.Messages.subscribe(mGoogleApiClient, mMessageListener, options)
                .setResultCallback(new ResultCallback<com.google.android.gms.common.api.Status>() {
                    @Override
                    public void onResult(@NonNull com.google.android.gms.common.api.Status status) {
                        if (status.isSuccess()) {
                            succeededlistener.onSubscribeSucceeded();
                            //Log.i(TAG, "Subscribed successfully.");
                        } else {
                            failedListener.onSubscribeFailed(status);
                            //logAndShowSnackbar("Could not subscribe, status = " + status);}
                        }
                    }
                });

    }
    /**
     * Publishes a message to nearby devices and updates the UI if the publication either fails or
     * TTLs.
     */
    public void publish(final Message mPubMessage, final ChatMessage message, final OnPublishSucceededListener succeededListener, final OnPublishFailedListener failedListener) {
        Log.i(TAG, "Publishing");
        PublishOptions options = new PublishOptions.Builder()
                .setStrategy(PUB_SUB_STRATEGY)
                .setCallback(new PublishCallback() {
                    @Override
                    public void onExpired() {
                        super.onExpired();
                        Log.i(TAG, "No longer publishing");
                    }
                }).build();


        com.google.android.gms.nearby.Nearby.Messages.publish(mGoogleApiClient, mPubMessage, options)
                .setResultCallback(new ResultCallback<com.google.android.gms.common.api.Status>() {
                    @Override
                    public void onResult(@NonNull com.google.android.gms.common.api.Status status) {
                        if (status.isSuccess()) {
                            succeededListener.onPublishSucceeded(mPubMessage);

                        } else {
                            failedListener.onPublishFailed(status);

                        }
                    }
                });
    }
    private void unsubscribe() {
        Log.i(TAG, "Unsubscribing.");
        com.google.android.gms.nearby.Nearby.Messages.unsubscribe(mGoogleApiClient, mMessageListener);
    }
}