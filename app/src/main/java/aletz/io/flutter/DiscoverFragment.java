package aletz.io.flutter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.android.gms.nearby.messages.Messages;
import com.google.android.gms.nearby.messages.PublishCallback;
import com.google.android.gms.nearby.messages.PublishOptions;
import com.google.android.gms.nearby.messages.Strategy;
import com.google.android.gms.nearby.messages.SubscribeCallback;
import com.google.android.gms.nearby.messages.SubscribeOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import java.util.UUID;

/**
 * A fragment that allows a user to publish device information, and receive information about
 * nearby devices.
 * We check the app's permissions and present an opt-in dialog to the user, who can then grant the
 * required location permission.
 */
public class DiscoverFragment extends Fragment {

    // for logging purposes
    private static final String TAG = DiscoverFragment.class.getSimpleName();

    // for displaying on the UI tab
    public static final String TITLE = "Discover";

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

    // Views.
    private SwitchCompat mPublishSwitch;
    private SwitchCompat mSubscribeSwitch;

    // object used to broadcast information about the device to nearby devices.
    private Message mPubMessage;
    // for processing messages from nearby devices.
    private MessageListener mMessageListener;
    //Adapter for working with messages from nearby publishers.
    private ArrayAdapter<String> mNearbyDevicesArrayAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_discover, container, false);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View v = getView();

        mSubscribeSwitch = (SwitchCompat) v.findViewById(R.id.subscribe_switch);
        mPublishSwitch = (SwitchCompat) v.findViewById(R.id.publish_switch);

        // Build the message that is going to be published. This contains the device name and a
        // UUID.
//        mPubMessage = DeviceMessage.newNearbyMessage(getUUID(getSharedPreferences(
//                getApplicationContext().getPackageName(), Context.MODE_PRIVATE)));
        mPubMessage = DeviceMessage.newNearbyMessage("Test");

        mMessageListener = new MessageListener() {
            @Override
            public void onFound(final Message message) {
                // Called when a new message is found.
                mNearbyDevicesArrayAdapter.add(
                        DeviceMessage.fromNearbyMessage(message).getMessageBody());
            }

            @Override
            public void onLost(final Message message) {
                // Called when a message is no longer detectable nearby.
                mNearbyDevicesArrayAdapter.remove(
                        DeviceMessage.fromNearbyMessage(message).getMessageBody());
            }
        };

        mSubscribeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // If GoogleApiClient is connected, perform sub actions in response to user action.
                // If it isn't connected, do nothing, and perform sub actions when it connects (see
                // onConnected()).
                if (MainActivity.GoogleApiClient != null && MainActivity.GoogleApiClient.isConnected()) {
                    if (isChecked) {
                        subscribe();
                    } else {
                        unsubscribe();
                    }
                }
            }
        });

        mPublishSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // If GoogleApiClient is connected, perform pub actions in response to user action.
                // If it isn't connected, do nothing, and perform pub actions when it connects (see
                // onConnected()).
                if (MainActivity.GoogleApiClient != null && MainActivity.GoogleApiClient.isConnected()) {
                    if (isChecked) {
                        publish();
                    } else {
                        unpublish();
                    }
                }
            }
        });

        final List<String> nearbyDevicesArrayList = new ArrayList<>();
        mNearbyDevicesArrayAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_list_item_1,
                nearbyDevicesArrayList);
        final ListView nearbyDevicesListView = (ListView) v.findViewById(
                R.id.nearby_devices_list_view);
        if (nearbyDevicesListView != null) {
            nearbyDevicesListView.setAdapter(mNearbyDevicesArrayAdapter);
        }

        TextView mUid = (TextView) v.findViewById(R.id.account_id);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) mUid.setText(user.getEmail());

    }

    /**
     * Subscribes to messages from nearby devices and updates the UI if the subscription either
     * fails or TTLs.
     */
    private void subscribe() {
        Log.i(TAG, "Subscribing");
        mNearbyDevicesArrayAdapter.clear();
        SubscribeOptions options = new SubscribeOptions.Builder()
                .setCallback(new SubscribeCallback() {
                    @Override
                    public void onExpired() {
                        super.onExpired();
                        Log.i(TAG, "No longer subscribing");
                        mSubscribeSwitch.setChecked(false);
                    }
                }).build();

        Nearby.Messages.subscribe(MainActivity.GoogleApiClient, mMessageListener, options)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if (status.isSuccess()) {
                            Log.i(TAG, "Subscribed successfully.");
                        } else {
                            logAndShowSnackbar("Could not subscribe, status = " + status);
                            mSubscribeSwitch.setChecked(false);
                        }
                    }
                });
    }

    /**
     * Publishes a message to nearby devices and updates the UI if the publication either fails or
     * TTLs.
     */
    private void publish() {
        Log.i(TAG, "Publishing");
        PublishOptions options = new PublishOptions.Builder()
                .setCallback(new PublishCallback() {
                    @Override
                    public void onExpired() {
                        super.onExpired();
                        Log.i(TAG, "No longer publishing");
                        mPublishSwitch.setChecked(false);
                    }
                }).build();

        Nearby.Messages.publish(MainActivity.GoogleApiClient, mPubMessage, options)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if (status.isSuccess()) {
                            Log.i(TAG, "Published successfully.");
                        } else {
                            logAndShowSnackbar("Could not publish, status = " + status);
                            mPublishSwitch.setChecked(false);
                        }
                    }
                });
    }

    /**
     * Stops subscribing to messages from nearby devices.
     */
    private void unsubscribe() {
        Log.i(TAG, "Unsubscribing.");
        Nearby.Messages.unsubscribe(MainActivity.GoogleApiClient, mMessageListener);
    }

    /**
     * Stops publishing message to nearby devices.
     */
    private void unpublish() {
        Log.i(TAG, "Unpublishing.");
        Nearby.Messages.unpublish(MainActivity.GoogleApiClient, mPubMessage);
    }

    /**
     * Logs a message and shows a {@link Snackbar} using {@code text};
     *
     * @param text The text used in the Log message and the SnackBar.
     */
    private void logAndShowSnackbar(final String text) {
        Log.w(TAG, text);
        View container = getView().findViewById(R.id.activity_main_container);
        if (container != null) {
            Snackbar.make(container, text, Snackbar.LENGTH_LONG).show();
        }
    }
}