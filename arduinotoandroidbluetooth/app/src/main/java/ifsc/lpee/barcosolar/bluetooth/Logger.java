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
    public static boolean checkFreeSpace(File dir) {
        if ((dir.getFreeSpace() / dir.getTotalSpace()) <= 0.9f)
            return true;
        return false;
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
                Environment.DIRECTORY_PICTURES), dirName);
        if (!dir.exists()){
            Log.e("Logger", "Directory not exist, attempt to create...");
            dir.mkdirs();
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
            outputStream = new FileOutputStream(file);
            outputStream.write(content.getBytes());
            outputStream.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

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
                day + "/" + month + "/" + year + "," +
                        hour + ":" + min + ":" + sec + ":" + msec + "," +
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
