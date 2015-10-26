package ifsc.lpee.barcosolar.bluetooth;

import android.os.Environment;
import android.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by joaoantoniocardoso on 10/25/15.
 *
 * referencias:
 * arquivos: http://developer.android.com/training/basics/data-storage/files.html
 * data: http://developer.android.com/reference/java/util/Calendar.html
 *
 * permissões:
 * <manifest ...>
 * <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
 * ...
 * </manifest>
 */
public class Files {

    public static boolean thereIsFreeSpace(File dir) {
        return (dir.getFreeSpace() / dir.getTotalSpace()) <= 0.9f;
    }

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /* makes a dir named dirName at user's public Documents directory
    (note: it isn't deleted when this app is uninstalled) */
    public static File mkDir(String dirName) {
        // Get the directory for the user's public pictures directory.
        File dir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM), dirName);
        if (!dir.exists()){
            Log.e("Logger", "Directory not exist, attempt to create...");

            if (!dir.mkdirs()) {
                Log.e("Logger", "Directory not created; check permissions");
            }
        }else{
            Log.e("Logger", "Directory already exists");
        }
        return dir;
    }

    /* save content into a file called fileName */
    public static boolean saveFile(String dir, String fileName, String content, boolean append) {
        File file;
        FileOutputStream outputStream;
        try {
            file = new File(dir, fileName);
            outputStream = new FileOutputStream(file, append);
            outputStream.write(content.getBytes());
            outputStream.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    //ref: http://www.mkyong.com/java/how-to-read-and-parse-csv-file-in-java/
    public static String[] readFile(String dir, String fileName, String separator){
        BufferedReader br = null;
        String line = "";
        try {
            // Get the directory for the user's public pictures directory.

            br = new BufferedReader(new FileReader(dir+"/"+fileName));
            while ((line = br.readLine()) != null) {
                //retorna um vetor com as strings que foram separadas por virgulas
                return line.split(separator);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

}
