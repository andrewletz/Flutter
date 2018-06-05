package aletz.io.flutter;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.FirebaseError;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class FlutterUser {

    final FirebaseDatabase database;
    private DatabaseReference mDatabase;
    private FirebaseUser fUser;
    private UserInfo info;
    private String retString;

    private String TAG = "FlutterUser";

    // Used for getting data from firebase
    private String returnString;

    public FlutterUser(FirebaseUser firebaseUser) {
        this.database = FirebaseDatabase.getInstance();
        this.fUser = firebaseUser;
    }

    public static FlutterUser getUser(FirebaseUser firebaseUser) {
        FlutterUser user = new FlutterUser(firebaseUser);
        user.getInfo();
        return user;
    }

    public static FlutterUser newUser(FirebaseUser firebaseUser) {
        FlutterUser user = new FlutterUser(firebaseUser);
        user.setDefaults();
        return user;
    }

    private void resetDatabaseReference() {
        this.mDatabase = database.getReference().child("users").child("profile").child(fUser.getUid());
    }

    private void getInfo() {
        resetDatabaseReference();
        readData(new FirebaseCallback() {
            @Override
            public void onCallback(UserInfo gotInfo) {
                info = gotInfo;
            }
        });
    }

    private void setDefaults() {
        resetDatabaseReference();
        UserInfo userInfo = new UserInfo("N/A", "N/A", "N/A", "N/A", "N/A", "N/A");
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
