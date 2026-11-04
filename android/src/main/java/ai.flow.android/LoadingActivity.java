package ai.flow.android;

import ai.flow.R;
import ai.flow.common.ParamsInterface;
import ai.flow.openpilot.ServiceCalibrationd;
import ai.flow.openpilot.ServiceControlsd;
import ai.flow.openpilot.ServiceDebugd;
import ai.flow.openpilot.ServiceFlowreset;
import ai.flow.openpilot.ServiceKeyvald;
import ai.flow.openpilot.ServiceLogmessaged;
import ai.flow.openpilot.ServicePlannerd;
import ai.flow.openpilot.ServiceRadard;
import ai.flow.openpilot.ServiceThermald;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.Settings;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.badlogic.gdx.Gdx;
import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ai.flow.flowy.PythonRunner;

import static android.os.Build.VERSION.SDK_INT;

import org.kivy.android.PythonUtil;

public class LoadingActivity extends AppCompatActivity {

    List<String> requiredPermissions = Arrays.asList(Manifest.permission.CAMERA,
            //Manifest.permission.WRITE_EXTERNAL_STORAGE, // unused on android 13+
            //Manifest.permission.READ_EXTERNAL_STORAGE, // unused on android 13+
//            Manifest.permission.RECORD_AUDIO,
//            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.VIBRATE);

    public boolean bootComplete = false;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        Window window = getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_loading);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        ImageView imageView = findViewById(R.id.spinner);
        Glide.with(this).load(R.drawable.spinner).into(imageView);

        ensureBoot();
    }

    private boolean checkPermissions() {
        for (String permission: requiredPermissions){
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void ensureBoot(){
        new Thread(new Runnable() {
            public void run() {
                // request permissions and wait till granted.
                requestPermissions();
                int i = 0;
                while (!checkPermissions()){
                    // show toast every 4 seconds
                    if (i%40 == 0) {
                        try {
                            Toast.makeText(getApplicationContext(), "Flowpilot needs all required permissions to be granted to work.", Toast.LENGTH_LONG).show();
                        } catch (Exception e) { }
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    i++;
                }

                // Hack to clean out Params
                ServiceFlowreset.prepare(getApplication().getApplicationContext());
                ServiceFlowreset.start(getApplication().getApplicationContext(), "");

                // These can run all the time
                ServiceKeyvald.prepare(getApplication().getApplicationContext());
                ServiceKeyvald.start(getApplication().getApplicationContext(), "");
                ServiceLogmessaged.prepare(getApplication().getApplicationContext());
                ServiceLogmessaged.start(getApplication().getApplicationContext(), "");
                ServiceThermald.prepare(getApplication().getApplicationContext());
                ServiceThermald.start(getApplication().getApplicationContext(), "");

                ServiceDebugd.prepare(getApplication().getApplicationContext());
                ServiceDebugd.start(getApplication().getApplicationContext(), "");

                // These will spinloop until CarParams become available, for startup performance
                ServiceControlsd.prepare(getApplication().getApplicationContext());
                ServiceControlsd.start(getApplication().getApplicationContext(), "");
                ServiceRadard.prepare(getApplication().getApplicationContext());
                ServiceRadard.start(getApplication().getApplicationContext(), "");
                ServiceCalibrationd.prepare(getApplication().getApplicationContext());
                ServiceCalibrationd.start(getApplication().getApplicationContext(), "");
                ServicePlannerd.prepare(getApplication().getApplicationContext());
                ServicePlannerd.start(getApplication().getApplicationContext(), "");

                try {
                    ParamsInterface params = ParamsInterface.getInstance();
                    params.blockTillExists("F3");
                } catch (InterruptedException ignored) {}
                bootComplete = true;

                Intent intent = new Intent(getApplicationContext(), AndroidLauncher.class);
                startActivity(intent);

                // destroy current activity
                finish();
            }
        }).start();
    }

    private void requestPermissions() {
        List<String> requestPermissions = new ArrayList<>();
        for (String permission: requiredPermissions){
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED)
                requestPermissions.add(permission);
        }
        if (!requestPermissions.isEmpty())
            ActivityCompat.requestPermissions(this, requestPermissions.toArray(new String[0]), 1);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        return;
    }
}