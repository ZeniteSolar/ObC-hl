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
    public boolean checkFreeSpace(File dir) {
        float freeSpace = dir.getFreeSpace();
        float totalSpace = dir.getTotalSpace();
        float full = 0.1f;
        if ((freeSpace / totalSpace) < full)
            return true;
        return false;
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state))
            return true;
        return false;
    }

    /* makes a dir named dirName at user's public Documents directory
    (note: it isn't deleted when this app is uninstalled) */
    public File mkDir(String dirName) {
        File dir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_NOTIFICATIONS), dirName);
        if (!dir.mkdirs()) {
            Log.e("logger", "Directory not created");
        }
        return dir;
    }

    /* save content into a file called fileName */
    public void saveFile(String dirName, String fileName, String content) {
        File file;
        FileOutputStream outputStream;
        try {
            file = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_NOTIFICATIONS), fileName);
            outputStream = new FileOutputStream(file);
            outputStream.write(content.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int logger() {
        Calendar rightNow = Calendar.getInstance();
        int day = rightNow.get(Calendar.DAY_OF_MONTH);
        int month = rightNow.get(Calendar.MONTH);
        int year = rightNow.get(Calendar.YEAR);
        int hour = rightNow.get(Calendar.HOUR_OF_DAY);
        int min = rightNow.get(Calendar.MINUTE);
        int sec = rightNow.get(Calendar.SECOND);
        int msec = rightNow.get(Calendar.MILLISECOND);
        String dirName = "Testes no lic" + " " + year + month + day;
        String fileName = "nomedoarquivo" + ".csv";
        String content =
                day + "/" + month + "/" + year + "," +
                        hour + ":" + min + ":" + sec + ":" + msec + "," +
                        "leitura 1" + "," +
                        "leitura 2" + "," +
                        "leitura 3" +
                        "\n";

        if (!isExternalStorageWritable()) return 1; //verifica se está disponível -> erro 1

        File dir = mkDir(dirName);
        checkFreeSpace(dir);

        if (!checkFreeSpace(dir)) return 2; //verifica se tem espaço livre -> erro 2

        saveFile(dir.toString(), fileName, content);

        return 0; // sucesso -> retorna 0;
    }
}
