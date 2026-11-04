package ai.flow.flowy;

import android.app.Service;
import android.content.Context;
import android.os.IBinder;
import android.content.Intent;
import android.util.Log;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;

import java.util.HashMap;

class PandaInstance implements Runnable {
    private int fd;
    public PandaInstance(int fd) {
        this.fd = fd;
    }
    @Override
    public void run(){
        ServicePandad.nativeStart(this.fd);
    }
}

public class ServicePandad extends Service {

    private static final String TAG = "ServicePandad";
    private Thread applicationThread = null;

	private static final String ACTION_USB_PERMISSION = "ai.flow.flowy.USB_PERMISSION";
    private BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        System.out.println("RECEIVING INTENT: " + action);

        if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
            synchronized (this) {
                UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                maybeRequestUSBPermission(usbDevice, context);
            }
        } else if (ACTION_USB_PERMISSION.equals(action)) {
            synchronized (this) {
                UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    if(device != null) {
                        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
                        UsbDeviceConnection usbDeviceConnection = usbManager.openDevice(device);
                        Log.i(TAG, "Permission granted for serial "+usbDeviceConnection.getSerial());
                        PandaInstance pandaInstance = new PandaInstance(usbDeviceConnection.getFileDescriptor());
                        new Thread(pandaInstance).start();
                    }
                }
                else {
                    Log.i(TAG, "Permission denied for device " + device);
                }
            }
        }
        }
    };

    private void maybeRequestUSBPermission(UsbDevice device, Context context) {
        if (device == null) {
            Log.w(TAG, "maybeRequestUSBPermission got a null device");
            return;
        }

        // Check for both panda vendor IDs and product IDs
        if ((device.getVendorId() == 0xbbaa || device.getVendorId() == 0x3801) &&
            (device.getProductId() == 0xddcc || device.getProductId() == 0xddee)) {
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_MUTABLE);
            ((UsbManager) context.getSystemService(Context.USB_SERVICE)).requestPermission(device, pendingIntent);
        } else {
            Log.w(TAG, "Found a USB device that's not a Panda (VID: " + device.getVendorId() + ", PID: " + device.getProductId() + ")");
        }
    }

    public int startType() {
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        System.loadLibrary("pandad");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (applicationThread != null) {
            return startType();
        }

        // Flash Panda
        System.loadLibrary("pandaflash");
        String app_root =  getFilesDir().getAbsolutePath() + "/app";
        System.out.println("Flashing Panda");
        PythonRunner.run(0, app_root + "/panda/board/obj/");

        IntentFilter attachFilter = new IntentFilter();
        // Receiver for attached devices, used to request permission when plugging in a device
        attachFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        // Receiver for extended permissions, called when the user accepts USB permissions
        attachFilter.addAction(ACTION_USB_PERMISSION);
        registerReceiver(usbReceiver, attachFilter, Context.RECEIVER_EXPORTED);

        // Request permission for already plugged devices
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
		Log.i(TAG, "Number of USB devices found: "+deviceList.size());
        for (UsbDevice usbDevice : deviceList.values())
        {
            maybeRequestUSBPermission(usbDevice, this);
        }

        return startType();
    }

    public static void start(Context ctx) {
        Intent serviceIntent = new Intent(ctx, ServicePandad.class);
        ctx.startService(serviceIntent);
    }

    private void requestUsbPermissions(UsbDevice device) {
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        Intent usbIntent = new Intent(ACTION_USB_PERMISSION);
        usbIntent.setPackage(this.getPackageName());
        PendingIntent permissionIntent = PendingIntent.getBroadcast(this, 0, usbIntent, PendingIntent.FLAG_MUTABLE);
        System.out.println("REQUESTING PERMISSIONS FOR " + device.getDeviceName());
        manager.requestPermission(device, permissionIntent);
    }

    @Override
    public void onDestroy() {
        nativeStop();
        unregisterReceiver(usbReceiver);
    }

    // Native part
    public static native void nativeStart(int fd);
    public static native void nativeStop();
}
