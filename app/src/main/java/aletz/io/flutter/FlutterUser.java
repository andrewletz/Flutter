package aletz.io.flutter;
/**
 * Created by Andrew Letz on 6-3-18
 * Last modified by Andrew Letz on 6-4-18
 */

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.FirebaseError;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;

/**
 * Class used to more easily interact with the Firebase database
 */
public class FlutterUser {

    final FirebaseDatabase database;
    private DatabaseReference mDatabase;

    // reference to the Firebase users uid
    private String uid;

    // stores all info for the Flutter user in the database
    private UserInfo info;

    // used for debugging purposes
    private String TAG = "FlutterUser";

    // Used for getting data from firebase
    private String returnString;

    public FlutterUser(FirebaseUser firebaseUser) {
        this.database = FirebaseDatabase.getInstance();
        this.uid = firebaseUser.getUid();
    }

    public FlutterUser(String uid) {
        this.database = FirebaseDatabase.getInstance();
        this.uid = uid;
    }

    /**
     * Gets an already existing user with a FirebaseUser reference
     * @param firebaseUser
     * @return appropriate FlutterUser
     */
    public static FlutterUser getUser(FirebaseUser firebaseUser) {
        FlutterUser user = new FlutterUser(firebaseUser);
        user.getInfo();
        return user;
    }

    /**
     * Gets an already existing user with a uid
     * @param uid
     * @return appropriate FlutterUser
     */
    public static FlutterUser getUser(String uid) {
        FlutterUser user = new FlutterUser(uid);
        user.getInfo();
        return user;
    }

    /**
     * Creates a new FlutterUser with appropriate FirebaseUser.
     * Used on registration activity to load in default values into realtime database.
     * Should not be used to get a new instance of an already existing Flutter user.
     * @param firebaseUser
     * @return appropriate FlutterUser
     */
    public static FlutterUser newUser(FirebaseUser firebaseUser) {
        FlutterUser user = new FlutterUser(firebaseUser);
        user.setDefaults();
        return user;
    }

    /**
     * returns the database reference to the head of the users information
     */
    private void resetDatabaseReference() {
        this.mDatabase = database.getReference().child("users").child("profile").child(uid);
    }

    /**
     * refreshes a FlutterUsers UserInfo variable with current information from database
     */
    private void getInfo() {
        resetDatabaseReference();
        readData(new FirebaseCallback() {
            @Override
            public void onCallback(UserInfo gotInfo) {
                info = gotInfo;
            }
        });
    }

    public UserInfo getUserinfo() {
        return this.info;
    }

    /**
     * Used on registration activity to load in default values into realtime database
     */
    private void setDefaults() {
        resetDatabaseReference();
        UserInfo userInfo = new UserInfo("N/A", "N/A", "N/A",
                "https://firebasestorage.googleapis.com/v0/b/flutter-6ef7f.appspot.com/o/profile.jpg?alt=media&token=a4690bda-bd8b-4510-b991-160c79c201d3",
                "N/A", "N/A");

        mDatabase.setValue(userInfo);
    }

    public void setUserBio(String bio) {
        resetDatabaseReference();
        this.mDatabase = this.mDatabase.child("bio");
        this.mDatabase.setValue(bio);
    }

    public void setUserEmail(String email) {
        resetDatabaseReference();
        this.mDatabase = this.mDatabase.child("email");
        this.mDatabase.setValue(email);
    }

    public void setUserPhone(String phone) {
        resetDatabaseReference();
        this.mDatabase = this.mDatabase.child("phone");
        this.mDatabase.setValue(phone);
    }

    public void setUserPhotoURL(String url) {
        resetDatabaseReference();
        this.mDatabase = this.mDatabase.child("photoURL");
        this.mDatabase.setValue(url);
    }

    public void setUserTitle(String title) {
        resetDatabaseReference();
        this.mDatabase = this.mDatabase.child("title");
        this.mDatabase.setValue(title);
    }

    public void setUsername(String username) {
        resetDatabaseReference();
        this.mDatabase = this.mDatabase.child("username");
        this.mDatabase.setValue(username);
    }

    /**
     * Asynchronous retrieving of data from the Firebase database
     * Needs a new FirebaseCallback passed in so it can pass back the gathered info
     * @param firebaseCallback
     */
    public void readData(final FirebaseCallback firebaseCallback) {
        resetDatabaseReference();

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                info = dataSnapshot.getValue(UserInfo.class);
                firebaseCallback.onCallback(info);
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) { }
        });
    }

    public interface FirebaseCallback {
        void onCallback(UserInfo gotInfo);
    }

}
