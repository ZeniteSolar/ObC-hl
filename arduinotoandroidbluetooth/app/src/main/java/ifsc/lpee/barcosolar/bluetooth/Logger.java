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

/**
 * Created by joaoantoniocardoso on 9/20/15.
 * <p/>
 * referencias:
 * arquivos: http://developer.android.com/training/basics/data-storage/files.html
 * data: http://developer.android.com/reference/java/util/Calendar.html
 * <p/>
 * permissões:
 * <manifest ...>
 * <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
 * ...
 * </manifest>
 */

public class Logger {

    /* Checks if the device has free space*/
<<<<<<< HEAD
    public static boolean thereIsFreeSpace(File dir) {
        return (dir.getFreeSpace() / dir.getTotalSpace()) <= 0.9f;
=======
    public static boolean checkFreeSpace(File dir) {
        if ((dir.getFreeSpace() / dir.getTotalSpace()) <= 0.9f)
            return true;
        return false;
>>>>>>> dc58aa4523b8e4bf00ab02f4b07a73daefd20635
    }

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state))
            return true;
        return false;
    }

    /* makes a dir named dirName at user's public Documents directory
    (note: it isn't deleted when this app is uninstalled) */
    public static File mkDir(String dirName) {
        // Get the directory for the user's public pictures directory.
        File dir = new File(Environment.getExternalStoragePublicDirectory(
<<<<<<< HEAD
                Environment.DIRECTORY_DCIM), dirName);
        if (!dir.exists()){
            Log.e("Logger", "Directory not exist, attempt to create...");

=======
                Environment.DIRECTORY_PICTURES), dirName);
        if (!dir.exists()){
            Log.e("Logger", "Directory not exist, attempt to create...");
            dir.mkdirs();
>>>>>>> dc58aa4523b8e4bf00ab02f4b07a73daefd20635
            if (!dir.mkdirs()) {
                Log.e("Logger", "Directory not created; check permissions");
            }
        }else{
            Log.e("Logger", "Directory already exists");
        }
        return dir;
    }

    /* save content into a file called fileName */
    public static boolean saveFile(String dir, String fileName, String content) {
        File file;
        FileOutputStream outputStream;
        try {
            file = new File(dir, fileName);
<<<<<<< HEAD
            outputStream = new FileOutputStream(file,true);
=======
            outputStream = new FileOutputStream(file, true);
>>>>>>> dc58aa4523b8e4bf00ab02f4b07a73daefd20635
            outputStream.write(content.getBytes());
            outputStream.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

<<<<<<< HEAD
    public static void logger() {
        Log.d("Marcio","Logger: Logger");
        Thread thLogger = new Thread(new Runnable() {
            @Override
            public void run() {
                while(MainActivity.log) {
                    Calendar rightNow = Calendar.getInstance();
                    int day = rightNow.get(Calendar.DAY_OF_MONTH);
                    int month = rightNow.get(Calendar.MONTH);
                    int year = rightNow.get(Calendar.YEAR);
                    int hour = rightNow.get(Calendar.HOUR_OF_DAY);
                    int min = rightNow.get(Calendar.MINUTE);
                    int sec = rightNow.get(Calendar.SECOND);
                    int msec = rightNow.get(Calendar.MILLISECOND);
                    String dirName = "Zenite";
                    String fileName = MainActivity.titulo + " " + day + "/" + month + "/" + year + ".csv";
                    String content = hour + ":" + min + ":" + sec + ":" + msec + "," +
                                    "leitura 1" + "," +
                                    "leitura 2" + "," +
                                    "leitura 3" +
                                    "\n";

                    if(!isExternalStorageWritable()){
                        Log.e("Logger", "Error: Directory isn't writable");
                        break;
                    }
                    Log.e("Logger", "Storage is writable");

                    File dir = mkDir(dirName);
                    if(!dir.isDirectory()){
                        break;
                    }

                    if(!thereIsFreeSpace(dir)){
                        Log.e("Logger", "Error: There is no free space");
                        break;
                    }
                    Log.e("Logger", "There is free space");

                    if(!saveFile(dir.toString(), fileName, content)){
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
=======
    public static int logger() {
        Calendar rightNow = Calendar.getInstance();
        int day = rightNow.get(Calendar.DAY_OF_MONTH);
        int month = rightNow.get(Calendar.MONTH);
        int year = rightNow.get(Calendar.YEAR);
        int hour = rightNow.get(Calendar.HOUR_OF_DAY);
        int min = rightNow.get(Calendar.MINUTE);
        int sec = rightNow.get(Calendar.SECOND);
        int msec = rightNow.get(Calendar.MILLISECOND);
        boolean error;
        String dirName = "Testes no LIC" + " " + year + month + day;
        String fileName = "nomedoarquivo" + ".csv";
        String content =
                        String.format("%02d", day) + "/" +
                        String.format("%02d", month) + "/" +
                        String.format("%02d", year ) + "," +
                        String.format("%02d", hour) + ":" +
                        String.format("%02d", min) + ":" +
                        String.format("%02d", sec) + ":" +
                        String.format("%03d", msec) + "," +
                        "leitura 1" + "," +
                        "leitura 2" + "," +
                        "leitura 3" +
                        "\n";

        error = isExternalStorageWritable();
        if(!error){
            Log.e("Logger", "Error: Directory isn't writable");
            return 20;
        }
        Log.e("Logger", "Storage is writable");

        File dir = mkDir(dirName);

        error = checkFreeSpace(dir);
        if(!error){
            Log.e("Logger", "Error: There is no free space");
            return 21;
        }
        Log.e("Logger", "There is free space");

        error = saveFile(dir.toString(), fileName, content);
        if(!error){
            Log.e("Logger", "Error: can't save file");
            return 22;
        }
        Log.e("Logger", "File saved at " + dir.toString() + "/" + fileName);
        return 0;
    }
}
>>>>>>> dc58aa4523b8e4bf00ab02f4b07a73daefd20635
