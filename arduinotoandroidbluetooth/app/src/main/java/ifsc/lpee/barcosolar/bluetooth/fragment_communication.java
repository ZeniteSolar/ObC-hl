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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Formatter;
import java.util.Locale;

import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class fragment_communication extends Fragment implements OnClickListener, IBaseGpsListener{

	TextView tvTemperatureBattery;
	TextView tvTemperatureMotor;
	TextView tvVoltageBattery;
	TextView tvVelocity;
	//	TextView tvCurrentBatteryIn;
//	TextView tvCurrentBatteryOut;
//	TextView tvPotIn;
//	TextView tvPotSaida;
	TextView tvPot;
	TextView tvPowerRate;
	TextView tvSystemStatus;
	TextView tvAutonomy;

	Switch switch1;

	public static float Temperature1 = 0, Temperature2 = 0 , Voltage1 = 0, Current1= 0, Current2 = 0, nCurrentSpeed = 0;

//	public BluetoothAdapter mBluetoothAdapter = new MainActivity().mBluetoothAdapter;
//	public BluetoothSocket mmSocket = new MainActivity().mmSocket;
//	public BluetoothDevice mmDevice = new MainActivity().mmDevice;
//	public OutputStream mmOutputStream = new MainActivity().mmOutputStream;
//	public InputStream mmInputStream = new MainActivity().mmInputStream;

	Thread workerThread;
	//	byte[] readBuffer;
	int readBufferPosition;
	volatile boolean stopWorker;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 0, this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.fragment_communication, container,
				false);

		// Intent i = getActivity().getIntent();

		tvPot = (TextView) v.findViewById(R.id.tvPot);
		tvTemperatureBattery = (TextView) v.findViewById(R.id.tvTemperatureBattery);
		tvTemperatureMotor = (TextView) v.findViewById(R.id.tvTemperatureMotor);
		tvVoltageBattery = (TextView) v.findViewById(R.id.tvVoltageBattery);
		tvVelocity = (TextView) v.findViewById(R.id.tvVelocity);
		tvAutonomy = (TextView) v.findViewById(R.id.tvAutonomy);
		switch1 = (Switch) v.findViewById(R.id.switch1);

		switch1.setChecked(MainActivity.log);

		if(MainActivity.flag1==1) {
			beginListenForData();
		}
		return v;
	}

	@Override
	public void onClick(View v) {
		Toast.makeText(getActivity(),"onClick:",Toast.LENGTH_LONG).show();
	}

	//	Handler mHandler;
	//ref: http://stackoverflow.com/questions/12716850/android-update-textview-in-thread-and-runnable
	private void updateTextView(final TextView textView, final String data) {

		Thread th = new Thread(new Runnable() {
			public void run() {
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						textView.setText(data);
						textView.setTextColor(Color.RED);
						//Log.d("Escrito na tela -> ", data + "  on  " + textView);
					}
				});
				try {
					Thread.sleep(1000);
				}
				catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		th.start();
	}

	void beginListenForData() {
		//Log.d("recebido -> ", "BluetoothAdapter" + ": " + MainActivity.mBluetoothAdapter);
		//Log.d("recebido -> ", "BluetoothSocket" + ": " + MainActivity.mmSocket);
		//Log.d("recebido -> ", "BluetoothDevice" + ": " + MainActivity.mmDevice);
		//Log.d("recebido -> ", "OutputStream" + ": " + MainActivity.mmOutputStream);
		//Log.d("recebido -> ", "InputStream" + ": " + MainActivity.mmInputStream);
//		final Handler handler = new Handler();
		stopWorker = false;
		readBufferPosition = 0;

		workerThread = new Thread(new Runnable() {
			public void run() {
				byte sensor;
				int flag = 0; // Idle
				byte[] packetBytes = new byte[7];
				while (!Thread.currentThread().isInterrupted() && !stopWorker) {
					try {
						switch (flag) {
							case 0: // Idle
								MainActivity.mmOutputStream.write(0x01);                            //envia o ACK
								//Log.d("enviado -> ", " ACK ");
								if (MainActivity.mmInputStream.available() > 0) {                   //se o buffer esta disponivel
									byte receivedByte1 = (byte) MainActivity.mmInputStream.read();  //recebe byte1
									if (receivedByte1 == 0x01) {                                    //se byte1 for de inicio ACK
										//Log.d("recebido -> ", " ACK ");
										if (MainActivity.mmInputStream.available() > 0) {           // se buffer esta disponivel
											MainActivity.mmInputStream.read(packetBytes, 0, 7);     //le packetbytes (7)
											//Log.d("recebido -> ", " packetBytes[0]: "
											//+ String.format("%20x", packetBytes[0]));
											//Log.d("recebido -> ", " packetBytes[1]: "
											//+ String.format("%20x", packetBytes[1]));
											//Log.d("recebido -> ", " packetBytes[2]: "
											//+ String.format("%20x", packetBytes[2]));
											//Log.d("recebido -> ", " packetBytes[3]: "
//													+ String.format("%20x", packetBytes[3]));
											//Log.d("recebido -> ", " packetBytes[4]: "
//													+ String.format("%20x", packetBytes[4]));
											//Log.d("recebido -> ", " packetBytes[5]: "
//													+ String.format("%20x", packetBytes[5]));
											//Log.d("recebido -> ", " packetBytes[6]: "
//													+ String.format("%20x", packetBytes[6]));
										}
										flag = 1;
									}
									else
										while(MainActivity.mmInputStream.available() > 0) {			//esvaziar buffer
											receivedByte1 = (byte) MainActivity.mmInputStream.read();
											//Log.d("->","Buffer Limpo");
										}
								}
								break;
							case 1:
								flag = 0;
								if (packetBytes[0] == 0x02 && packetBytes[6] == 0x03) {
									//Log.d("->", " VALIDADO ");
									sensor = packetBytes[1];
									//Log.d("Sensor = ",String.format("%20x", sensor));
									switch (sensor) {                                                    //muda o valor dos sensores
										case (byte) 0xA0:
											Temperature1 = fourBytesToFloat(Arrays.copyOfRange(packetBytes, 2, 6));
											//Log.d("->", " Atualiza T1 ");
											updateTextView(tvTemperatureBattery, Float.toString(Temperature1));
											break;
										case (byte) 0xA1:
											Temperature2 = fourBytesToFloat(Arrays.copyOfRange(packetBytes, 2, 6));
											updateTextView(tvTemperatureMotor, Float.toString(Temperature2));

											break;
										case (byte) 0xA2:
											Voltage1 = fourBytesToFloat(Arrays.copyOfRange(packetBytes, 2, 6));
											updateTextView(tvVoltageBattery, Float.toString(Voltage1));
											break;
										case (byte) 0xA3:
											Current1 = fourBytesToFloat(Arrays.copyOfRange(packetBytes, 2, 6));
//											updateTextView(tvCurrentBatteryIn,  Float.toString(Current1));
											break;
										case (byte) 0xA4:
											Current2 = fourBytesToFloat(Arrays.copyOfRange(packetBytes, 2, 6));
//											updateTextView(tvCurrentBatteryOut,  Float.toString(Current2));
//											updateTextView(tvPotIn,   Float.toString(Current1*Voltage1));
//											updateTextView(tvPotSaida,   Float.toString(Current2*Voltage1));
											updateTextView(tvPot,   Float.toString(Current1*Voltage1 - Current2*Voltage1));
											break;
										default:
											//Log.d("-> ", "DEFAUL  sensor");
											break;
									}
								}
								packetBytes[0] = 0;
								packetBytes[1] = 0;
								packetBytes[2] = 0;
								packetBytes[3] = 0;
								packetBytes[4] = 0;
								packetBytes[5] = 0;
								packetBytes[6] = 0;
								break;
							default:
								//Log.d("-> ", "DEFAUL  flag"
//										+ String.format("%20x", flag));
								flag = 0;
								break;
						}
					} catch (IOException ex) {
						// TODO: handle exception
						stopWorker = true;
					}
				}
			}
		});
		workerThread.start();
	}

	private float fourBytesToFloat(byte[] packetBytes) {
		return (ByteBuffer.wrap(packetBytes).order(ByteOrder.LITTLE_ENDIAN).getFloat());
	}

	/*
        public static int fourBytesToUnsignedInt(byte[] packetBytes) {
            byte a = packetBytes[0];
            byte b = packetBytes[1];
            byte c = packetBytes[2];
            byte d = packetBytes[3];
            return ((a << 24) | (b << 16) | (c << 8) | (d << 0));
        }
        public static int twoBytesToUnsignedInt(byte a, byte b) {
            return ((a << 8) | (b & 0xFF));
        }
        public static int oneByteToUnsignedInt(byte a) {
            return (a & 0xFF);
        }
        */

	private void updateSpeed(Location location){
//		//TODO Auto-generated method stub

		String strCurrentSpeed = "_";

		if(location!=null) {
			nCurrentSpeed = location.getSpeed() * 3.6f;

			strCurrentSpeed = String.format(Locale.US, "%3.1f",nCurrentSpeed);
		}

		updateTextView(tvVelocity,strCurrentSpeed); //atualiza o campo da velocidade
	}

	@Override
	public void onProviderDisabled(String provider) {
		this.updateSpeed(null);
	}

	@Override
	public void onProviderEnabled(String provider) {

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {

	}

	@Override
	public void onGpsStatusChanged(int event) {

	}

	@Override
	public void onLocationChanged(Location location) {//quando a localização mudar
		//TODO Auto-generated method stub
		if(location != null){
			this.updateSpeed(location);
		}
	}

}