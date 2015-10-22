package ifsc.lpee.barcosolar.bluetooth;

import java.io.IOException;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

public class ConnectThread extends Thread {

	public static final UUID MY_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB"); // generic UUID
	public BluetoothAdapter myBluetoothAdapter;

	public ConnectThread(BluetoothDevice device) {
		// Use a temporary object that is later assigned to mmSocket,
		// because mmSocket is final
		BluetoothSocket tmp = null;
		MainActivity.mmDevice = device;

		// Get a BluetoothSocket to connect with the given BluetoothDevice
		try {
			// MY_UUID is the app's UUID string, also used by the server
			// code
			tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
		} catch (IOException e) {
		}
        MainActivity.mmSocket = tmp;
	}

	public void run() {
		myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		// Cancel discovery because it will slow down the connection
		myBluetoothAdapter.cancelDiscovery();

		try {
			// Connect the device through the socket. This will block
			// until it succeeds or throws an exception
            MainActivity.mmSocket.connect();
            MainActivity.mmOutputStream = MainActivity.mmSocket.getOutputStream();
            MainActivity.mmInputStream = MainActivity.mmSocket.getInputStream();

		} catch (IOException connectException) {
			// Unable to connect; close the socket and get out
			try {
                MainActivity.mmSocket.close();
			} catch (IOException closeException) {
			}
			return;
		}

		// Do work to manage the connection (in a separate thread)
        MainActivity.connected=true;

	}

}
