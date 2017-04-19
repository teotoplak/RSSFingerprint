package project.teo.rssfingerprint;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private int PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method

        } else {
//            getScanningResults();
            //do something, permission was previously granted; or legacy device
            doingWork();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Do something with granted permission
            doingWork();

        }
    }

    private void doingWork() {

        //IMPORTANTE

        final WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifi.setWifiEnabled(true);
        WifiReceiver receiverWifi = new WifiReceiver(wifi);
        registerReceiver(receiverWifi, new IntentFilter(
                WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        Timer t = new Timer();
        wifi.startScan();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                wifi.startScan();
            }
        }, 1,150);
//        new Thread(new work(wifi)).start();



    }

//    private class work implements Runnable {
//        WifiManager wifi;
//
//        public work(WifiManager wifi) {
//            this.wifi = wifi;
//        }
//
//        @Override
//        public void run() {
//            while (true) {
//                int state = wifi.getWifiState();
//                wifi.startScan();
//                if (state == WifiManager.WIFI_STATE_ENABLED) {
//                    List<ScanResult> results = wifi.getScanResults();
//                    runOnUiThread(new writeWork(results.size() + ""));
//
//                }
//                try {
//                    Thread.sleep(100);
//                } catch (Exception ex) {
//
//                }
//            }
//
//        }
//    }

    private class writeWork implements Runnable {
        String string;

        public writeWork(String string) {
            this.string = string;
        }

        @Override
        public void run() {
            TextView textView = (TextView) findViewById(R.id.textView);
            textView.setText("");
            textView.append(string + "\n");
        }
    }


    private void sout(String string) {

        TextView textView = (TextView) findViewById(R.id.textView);
        textView.setText("");
        textView.append(string + "\n");
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
                string += result.level;
                string += " " + result.SSID;
                string += "\n";
            }
            sout(string);

            wifi.startScan();

        }
    }


}
