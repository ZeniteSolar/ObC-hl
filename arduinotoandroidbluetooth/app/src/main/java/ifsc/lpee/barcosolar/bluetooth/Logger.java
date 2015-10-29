//////////////////////////////////////////////////////////////////////////////////////////////////
// INSTITUTO FEDERAL DE EDUCAÇÃO, CIÊNCIA E TECNOLOGIA DE SANTA CATARINA - CAMPUS FLORIANÓPOLIS //
// Equipe Zênite Solar                                                                          //
//////////////////////////////////////////////////////////////////////////////////////////////////

package ifsc.lpee.barcosolar.bluetooth;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by joaoantoniocardoso on 9/20/15.
 */

public class Logger {

    /* Checks if the device has free space*/

    public static void logger() {
        Thread thLogger = new Thread(new Runnable() {
            @Override
            public void run() {

                String content;
                String dirName = "Zenite";
                File dir = Files.mkDir(dirName);
                if(!dir.isDirectory()){
                    return;
                }
                Calendar rightNow = Calendar.getInstance();
                int day = rightNow.get(Calendar.DAY_OF_MONTH);
                int month = rightNow.get(Calendar.MONTH) + 1;//retorna um a menos que o mes atual
                int year = rightNow.get(Calendar.YEAR);
                content =
                        String.format("%02d", day) + "_" +
                                String.format("%02d", month)  + "_" +
                                String.format("%02d", year)  + "," +
                                "Temperature1"  + "," +
                                "Temperature2"  + "," +
                                "Current1"      + "," +
                                "Current2"      + "," +
                                "Voltage1"      + "," +
                                "Velocidade"    + "," +
                                "Latitude"      + "," +
                                "Longitude"     + "," +
                                "SOC"           + "," +
                                "\n";
                String fileName =
                        MainActivity.titulo          + " " +
                                String.format("%02d", day)   + "_" +
                                String.format("%02d", month) + "_" +
                                String.format("%02d", year ) + ".csv";
                if(!Files.isExternalStorageWritable()){
                    Log.e("Logger", "Error: Directory isn't writable");
                }
                Log.e("Logger", "Storage is writable");

                if(!Files.thereIsFreeSpace(dir)){
                    Log.e("Logger", "Error: There is no free space");
                    return;
                }
                Log.e("Logger", "There is free space");

                if(!Files.saveFile(dir.toString(), fileName, content, true)){
                    Log.e("Logger", "Error: can't save file");
                    return;
                }

                Log.e("Logger", "File saved at " + dir.toString() + "/" + fileName);


                while(MainActivity.log) {
                    rightNow = Calendar.getInstance();//tem que ti im while?
                    int hour = rightNow.get(Calendar.HOUR_OF_DAY);
                    int min = rightNow.get(Calendar.MINUTE);
                    int sec = rightNow.get(Calendar.SECOND);
                    int msec = rightNow.get(Calendar.MILLISECOND);
                    content =
                            String.format("%02d", hour) + ":" +
                            String.format("%02d", min)  + ":" +
                            String.format("%02d", sec)  + ":" +
                            String.format("%03d", msec) + "," +
                            String.format(Locale.US, "%3.1f",fragment_communication.Temperature1)  + "," +
                            String.format(Locale.US, "%3.1f",fragment_communication.Temperature2)  + "," +
                            String.format(Locale.US, "%3.1f",fragment_communication.Current1)      + "," +
                            String.format(Locale.US, "%3.1f",fragment_communication.Current2)      + "," +
                            String.format(Locale.US, "%3.1f",fragment_communication.Voltage1)      + "," +
                            String.format(Locale.US, "%3.1f",fragment_communication.Speed)         + "," +
                            Double.toString(fragment_communication.Latitude)                       + "," +
                            Double.toString(fragment_communication.Longitude)                      + "," +
                            Double.toString(StateOfCharge.soc)                                     + "," +
                            "\n";

                    if(!Files.thereIsFreeSpace(dir)){
                        Log.e("Logger", "Error: There is no free space");
                        break;
                    }
                    Log.e("Logger", "There is free space");

                    if(!Files.saveFile(dir.toString(), fileName, content, true)){
                        Log.e("Logger", "Error: can't save file");
                        break;
                    }

                    Log.e("Logger", "File saved at " + dir.toString() + "/" + fileName);

                    try {
                        Thread.sleep(1000);
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thLogger.start();
    }

}