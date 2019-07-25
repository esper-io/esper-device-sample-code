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
 *  1. Ensures the application starts in with a full-screen window
 *  2. Sets the Window-flags to keep the screen ON (see AndroidManifest.xml)
 *  3. Loads the image of Andi in the UI :)
 *  4. Calls the appropriate API to pin the screen (so folks cannot go away from the app)
 *  5. Suppressed the ability for users to tinker with the volume buttons
 *  6. Users of this app can swipe from the top or the bottom of the screen to bring up the
 *     the navigation bar. In such a case, (a) if the user touches anywhere on the screen again, the
 *     system hides the navigation bar again, and (b) if the user doesn't interact with the system
 *     after swiping, the system does the same, after a small delay.
 *  7. Lastly, it demonstrates the use of the Esper Android SDK to acquire the device ID in case
 *     this device is provisioned.
 *
 */
public class EsperKioskActivity extends AppCompatActivity {

    // The Andi Logo
    private final String ANDI_LOGO_FILENAME = "andi.jpg";

    // The DevicePolicyManager will be used to check on permissions this app's package has
    private DevicePolicyManager mDevicePolicyManager = null;

    // The Activity Manager will be used to check up on the existing state of permissions this app has
    private ActivityManager mActivityManager = null;

    // A tag to log with
    private final String TAG = AppCompatActivity.class.getSimpleName();

    // A helper-handler used to hide the navigation bar in case the user swipes on the screen
    private final Handler mHideHandler = new Handler();

    private final int HIDE_SYSTEM_UI_FLAGS = View.SYSTEM_UI_FLAG_LOW_PROFILE
                                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // initialize the managers for usage further in the activity
        mDevicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        mActivityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

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
     *
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
        EsperDeviceSDK sdk = EsperDeviceSDK.getInstance(getApplicationContext(), new Handler(Looper.getMainLooper()));
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

    @Override
    public void onResume() {
        super.onResume();

        // with an esper-provisioned device, the policy manager automatically gives your app the
        // permission to allow pinning the screen. this check ensures that's the case. if that's not
        // the case, one has to make this app an admin which means going through a lot of hoops.
        // esper takes care of this for you!
        if (mDevicePolicyManager != null && mDevicePolicyManager.isLockTaskPermitted(getPackageName())) {
            // onResume() might be called multiple times, so before the screen is pinned, this logic
            // checks if it's already pinned. if not, it will pin it. otherwise it just moves on.
            // putting a check here prevents the "screen is now pinned" message-toast from showing
            // up every time the screen is attempted to be pinned.
            if (mActivityManager.getLockTaskModeState() != ActivityManager.LOCK_TASK_MODE_LOCKED) {
                // pin-it!
                startLockTask();
            }
        } else {
            // this is the case where one is probably not using an esper-provisioned device. in that
            // case an error message should indicate to the user that pinning is not going to work.
            Log.e(TAG, getString(R.string.screen_pinning_disallowed));
        }

        startLockTask();
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
