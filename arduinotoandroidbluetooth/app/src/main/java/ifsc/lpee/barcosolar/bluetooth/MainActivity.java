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

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends Activity implements
		NavigationDrawerFragment.NavigationDrawerCallbacks,IBaseGpsListener{

	public static BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	public static BluetoothSocket mmSocket;
	public static BluetoothDevice mmDevice;
	public static OutputStream mmOutputStream;
	public static InputStream mmInputStream;
	public static boolean connected;

	public static String mTitle;
	public static boolean log = false;

	public static String titulo = "Default";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		//set up the fragment
		Fragment fragment; // main screen
		FragmentManager fragmentManager = getFragmentManager();
		fragment = new fragment_communication();
		mTitle = getString(R.string.title_section1);
		fragmentManager.beginTransaction().replace(R.id.container, fragment)
				.commit();

		LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,100, 0, this);

//		StateOfCharge.SOC();


	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
        //DONE 25/10/2015: salvar SOC atual.
        if(MainActivity.connected){
            Configurations.storeSOCConfigs();
        }

        //TODO: verificar se o gps ('local') esta ligado, caso contrario as linhas abaixo bugam o software
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,100, 0, this);
        locationManager.removeUpdates(this);
	}

	@Override
	public void onNavigationDrawerItemSelected(int position) {
	}


@Override
public boolean onCreateOptionsMenu(Menu menu) {
	getMenuInflater().inflate(R.menu.main, menu);
	return true;
}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {//trata do toque no menu, retorna true ser tratou a interrupçao
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		Fragment fragment; // main screen
		FragmentManager fragmentManager = getFragmentManager();
		int id = item.getItemId();

		if (id == R.id.title_section1){

			fragment = new fragment_communication();
			mTitle = getString(R.string.title_section1);
			fragmentManager.beginTransaction().replace(R.id.container, fragment)
					.commit();
			return true;
		}
		if(id == R.id.title_section2){
			fragment = new fragment_bluetooth();
			mTitle = getString(R.string.title_section2);
			fragmentManager.beginTransaction().replace(R.id.container, fragment)
					.commit();
			return true;
		}
		if(id == R.id.title_section3){
			Toast.makeText(getApplicationContext() , "Opção indisponível", Toast.LENGTH_SHORT)
					.show();
			return true;
		}
		return false;//TODO: Se retornar falso o app da crash, tratar isso.
	}

	public void TituloLoggerOnOff(View view) {//controla o toque na chave do Logger
		Switch chave = (Switch) findViewById(R.id.switch1);
		if (chave.isChecked()) {
			ChangeTituloView(true);
		}else{
			log = false;
			ChangeTituloView(false);
		}
	}

	public void ChangeTituloView(boolean i){//deixa ou não a opcao de titulo visivel
		View RL = findViewById(R.id.RLLogger);
		if(i) {
			RL.setVisibility(View.VISIBLE);
		}
		else{
			RL.setVisibility(View.GONE);
		}
	}


	public void TituloOk(View view){

		EditText Txt = (EditText) findViewById(R.id.editText);
		titulo = Txt.getText().toString();
		Toast.makeText(getBaseContext(),"Titulo definido como: " + titulo, Toast.LENGTH_LONG).show();
		ChangeTituloView(false);
		log = true;
		Logger.logger();
	}


	private void updateSpeed(Location location){
//		//TODO ???
		if (MainActivity.mTitle.equals(getString(R.string.title_section1))) {
			TextView tvVelocity = (TextView) findViewById(R.id.tvVelocity);
			String strCurrentSpeed = "_";
			if(location!=null) {
				fragment_communication.Speed = location.getSpeed() * 3.6f;//km/h
				fragment_communication.Latitude = location.getLatitude();
				fragment_communication.Longitude = location.getLongitude();
				strCurrentSpeed = String.format("%3.1f",fragment_communication.Speed);
			}
			tvVelocity.setText(strCurrentSpeed);
		}
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
		if(location != null){
			this.updateSpeed(location);
		}
	}


}