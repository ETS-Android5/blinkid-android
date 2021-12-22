package com.microblink.blinkid;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.Toast;

import com.microblink.entities.recognizers.Recognizer;
import com.microblink.entities.recognizers.RecognizerBundle;
import com.microblink.entities.recognizers.blinkbarcode.usdl.UsdlRecognizer;
import com.microblink.entities.recognizers.blinkid.documentface.DocumentFaceRecognizer;
import com.microblink.entities.recognizers.blinkid.generic.BlinkIdCombinedRecognizer;
import com.microblink.entities.recognizers.blinkid.idbarcode.IdBarcodeRecognizer;
import com.microblink.entities.recognizers.blinkid.mrtd.MrtdCombinedRecognizer;
import com.microblink.entities.recognizers.blinkid.mrtd.MrtdRecognizer;
import com.microblink.entities.recognizers.blinkid.passport.PassportRecognizer;
import com.microblink.entities.recognizers.blinkid.usdl.UsdlCombinedRecognizer;
import com.microblink.entities.recognizers.blinkid.visa.VisaRecognizer;
import com.microblink.menu.MenuListItem;
import com.microblink.menu.ResultHandlerMenuActivity;
import com.microblink.result.activity.RecognizerBundleResultActivity;
import com.microblink.uisettings.ActivityRunner;
import com.microblink.uisettings.BlinkIdUISettings;
import com.microblink.uisettings.DocumentUISettings;
import com.microblink.uisettings.LegacyDocumentVerificationUISettings;
import com.microblink.uisettings.UISettings;
import com.microblink.uisettings.options.BeepSoundUIOptions;
import com.microblink.uisettings.options.OcrResultDisplayMode;
import com.microblink.uisettings.options.OcrResultDisplayUIOptions;
import com.microblink.util.ImageSettings;
import com.microblink.util.RecognizerCompatibility;
import com.microblink.util.RecognizerCompatibilityStatus;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MenuActivity extends ResultHandlerMenuActivity {

    public static final int MY_BLINKID_REQUEST_CODE = 123;

    @Override
    protected String getTitleText() {
        return getString(R.string.app_name);
    }

    @Override
    protected boolean isScanRequestCode(int code) {
        return code == MY_BLINKID_REQUEST_CODE;
    }

    @Override
    protected Class<?> getResultActivityForRequestCode(int requestCode) {
        return RecognizerBundleResultActivity.class;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // in case of problems with the SDK (crashes or ANRs, uncomment following line to enable
        // verbose logging that can help developers track down the problem)
        //Log.setLogLevel(Log.LogLevel.LOG_VERBOSE);

        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // check if BlinkID is supported on the device
        RecognizerCompatibilityStatus supportStatus = RecognizerCompatibility.getRecognizerCompatibilityStatus(this);
        if (supportStatus != RecognizerCompatibilityStatus.RECOGNIZER_SUPPORTED) {
            Toast.makeText(this, "BlinkID is not supported! Reason: " + supportStatus.name(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected List<MenuListItem> createMenuListItems() {
        List<MenuListItem> items = new ArrayList<>();

        items.add(buildBlinkIdCombinedElement());
        items.add(buildPassportElement());
        items.add(buildVisaElement());

        items.add(buildMrtdElement());
        items.add(buildMrtdCombinedElement());
        items.add(buildDocumentFaceElement());

        items.add(buildIdBarcodeElement());
        items.add(buildUsdlElement());
        items.add(buildUsdlCombinedElement());

        return items;
    }

    /**
     * Starts scan activity. Activity that will be used is determined by the passed activity settings.
     * UI options are configured inside this method.
     * @param activitySettings activity settings that will be used for scanning, only recognizers are
     *                         important, UI options will be configured inside this method.
     * @param helpIntent help intent that can be launched if scan activity supports that
     */
    private void scanAction(@NonNull UISettings activitySettings, @Nullable Intent helpIntent) {
        setupActivitySettings(activitySettings, helpIntent);
        ActivityRunner.startActivityForResult(this, MY_BLINKID_REQUEST_CODE, activitySettings);
    }

    /**
     * Starts scan activity. Activity that will be used is determined by the passed activity settings.
     * UI options are configured inside this method.
     * @param activitySettings activity settings that will be used for scanning, only recognizers are
     *                         important, UI options will be configured inside this method.
     */
    private void scanAction(@NonNull UISettings activitySettings) {
        scanAction(activitySettings, null);
    }

    /**
     * Starts {@link com.microblink.activity.LegacyDocumentVerificationActivity} with given recognizer.
     * @param combinedRecognizer recognizer that will be used.
     */
    private void combinedRecognitionAction(Recognizer combinedRecognizer) {
        LegacyDocumentVerificationUISettings uiSettings = new LegacyDocumentVerificationUISettings(new RecognizerBundle(combinedRecognizer));
        uiSettings.setBeepSoundResourceID(R.raw.beep);

        ActivityRunner.startActivityForResult(this, MY_BLINKID_REQUEST_CODE, uiSettings);
    }

    private void setupActivitySettings(@NonNull UISettings settings, @Nullable Intent helpIntent) {
        if (settings instanceof BeepSoundUIOptions) {
            // optionally, if you want the beep sound to be played after a scan
            // add a sound resource id
            ((BeepSoundUIOptions) settings).setBeepSoundResourceID(R.raw.beep);
        }
        if (settings instanceof OcrResultDisplayUIOptions) {
            // If you want, you can disable drawing of OCR results on scan activity. Drawing OCR results can be visually
            // appealing and might entertain the user while waiting for scan to complete, but might introduce a small
            // performance penalty.
            // ((ShowOcrResultUIOptions) settings).setShowOcrResult(false);

            // Enable showing of OCR results as animated dots. This does not have effect if non-OCR recognizer like
            // barcode recognizer is active.
            ((OcrResultDisplayUIOptions) settings).setOcrResultDisplayMode(OcrResultDisplayMode.ANIMATED_DOTS);
        }
    }

    private MenuListItem buildBlinkIdCombinedElement() {
        return new MenuListItem("BlinkId Combined", new Runnable() {
            @Override
            public void run() {
                BlinkIdCombinedRecognizer blinkIdCombined = new BlinkIdCombinedRecognizer();
                ImageSettings.enableAllImages(blinkIdCombined);
                scanAction(new BlinkIdUISettings(prepareRecognizerBundle(blinkIdCombined)));
            }
        });
    }

    private MenuListItem buildPassportElement() {
        return new MenuListItem("Passport", new Runnable() {
            @Override
            public void run() {
                PassportRecognizer passportRecognizer = new PassportRecognizer();
                ImageSettings.enableAllImages(passportRecognizer);
                scanAction(new DocumentUISettings(prepareRecognizerBundle(passportRecognizer)));
            }
        });
    }

    private MenuListItem buildVisaElement() {
        return new MenuListItem("Visa", new Runnable() {
            @Override
            public void run() {
                VisaRecognizer visaRecognizer = new VisaRecognizer();
                ImageSettings.enableAllImages(visaRecognizer);
                scanAction(new DocumentUISettings(prepareRecognizerBundle(visaRecognizer)));
            }
        });
    }

    private MenuListItem buildMrtdElement() {
        return new MenuListItem("Passport and ID", new Runnable() {
            @Override
            public void run() {
                MrtdRecognizer mrtdRecognizer = new MrtdRecognizer();
                mrtdRecognizer.setAllowUnverifiedResults(true);
                ImageSettings.enableAllImages(mrtdRecognizer);
                scanAction(new DocumentUISettings(prepareRecognizerBundle(mrtdRecognizer)));
            }
        });
    }

    private MenuListItem buildMrtdCombinedElement() {
        return new MenuListItem("Passport and ID Combined", new Runnable() {
            @Override
            public void run() {
                MrtdCombinedRecognizer mrtdCombinedRecognizer = new MrtdCombinedRecognizer();
                ImageSettings.enableAllImages(mrtdCombinedRecognizer);
                combinedRecognitionAction(mrtdCombinedRecognizer);
            }
        });
    }

    private MenuListItem buildDocumentFaceElement() {
        return new MenuListItem("Document Face", new Runnable() {
            @Override
            public void run() {
                DocumentFaceRecognizer documentFaceRecognizer = new DocumentFaceRecognizer();
                ImageSettings.enableAllImages(documentFaceRecognizer);
                scanAction(new DocumentUISettings(prepareRecognizerBundle(documentFaceRecognizer)));
            }
        });
    }

    private MenuListItem buildIdBarcodeElement() {
        return new MenuListItem("ID barcode", new Runnable() {
            @Override
            public void run() {
                IdBarcodeRecognizer idBarcodeRecognizer = new IdBarcodeRecognizer();
                scanAction(new DocumentUISettings(prepareRecognizerBundle(idBarcodeRecognizer)));
            }
        });
    }

    private MenuListItem buildUsdlElement() {
        return new MenuListItem("USDL", new Runnable() {
            @Override
            public void run() {
                UsdlRecognizer usdlRecognizer = new UsdlRecognizer();
                ImageSettings.enableAllImages(usdlRecognizer);
                scanAction(new DocumentUISettings(prepareRecognizerBundle(usdlRecognizer)));
            }
        });
    }

    private MenuListItem buildUsdlCombinedElement() {
        return new MenuListItem("USDL Combined", new Runnable() {
            @Override
            public void run() {
                UsdlCombinedRecognizer usdlCombinedRecognizer = new UsdlCombinedRecognizer();
                ImageSettings.enableAllImages(usdlCombinedRecognizer);
                combinedRecognitionAction(usdlCombinedRecognizer);
            }
        });
    }

    private RecognizerBundle prepareRecognizerBundle(@NonNull Recognizer<?>... recognizers ) {
        return new RecognizerBundle(recognizers);
    }

}