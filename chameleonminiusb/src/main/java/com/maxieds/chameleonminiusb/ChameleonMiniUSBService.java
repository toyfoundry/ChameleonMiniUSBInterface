package com.maxieds.chameleonminiusb;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;
import android.support.graphics.drawable.R;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import static com.maxieds.chameleonminiusb.ChameleonDeviceConfig.getChameleonMiniUSBDeviceParams;
import static com.maxieds.chameleonminiusb.LibraryLogging.LocalLoggingLevel.LOG_ADB_VERBOSE;
import static junit.framework.Assert.assertNotNull;

/**
 * This service performs the usual function of the main activity in that it listens for new
 * USB connections and newly detached USB connections and configures the target Chameleon Mini
 * devices accordingly.
 */
@TargetApi(27)
public class ChameleonMiniUSBService extends IntentService {

    private static final String TAG = ChameleonMiniUSBService.class.getSimpleName();

    public static ChameleonMiniUSBService localService;
    public static boolean vibrateOnUSBDeviceAttach = true;
    public static int vibrateDurationMs = 200;

    @TargetApi(27)
    public ChameleonMiniUSBService() {
        super("ChameleonMiniUSBService");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    public static final String NCHANNELID_SERVICE = "com.maxieds.chameleonminiusb.ChameleonMiniUSBService";
    public static final String NCHANNELID_TASK = "com.maxieds.chameleonminiusb.ChameleonMiniUSBService.info";
    private static final int CHAMELEONUSB_SERVICE_PROCID = 0xafbb3753;

    public void initNotifyChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // Oreo 8.1 breaks startForground significantly
            NotificationManager notifyManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notifyManager.createNotificationChannel(new NotificationChannel(NCHANNELID_SERVICE, "Chameleon Mini USB Service", NotificationManager.IMPORTANCE_HIGH));
            notifyManager.createNotificationChannel(new NotificationChannel(NCHANNELID_TASK, "Chameleon Mini USB Service Info", NotificationManager.IMPORTANCE_HIGH));
        }
    }

    @Override
    public void onCreate() {
        LibraryLogging.i(TAG, "Started Chameleon Mini USB service in the background...");
        super.onCreate();
        initNotifyChannel();
        localService = this;
    }

    @Override
    public void onDestroy() {
        ChameleonDeviceConfig.shutdownSerialConnection();
        ChameleonDeviceConfig.usbReceiversRegistered = false;
        localService = null;
    }

    @Override
    @RequiresPermission("com.android.example.USB_PERMISSION")
    protected void onHandleIntent(Intent intent) {
        if(intent == null) {
            LibraryLogging.w(TAG, "onHandleIntent passed a NULL intent object.");
            return;
        }
        String intentAction = intent.getAction();
        if(intentAction == null) {
            LibraryLogging.w(TAG, "onHandleIntent passed a NULL intentAction.");
            return;
        }
        else if(intentAction.equals("MONITOR_CHAMELEON_USB")) {
            startForeground(CHAMELEONUSB_SERVICE_PROCID, getForegroundServiceNotify("We are currently watching for Chameleon Mini USB hotplugging!"));
        }
        else if(intentAction.equalsIgnoreCase(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
            if(ChameleonDeviceConfig.THE_CHAMELEON_DEVICE.isConfigured()) {
                LibraryLogging.i(TAG, "Another USB device was attached, but we already have a configured Chameleon.");
            }
            else if(ChameleonDeviceConfig.THE_CHAMELEON_DEVICE.handleNewUSBDeviceAttached()) {
                LibraryLogging.i(TAG, "A new Chameleon device was just attached.");
                LibraryLogging.LogEntry.enqueueNewLog(LOG_ADB_VERBOSE, TAG, getChameleonMiniUSBDeviceParams());
                if(vibrateOnUSBDeviceAttach) {
                    Vibrator vbr = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vbr.vibrate(VibrationEffect.createOneShot(vibrateDurationMs, VibrationEffect.DEFAULT_AMPLITUDE));
                    }
                    else{
                        vbr.vibrate(vibrateDurationMs);
                    }
                }
            }
            else {
                LibraryLogging.i(TAG, "A non-Chameleon USB device was attached to the phone.");
            }
        }
        else if(intentAction.equalsIgnoreCase(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
            ChameleonDeviceConfig.shutdownSerialConnection();
            LibraryLogging.i(TAG, "Chameleon device detached ... shutting down serial port connection for now.")
        }
    }

    public Notification getForegroundServiceNotify(String bannerMsg) {
        Intent intent = new Intent(this, ChameleonMiniUSBService.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder fgNotify = new NotificationCompat.Builder(this, NCHANNELID_TASK);
        fgNotify.setOngoing(true);
        fgNotify.setContentTitle("Chameleon Mini USB Monitor Service")
                .setColor(0xAEEEEE)
                .setTicker(bannerMsg)
                .setContentText(bannerMsg)
                .setOngoing(true)
                .setSmallIcon(R.drawable.chameleonusb64)
                .setWhen(System.currentTimeMillis())
                .setCategory(Notification.CATEGORY_SERVICE)
                .setContentIntent(pendingIntent);
        return fgNotify.build();
    }

}
