package com.maxieds.chameleonminiusb;

import android.content.Context;
import android.content.Intent;

public interface ChameleonLibraryInterfaceReceiver {

    /**
     * This method should be overridden in an interface implementation to handle real-time
     * processing of logs generated by the library at runtime and throughout its
     * lifecycle in a third-party application.
     * @param logEntryIntent : the newly generated logging information.
     */
    void onReceiveNewLoggingData(Intent logEntryIntent);

    /**
     * This method should provide a valid (Activity) Context which can be referenced by the
     * standalone library API.
     */
    Context getDefaultContext();

    /**
     * This function should call the equivalent of Activity.runOnUiThread(...) with the
     * Runnable parameter passed as input to the method.
     * @param newRunnableParam
     */
    void getRunOnUiThreadHandler(Runnable newRunnableParam);

    /**
     * This function should call the equivalent of AppCompatActivity.requestPermissions(...)
     * on the passed parameters.
     * @param permissions
     * @param requestCode
     */
    void getRequestPermissionsHandler(String[] permissions, int requestCode);

}
