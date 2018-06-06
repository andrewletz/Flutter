package aletz.io.flutter;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.InputStream;

public class ProfileFragment extends Fragment {

    // for displaying on the UI tab
    public static final String TITLE = "Profile";

    private FirebaseAuth mAuth;

    private FlutterUser flutterUser;

    private TextView mUsername;
    private ImageView mPhoto;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) this.flutterUser = FlutterUser.getUser(firebaseUser);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (flutterUser == null) return;
        View v = getView();

        mUsername = (TextView) v.findViewById(R.id.username);
        flutterUser.readData(new FlutterUser.FirebaseCallback() {
            @Override
            public void onCallback(UserInfo gotInfo) {
                mUsername.setText(gotInfo.getUsername());
            }
        });

        mPhoto = (ImageView) v.findViewById(R.id.profilePicture);
        flutterUser.readData(new FlutterUser.FirebaseCallback() {
            @Override
            public void onCallback(UserInfo gotInfo) {
                new DownloadImageTask((ImageView) mPhoto)
                        .execute("https://firebasestorage.googleapis.com/v0/b/flutter-6ef7f.appspot.com/o/profile.jpg?alt=media&token=a4690bda-bd8b-4510-b991-160c79c201d3");
            }
        });


    }

    public void updateUI() {
        Toast toast;
        if (this.flutterUser != null) {
            toast = Toast.makeText(getActivity(), "Updated UI", Toast.LENGTH_SHORT);
        } else {
            toast = Toast.makeText(getActivity(), "User is null", Toast.LENGTH_SHORT);
        }
        toast.setGravity(Gravity.TOP, 0, 0);
        toast.show();
    }

    /**
     * Signs the user out of their firebase account and redirects to login page
     */
    private void signOut() {
        mAuth.signOut();
        gotoLogin();
    }

    /**
     * Switches to the main activity
     */
    private void gotoLogin() {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
    }

}