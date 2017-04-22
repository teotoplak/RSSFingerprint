package project.teo.rssfingerprint;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private final int PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION = 1;
    private final int INTERVAL_FOR_SCAN = 50;
    private final String FINGERPRINT_FILE_NAME = "RSSFingerPrint.txt";
    private boolean writtingToFile = false;
    private Map<String,List<Integer>> rssMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        //setting up buttons
        final Button startButton = (Button) findViewById(R.id.startButton);
        final Button stopButton = (Button) findViewById(R.id.stopButton);
        final Button exportButton = (Button) findViewById(R.id.exportButton);
        final Button clearButton = (Button) findViewById(R.id.clearButton);
        stopButton.setEnabled(false);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writtingToFile = true;
                startButton.setEnabled(false);
                stopButton.setEnabled(true);
                rssMap.clear();
            }
        });
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writtingToFile = false;
                startButton.setEnabled(true);
                stopButton.setEnabled(false);
                EditText fingerprintEdit = (EditText) findViewById(R.id.fingerprintName);
                String fingerprintName = fingerprintEdit.getText().toString();
                EditText roomNameEdit = (EditText) findViewById(R.id.roomName);
                String roomName = roomNameEdit.getText().toString();
                String output = "";
                output += "Fingerprint name: " + fingerprintName + "\n";
                output += "Room name: " + roomName + "\n";
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DATE, 1);
                SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                output += "Date: " + format1.format(cal.getTime()) + "\n";
                output += "---------------------" + "\n";
                for (Map.Entry<String,List<Integer>> entry : rssMap.entrySet()) {
                    output += entry.getKey() + " " + convertListToString(entry.getValue()) + "\n";
                }
                output += "\n";
                try {
                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(getApplicationContext().openFileOutput(FINGERPRINT_FILE_NAME, MODE_APPEND));
                    outputStreamWriter.write(output);
                    outputStreamWriter.close();
                }
                catch (IOException e) {
                    Log.e("Exception", "File write failed: " + e.toString());
                    Toast.makeText(getApplicationContext(), "Problem writing to text file!", Toast.LENGTH_LONG).show();
                }
            }
        });
        exportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String extFileLocation = writeToExternal(getApplicationContext(),FINGERPRINT_FILE_NAME);
                File filelocation = new File(getApplicationContext().getExternalFilesDir(null), FINGERPRINT_FILE_NAME);
                Uri path = Uri.fromFile(filelocation);
                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent .setType("vnd.android.cursor.dir/email");
                emailIntent .putExtra(Intent.EXTRA_STREAM, path);
                emailIntent .putExtra(Intent.EXTRA_SUBJECT, "RSSFingerprint");
                startActivity(Intent.createChooser(emailIntent , "Send email..."));
            }
        });
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearAllFiles();
                Toast.makeText(getApplicationContext(), "Clearing fingerprint file...", Toast.LENGTH_SHORT).show();
            }
        });


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION);
            //After this point you wait for callback in onRequestPermissionsResult

        } else {
            startScanning();
        }

    }

    /**
     * Clearing both external and internal fingerprint file
     */
    public void clearAllFiles() {
        File file = new File(getApplicationContext().getExternalFilesDir(null), FINGERPRINT_FILE_NAME);
        file.delete();
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(getApplicationContext().openFileOutput(FINGERPRINT_FILE_NAME, MODE_ENABLE_WRITE_AHEAD_LOGGING));
            outputStreamWriter.write("");
            outputStreamWriter.close();
            TextView textView = (TextView) findViewById(R.id.fileLocationText);
            textView.setText("");
        }
        catch (IOException e) {
            Log.e("Exception", "File clear failed: " + e.toString());
            Toast.makeText(getApplicationContext(), "Problem clearing to text file!", Toast.LENGTH_LONG);
        }

    }


    public String writeToExternal(Context context, String filename){
        try {
            File file = new File(context.getExternalFilesDir(null), filename); //Get file location from external source
            String extFileLocation = context.getFilesDir() + File.separator + filename;
            InputStream is = new FileInputStream(extFileLocation); //get file location from internal
            OutputStream os = new FileOutputStream(file); //Open your OutputStream and pass in the file you want to write to
            byte[] toWrite = new byte[is.available()]; //Init a byte array for handing data transfer
            Log.i("Available ", is.available() + "");
            int result = is.read(toWrite); //Read the data from the byte array
            Log.i("Result", result + "");
            os.write(toWrite); //Write it to the output stream
            is.close(); //Close it
            os.close(); //Close it
            Log.i("Copying to", "" + context.getExternalFilesDir(null) + File.separator + filename);
            Log.i("Copying from", context.getFilesDir() + File.separator + filename + "");
            TextView textView = (TextView) findViewById(R.id.fileLocationText);
            textView.setText("Exported to: " + context.getExternalFilesDir(null).toString());
            return extFileLocation;
        } catch (Exception e) {
            Toast.makeText(context, "File write failed: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show(); //if there's an error, make a piece of toast and serve it up
        }
        return null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Do something with granted permission
            startScanning();

        }
    }

    private void startScanning() {

        final WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifi.setWifiEnabled(true);
        WifiReceiver receiverWifi = new WifiReceiver(wifi);
        registerReceiver(receiverWifi, new IntentFilter(
                WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                wifi.startScan();
            }
        }, 0,INTERVAL_FOR_SCAN);


    }

    private void writeToTextArea(String string) {

        TextView textView = (TextView) findViewById(R.id.textView);
        textView.setText("");
        textView.append(string + "\n");
    }

    long lastTime = -1;
    private void writeScanningInterval() {
        if (lastTime == -1) {
            lastTime = System.currentTimeMillis();
            return;
        } else {
            long period = System.currentTimeMillis() - lastTime;
            lastTime = System.currentTimeMillis();
            TextView intervalText = (TextView) findViewById(R.id.intervalText);
            intervalText.setText(period + "");
        }
    }


    class WifiReceiver extends BroadcastReceiver {

        private WifiManager wifi;

        public WifiReceiver(WifiManager wifi) {
            this.wifi = wifi;
        }

        public void onReceive(Context c, Intent intent) {

            List<ScanResult> wifiList = wifi.getScanResults();
            String string = "";
            for (ScanResult result : wifiList) {
                    String ssid = result.SSID;
                    Integer level = result.level;
                if (writtingToFile) {
                    if (!rssMap.containsKey(ssid)) {
                        rssMap.put(ssid, new LinkedList<Integer>());
                    }
                    rssMap.get(ssid).add(level);
                }
                string += level + " " + ssid  + "\n";
            }
            writeToTextArea(string);
            writeScanningInterval();

            wifi.startScan();

        }
    }

    private String convertListToString(List<Integer> list) {
        if (list == null) {
            return "";
        }
        String finalString = "[";
        for (Integer i : list) {
            finalString += i + " ";
        }
        finalString += "]";
        return  finalString;
    }



}
