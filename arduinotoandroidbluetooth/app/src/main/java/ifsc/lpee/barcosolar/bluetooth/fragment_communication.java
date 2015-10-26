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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

public class fragment_communication extends Fragment {

	RelativeLayout mainScreen;
	RelativeLayout RLTensao;
	TextView tvTemperatureBattery;
	TextView tvTemperatureMotor;
	TextView tvVoltageBattery;
	TextView tvVelocity;
	TextView tvPot;
	TextView tvSOC;
	TextView tvAutonomy;
	TextView tvDutyCycle;


	Switch switch1;

	public static float Temperature1 = 0, Temperature2 = 0 , Voltage1 = 24, Current1= 0, Current2 = 0 , Speed = 0, duttyCycle;
	public static double Latitude = 0, Longitude = 0;

	Thread workerThread;

	int readBufferPosition;
	volatile boolean stopWorker;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.fragment_communication, container,
				false);

		mainScreen = (RelativeLayout) v.findViewById(R.id.MainScreen);
		RLTensao = (RelativeLayout) v.findViewById(R.id.RLTensao);
		tvPot = (TextView) v.findViewById(R.id.tvPot);
		tvSOC = (TextView) v.findViewById(R.id.tvSOC);
		tvTemperatureBattery = (TextView) v.findViewById(R.id.tvTemperatureBattery);
		tvTemperatureMotor = (TextView) v.findViewById(R.id.tvTemperatureMotor);
		tvVoltageBattery = (TextView) v.findViewById(R.id.tvVoltageBattery);
		tvVelocity = (TextView) v.findViewById(R.id.tvVelocity);
		tvAutonomy = (TextView) v.findViewById(R.id.tvAutonomy);
		tvDutyCycle = (TextView) v.findViewById(R.id.tvDutycycle);
		switch1 = (Switch) v.findViewById(R.id.switch1);

		switch1.setChecked(MainActivity.log);

		if(MainActivity.connected) {
			beginListenForData();
		}

		return v;
	}

	//	Handler mHandler;
	//ref: http://stackoverflow.com/questions/12716850/android-update-textview-in-thread-and-runnable
	private void updateTextView(final TextView textView, final String data) {
    //TODO: essa thread ainda esta dando crash quando tentamos fechar o app. Adicionei a condicional para tentar resolver mas nao resolveu.
		Thread th = new Thread(new Runnable() {
			public void run() {
				getActivity().runOnUiThread(new Runnable() {
                    //TODO: essa thread ta dando crash ao encerrar o app. Provavelmente temos que parar ela antes de encerrar o app.
					@Override
					public void run() {
                        if (!Thread.currentThread().isInterrupted() && !stopWorker && MainActivity.mTitle.equals(getString(R.string.title_section1))){
                            try{
                                textView.setText(data);

                            }catch (Exception e){
                                e.printStackTrace();
                            }
                            try{
                                mainScreen.setBackgroundColor(
                                        (Temperature1 > 70 ||Temperature2 > 70 ||
                                                StateOfCharge.soc <= StateOfCharge.soc_min) ?
                                                Color.RED : Color.WHITE);
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
					}
				});
			}
		});
		th.start();
	}

	void beginListenForData() {

		stopWorker = false;
		readBufferPosition = 0;

		workerThread = new Thread(new Runnable() {
			public void run() {
				byte sensor;
				int flag = 0; // Idle
				byte[] packetBytes = new byte[7];
				while (!Thread.currentThread().isInterrupted() && !stopWorker) {
					if(MainActivity.mBluetoothAdapter.getState() == 0){//se desconectado
						MainActivity.connected = false;
						break;
					}
					try {
						switch (flag) {
							case 0: // Idle
								MainActivity.mmOutputStream.write(0x01);                            //envia o ACK
								if (MainActivity.mmInputStream.available() > 0) {                   //se o buffer esta disponivel
									byte receivedByte1 = (byte) MainActivity.mmInputStream.read();  //recebe byte1
									if (receivedByte1 == 0x01) {                                    //se byte1 for de inicio ACK
										if (MainActivity.mmInputStream.available() > 0) {           // se buffer esta disponivel
											MainActivity.mmInputStream.read(packetBytes, 0, 7);     //le packetbytes (7)
										}
										flag = 1;
									}
									else
										while(MainActivity.mmInputStream.available() > 0) {			//esvaziar buffer
											MainActivity.mmInputStream.read();
										}
								}
								break;
							case 1:
								flag = 0;
								if (packetBytes[0] == 0x02 && packetBytes[6] == 0x03) {
									sensor = packetBytes[1];
									switch (sensor) {                                                    //muda o valor dos sensores
										case (byte) 0xA0:
											Temperature1 = fourBytesToFloat(Arrays.copyOfRange(packetBytes, 2, 6));
											updateTextView(tvTemperatureBattery, String.format("%3.1f",Temperature1));
											break;
										case (byte) 0xA1:
											Temperature2 = fourBytesToFloat(Arrays.copyOfRange(packetBytes, 2, 6));
											updateTextView(tvTemperatureMotor, String.format("%3.1f", Temperature2));
											break;
										case (byte) 0xA2:
											String str = "_";
											//correcao do valor obtido para a placa do João
											//Voltage1 = (float) (1.0542105263*fourBytesToFloat(Arrays.copyOfRange(packetBytes, 2, 6)) + 0.1731578947);
											Voltage1 = fourBytesToFloat(Arrays.copyOfRange(packetBytes, 2, 6));
											if (Voltage1 > 8){
												str = String.format("%3.1f", Voltage1);
											}
											updateTextView(tvVoltageBattery, str);
											break;
										case (byte) 0xA3:
											Current1 = fourBytesToFloat(Arrays.copyOfRange(packetBytes, 2, 6));
											break;
										case (byte) 0xA4:
											Current2 = fourBytesToFloat(Arrays.copyOfRange(packetBytes, 2, 6));
											updateTextView(tvPot,   String.format("%2.0f", Current1 * Voltage1 - Current2 * Voltage1));
											updateTextView(tvSOC,   String.format("%1.0f", 100 * StateOfCharge.soc));
											updateTextView(tvAutonomy,   String.format("%1.0f", StateOfCharge.t_remain));

											break;
										case (byte) 0xA5:
											//TODO: calcular e mostrar o DutyCycle do PWM. Maior que 4.1*1023/5 é 0% e 0 é 100%.
											duttyCycle = 1f - fourBytesToUnsignedInt(Arrays.copyOfRange(packetBytes, 2, 6))/209f;
											updateTextView(tvDutyCycle,   String.format("%1.0f", StateOfCharge.t_remain));
											break;
										default:
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
								flag = 0;
								break;
						}
					} catch (IOException ex) {
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

	public static int fourBytesToUnsignedInt(byte[] packetBytes) {
		byte a = packetBytes[0];
		byte b = packetBytes[1];
		byte c = packetBytes[2];
		byte d = packetBytes[3];
		return ((a << 24) | (b << 16) | (c << 8) | (d << 0));
	}

}