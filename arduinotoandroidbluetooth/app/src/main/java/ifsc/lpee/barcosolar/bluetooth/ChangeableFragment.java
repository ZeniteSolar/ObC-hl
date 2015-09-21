package ifsc.lpee.barcosolar.bluetooth;

import android.app.Fragment;
import android.app.FragmentManager;

// substituir extends Fragment por extends ChangeableFragment;
public abstract class ChangeableFragment extends Fragment {
    public void changeFragment(Fragment fragment) {
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.container, fragment).commit();
    }
}
