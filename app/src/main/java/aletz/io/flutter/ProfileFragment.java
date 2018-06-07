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
import android.widget.Button;
import android.widget.EditText;
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

    private EditText mUsernameEdit;
    private EditText mTitleEdit;
    private EditText mBioEdit;
    private EditText mPhoneEdit;

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
        mUsernameEdit = (EditText) v.findViewById(R.id.name);
        mBioEdit = (EditText) v.findViewById(R.id.bio);
        mTitleEdit = (EditText) v.findViewById(R.id.title);
        mPhoneEdit = (EditText) v.findViewById(R.id.phone);
        mPhoto = (ImageView) v.findViewById(R.id.profilePicture);

        flutterUser.readData(new FlutterUser.FirebaseCallback() {
            @Override
            public void onCallback(UserInfo gotInfo) {
                mUsername.setText(gotInfo.getUsername());
                mUsernameEdit.setText(gotInfo.getUsername());
                mBioEdit.setText(gotInfo.getBio() != "N/A" ? gotInfo.getBio() : "");
                mTitleEdit.setText(gotInfo.getTitle() != "N/A" ? gotInfo.getTitle() : "");
                mPhoneEdit.setText(gotInfo.getPhone() != "N/A" ? gotInfo.getPhone() : "");
            }
        });

        flutterUser.readData(new FlutterUser.FirebaseCallback() {
            @Override
            public void onCallback(UserInfo gotInfo) {
                new DownloadImageTask((ImageView) mPhoto)
                        .execute("https://firebasestorage.googleapis.com/v0/b/flutter-6ef7f.appspot.com/o/profile.jpg?alt=media&token=a4690bda-bd8b-4510-b991-160c79c201d3");
            }
        });

        Button mUpdateButton = (Button) v.findViewById(R.id.update);
        Button mSignoutButton = (Button) v.findViewById(R.id.sign_out);

        mUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateDatabase();
            }
        });

        mSignoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
            }
        });


    }

    enum Action { SIGN_OUT, UPDATE_INFO }

    public void updateUI(Action action) {
        Toast toast;

        switch (action) {
            case SIGN_OUT:
                toast = Toast.makeText(getActivity(), "Signed out.", Toast.LENGTH_SHORT);
                break;

            case UPDATE_INFO:
                toast = Toast.makeText(getActivity(), "Updated information.", Toast.LENGTH_SHORT);
                break;

            default:
                toast = Toast.makeText(getActivity(), "Error in updateUI.", Toast.LENGTH_SHORT);
        }

        toast.setGravity(Gravity.TOP, 0, 0);
        toast.show();
    }

    public void updateDatabase() {
        flutterUser.setUserBio(mBioEdit.getText().toString());
        flutterUser.setUsername(mUsernameEdit.getText().toString());
        flutterUser.setUserPhone(mPhoneEdit.getText().toString());
        flutterUser.setUserTitle(mTitleEdit.getText().toString());
        updateUI(Action.UPDATE_INFO);
    }


    /**
     * Signs the user out of their firebase account and redirects to login page
     */
    private void signOut() {
        updateUI(Action.SIGN_OUT);
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