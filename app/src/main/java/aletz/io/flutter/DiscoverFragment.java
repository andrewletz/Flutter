package aletz.io.flutter;
/**
 * Credit to Google Nearby Android sample: https://github.com/googlesamples/android-nearby
 * Created by Andrew Letz on 5-31-18
 * Last modified by Andrew Letz on 6-6-18
 */

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.Image;
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
import android.widget.ImageView;
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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
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
    private static final String TAG = "Discover";

    // for displaying on the UI's tab
    public static final String TITLE = "Discover";

    // Key used in writing to and reading from SharedPreferences.
    private static final String KEY_UUID = "key_uuid";

    // Views.
    private SwitchCompat mDiscoverSwitch;

    private FirebaseUser firebaseUser;

    // object used to broadcast information about the device to nearby devices.
    private Message mPubMessage;
    // for processing messages from nearby devices.
    private MessageListener mMessageListener;
    // Adapter for working with messages from nearby publishers.
    private ConnectionRowAdapter mNearbyDevicesArrayAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    /** occurs after onCreate() is called, but before onActivityCreated */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_discover, container, false);
    }

    /** after the Activity's onCreate() has been completed, and after onCreateView()
        used for most logic as at this point we know we have a flutter user
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View v = getView();

        mDiscoverSwitch = (SwitchCompat) v.findViewById(R.id.discover_switch);

        // Build the message that is going to be published
        mPubMessage = DeviceMessage.newNearbyMessage(this.firebaseUser.getUid());
        Log.d(TAG, "mPubMessage is " + this.firebaseUser.getUid());

        mMessageListener = new MessageListener() {
            @Override
            public void onFound(final Message message) {
                // Called when a new message is found.
                FlutterUser user = FlutterUser.getUser(DeviceMessage.fromNearbyMessage(message).getUid());
                Log.d(TAG, "message is " + DeviceMessage.fromNearbyMessage(message).getUid());
                user.readData(new FlutterUser.FirebaseCallback() {
                    @Override
                    public void onCallback(UserInfo gotInfo) {
                        mNearbyDevicesArrayAdapter.add(gotInfo);
                    }
                });
            }

            @Override
            public void onLost(final Message message) {
                // Called when a message is no longer detectable nearby.
                FlutterUser user = FlutterUser.getUser(DeviceMessage.fromNearbyMessage(message).getUid());
                Log.d(TAG, "message is " + DeviceMessage.fromNearbyMessage(message).getUid());
                user.readData(new FlutterUser.FirebaseCallback() {
                    @Override
                    public void onCallback(UserInfo gotInfo) {
                        mNearbyDevicesArrayAdapter.remove(gotInfo);
                    }
                });
            }
        };

        mDiscoverSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // If GoogleApiClient is connected, perform sub actions in response to user action.
                // If it isn't connected, do nothing, and perform sub actions when it connects (see
                // onConnected()).
                if (MainActivity.GoogleApiClient != null && MainActivity.GoogleApiClient.isConnected()) {
                    mNearbyDevicesArrayAdapter.clear();
                    if (isChecked) {
                        subscribe();
                        publish();
                    } else {
                        unsubscribe();
                        unpublish();
                    }
                }
            }
        });

        final ArrayList<UserInfo> nearbyDevicesArrayList = new ArrayList<>();
        mNearbyDevicesArrayAdapter = new ConnectionRowAdapter(getActivity(),
                nearbyDevicesArrayList);

        final ListView nearbyDevicesListView = (ListView) v.findViewById(
                R.id.nearby_devices_list_view);
        if (nearbyDevicesListView != null) {
            nearbyDevicesListView.setAdapter(mNearbyDevicesArrayAdapter);
        }

//        FlutterUser testUser = FlutterUser.getUser("wJwl98Q6YuWWBzbpFGrXny7jCHP2");
//        for (int i = 0; i < 1; i ++) {
//            testUser.readData(new FlutterUser.FirebaseCallback() {
//                @Override
//                public void onCallback(UserInfo gotInfo) {
//                    mNearbyDevicesArrayAdapter.add(gotInfo);
//                }
//            });
//        }

    }

    /**
     * Subscribes to messages from nearby devices and updates the UI if the subscription fails
     */
    private void subscribe() {
        Log.i(TAG, "Subscribing");
        Nearby.Messages.subscribe(MainActivity.GoogleApiClient, mMessageListener)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if (status.isSuccess()) {
                            Log.i(TAG, "Subscribed successfully.");
                        } else {
                            logAndShowSnackbar("Could not subscribe, status = " + status);
                            mDiscoverSwitch.setChecked(false);
                        }
                    }
                });
    }

    /**
     * Publishes a message to nearby devices and updates the UI if the publication fails
     */
    private void publish() {
        Log.i(TAG, "Publishing");
        Nearby.Messages.publish(MainActivity.GoogleApiClient, mPubMessage)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if (status.isSuccess()) {
                            Log.i(TAG, "Published successfully.");
                        } else {
                            logAndShowSnackbar("Could not publish, status = " + status);
                            mDiscoverSwitch.setChecked(false);
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

    /**
     * attribution: http://hmkcode.com/android-custom-listview-items-row/
     * Used to create a custom listview for showing nearby people connected through Google nearby
     */
    public class ConnectionRowAdapter extends ArrayAdapter<UserInfo> {

        private final Context context;
        private final ArrayList<UserInfo> infoList;

        private TextView mUsername;
        private ImageView mPhoto;

        public ConnectionRowAdapter(Context context, ArrayList<UserInfo> infoList) {
            super(context, R.layout.listrow_connection, infoList);
            this.context = context;
            this.infoList = infoList;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            // Create inflater
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            // Get rowView from inflater
            View v = inflater.inflate(R.layout.listrow_connection, parent, false);

            // Get the two views
            mUsername = (TextView) v.findViewById(R.id.username);
            mPhoto = (ImageView) v.findViewById(R.id.profilePicture);

            // Set the appropriate information in views
            mUsername = (TextView) v.findViewById(R.id.username);
            mUsername.setText(infoList.get(position).getUsername());

            mPhoto = (ImageView) v.findViewById(R.id.profilePicture);
            new DownloadImageTask((ImageView) mPhoto)
                    .execute(infoList.get(position).getPhotoURL());

            return v;
        }
    }

}