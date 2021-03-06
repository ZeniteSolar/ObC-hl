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
import java.util.concurrent.TimeUnit;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
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

	public static float Temperature1 = 0, Temperature2 = 0 , Voltage1 = 0, Current1= 0, Current2 = 0 , Speed = 0, dutyCycle = 0;
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
		tvAutonomy = (TextView) v.findViewById(R.id.tvAutonomyHr);
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
					@Override
					public void run() {
                        if (!Thread.currentThread().isInterrupted() && !stopWorker){
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

    private int temp;
    int flag = 0;
	void beginListenForData() {

		stopWorker = false;
		readBufferPosition = 0;

		workerThread = new Thread(new Runnable() {
			public void run() {
				byte sensor;
//				int flag = 0; // Idle
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
											MainActivity.mmInputStream.read(packetBytes, 0, 5);     //le packetbytes (7)
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
								if (packetBytes[0] == 0x02 && packetBytes[4] == 0x03) {
									sensor = packetBytes[1];
									switch (sensor) {                                                    //muda o valor dos sensores
										case (byte) 0xA0:
                                            temp = twoBytesToUnsignedInt(Arrays.copyOfRange(packetBytes, 2, 4));
                                            Log.d("Comunication", "Receive : " +
                                                    "\tTemp1: " + temp + " = 0x" + Integer.toHexString(temp));
                                            Temperature1 = mapFloat(temp, 0, 1023, 0, 150);
											updateTextView(tvTemperatureBattery, formatString("%3.0f",Temperature1));
											break;
										case (byte) 0xA1:
                                            temp = twoBytesToUnsignedInt(Arrays.copyOfRange(packetBytes, 2, 4));
                                            Log.d("Comunication", "Receive : " +
                                                    "\tTemp2: " + temp + " = 0x" + Integer.toHexString(temp));
											Temperature2 = mapFloat(temp, 0, 1023, 0, 150);
											updateTextView(tvTemperatureMotor, formatString("%3.0f", Temperature2));
											break;
										case (byte) 0xA2:
//                                            B=-0.0600942028985525
//                                            A=0.914695652173913
//                                            Current2 = (0.0600942028985525f + mapFloat(twoBytesToUnsignedInt(Arrays.copyOfRange(packetBytes, 2, 4)), 0, 1023, 0, 150))/0.914695652173913f;
                                            Current2 = (0.295361938692203f + mapFloat(twoBytesToUnsignedInt(Arrays.copyOfRange(packetBytes, 2, 4)), 0, 1023, 0, 150))/0.933446178777308f;
                                            if(Current2 < 8)//Pq o sensor não mede abaixo de 8 amperes
                                                Current2 = 0;
//                                            Log.d("Comunication", "MappedData" + "A4: " + Current2);
											break;
										case (byte) 0xA3: //MotorCurrent
//                                            Log.d("Comunication", "MappedData" + "A3: " + Current1);
                                            Current1 = (0.295361938692203f + mapFloat(twoBytesToUnsignedInt(Arrays.copyOfRange(packetBytes, 2, 4)), 0, 1023, 0, 150))/0.933446178777308f;
                                            if(Current1 < 8)//Pq o sensor não mede abaixo de 8 amperes
                                                Current1 = 0;
                                            updateTextView(tvPot,   formatString("%2.0f", Current1 * Voltage1 - Current2 * Voltage1));
                                            updateTextView(tvSOC,   formatString("%1.0f", 100 * StateOfCharge.soc));
                                            updateTextView(tvAutonomy,   formatString("%3.0f", 60*StateOfCharge.t_left));
                                            break;
										case (byte) 0xA4:
//                                            B=-0.15280165926038
//                                            A=1.1166336269336
                                            Voltage1 = (-0.304610861f + mapFloat(twoBytesToUnsignedInt(Arrays.copyOfRange(packetBytes, 2, 4)), 0, 1023, 0, 48))/0.89518123880825f;
//                                            Log.d("Comunication", "MappedData" + "A2: " + Voltage1);
                                            if(Voltage1 < 8)//Pq o sensor não mede abaixo de 8 Volts
                                                Voltage1 = 0;
                                            updateTextView(tvVoltageBattery, formatString("%2.1f", Voltage1));
                                            break;
										case (byte) 0xA5:
											//DONE 26/10/2015: calcular e mostrar o DutyCycle do PWM. Maior que 4.1*1023/5 é 0% e 0 é 100%.
                                            dutyCycle = mapFloat(twoBytesToUnsignedInt(Arrays.copyOfRange(packetBytes, 2, 4)), 0, 819, 100, 0); // de 0 a 4 v na entrada
                                            updateTextView(tvDutyCycle,   formatString("%2.1f", dutyCycle));
                                            Log.d("Comunication", "Receive : " +
                                                    "\t Temp1: " + Temperature1 +
                                                    "\t Temp2: " + Temperature2 +
                                                    "\t Volt: " + Voltage1 +
                                                    "\t Curr1: " + Current1 +
                                                    "\t Curr2: " + Current2 +
                                                    "\t DT: " + dutyCycle
                                            );
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

    public static int twoBytesToUnsignedInt(byte[] packetBytes) {
//        Log.d("Receive ->>>>>>", Integer.toHexString((0x000000FF & (int)packetBytes[1])) + " | " + Integer.toHexString((0x000000FF & (int)packetBytes[0])) + " = " + Integer.toHexString((((0x000000FF & (int)packetBytes[0]) << 8) | (0x000000FF & (int)packetBytes[1]))));
        return ((0x000000FF & (int)packetBytes[0]) << 8) | (0x000000FF & (int)packetBytes[1]);
    }

    public static float mapFloat(int value, int in_min, int in_max, int out_min, int out_max){
        if(value > in_max) value = in_max;
        else if (value < in_min) value = in_min;
        return ((float)value - (float)in_min) * ((float)out_max - (float)out_min) / ((float)in_max - (float)in_min) + (float)out_min;
    }

    public static String formatString(String format, double data){
        if(Double.isInfinite(data)){
            return "\u221e"; //infinity symbol
        }else if(Double.isNaN(data)){
            return " ? ";
        }
        return String.format(format, data);
    }

}