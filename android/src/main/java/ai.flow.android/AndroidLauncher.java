package ai.flow.android;

import ai.flow.android.sensor.CameraHandler;
//import ai.flow.android.vision.ONNXModelRunner;
//import ai.flow.android.vision.SNPEModelRunner;
import ai.flow.android.sensor.CameraManager;
import ai.flow.android.vision.THNEEDModelRunner;
import ai.flow.app.FlowUI;
import ai.flow.common.ParamsInterface;
import ai.flow.common.Path;
import ai.flow.common.transformations.Camera;
import ai.flow.common.utils;
import ai.flow.hardware.HardwareManager;
import ai.flow.launcher.Launcher;
import ai.flow.modeld.*;
import ai.flow.sensor.SensorInterface;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Process;
import android.os.*;
import android.provider.Settings;
import android.system.ErrnoException;
import android.system.Os;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.AndroidFragmentApplication;

import org.acra.ACRA;
import org.acra.ErrorReporter;

import java.util.*;

import ai.flow.android.sensor.PandaManager;
import ai.flow.android.sensor.OnroadManager;


/** Launches the main android flowpilot application. */
public class AndroidLauncher extends FragmentActivity implements AndroidFragmentApplication.Callbacks {
	public static Map<String, SensorInterface> sensors;
	public static Map<String, SensorInterface> managers;
	public static Context appContext;
	public static ParamsInterface params;

	@SuppressLint("HardwareIds")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		appContext = getApplicationContext();

		// set environment variables from intent extras.
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			for (String key : bundle.keySet()) {
				if (bundle.get(key) == null)
					continue;
				try {
					Os.setenv(key, (String)bundle.get(key), true);
				} catch (Exception ignored) {
				}
			}
		}

		try {
			Os.setenv("USE_GPU", "1", true);
		} catch (ErrnoException e) {
			throw new RuntimeException(e);
		}

		Window activity = getWindow();
		HardwareManager androidHardwareManager = new AndroidHardwareManager(activity);
		androidHardwareManager.enableScreenWakeLock(true);
		activity.setSustainedPerformanceMode(true);
		activity.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


		params = ParamsInterface.getInstance();

		TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		String dongleID = "";
		if (telephonyManager != null) {
			dongleID = Settings.Secure.getString(appContext.getContentResolver(), Settings.Secure.ANDROID_ID);
		}

		// populate device specific info.
		params.put("DongleId", dongleID);
		params.put("DeviceManufacturer", Build.MANUFACTURER);
		params.put("DeviceModel", Build.MODEL);

		AndroidApplicationConfiguration configuration = new AndroidApplicationConfiguration();
//		SensorManager sensorManager = new SensorManager(appContext, 20);
		CameraHandler cameraManager = new CameraHandler(getApplication().getApplicationContext());
//		CameraManager cameraManager = new CameraManager(getApplication().getApplicationContext(), utils.F2 || Camera.FORCE_TELE_CAM_F3 ? Camera.CAMERA_TYPE_ROAD : Camera.CAMERA_TYPE_WIDE);

		PandaManager pandaManager = new PandaManager(getApplication().getApplicationContext());
		OnroadManager onroadManager = new OnroadManager(getApplication().getApplicationContext());
//		ModelparsedManager modelparsedManager = new ModelparsedManager(getApplication().getApplicationContext());
		managers = new HashMap<String, SensorInterface>() {{
			put("panda", pandaManager);
			put("onroad", onroadManager);
//			put("modelparsed", modelparsedManager);
		}};
		sensors = new HashMap<String, SensorInterface>() {{
			put("roadCamera", cameraManager);
//			put("motionSensors", sensorManager);
		}};


		int pid = Process.myPid();

		String modelPath = Path.getModelDir();

		ModelRunner model = null;
		boolean useGPU = true; // always use gpus on android phones.
		switch (utils.Runner) {
//			case SNPE:
//				model = new SNPEModelRunner(getApplication(), modelPath, useGPU);
//				break;
//			case TNN:
//				model = new TNNModelRunner(modelPath, useGPU);
//				break;
//			case ONNX:
//				model = new ONNXModelRunner(modelPath, useGPU);
//				break;
			case THNEED:
				model = new THNEEDModelRunner(modelPath, getApplication());
				break;
			case EXTERNAL_TINYGRAD:
				// start the special model parser
				/*Intent intent = new Intent();
				intent.setClassName(TermuxConstants.TERMUX_PACKAGE_NAME, TermuxConstants.TERMUX_APP.RUN_COMMAND_SERVICE_NAME);
				intent.setAction(TermuxConstants.TERMUX_APP.RUN_COMMAND_SERVICE.ACTION_RUN_COMMAND);
				intent.putExtra(TermuxConstants.TERMUX_APP.RUN_COMMAND_SERVICE.EXTRA_COMMAND_PATH, "/data/data/com.termux/files/usr/bin/bash");
				intent.putExtra(TermuxConstants.TERMUX_APP.RUN_COMMAND_SERVICE.EXTRA_ARGUMENTS, new String[]{"run"});
				intent.putExtra(TermuxConstants.TERMUX_APP.RUN_COMMAND_SERVICE.EXTRA_WORKDIR, "/data/data/com.termux/files/home/flowpilot_env_root/root/flowpilot/thneedrunner");
				intent.putExtra(TermuxConstants.TERMUX_APP.RUN_COMMAND_SERVICE.EXTRA_BACKGROUND, true);
				intent.putExtra(TermuxConstants.TERMUX_APP.RUN_COMMAND_SERVICE.EXTRA_SESSION_ACTION, "0");
				intent.putExtra(TermuxConstants.TERMUX_APP.RUN_COMMAND_SERVICE.EXTRA_COMMAND_LABEL, "run thneedrunner");
				startService(intent);*/
				break;
		}

		ModelExecutor modelExecutor;
		if (utils.Runner == utils.USE_MODEL_RUNNER.EXTERNAL_TINYGRAD)
			modelExecutor = new ModelExecutorExternal();
		else
			modelExecutor = new ModelExecutorF3(model);
		Launcher launcher = new Launcher(sensors, modelExecutor, managers);


		ErrorReporter ACRAreporter = ACRA.getErrorReporter();
		ACRAreporter.putCustomData("DongleId", dongleID);
//		ACRAreporter.putCustomData("AndroidAppVersion", ai.flow.app.BuildConfig.VERSION_NAME);
		ACRAreporter.putCustomData("FlowpilotVersion", params.getString("Version"));
		ACRAreporter.putCustomData("VersionMisMatch", checkVersionMisMatch().toString());

		ACRAreporter.putCustomData("GitCommit", params.getString("GitCommit"));
		ACRAreporter.putCustomData("GitBranch", params.getString("GitBranch"));
		ACRAreporter.putCustomData("GitRemote", params.getString("GitRemote"));


		MainFragment fragment = new MainFragment(new FlowUI(launcher, androidHardwareManager, pid));
//		cameraManager.setLifeCycleFragment(fragment);
		FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
		trans.replace(android.R.id.content, fragment);
		trans.commit();
	}

	public static class MainFragment extends AndroidFragmentApplication {
		FlowUI flowUI;

		MainFragment(FlowUI flowUI) {
			this.flowUI = flowUI;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			return initializeForView(flowUI);
		}
	}

	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);
	}

	private Boolean checkVersionMisMatch() {
		// check version mismatch between android app and github repo project.
//		if (!params.getString("Version").equals(ai.flow.app.BuildConfig.VERSION_NAME)) {
//			Toast.makeText(appContext, "WARNING: App version mismatch detected. Make sure you are using compatible versions of apk and github repo.", Toast.LENGTH_LONG).show();
//			return true;
//		}
		return false;
	}

	@Override
	public void exit() {
	}

	@Override
	public void onBackPressed() {
		return;
	}
}

