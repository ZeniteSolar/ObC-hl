// WORKING 20.07 02:42

//reference: http://stackoverflow.com/questions/6091194/how-to-handle-button-clicks-using-the-xml-onclick-within-fragments
//reference: http://labdegaragem.com/forum/topics/tutorial-appbluetooth-android-em-eclipse
//reference: http://stanford.edu/~tpurtell/BluetoothChatService.java
//reference: http://stackoverflow.com/questions/7211610/completing-the-bluetooth-connection

//////////////////////////////////////////////////////////////////////////////////////////////////
// INSTITUTO FEDERAL DE EDUCAÇÃO, CIÊNCIA E TECNOLOGIA DE SANTA CATARINA - CAMPUS FLORIANÓPOLIS //
// Departamento: Eletrônica (DAELN)																//
// Curso: Engenharia Eletrônica																	//
// Aluno: João Antônio Cardoso																	//
//////////////////////////////////////////////////////////////////////////////////////////////////
// CONTATO:																						//
// joao.maker@gmail.com 																		//
// facebook.com/joaoantoniocardoso 																//
// github.com/joaoantoniocardoso 																//
//////////////////////////////////////////////////////////////////////////////////////////////////

package ifsc.lpee.barcosolar.bluetooth;

import java.util.Set;

import android.app.Fragment;
import android.app.FragmentManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

//public class fragment_blueTooth extends Fragment {
public class fragment_bluetooth extends Fragment implements OnClickListener {

	private Switch swBluetoothStatus;
	private Button btBluetoothGetVisible;
	private ToggleButton tbShowPairedList;
	private ListView lvBluetoothPairedDevicesList;

    private ArrayAdapter<String> mArrayAdapter;

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater
				.inflate(R.layout.fragment_bluetooth, container, false);

		lvBluetoothPairedDevicesList = (ListView) v
				.findViewById(R.id.lvBluetoothPairedDevicesList);
		btBluetoothGetVisible = (Button) v
				.findViewById(R.id.btBluetoothGetVisible);
		swBluetoothStatus = (Switch) v.findViewById(R.id.swBluetoothStatus);
		tbShowPairedList = (ToggleButton) v.findViewById(R.id.tbShowPairedList);

		// adapter for paired devices' list
		mArrayAdapter = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_list_item_1);
		lvBluetoothPairedDevicesList.setAdapter(mArrayAdapter);

		setListners();
		verifyBluetooth();
		setOnItemClickListener();

		// return View v
		return v;
	}

	// Listener to connect
	public void setOnItemClickListener() {
		lvBluetoothPairedDevicesList
				.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View v,
							int position, long id) {

						MainActivity.mBluetoothAdapter.cancelDiscovery();

						// Get the device MAC address, which is the last 17
						// chars in the View
						String info = ((TextView) v).getText().toString();
						String address = info.substring(info.length() - 17);
						MainActivity.mmDevice = MainActivity.mBluetoothAdapter.getRemoteDevice(address);

						Toast.makeText(getActivity(), address, Toast.LENGTH_SHORT).show();
						connect();

					}
				});
	}

	public void connect() {
		// Start the thread to connect with the given device
		ConnectThread mConnectThread = new ConnectThread(MainActivity.mmDevice);
		mConnectThread.run();
//TODO: verificar se a conexao foi completa

		changeToCommunicationFragment();
		StateOfCharge.SOC();//liga o monitoramento da carga da bateria

	}

	// Replaces the current fragment to fragment_communication
	public void changeToCommunicationFragment() {

		Fragment fragment = new fragment_communication();
		// update the main content by replacing fragments
		FragmentManager fragmentManager = getFragmentManager();
		fragmentManager.beginTransaction().replace(R.id.container, fragment)
				.commit();
	}


	// All setOnClickListener should be initialized here
	public void setListners() {
		tbShowPairedList.setOnClickListener(this);
		swBluetoothStatus.setOnClickListener(this);
		btBluetoothGetVisible.setOnClickListener(this);
	}

	// Verifies if the device supports blueTooth and turn visible or
	// invisible the blueTooth controls.
	public void verifyBluetooth() {

		if (MainActivity.mBluetoothAdapter == null) {
			Toast.makeText(getActivity(),
					getString(R.string.BluetoothDeviceDoenstSupport),
					Toast.LENGTH_SHORT).show(); // Shows message for the case
												// that the device does not
												// support blueTooth
												// swBluetoothStatus.setVisibility(View.INVISIBLE);

		} else {
			Toast.makeText(
					getActivity(),
					getString(R.string.BluetoothDeviceSupports)
							+ ", the device is: " + MainActivity.mBluetoothAdapter,
					Toast.LENGTH_SHORT).show(); // Shows message for the case
												// that the device supports
												// blueTooth
												// swBluetoothStatus.setVisibility(View.VISIBLE);
		}

		if (MainActivity.mBluetoothAdapter.isEnabled()) {
			setBluetoothON();
		} else {
			setBluetoothOFF();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btBluetoothGetVisible:
			if (MainActivity.mBluetoothAdapter.isEnabled()) {
				Intent getVisible = new Intent(
						BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
				startActivityForResult(getVisible, 0);
			} else {
				Toast.makeText(getActivity(),
						getString(R.string.BluetoothStatusOff),
						Toast.LENGTH_SHORT).show(); // Shows message for the
													// device turned off
			}
			break;
		case R.id.swBluetoothStatus:
			if (MainActivity.mBluetoothAdapter.isEnabled()) {
				setBluetoothOFF();
			} else {
				setBluetoothON();
			}
			break;
		case R.id.tbShowPairedList:
			onClickedPairedListToggleButton();
			break;
		}

	}

	public void setBluetoothON() {
		swBluetoothStatus.setChecked(true);
		btBluetoothGetVisible.setVisibility(View.VISIBLE);
		tbShowPairedList.setVisibility(View.VISIBLE);

		MainActivity.mBluetoothAdapter.enable(); // forces enable without request
		Toast.makeText(getActivity(), getString(R.string.BluetoothTurnedOn),
				Toast.LENGTH_SHORT).show(); // Shows message for the
											// device turned on
	}

	public void setBluetoothOFF() {
		swBluetoothStatus.setChecked(false);
		btBluetoothGetVisible.setVisibility(View.INVISIBLE);
		tbShowPairedList.setVisibility(View.INVISIBLE);

		MainActivity.mBluetoothAdapter.disable(); // forces disable without request
		Toast.makeText(getActivity(), getString(R.string.BluetoothTurnedOff),
				Toast.LENGTH_SHORT).show(); // Shows message for the
											// device turned off
		mArrayAdapter.clear();
		tbShowPairedList.setChecked(false);
	}

	//
	// The list of the PAIRED blueTooth devices near by
	//
	public void onClickedPairedListToggleButton() {
		if (tbShowPairedList.isChecked()) {
			Toast.makeText(getActivity(),
					getString(R.string.BluetoothShowingPairedDevices),
					Toast.LENGTH_SHORT).show();

			// Listing paired devices
            Set<BluetoothDevice> pairedDevices = MainActivity.mBluetoothAdapter.getBondedDevices();

			// Loop through paired devices
			for (BluetoothDevice device : pairedDevices) {
				// Add the name and address to an array adapter to show in a
				// ListView
				mArrayAdapter
						.add(device.getName() + "\n" + device.getAddress());
			}

		} else {
			mArrayAdapter.clear();
		}

	}

}
