package aletz.io.flutter;
/**
 * Created by Andrew Letz on 6-4-18
 * Last modified by Andrew Letz on 6-4-18
 */

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * Class to be used for showing past connections, including the options to connect
 * and decline connections
 * Incomplete
 */
public class ConnectionsFragment extends Fragment {

    // for displaying on the UI tab
    public static final String TITLE = "Connections";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_connections, container, false);
    }
}
