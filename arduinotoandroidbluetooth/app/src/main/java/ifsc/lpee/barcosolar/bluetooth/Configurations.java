package ifsc.lpee.barcosolar.bluetooth;

import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Locale;

/**
 * Created by joaoantoniocardoso on 10/25/15.
 */

public class Configurations {

    private static String confDirName = "Zenite/.configs";
    private static String socFileName = "soc.csv";

    public static boolean restoreSOCConfigs() {
        try {
            String[] content = Files.readFile(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DCIM)+"/"+confDirName, socFileName, ",");
            if (content == null) {
                Log.e("Configs", "Error while loading configs file: content null");
                return false;
            } else {
                Log.d("Configs", "Ok, configs file loaded");
                StateOfCharge.C1 = Float.parseFloat(content[0]);
                StateOfCharge.R1 = Float.parseFloat(content[1]);
                StateOfCharge.C2 = Float.parseFloat(content[2]);
                StateOfCharge.R2 = Float.parseFloat(content[3]);
                StateOfCharge.NominalVoltage = Float.parseFloat(content[4]);
                StateOfCharge.soc_zero = Double.parseDouble(content[5]); //note that now soc_zero = soc
                StateOfCharge.soc_min = Double.parseDouble(content[6]);
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean storeSOCConfigs() {
        String content =
                String.format(Locale.US, "%3.1f", StateOfCharge.C1) + "," +
                String.format(Locale.US, "%3.1f", StateOfCharge.R1) + "," +
                String.format(Locale.US, "%3.1f", StateOfCharge.C2) + "," +
                String.format(Locale.US, "%3.1f", StateOfCharge.R2) + "," +
                String.format(Locale.US, "%f", StateOfCharge.NominalVoltage) + "," +
                String.format(Locale.US, "%f", StateOfCharge.soc) + "," +
                String.format(Locale.US, "%3.1f", StateOfCharge.soc_min) + "," +
                "\n";
        if (!Files.saveFile(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM)+"/"+confDirName, socFileName, content, false)) {
                Log.e("Configs", "Error: can't save configs file");
                return false;
        }
        return true;

    }
}