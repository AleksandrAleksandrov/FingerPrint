package com.aleksandr.aleksandrov.sample.print.finger.fingerprint;

import android.Manifest;
import android.app.KeyguardManager;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private TextView tvWarning;

    private FingerprintManager.CryptoObject mCryptoObject;

    private FingerprintManager mFingerprintManager;

    private KeyguardManager mKeyguardManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvWarning = findViewById(R.id.tv_warning);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            setUpFingerPrint();
        else
            setWarning("It's needed Android Marshmallow or above to use finger print sensor");

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void setUpFingerPrint() {

        mFingerprintManager = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);

        //Check whether the device has a fingerprint sensor
        if (mFingerprintManager.isHardwareDetected()) {
            MyCipherGenerator key = new MyCipherGenerator();
            //Get an instance of KeyguardManager and FingerprintManager
            mKeyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);

            //Check whether the user has granted your app the USE_FINGERPRINT permission
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                // If your app doesn't have this permission, then display the following text
                setWarning("Please enable the fingerprint permission");
            }

            //Check that the user has registered at least one fingerprint
            if (!mFingerprintManager.hasEnrolledFingerprints()) {
                // If the user hasn’t configured any fingerprints, then display the following message
                setWarning("No fingerprint configured. Please register at least one fingerprint in your device's Settings");
            }

            //Check that the lock screen is secured
            if (!mKeyguardManager.isKeyguardSecure()) {
                // If the user hasn’t secured their locks creen with a PIN password or pattern, then display the following text
                setWarning("Please enable lockscreen security in your device's Settings");
            } else {
                try {
                    key.generateKey();
                } catch (MyCipherGenerator.FingerprintException e) {
                    e.printStackTrace();
                }

                if (key.initCipher()) {
                    //If the cipher is initialized successfully, then create a CryptoObject instance
                    mCryptoObject = new FingerprintManager.CryptoObject(key.getCipher());

                    // Here, I’m referencing the FingerprintHandler class that we’ll create in the next section. This class will be responsible
                    // for starting the authentication process (via the startAuth method) and processing the authentication process events
                    FingerprintHandler helper = new FingerprintHandler(this);
                    helper.startAuth(mFingerprintManager, mCryptoObject);
                }
            }
        } else
            setWarning("Your device doesn't support fingerprint authentication");
    }

    private void setWarning(String message) {
        tvWarning.setText(message);
    }
}
