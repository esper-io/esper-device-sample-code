/**
 * This file implements a simple Fullscreen activity for a Kiosk-mode application.
 */
package io.esper.kioskapp;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.io.InputStream;

import io.esper.devicesdk.EsperDeviceSDK;
import io.esper.devicesdk.exceptions.EsperSDKNotFoundException;
import io.esper.devicesdk.models.EsperDeviceInfo;

/**
 *  This class implements a Kiosk-mode activity, that does the following main sub-activities within
 *  it:
 *
 *  1. Ensures the application starts in with a full-screen window (see FLAG_LAYOUT_NO_LIMITS below).
 *  2. Loads the image of Andi in the UI, and puts some animation on it :)
 *  3. Suppresses the ability of users to tinker with the volume buttons.
 *  4. Demonstrates the use of the Esper Android SDK to acquire the device ID in case
 *     this device is provisioned.
 *
 */
public class EsperKioskActivity extends AppCompatActivity {

    // The Andi Logo
    private final String ANDI_LOGO_FILENAME = "andi.jpg";

    // A tag to log with
    private final String TAG = AppCompatActivity.class.getSimpleName();

    //
    private final int SDK_INIT_DELAY_MS = 1000;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set the main content view
        setContentView(R.layout.activity_fullscreen);

        // set window flags so that this activity shows over the navigation/task bars
        // without this the bars are not fully transparent.
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );

        // show andi, and populate the device id
        showAnimatedAndi();
        populateDeviceId();
    }

    /**
     * This function loads Andi's image from assets and populated it into the designated ImageView.
     * Furthermore it makes Andi slightly animated--after all a Kiosk should always attract
     * customers, right?
     */
    private void showAnimatedAndi() {
        // load up Andi!
        ImageView andiLogoImageView = findViewById(R.id.andiLogoImageView);
        Bitmap bm = getAndiFromAssets();
        if (null != bm) {
            andiLogoImageView.setImageBitmap(bm);
        }

        // animate Andi
        ObjectAnimator scaleAnim =
                ObjectAnimator.ofPropertyValuesHolder(
                        andiLogoImageView,
                        PropertyValuesHolder.ofFloat("scaleX", 1.1f),
                        PropertyValuesHolder.ofFloat("scaleY", 1.1f)
                );
        scaleAnim.setDuration(1800);
        scaleAnim.setRepeatCount(ObjectAnimator.INFINITE);
        scaleAnim.setRepeatMode(ObjectAnimator.REVERSE);
        scaleAnim.start();
    }

    /**
     * This function makes use of the Esper Device SDK to get the device ID. Note that in case the
     * device is not provisioned to your Esper Backend, it's still OK to call it, but it will fail
     * with a known error. One great advantage of using the ID via the SDK is that now you can
     * correlate activity that's going on, on this Kiosk from the backend via Esper's Cloud APIs.
     * The key to tie the two together is the device ID!
     */
    private void populateDeviceId() {

        // by definition a kiosk mode app starts almost immediately after boot. on a device
        // provisioned by esper, the esper agent will launch the kiosk mode app for you. on some
        // devices, it takes a little longer for the sdk services to be available. hence, putting
        // a small delay here ensures the sdk is available at the time we need to use it.
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                // get a handle to the sdk
                EsperDeviceSDK sdk =
                        EsperDeviceSDK.getInstance(
                                getApplicationContext(),
                                new Handler(Looper.getMainLooper())
                        );

                // gran the device ID for population into the view
                sdk.getEsperDeviceInfo(new EsperDeviceSDK.Callback<EsperDeviceInfo>() {
                    @Override
                    public void onSuccess(EsperDeviceInfo response) {
                        // if the execution is here, an ID is available -- use it to populate the ID
                        // on the screen
                        ((TextView)findViewById(R.id.deviceId)).setText(response.getDeviceId());
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        // if execution is here, something wen't awry. one of the reasons is that this
                        // device might simply not been provisioned. in such a case, an exception of type
                        // EsperSDKNotFoundException is thrown. check for that explicitly.
                        if (!(t instanceof EsperSDKNotFoundException)) {
                            // note that the device id is already populated with a placeholder, so there's
                            // no need to put a value in there in the error case.
                            Log.e(TAG, getString(R.string.device_id_acquisition_failed), t);
                        }
                    }
                });
            }

        }, SDK_INIT_DELAY_MS);
    }

    /**
     * In case the kiosk has volume keys, this function will intercept any volume presses and try
     * to suppress them. Note that if one really needs to disable the keys, then using Esper-Enhanced
     * Android is the way to go--where you get low-level control of the device.
     *
     * @param keyCode The key code of the pressed key
     * @param event The event for the key
     *
     * @return True if this is handled by this logic, false otherwise.
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
            Toast.makeText(
                    this,
                    getString(R.string.vol_butts_disabled),
                    Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * Helper function to get the Andi logo from the Assets' folder.
     *
     * @return A Bitmap representation of the Andi logo. Null if there's an error retrieving it.
     */
    private Bitmap getAndiFromAssets() {
        InputStream is = null;
        Bitmap bitmap = null;
        AssetManager am = getAssets();
        try {
            is = am.open(ANDI_LOGO_FILENAME);
            bitmap = BitmapFactory.decodeStream(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }
}
