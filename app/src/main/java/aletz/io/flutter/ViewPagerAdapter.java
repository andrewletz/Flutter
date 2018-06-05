package aletz.io.flutter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    private static int TAB_COUNT = 3;

    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                return new DiscoverFragment();
            case 1:
                return new ProfileFragment();
            case 2:
                return new ConnectionsFragment();
        }
        return null;
    }

    @Override
    public int getCount() {
        return TAB_COUNT;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return DiscoverFragment.TITLE;

            case 1:
                return ProfileFragment.TITLE;

            case 2:
                return ConnectionsFragment.TITLE;
        }
        return super.getPageTitle(position);
    }
}
