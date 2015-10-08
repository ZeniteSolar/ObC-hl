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


import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


import org.w3c.dom.Text;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Formatter;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends Activity implements
		NavigationDrawerFragment.NavigationDrawerCallbacks{

	public static BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	public static BluetoothSocket mmSocket;
	public static BluetoothDevice mmDevice;
	public static OutputStream mmOutputStream;
	public static InputStream mmInputStream;
	public static int flag1;

	public String mTitle;
	public static boolean log = false;

	public static String titulo = "Default";
	/**
	 * Used to store the last screen title. For use in
	 * {__link #restoreActionBar()}.
	 */
	//private CharSequence mTitle;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//requestWindowFeature(Window.FEATURE_NO_TITLE);//se tirar o titulo, buga
		Log.d("Marcio", "MainActivity: onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		/*
	  Fragment managing the behaviors, interactions and presentation of the
	  navigation drawer.
	 */
//		NavigationDrawerFragment mNavigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager()
//				.findFragmentById(R.id.navigation_drawer);
//		mTitle = getTitle();

		// Set up the drawer.
//		mNavigationDrawerFragment.setUp(R.id.navigation_drawer,
//	    			(DrawerLayout) findViewById(R.id.drawer_layout));

		//set up the fragment
		Fragment fragment; // main screen
		FragmentManager fragmentManager = getFragmentManager();
		fragment = new fragment_communication();
		mTitle = getString(R.string.title_section1);
		fragmentManager.beginTransaction().replace(R.id.container, fragment)
				.commit();

	}

	// http://stackoverflow.com/questions/20638967/how-to-change-fragments-using-android-navigation-drawer
	@Override
	public void onNavigationDrawerItemSelected(int position) {
		Log.d("Marcio", "MainActivity: onNavigationDrawerItemSelected " + position);
		// update the main content by replacing fragments
//		Fragment fragment = new fragment_bluetooth(); // main screen
//		FragmentManager fragmentManager = getFragmentManager();
//		switch (position) {
//		case 0:
//			fragment = new fragment_communication();
//			break;
//		case 1:
//			fragment = new fragment_bluetooth();
//			break;
//		case 2:
////			fragment = new fragment_bluetooth();
//			break;
//		}
//		fragmentManager.beginTransaction().replace(R.id.container, fragment)
//				.commit();
	}

//	public void onSectionAttached(int number) {
//		Log.d("Marcio", "MainActivity: onSectionAttached");
//		switch (number) {
//			case 1:
//				mTitle = getString(R.string.title_section1);
//				break;
//			case 2:
//				mTitle = getString(R.string.title_section2);
//				break;
//			case 3:
//				mTitle = getString(R.string.title_section3);
//				break;
//		}
//	}

//
//	public void restoreActionBar() {
//		ActionBar actionBar = getActionBar();
//		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
//		actionBar.setDisplayShowTitleEnabled(true);
//		actionBar.setTitle(mTitle);
@Override
public boolean onCreateOptionsMenu(Menu menu) {
	Log.d("Marcio", "MainActivity: onCreateOptionsMenu");
	getMenuInflater().inflate(R.menu.main, menu);
	return true;
}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {//trata do toque no menu, retorna true ser tratou a interrupçao
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		Log.d("Marcio", "MainActivity: onOptionsItemSelected " + item.toString());
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
		Log.e("Marcio", "Opção do main menu sem tratamento.");
		return false;//Se retornar falso o app da crash
		//return super.onOptionsItemSelected(item);
	}

	public void TituloLoggerOnOff(View view) {//controla o toque na chave do Logger
		Switch chave = (Switch) findViewById(R.id.switch1);
		if (chave.isChecked()) {
			Toast.makeText(getBaseContext(),"SwitchOn",Toast.LENGTH_LONG ).show();
			ChangeTituloView(true);
		}else{
			Toast.makeText(getBaseContext(),"SwitchOff",Toast.LENGTH_LONG ).show();
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

//	}


	/*
	 * A placeholder fragment containing a simple view.
	 */
	/*public static class PlaceholderFragment extends Fragment {
		/**
		private static final String ARG_SECTION_NUMBER = "section_number";
		/**
		 * Returns a new instance of this fragment for the given section number.
		 */
		/*
		public static PlaceholderFragment newInstance(int sectionNumber) {
			PlaceholderFragment fragment = new PlaceholderFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}
		public PlaceholderFragment() {
		}
*
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			return inflater.inflate(R.layout.fragment_communication,
					container, // main screen
					false);
		}
		@Override
		public void onAttach(Activity activity) {
			Log.d("Marcio", "MainActivity: onAttach");
			super.onAttach(activity);
			((MainActivity) activity).onSectionAttached(getArguments().getInt(
					ARG_SECTION_NUMBER));
		}
	}*/

	public void TituloOk(View view){
		Log.d("Marcio", "ActivityMain: TituloOk");

		EditText Txt = (EditText) findViewById(R.id.editText);
		titulo = Txt.getText().toString();
		Toast.makeText(getBaseContext(),"Titulo definido como: " + titulo, Toast.LENGTH_LONG).show();
		ChangeTituloView(false);
		log = true;
		Logger.logger();
	}




}