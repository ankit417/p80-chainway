// The present software is not subject to the US Export Administration Regulations (no exportation license required), May 2012
// The present software is not subject to the US Export Administration Regulations (no exportation license required), May 2012
package com.codniv.cookbook1.;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.codniv.cookbook1.database.DatabaseItem;
import com.codniv.cookbook1.info.CaptureInfo;
import com.codniv.cookbook1.info.EnrollInfo;
import com.codniv.cookbook1.info.FingerPrintInfo;
import com.codniv.cookbook1.info.IdentifyInfo;
import com.codniv.cookbook1.info.MorphoInfo;
import com.codniv.cookbook1.info.ProcessInfo;
import com.codniv.cookbook1.info.VerifyInfo;
import com.codniv.cookbook1.info.subtype.AuthenticationMode;
import com.codniv.cookbook1.info.subtype.CaptureType;
import com.codniv.cookbook1.info.subtype.FingerPrintMode;
import com.codniv.cookbook1.info.subtype.SecurityOption;
import com.codniv.cookbook1.tools.MorphoTools;
import com.morpho.morphosmart.sdk.CallbackMask;
import com.morpho.morphosmart.sdk.CallbackMessage;
import com.morpho.morphosmart.sdk.Coder;
import com.morpho.morphosmart.sdk.CompressionAlgorithm;
import com.morpho.morphosmart.sdk.DetectionMode;
import com.morpho.morphosmart.sdk.EnrollmentType;
import com.morpho.morphosmart.sdk.ErrorCodes;
import com.morpho.morphosmart.sdk.FalseAcceptanceRate;
import com.morpho.morphosmart.sdk.ITemplateType;
import com.morpho.morphosmart.sdk.LatentDetection;
import com.morpho.morphosmart.sdk.MatchingStrategy;
import com.morpho.morphosmart.sdk.MorphoDatabase;
import com.morpho.morphosmart.sdk.MorphoDevice;
import com.morpho.morphosmart.sdk.MorphoImage;
import com.morpho.morphosmart.sdk.MorphoUser;
import com.morpho.morphosmart.sdk.MorphoWakeUpMode;
import com.morpho.morphosmart.sdk.ResultMatching;
import com.morpho.morphosmart.sdk.Template;
import com.morpho.morphosmart.sdk.TemplateFVP;
import com.morpho.morphosmart.sdk.TemplateFVPType;
import com.morpho.morphosmart.sdk.TemplateList;
import com.morpho.morphosmart.sdk.TemplateType;
import com.morpho.morphosmart.sdk.MorphoDevice.MorphoDevicePrivacyModeStatus;

public class MorphoFingerprint extends AppCompatActivity implements Observer
{

    private int				currentCaptureBitmapId	= 0;
    private boolean			isCaptureVerif			= false;
    private Handler			mHandler				= new Handler();
    String					strMessage				= new String();
    private int				index;
    private MorphoDevice	morphoDevice;
    private MorphoDatabase	morphoDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fingerprint_morpho);
//        morphoDatabase = ProcessInfo.getInstance().getMorphoDatabase();
        morphoDevice = ProcessInfo.getInstance().getMorphoDevice();
    }



    @Override
    public void onResume() {
        super.onResume();
        LinearLayout ll = (LinearLayout) findViewById(R.id.content_process);
        LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        int layoutId = R.layout.activity_process_capture;
        if ((ProcessInfo.getInstance().getMorphoInfo().getClass() == VerifyInfo.class) || (ProcessInfo.getInstance().getMorphoInfo().getClass() == IdentifyInfo.class)
                || (ProcessInfo.getInstance().getMorphoInfo().getClass() == FingerPrintInfo.class)) {
            layoutId = R.layout.activity_process_verify;
        }
        ViewGroup vg = (ViewGroup) vi.inflate(layoutId, null);
        ll.removeAllViews();
        ll.addView(vg);
        final MorphoInfo morphoInfo = ProcessInfo.getInstance().getMorphoInfo();
        final MorphoFingerprint processActivity = this;
        currentCaptureBitmapId = R.id.imageView1;

//        if (morphoInfo.getClass() == VerifyInfo.class) {
//            verify(processActivity);
//        } else if (morphoInfo.getClass() == CaptureInfo.class) {
//            morphoDeviceCapture(processActivity);
//        } else if (morphoInfo.getClass() == EnrollInfo.class) {
//            morphoUserEnroll(processActivity);
//        } else if (morphoInfo.getClass() == IdentifyInfo.class) {
//            morphoDatabaseIdentify(processActivity);
//        } else if (morphoInfo.getClass() == FingerPrintInfo.class) {
            morphoDeviceGetImage(processActivity);
//        }
    }

    public void morphoDeviceGetImage(final Observer observer) {
        Thread commandThread = (new Thread(new Runnable() {
            @Override
            public void run() {

                Looper.prepare();

                MorphoInfo morphoInfo = ProcessInfo.getInstance().getMorphoInfo();
                int timeOut = ProcessInfo.getInstance().getTimeout();
                int acquisitionThreshold = 0;
                final CompressionAlgorithm compressAlgo = ((FingerPrintInfo) morphoInfo).getCompressionAlgorithm();
                int compressRate = 0;
                int detectModeChoice;
                LatentDetection latentDetection;
                final MorphoImage[] morphoImage = new MorphoImage[] {new MorphoImage()};
                int callbackCmd = ProcessInfo.getInstance().getCallbackCmd();

                callbackCmd &= ~CallbackMask.MORPHO_CALLBACK_ENROLLMENT_CMD.getValue();

                if(ProcessInfo.getInstance().isFingerprintQualityThreshold()) {
                    acquisitionThreshold = ProcessInfo.getInstance().getFingerprintQualityThresholdvalue();
                }

                if (!compressAlgo.equals(CompressionAlgorithm.MORPHO_NO_COMPRESS)) {
                    compressRate = ((FingerPrintInfo) morphoInfo).getCompressRatio();
                    if (compressAlgo.equals(CompressionAlgorithm.MORPHO_COMPRESS_V1)) {
                        compressRate = 0;
//                        alert("Save Image File, Compressed V1 image shall be decompressed after save !");
                    }
                }

                detectModeChoice = DetectionMode.MORPHO_ENROLL_DETECT_MODE.getValue();

                if (((FingerPrintInfo) morphoInfo).getFingerPrintMode().equals(FingerPrintMode.Verify))	{
                    detectModeChoice = DetectionMode.MORPHO_VERIF_DETECT_MODE.getValue();
                }

                if (ProcessInfo.getInstance().isForceFingerPlacementOnTop()) {
                    detectModeChoice |= DetectionMode.MORPHO_FORCE_FINGER_ON_TOP_DETECT_MODE.getValue();
                }

                if (ProcessInfo.getInstance().isWakeUpWithLedOff())	{
                    detectModeChoice |= MorphoWakeUpMode.MORPHO_WAKEUP_LED_OFF.getCode();
                }

                latentDetection = (((FingerPrintInfo) morphoInfo).isLatentDetect() == true ? LatentDetection.LATENT_DETECT_ENABLE : LatentDetection.LATENT_DETECT_DISABLE);

                final int ret = morphoDevice.getImage(timeOut, acquisitionThreshold, compressAlgo, compressRate, detectModeChoice, latentDetection, morphoImage[0], callbackCmd, observer);
                ProcessInfo.getInstance().setCommandBioStart(false);

                getAndWriteFFDLogs();

                if (ret == ErrorCodes.MORPHO_OK) {
                    try {
                        // check if Privacy Mode is enabled
                        if (MorphoDevicePrivacyModeStatus.PRIVACY_MODE_DISABLED != ProcessInfo.getInstance().getPrivacyModeStatus()) {
                            // display alert dialog to choose whether image received shall be decrypted before being saved
                            final AlertDialog alertDialog = new AlertDialog.Builder(ProcessInfo.getInstance().getMorphoSample()).create();
                            alertDialog.setTitle(ProcessInfo.getInstance().getMorphoSample().getResources().getString(R.string.TMS));
                            alertDialog.setMessage(ProcessInfo.getInstance().getMorphoSample().getResources().getString(R.string.TMS));
                            alertDialog.setCancelable(false);
                            alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, ProcessInfo.getInstance().getMorphoSample().getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    String message = "";
                                    try {
                                        // Data will be saved without being decrypted
                                        String fileName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+"Image_pcrypt" + compressAlgo.getExtension();
                                        if (compressAlgo.equals(CompressionAlgorithm.MORPHO_NO_COMPRESS)) { // RAW image
                                            FileOutputStream fos = new FileOutputStream(fileName);
                                            fos.write(morphoImage[0].getImage());
                                            message = "Image RAW successfully exported in file [" + fileName + "]";
                                            fos.close();
                                        } else if (compressAlgo.equals(CompressionAlgorithm.MORPHO_COMPRESS_V1)) { // SAGEM_V1 compressed image
                                            FileOutputStream fos = new FileOutputStream(fileName);
                                            fos.write(morphoImage[0].getCompressedImage());
                                            message = "Image SAGEM_V1 successfully exported in file [" + fileName + "]\nNote : image shall be decompressed after decrypt before being used !";
                                            fos.close();
                                        } else if (compressAlgo.equals(CompressionAlgorithm.MORPHO_COMPRESS_WSQ))	{ // WSQ image
                                            FileOutputStream fos = new FileOutputStream(fileName);
                                            fos.write(morphoImage[0].getCompressedImage());
                                            message = "Image WSQ successfully exported in file [" + fileName + "]";
                                            fos.close();
                                        }
                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                    final String alertMessage = message;
                                    final int internalError = morphoDevice.getInternalError();
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public synchronized void run() {
//                                            alert(ret, internalError, "GetImage", alertMessage, false);
                                        }
                                    });
                                    notifyEndProcess();
                                }
                            });
                            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, ProcessInfo.getInstance().getMorphoSample().getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    String message = "";
                                    try {
                                        // Data will be decrypted before being saved
                                        // get privacy key
                                        byte[] privacyKey = ProcessInfo.getInstance().getMorphoSample().getPrivacyKey();
                                        if (null == privacyKey) {
//                                            alert("An error occured while getting Privacy Key");
                                        } else {
                                            String fileName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+"Image" + compressAlgo.getExtension();
                                            if (compressAlgo.equals(CompressionAlgorithm.MORPHO_NO_COMPRESS)) { // RAW image
                                                // Decrypted image will contain 12 bytes for header then raw image data
                                                byte[] decryptedImageWithHeader = ProcessInfo.getInstance().getMorphoSample().decryptAndCheckPrivacyData(morphoImage[0].getImage(), privacyKey);
                                                if (null != decryptedImageWithHeader) {
                                                    byte[] decryptedImage = Arrays.copyOfRange(decryptedImageWithHeader, 12, decryptedImageWithHeader.length);
                                                    FileOutputStream fos = new FileOutputStream(fileName);
                                                    fos.write(decryptedImage);
                                                    fos.close();
                                                    message = "Image RAW successfully exported in file [" + fileName + "]";
                                                } else {
                                                    message = "Fail to decrypt RAW image";
                                                }

                                            } else if (compressAlgo.equals(CompressionAlgorithm.MORPHO_COMPRESS_V1)) { // SAGEM_V1 compressed image
                                                FileOutputStream fos = new FileOutputStream(fileName);
                                                byte[] decryptedImage = ProcessInfo.getInstance().getMorphoSample().decryptAndCheckPrivacyData(morphoImage[0].getImage(), privacyKey);
                                                if (null != decryptedImage) {
                                                    fos.write(decryptedImage);
                                                    fos.close();
                                                    message = "Image SAGEM_V1 successfully exported in file [" + fileName + "]\nNote : image shall be decompressed before being used !";
                                                } else {
                                                    message = "Fail to decrypt compressed SAGEM V1 image";
                                                }
                                            } else if (compressAlgo.equals(CompressionAlgorithm.MORPHO_COMPRESS_WSQ))	{ // WSQ image
                                                FileOutputStream fos = new FileOutputStream(fileName);
                                                // Decrypted image will contain 12 bytes for RAW header, 12 bytes of WSQ header, then WSQ image data
                                                byte[] decryptedImageWithHeaders = ProcessInfo.getInstance().getMorphoSample().decryptAndCheckPrivacyData(morphoImage[0].getImage(), privacyKey);
                                                if (null != decryptedImageWithHeaders) {
                                                    byte[] decryptedImage = Arrays.copyOfRange(decryptedImageWithHeaders, 24, decryptedImageWithHeaders.length);
                                                    fos.write(decryptedImage);
                                                    fos.close();
                                                    message = "Image WSQ successfully exported in file [" + fileName + "]";
                                                } else {
                                                    message = "Fail to decrypt WSQ image";
                                                }
                                            }
                                        }

                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                    final String alertMessage = message;
                                    final int internalError = morphoDevice.getInternalError();
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public synchronized void run() {
//                                            alert(ret, internalError, "GetImage", alertMessage, false);
                                        }
                                    });
                                    notifyEndProcess();
                                }
                            });
                            alertDialog.show();
                        } else {
                            String message = "";
                            String fileName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+"Image" + compressAlgo.getExtension();
                            if (compressAlgo.equals(CompressionAlgorithm.MORPHO_NO_COMPRESS)) { // RAW image
                                FileOutputStream fos = new FileOutputStream(fileName);
                                fos.write(morphoImage[0].getImage());
                                message = "Image RAW successfully exported in file [" + fileName + "]";
                                fos.close();
                            } else if (compressAlgo.equals(CompressionAlgorithm.MORPHO_COMPRESS_V1)) { // SAGEM_V1 compressed image
                                FileOutputStream fos = new FileOutputStream(fileName);
                                fos.write(morphoImage[0].getCompressedImage());
                                message = "Image SAGEM_V1 successfully exported in file [" + fileName + "]";
                                fos.close();
                            } else if (compressAlgo.equals(CompressionAlgorithm.MORPHO_COMPRESS_WSQ))	{ // WSQ image
                                FileOutputStream fos = new FileOutputStream(fileName);
                                fos.write(morphoImage[0].getCompressedImage());
                                message = "Image WSQ successfully exported in file [" + fileName + "]";
                                fos.close();
                            }

                            final String alertMessage = message;
                            final int internalError = morphoDevice.getInternalError();
                            mHandler.post(new Runnable() {
                                @Override
                                public synchronized void run() {
//                                    alert(ret, internalError, "GetImage", alertMessage, false);
                                }
                            });
                            notifyEndProcess();
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e)	{
                        e.printStackTrace();
                    } catch (Exception ex) {
//                        alert(ex.getMessage());
                    }
                }

                Looper.loop();
            }
        }));
        commandThread.start();
    }

    public void verify(final Observer observer)
    {
        MorphoInfo morphoInfo = ProcessInfo.getInstance().getMorphoInfo();
        AuthenticationMode am = ((VerifyInfo) morphoInfo).getAuthenticationMode();
        if (am == AuthenticationMode.File)
        {
            final String fileName = ((VerifyInfo) morphoInfo).getFileName();
            morphoDeviceVerifyWithFile(observer, fileName);
        }
        else
        {
            morphoUserVerify(observer);
        }
    }

    public void morphoDatabaseIdentify(final Observer observer)	{
        Thread commandThread = (new Thread(new Runnable() {
            @Override
            public void run() {
                index = 0;
                int timeout = ProcessInfo.getInstance().getTimeout();
                int far = ProcessInfo.getInstance().getMatchingThreshold();
                Coder coder = ProcessInfo.getInstance().getCoder();
                int detectModeChoice;
                MatchingStrategy matchingStrategy = ProcessInfo.getInstance().getMatchingStrategy();
                int callbackCmd = ProcessInfo.getInstance().getCallbackCmd();
                callbackCmd &= ~CallbackMask.MORPHO_CALLBACK_ENROLLMENT_CMD.getValue();
                ResultMatching resultMatching = null;
                final MorphoUser morphoUser = new MorphoUser();

                if (ProcessInfo.getInstance().isForceFingerPlacementOnTop()) {
                    detectModeChoice = DetectionMode.MORPHO_ENROLL_DETECT_MODE.getValue();
                    detectModeChoice |= DetectionMode.MORPHO_FORCE_FINGER_ON_TOP_DETECT_MODE.getValue();
                } else {
                    detectModeChoice = DetectionMode.MORPHO_VERIF_DETECT_MODE.getValue();
                    if (ProcessInfo.getInstance().isWakeUpWithLedOff())	{
                        detectModeChoice |= MorphoWakeUpMode.MORPHO_WAKEUP_LED_OFF.getCode();
                    }
                }

                int ret = morphoDevice.setStrategyAcquisitionMode(ProcessInfo.getInstance().getStrategyAcquisitionMode());

                // Check if running under security mode
                boolean isTunnelingMode = false;
                boolean isOfferedSecurityMode = false;
                ArrayList<SecurityOption> securityOptions = ProcessInfo.getInstance().getSecurityOptions();
                for (SecurityOption so : securityOptions) {
                    if (so.title.equals("Mode Tunneling")) {
                        isTunnelingMode = so.activated;
                    } else if (so.title.equals("Mode Offered Security")) {
                        isOfferedSecurityMode = so.activated;
                    }
                }
                if (!isTunnelingMode && !isOfferedSecurityMode) {
                    resultMatching = new ResultMatching();
                }

                if(ret == 0) {
                    ret = morphoDatabase.identify(timeout, far, coder, detectModeChoice, matchingStrategy, callbackCmd, observer, resultMatching, 2, morphoUser);
                }

                ProcessInfo.getInstance().setCommandBioStart(false);
                getAndWriteFFDLogs();

                final int retvalue = ret;
                mHandler.post(new Runnable() {
                    @Override
                    public synchronized void run() {
                        final String[] message = {""};
                        if (retvalue == 0)
                        {
                            // check if PRivacy Mode is enabled
                            if (MorphoDevicePrivacyModeStatus.PRIVACY_MODE_DISABLED != ProcessInfo.getInstance().getPrivacyModeStatus()) {
                                // get privacy key
                                final byte[] privacyKey = ProcessInfo.getInstance().getMorphoSample().getPrivacyKey();
                                if (null == privacyKey) {
                                    message[0] = "En error occured while getting Privacy Key";
                                } else {
                                    //////////////////////////
                                    final AlertDialog alertDialog = new AlertDialog.Builder(ProcessInfo.getInstance().getMorphoSample()).create();
                                    alertDialog.setTitle(ProcessInfo.getInstance().getMorphoSample().getResources().getString(R.string.morphosample));
                                    alertDialog.setMessage("Decrypt User info : ");
                                    alertDialog.setCancelable(false);
                                    alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener()
                                    {
                                        public void onClick(DialogInterface dialog, int which)
                                        {
                                            byte[] 	userIDB = morphoUser.getBufferField(0);
                                            byte[]  firstNameB = morphoUser.getBufferField(1);
                                            byte[]  lastNameB = morphoUser.getBufferField(2);

                                            String userID = new String(userIDB);
                                            String firstName= new String(firstNameB);
                                            String lastName= new String(lastNameB);

                                            message[0] = "User identified";
                                            message[0] += "\r\nUser ID   : \t\t" + userID;
                                            message[0] += "\r\nFirstName : \t\t" + firstName;
                                            message[0] += "\r\nLastName  : \t\t" + lastName;
                                            alert(retvalue, morphoDevice.getInternalError(), "Identify", message[0], true);
                                        }
                                    });
                                    alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.ok), new DialogInterface.OnClickListener()
                                    {
                                        public void onClick(DialogInterface dialog, int which)
                                        {
                                            byte[] 	userIDB = morphoUser.getBufferField(0);
                                            byte[]  firstNameB = morphoUser.getBufferField(1);
                                            byte[]  lastNameB = morphoUser.getBufferField(2);

                                            byte[] decrypted_bio_data=	ProcessInfo.getInstance().getMorphoSample().decryptAndCheckPrivacyData(userIDB,privacyKey);


                                            String userID = new String(decrypted_bio_data);

                                            decrypted_bio_data=	ProcessInfo.getInstance().getMorphoSample().decryptAndCheckPrivacyData(firstNameB,privacyKey);

                                            String firstName= new String(decrypted_bio_data);

                                            decrypted_bio_data=	ProcessInfo.getInstance().getMorphoSample().decryptAndCheckPrivacyData(lastNameB,privacyKey);

                                            String lastName= new String(decrypted_bio_data);

                                            message[0] = "User identified";
                                            message[0] += "\r\nUser ID   : \t\t" + userID;
                                            message[0] += "\r\nFirstName : \t\t" + firstName;
                                            message[0] += "\r\nLastName  : \t\t" + lastName;
                                            alert(retvalue, morphoDevice.getInternalError(), "Identify", message[0], true);
                                        };
                                    });
                                    /////////////////////////
                                    alertDialog.show();
                                }
                            } else {
                                String userID = morphoUser.getField(0);
                                String firstName = morphoUser.getField(1);
                                String lastName = morphoUser.getField(2);
                                message[0] = "User identified";
                                message[0] += "\r\nUser ID   : \t\t" + userID;
                                message[0] += "\r\nFirstName : \t\t" + firstName;
                                message[0] += "\r\nLastName  : \t\t" + lastName;
                                alert(retvalue, morphoDevice.getInternalError(), "Identify", message[0], true);							}
                        }else
                            alert(retvalue, morphoDevice.getInternalError(), "Identify", message[0], true);
                    }
                });

                notifyEndProcess();
            }
        }));
        commandThread.start();
    }

    @SuppressLint("SimpleDateFormat")
    public void getAndWriteFFDLogs()
    {
        String ffdLogs = morphoDevice.getFFDLogs();

        if(ffdLogs != null)
        {
            String serialNbr = ProcessInfo.getInstance().getMSOSerialNumber();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            String currentDateandTime = sdf.format(new Date());
            String saveFile = AppContext.RootPath + serialNbr + "_" + currentDateandTime + "_Audit.log";

            try
            {
                FileWriter fstream = new FileWriter(saveFile,true);
                BufferedWriter out = new BufferedWriter(fstream);
                out.write(ffdLogs);
                out.close();
            }
            catch (IOException e)
            {
                Log.e("getAndWriteFFDLogs", e.getMessage());
            }
        }
    }

    public void morphoUserEnroll(final Observer observer) {
        Thread commandThread = (new Thread(new Runnable() {
            @Override
            public void run() {

                Looper.prepare();

                index = 0;
                MorphoInfo morphoInfo = ProcessInfo.getInstance().getMorphoInfo();
                final String idUser = ((EnrollInfo) morphoInfo).getIDNumber();
                final String firstName = ((EnrollInfo) morphoInfo).getFirstname();
                final String lastName = ((EnrollInfo) morphoInfo).getLastName();

                MorphoUser morphoUser = new MorphoUser();
                int ret = 0;
                byte[] privacyKey = null;
                boolean ok = true;

                if (MorphoDevicePrivacyModeStatus.PRIVACY_MODE_DISABLED != ProcessInfo.getInstance().getPrivacyModeStatus()) {
                    // get Privacy key
                    privacyKey = ProcessInfo.getInstance().getMorphoSample().getPrivacyKey();
                    if (null == privacyKey) {
                        ok = false;
                        alert("An error occured while getting Privacy key");
                    } else {
                        // crypt and update user data
                        byte[] random = new byte[4];
                        new Random().nextBytes(random);

                        byte[] idUserCrypted = ProcessInfo.getInstance().getMorphoSample().encryptPrivacyData(idUser.getBytes(), privacyKey, random);
                        ret = morphoDatabase.getUserBuffer(idUserCrypted, morphoUser);
                        if (ErrorCodes.MORPHO_OK == ret) {
                            byte[] firstNameCrypted = ProcessInfo.getInstance().getMorphoSample().encryptPrivacyData(firstName.getBytes(), privacyKey, random);
                            ret = morphoUser.putBufferField(1, MorphoTools.checkfield(firstNameCrypted,((EnrollInfo) morphoInfo).isUpdateTemplate()));
                            if (ErrorCodes.MORPHO_OK == ret) {
                                byte[] lastNameCrypted = ProcessInfo.getInstance().getMorphoSample().encryptPrivacyData(lastName.getBytes(), privacyKey, random);
                                ret = morphoUser.putBufferField(2, MorphoTools.checkfield(lastNameCrypted,((EnrollInfo) morphoInfo).isUpdateTemplate()));
                            }
                        }
                    }
                } else {
                    ret = morphoDatabase.getUser(idUser, morphoUser);
                    if (ErrorCodes.MORPHO_OK == ret) {
                        ret = morphoUser.putField(1, MorphoTools.checkfield(firstName,((EnrollInfo) morphoInfo).isUpdateTemplate()));
                        if (ErrorCodes.MORPHO_OK == ret) {
                            ret = morphoUser.putField(2, MorphoTools.checkfield(lastName,((EnrollInfo) morphoInfo).isUpdateTemplate()));
                        }
                    }
                }

                if (ok && (ErrorCodes.MORPHO_OK == ret)) {

                    ProcessInfo processInfo = ProcessInfo.getInstance();
                    if (processInfo.isNoCheck()) {
                        morphoUser.setNoCheckOnTemplateForDBStore(true);
                    }

                    int timeout = processInfo.getTimeout();

                    int acquisitionThreshold = 0;
                    int advancedSecurityLevelsRequired = 0;
                    final CompressionAlgorithm compressAlgo = ((EnrollInfo) morphoInfo).getCompressionAlgorithm();

                    if (processInfo.isFingerprintQualityThreshold()) {
                        acquisitionThreshold = processInfo.getFingerprintQualityThresholdvalue();
                    }

                    int compressRate = 0;
                    TemplateList templateList = new TemplateList();
                    if (!compressAlgo.equals(CompressionAlgorithm.NO_IMAGE)) {
//						if (ProcessInfo.getInstance().getBioDataEncryptionState() == true) {
//							// No image export when encryption data is enabled
//							mHandler.post(new Runnable()
//							{
//								@Override
//								public synchronized void run()
//								{
//									alert("Image export is forbidden when encryption data option is enabled");
//								}
//							});
//							notifyEndProcess();
//							return;
//						}

                        templateList.setActivateFullImageRetrieving(true);
                        if (compressAlgo.equals(CompressionAlgorithm.MORPHO_COMPRESS_WSQ)) {
                            compressRate = 15;
                        }
                    }

                    // FVP-2644 : export image in android
                    int exportMinutiae = 1;
                    final TemplateType templateType = ((EnrollInfo) morphoInfo).getTemplateType();
                    TemplateFVPType templateFVPType = ((EnrollInfo) morphoInfo).getFVPTemplateType();

                    // FVP-2644 : export image in android
//					if (templateType.compareTo(TemplateType.MORPHO_NO_PK_FP) != 0 || templateFVPType.compareTo(TemplateFVPType.MORPHO_NO_PK_FVP) != 0)
//					{
//						exportMinutiae = 1;
//					}

                    final int fingerNumber = ((EnrollInfo) morphoInfo).getFingerNumber();

                    boolean saveRecord = ((EnrollInfo) morphoInfo).isSavePKinDatabase();
                    Coder coder = processInfo.getCoder();

                    int detectModeChoice;
                    detectModeChoice = DetectionMode.MORPHO_ENROLL_DETECT_MODE.getValue();
                    if (processInfo.isForceFingerPlacementOnTop()) {
                        detectModeChoice |= DetectionMode.MORPHO_FORCE_FINGER_ON_TOP_DETECT_MODE.getValue();
                    }
                    if (processInfo.isWakeUpWithLedOff()) {
                        detectModeChoice |= MorphoWakeUpMode.MORPHO_WAKEUP_LED_OFF.getCode();
                    }
                    int callbackCmd = ProcessInfo.getInstance().getCallbackCmd();

                    // Prepare finger update if necessary
                    if(saveRecord &&  ((EnrollInfo) morphoInfo).isUpdateTemplate())	{
                        if(fingerNumber == 2) { // Update both fingers
                            boolean[] mask = {true,true};
                            ret = morphoUser.setTemplateUpdateMask(mask);
                        } else if(((EnrollInfo) morphoInfo).getFingerIndex() == 1) { // Update first finger only
                            boolean[] mask = {true};
                            ret = morphoUser.setTemplateUpdateMask(mask);
                        } else { // Update second finger

                            boolean[] mask = {false,true};
                            ret = morphoUser.setTemplateUpdateMask(mask);
                        }
                    }

                    if(ErrorCodes.MORPHO_OK == ret) {
                        ret = morphoDevice.setStrategyAcquisitionMode(ProcessInfo.getInstance().getStrategyAcquisitionMode());
                    }

                    if (ErrorCodes.MORPHO_OK == ret) {
                        ret = morphoUser.enroll(
                                timeout,
                                acquisitionThreshold,
                                advancedSecurityLevelsRequired,
                                compressAlgo,
                                compressRate,
                                exportMinutiae,
                                fingerNumber,
                                templateType,
                                templateFVPType,
                                saveRecord,
                                coder,
                                detectModeChoice,
                                templateList,
                                callbackCmd,
                                observer
                        );
                    }

                    ProcessInfo.getInstance().setCommandBioStart(false);

                    getAndWriteFFDLogs();

                    if (ErrorCodes.MORPHO_OK == ret && saveRecord) {
                        DatabaseItem databaseItemsItem = new DatabaseItem(idUser, firstName, lastName);
                        List<DatabaseItem> databaseItems = processInfo.getDatabaseItems();
                        databaseItems.add(databaseItemsItem);
                        processInfo.setDatabaseItems(databaseItems);
                    }

                    String message = "";
                    final TemplateList finalTemplateList = templateList;
                    final byte[] finalPrivacyKey = (privacyKey != null) ? Arrays.copyOf(privacyKey, privacyKey.length) : null;
                    if (MorphoDevicePrivacyModeStatus.PRIVACY_MODE_DISABLED != ProcessInfo.getInstance().getPrivacyModeStatus()) {
                        final int finalRet = ret;
                        // display alert dialog to choose whether template/image shall be decrypted before being saved
                        final AlertDialog alertDialog = new AlertDialog.Builder(ProcessInfo.getInstance().getMorphoSample()).create();
                        alertDialog.setTitle(ProcessInfo.getInstance().getMorphoSample().getResources().getString(R.string.morphosample));
                        alertDialog.setMessage(ProcessInfo.getInstance().getMorphoSample().getResources().getString(R.string.privacyDataDecryptChoice));
                        alertDialog.setCancelable(false);
                        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, ProcessInfo.getInstance().getMorphoSample().getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which)
                            {
                                // Data will be saved without being decrypted
                                String message = "";
                                try {
                                    for (int fingerIndex = 0; fingerIndex < fingerNumber; ++fingerIndex) {
                                        // Template extraction
                                        if ((ErrorCodes.MORPHO_OK == finalRet) && (templateType != TemplateType.MORPHO_NO_PK_FP)) {
                                            String template_file_name = AppContext.RootPath+"TemplateFP_" + idUser + "_finger" + fingerIndex + "_" + (fingerIndex + 1);
                                            template_file_name += "_pcrypt"; // Add "pcrypt" extension for "privacy crypt"
                                            template_file_name += templateType.getExtension();
                                            FileOutputStream fos = new FileOutputStream(template_file_name);
                                            fos.write(finalTemplateList.getTemplate(fingerIndex).getData());
                                            fos.close();
                                            message += "Finger #" + (fingerIndex + 1) + " - FP Template successfully exported in file [" + template_file_name + "]\n";
                                        }
                                        // Image extraction
                                        if ((ErrorCodes.MORPHO_OK == finalRet) && finalTemplateList.isActivateFullImageRetrieving()) {
                                            String image_file_name = "";
                                            image_file_name += AppContext.RootPath+"Image_" + idUser + "_" + (fingerIndex + 1) + "_pcrypt";
                                            image_file_name += compressAlgo.getExtension();

                                            FileOutputStream fos = new FileOutputStream(image_file_name);
                                            fos.write(finalTemplateList.getImage(fingerIndex).getImage());
                                            fos.close();
                                            message += "Finger #" + (fingerIndex + 1) + " - Image successfully exported in file ["+image_file_name+ "]\n";
                                        }
                                    }
                                } catch (FileNotFoundException e) {
                                    Log.i("ENROLL", e.getMessage());
                                } catch (IOException e)	{
                                    Log.i("ENROLL", e.getMessage());
                                }

                                final int internalError = morphoDevice.getInternalError();
                                final String alerMessage = message;
                                mHandler.post(new Runnable()
                                {
                                    @Override
                                    public synchronized void run()
                                    {
                                        alert(finalRet, internalError, "Enroll", alerMessage, false);
                                    }
                                });

                                notifyEndProcess();
                            }
                        });
                        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, ProcessInfo.getInstance().getMorphoSample().getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which)
                            {
                                // Data will be decrypted before being saved
                                String message = "";
                                try {
                                    for (int fingerIndex = 0; fingerIndex < fingerNumber; ++fingerIndex) {
                                        // Template extraction
                                        if ((ErrorCodes.MORPHO_OK == finalRet) && (templateType != TemplateType.MORPHO_NO_PK_FP)) {
                                            byte[] decryptedTemplate = ProcessInfo.getInstance().getMorphoSample().decryptAndCheckPrivacyData(finalTemplateList.getTemplate(fingerIndex).getData(), finalPrivacyKey);
                                            String template_file_name = AppContext.RootPath+"TemplateFP_" + idUser + "_finger" + fingerIndex + "_" + (fingerIndex + 1);
                                            template_file_name += templateType.getExtension();
                                            FileOutputStream fos = new FileOutputStream(template_file_name);
                                            fos.write(decryptedTemplate);
                                            fos.close();
                                            message += "Finger #" + (fingerIndex + 1) + " - FP Template successfully exported in file [" + template_file_name + "]\n";
                                        }
                                        // Image extraction
                                        if ((ErrorCodes.MORPHO_OK == finalRet) && finalTemplateList.isActivateFullImageRetrieving()) {
                                            byte[] decryptedImageWithHeader = ProcessInfo.getInstance().getMorphoSample().decryptAndCheckPrivacyData(finalTemplateList.getImage(fingerIndex).getImage(), finalPrivacyKey);
                                            String image_file_name = AppContext.RootPath+"Image_" + idUser + "_" + (fingerIndex + 1);
                                            image_file_name += compressAlgo.getExtension();
                                            FileOutputStream fos = new FileOutputStream(image_file_name);
                                            if (compressAlgo.equals(CompressionAlgorithm.MORPHO_NO_COMPRESS)) {
                                                // Ignore first 12 bytes of RAW header
                                                fos.write(Arrays.copyOfRange(decryptedImageWithHeader, 12, decryptedImageWithHeader.length));
                                            } else if (compressAlgo.equals(CompressionAlgorithm.MORPHO_COMPRESS_WSQ)) {
                                                // Ignore first 12 bytes of RAW header and 12 bytes of WSQ header
                                                fos.write(Arrays.copyOfRange(decryptedImageWithHeader, 24, decryptedImageWithHeader.length));
                                            } else {
                                                fos.write(decryptedImageWithHeader);
                                            }
                                            fos.close();
                                            message += "Finger #" + (fingerIndex + 1) + " - Image successfully exported in file ["+image_file_name+ "]\n";
                                        }
                                    }
                                } catch (FileNotFoundException e) {
                                    Log.i("ENROLL", e.getMessage());
                                } catch (IOException e)	{
                                    Log.i("ENROLL", e.getMessage());
                                }

                                final int internalError = morphoDevice.getInternalError();
                                final String alerMessage = message;
                                mHandler.post(new Runnable()
                                {
                                    @Override
                                    public synchronized void run()
                                    {
                                        alert(finalRet, internalError, "Enroll", alerMessage, false);
                                    }
                                });

                                notifyEndProcess();
                            }
                        });

                        if (ErrorCodes.MORPHO_OK == ret) {
                            alertDialog.show();
                        } else {
                            final int internalError = morphoDevice.getInternalError();
                            final String alerMessage = message;
                            mHandler.post(new Runnable()
                            {
                                @Override
                                public synchronized void run()
                                {
                                    alert(finalRet, internalError, "Enroll", alerMessage, false);
                                }
                            });

                            notifyEndProcess();
                        }
                    } else {
                        try {
                            for (int fingerIndex = 0; fingerIndex < fingerNumber; ++fingerIndex) {
                                // Template extraction
                                if ((ErrorCodes.MORPHO_OK == ret) && (templateType != TemplateType.MORPHO_NO_PK_FP)) {
                                    String template_file_name = AppContext.RootPath+"TemplateFP_" + idUser + "_finger" + fingerIndex + "_" + (fingerIndex + 1);
                                    if (ProcessInfo.getInstance().getBioDataEncryptionState() == true) {
                                        template_file_name += "_crypt";
                                    }
                                    template_file_name += templateType.getExtension();
                                    FileOutputStream fos = new FileOutputStream(template_file_name);
                                    fos.write(finalTemplateList.getTemplate(fingerIndex).getData());
                                    fos.close();
                                    message += "Finger #" + (fingerIndex + 1) + " - FP Template successfully exported in file [" + template_file_name + "]\n";
                                }
                                // FVP template extraction
                                if ((ErrorCodes.MORPHO_OK == ret) && ((MorphoInfo.m_b_fvp) && (templateFVPType != TemplateFVPType.MORPHO_NO_PK_FVP))) {
                                    FileOutputStream fos = new FileOutputStream(AppContext.RootPath+"TemplateFVP_" + idUser + "_finger" + fingerIndex + "_" + (fingerIndex + 1) + templateFVPType.getExtension());
                                    fos.write(finalTemplateList.getFVPTemplate(fingerIndex).getData());
                                    fos.close();
                                    message += "Finger #" + (fingerIndex + 1) + " - FVP Template successfully exported in file ["+AppContext.RootPath+"TemplateFVP_" + idUser + "_finger" + fingerIndex + "_"
                                            + (fingerIndex + 1) + templateFVPType.getExtension() + "]\n";
                                }
                                // Image extraction
                                if ((ErrorCodes.MORPHO_OK == ret) && finalTemplateList.isActivateFullImageRetrieving()) {
                                    byte[] data = null;
                                    String image_file_name = "";
                                    if (compressAlgo.equals(CompressionAlgorithm.MORPHO_COMPRESS_WSQ) || compressAlgo.equals(CompressionAlgorithm.MORPHO_COMPRESS_V1)) {
                                        //Case of WSQ or morpho_v1 image
                                        if (ProcessInfo.getInstance().getBioDataEncryptionState() == true) {
                                            data = finalTemplateList.getImage(fingerIndex).getImage();
                                        } else	{
                                            data = finalTemplateList.getImage(fingerIndex).getCompressedImage();
                                        }
                                    } else {
                                        //Case of RAW Image
                                        data = finalTemplateList.getImage(fingerIndex).getImage();
                                    }

                                    image_file_name += AppContext.RootPath+"Image_" + idUser + "_" + (fingerIndex + 1);
                                    if (ProcessInfo.getInstance().getBioDataEncryptionState() == true) {
                                        image_file_name += "_crypt";
                                    }
                                    image_file_name += compressAlgo.getExtension();

                                    FileOutputStream fos = new FileOutputStream(image_file_name);
                                    fos.write(data);
                                    fos.close();
                                    message += "Finger #" + (fingerIndex + 1) + " - Image successfully exported in file ["+image_file_name+ "]\n";
                                }
                            }
                        } catch (FileNotFoundException e) {
                            Log.i("ENROLL", e.getMessage());
                        } catch (IOException e)	{
                            Log.i("ENROLL", e.getMessage());
                        }

                        final int internalError = morphoDevice.getInternalError();
                        final int retvalue = ret;
                        final String alerMessage = message;
                        mHandler.post(new Runnable()
                        {
                            @Override
                            public synchronized void run()
                            {
                                alert(retvalue, internalError, "Enroll", alerMessage, false);
                            }
                        });

                        notifyEndProcess();
                    }

                    Looper.loop();
                } // enrol OK
            }
        }));
        commandThread.start();
    }

    public void morphoUserVerify(final Observer observer) {
        Thread commandThread = (new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                try	{
                    if (ProcessInfo.getInstance().getDatabaseSelectedIndex() != -1)	{
                        int i = ProcessInfo.getInstance().getDatabaseSelectedIndex();

                        List<DatabaseItem> databaseItems = ProcessInfo.getInstance().getDatabaseItems();

                        String userID = databaseItems.get(i).getId();

                        final MorphoUser morphoUser = new MorphoUser();

                        int ret = 0;
                        final byte[] privacyKey;

                        if (MorphoDevicePrivacyModeStatus.PRIVACY_MODE_DISABLED != ProcessInfo.getInstance().getPrivacyModeStatus()) {
                            // get Privacy key
                            privacyKey = ProcessInfo.getInstance().getMorphoSample().getPrivacyKey();
                            if (null == privacyKey) {
                                alert("An error occured while getting Privacy key");
                            } else {
                                // crypt and update user data
                                byte[] random = new byte[4];
                                new Random().nextBytes(random);

                                byte[] idUserCrypted = ProcessInfo.getInstance().getMorphoSample().encryptPrivacyData(userID.getBytes(), privacyKey, random);
                                ret = morphoDatabase.getUserBuffer(idUserCrypted, morphoUser);
                            }
                        } else {
                            ret = morphoDatabase.getUser(userID, morphoUser);
                        }

                        if (ret == 0) {
                            ProcessInfo processInfo = ProcessInfo.getInstance();
                            int timeout = processInfo.getTimeout();
                            int far = processInfo.getMatchingThreshold();
                            Coder coder = processInfo.getCoder();
                            int detectModeChoice;
                            MatchingStrategy matchingStrategy = processInfo.getMatchingStrategy();

                            int callbackCmd = ProcessInfo.getInstance().getCallbackCmd();

                            callbackCmd &= ~CallbackMask.MORPHO_CALLBACK_ENROLLMENT_CMD.getValue();

                            ResultMatching resultMatching = null;
                            // Check if running under security mode
                            boolean isTunnelingMode = false;
                            boolean isOfferedSecurityMode = false;
                            ArrayList<SecurityOption> securityOptions = ProcessInfo.getInstance().getSecurityOptions();
                            for (SecurityOption so : securityOptions) {
                                if (so.title.equals("Mode Tunneling")) {
                                    isTunnelingMode = so.activated;
                                } else if (so.title.equals("Mode Offered Security")) {
                                    isOfferedSecurityMode = so.activated;
                                }
                            }
                            if (!isTunnelingMode && !isOfferedSecurityMode) {
                                resultMatching = new ResultMatching();
                            }

                            detectModeChoice = DetectionMode.MORPHO_ENROLL_DETECT_MODE.getValue();

                            if (processInfo.isForceFingerPlacementOnTop())	{
                                detectModeChoice |= DetectionMode.MORPHO_FORCE_FINGER_ON_TOP_DETECT_MODE.getValue();
                            }

                            if (processInfo.isWakeUpWithLedOff()) {
                                detectModeChoice |= MorphoWakeUpMode.MORPHO_WAKEUP_LED_OFF.getCode();
                            }

                            ret = morphoDevice.setStrategyAcquisitionMode(ProcessInfo.getInstance().getStrategyAcquisitionMode());

                            if(ret == 0) {
                                ret = morphoUser.verify(timeout, far, coder, detectModeChoice, matchingStrategy, callbackCmd, observer, resultMatching);
                            }

                            getAndWriteFFDLogs();

                            final String [] message = {""};
                            if (ret == ErrorCodes.MORPHO_OK) {
                                String user_authenticated = "";
                                for (int j = 0; j <= 2; j++) {
                                    if (MorphoDevicePrivacyModeStatus.PRIVACY_MODE_DISABLED != ProcessInfo.getInstance().getPrivacyModeStatus()) {
                                        if(ProcessInfo.getInstance().getPrivacyKey()==null)
                                        {
                                            alert("An error occured while getting Privacy key");
                                        }
                                        else
                                        {
                                            byte[] 	memB = morphoUser.getBufferField(j);
                                            byte[] decrypted_bio_data =	ProcessInfo.getInstance().getMorphoSample().decryptAndCheckPrivacyData(memB, ProcessInfo.getInstance().getPrivacyKey());

                                            String mem = new String(decrypted_bio_data);
                                            user_authenticated = user_authenticated + " " + mem;
                                        }
                                    } else {
                                        String mem = morphoUser.getField(j);
                                        user_authenticated = user_authenticated + " " + mem;
                                    }
                                }

                                message[0] = "User authenticated :\n";
                                if (MorphoDevicePrivacyModeStatus.PRIVACY_MODE_DISABLED != ProcessInfo.getInstance().getPrivacyModeStatus()) {
                                    //////////////////////////
                                    final AlertDialog alertDialog = new AlertDialog.Builder(ProcessInfo.getInstance().getMorphoSample()).create();
                                    alertDialog.setTitle(ProcessInfo.getInstance().getMorphoSample().getResources().getString(R.string.morphosample));
                                    alertDialog.setMessage("Decrypt User info : ");
                                    alertDialog.setCancelable(false);
                                    alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener()
                                    {
                                        public void onClick(DialogInterface dialog, int which)
                                        {
                                            byte[] 	userIDB = morphoUser.getBufferField(0);
                                            byte[]  firstNameB = morphoUser.getBufferField(1);
                                            byte[]  lastNameB = morphoUser.getBufferField(2);

                                            String userIDt = new String(userIDB);
                                            String firstName= new String(firstNameB);
                                            String lastName= new String(lastNameB);

                                            message[0] += "\t" + getString(R.string.idnumber) + " : " + userIDt + "\n";
                                            message[0] += "\t" + getString(R.string.firstname) + " : " + firstName + "\n";
                                            message[0] += "\t" + getString(R.string.lastname) + " : " + lastName + "\n";

                                            final String msg = message[0];
                                            final int l_ret = 0;
                                            final int internalError = morphoDevice.getInternalError();

                                            mHandler.post(new Runnable()
                                            {
                                                @Override
                                                public synchronized void run()
                                                {
                                                    alert(l_ret, internalError, "Verify", msg, true);
                                                }
                                            });
                                        }
                                    });
                                    alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.ok), new DialogInterface.OnClickListener()
                                    {
                                        public void onClick(DialogInterface dialog, int which)
                                        {
                                            byte[] 	userIDB = morphoUser.getBufferField(0);
                                            byte[]  firstNameB = morphoUser.getBufferField(1);
                                            byte[]  lastNameB = morphoUser.getBufferField(2);
                                            if(ProcessInfo.getInstance().getPrivacyKey()==null)
                                            {
                                                alert("An error occured while getting Privacy key");
                                            }
                                            else
                                            {
                                                byte[] decrypted_bio_data=	ProcessInfo.getInstance().getMorphoSample().decryptAndCheckPrivacyData(userIDB,ProcessInfo.getInstance().getPrivacyKey());


                                                String userIDt = new String(decrypted_bio_data);

                                                decrypted_bio_data=	ProcessInfo.getInstance().getMorphoSample().decryptAndCheckPrivacyData(firstNameB,ProcessInfo.getInstance().getPrivacyKey());

                                                String firstName= new String(decrypted_bio_data);

                                                decrypted_bio_data=	ProcessInfo.getInstance().getMorphoSample().decryptAndCheckPrivacyData(lastNameB,ProcessInfo.getInstance().getPrivacyKey());

                                                String lastName= new String(decrypted_bio_data);

                                                message[0] += "\t" + getString(R.string.idnumber) + " : " + userIDt + "\n";
                                                message[0] += "\t" + getString(R.string.firstname) + " : " + firstName + "\n";
                                                message[0] += "\t" + getString(R.string.lastname) + " : " + lastName + "\n";

                                                final String msg = message[0];
                                                final int l_ret = 0;
                                                final int internalError = morphoDevice.getInternalError();

                                                mHandler.post(new Runnable()
                                                {
                                                    @Override
                                                    public synchronized void run()
                                                    {
                                                        alert(l_ret, internalError, "Verify", msg, true);
                                                    }
                                                });
                                            };
                                        }
                                    });
                                    /////////////////////////
                                    alertDialog.show();

                                } else {
                                    message[0] += "\t" + getString(R.string.idnumber) + " : " + morphoUser.getField(0) + "\n";
                                    message[0] += "\t" + getString(R.string.firstname) + " : " + morphoUser.getField(1) + "\n";
                                    message[0] += "\t" + getString(R.string.lastname) + " : " + morphoUser.getField(2) + "\n";
                                }
                                if (resultMatching != null) {
                                    message[0] += "\tMatching Score = " + resultMatching.getMatchingScore() + "\n";
                                    message[0] += "\tPK Number = " + resultMatching.getMatchingPKNumber();
                                }
                            }
                            final String msg = message[0];
                            final int l_ret = ret;
                            final int internalError = morphoDevice.getInternalError();

                            mHandler.post(new Runnable()
                            {
                                @Override
                                public synchronized void run()
                                {
                                    alert(l_ret, internalError, "Verify", msg, true);
                                }
                            });
                        }
                    }
                    else
                    {
                        mHandler.post(new Runnable()
                        {
                            @Override
                            public synchronized void run()
                            {
                                alert("Select a user in the list view.");
                            }
                        });
                    }
                }
                catch (Exception e)
                {
                    Log.e("ERROR", e.getMessage());
                }
                ProcessInfo.getInstance().setCommandBioStart(false);

                notifyEndProcess();
                Looper.loop();
            }
        }));
        commandThread.start();
    }

    public static ITemplateType getTemplateTypeFromExtention(String extention)
    {
        for (TemplateType templateType : TemplateType.values())
        {
            if (templateType.getExtension().equalsIgnoreCase(extention))
            {
                return templateType;
            }
        }
        for (TemplateFVPType templateFVPType : TemplateFVPType.values())
        {
            if (templateFVPType.getExtension().equalsIgnoreCase(extention))
            {
                return templateFVPType;
            }
        }
        return TemplateType.MORPHO_NO_PK_FP;
    }

    public static String getFileExtension(String fileName)
    {
        String extension = "";
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex >= 0)
        {
            extension = fileName.substring(dotIndex);
        }
        return extension;
    }

    public void morphoDeviceVerifyWithFile(final Observer observer, final String fileName)
    {
        DataInputStream dis;
        try
        {
            dis = new DataInputStream(new FileInputStream(fileName));

            int length = dis.available();
            final byte[] buffer = new byte[length];
            dis.readFully(buffer);

            Thread commandThread = (new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    Template template = new Template();
                    TemplateFVP templateFVP = new TemplateFVP();

                    TemplateList templateList = new TemplateList();
                    byte[] random = new byte[4];
                    new Random().nextBytes(random);

                    ITemplateType iTemplateType = getTemplateTypeFromExtention(getFileExtension(fileName));
                    if (iTemplateType instanceof TemplateFVPType)
                    {
                        if(MorphoDevicePrivacyModeStatus.PRIVACY_MODE_DISABLED != ProcessInfo.getInstance().getPrivacyModeStatus()) {
                            byte[] privacyKey = null;
                            privacyKey = ProcessInfo.getInstance().getPrivacyKey();
                            if (null == privacyKey) {
                                alert("An error occured while getting Privacy Key");
                                return;
                            }

                            byte[] templateCrypted = ProcessInfo.getInstance().getMorphoSample().encryptPrivacyData(buffer,privacyKey, random);
                            templateFVP.setData(templateCrypted);
                        } else {
                            templateFVP.setData(buffer);
                        }
                        templateFVP.setTemplateFVPType((TemplateFVPType) iTemplateType);
                        templateList.putFVPTemplate(templateFVP);
                    }
                    else
                    {
                        if(MorphoDevicePrivacyModeStatus.PRIVACY_MODE_DISABLED != ProcessInfo.getInstance().getPrivacyModeStatus()) {
                            byte[] privacyKey = null;
                            privacyKey = ProcessInfo.getInstance().getPrivacyKey();
                            if (null == privacyKey) {
                                alert("An error occured while getting Privacy Key");
                                return;
                            }

                            byte[] templateCrypted = ProcessInfo.getInstance().getMorphoSample().encryptPrivacyData(buffer,privacyKey, random);
                            template.setData(templateCrypted);
                        } else {
                            template.setData(buffer);
                        }
                        template.setTemplateType((TemplateType) iTemplateType);
                        templateList.putTemplate(template);
                    }

                    int timeOut = 0;
                    int far = FalseAcceptanceRate.MORPHO_FAR_5;
                    Coder coderChoice = Coder.MORPHO_DEFAULT_CODER;
                    int detectModeChoice = DetectionMode.MORPHO_VERIF_DETECT_MODE.getValue();
                    int matchingStrategy = 0;

                    int callbackCmd = ProcessInfo.getInstance().getCallbackCmd();

                    callbackCmd &= ~CallbackMask.MORPHO_CALLBACK_ENROLLMENT_CMD.getValue();

                    ResultMatching resultMatching = null;
                    // Check if running under security mode
                    boolean isTunnelingMode = false;
                    boolean isOfferedSecurityMode = false;
                    ArrayList<SecurityOption> securityOptions = ProcessInfo.getInstance().getSecurityOptions();
                    for (SecurityOption so : securityOptions) {
                        if (so.title.equals("Mode Tunneling")) {
                            isTunnelingMode = so.activated;
                        } else if (so.title.equals("Mode Offered Security")) {
                            isOfferedSecurityMode = so.activated;
                        }
                    }
                    if (!isTunnelingMode && !isOfferedSecurityMode) {
                        resultMatching = new ResultMatching();
                    }

                    int ret = morphoDevice.setStrategyAcquisitionMode(ProcessInfo.getInstance().getStrategyAcquisitionMode());

                    if(ret ==0)
                    {
                        ret = morphoDevice.verify(timeOut, far, coderChoice, detectModeChoice, matchingStrategy, templateList, callbackCmd, observer, resultMatching);
                    }

                    ProcessInfo.getInstance().setCommandBioStart(false);

                    getAndWriteFFDLogs();

                    String message = "";

                    if (ret == ErrorCodes.MORPHO_OK && resultMatching != null)
                    {
                        message = "Matching Score = " + resultMatching.getMatchingScore() + "\nPK Number = " + resultMatching.getMatchingPKNumber();
                    }

                    final String msg = message;
                    final int l_ret = ret;
                    final int internalError = morphoDevice.getInternalError();
                    mHandler.post(new Runnable()
                    {
                        @Override
                        public synchronized void run()
                        {
                            alert(l_ret, internalError, "Verify", msg, true);
                        }
                    });
                    notifyEndProcess();
                }
            }));

            commandThread.start();

            if (dis != null)
            {
                dis.close();
            }
        }
        catch (FileNotFoundException e)
        {
            alert(e.getMessage());
        }
        catch (IOException e)
        {
            alert(e.getMessage());
        }
        catch (Exception e)
        {
            alert(e.getMessage());
        }
    }

    public void morphoDeviceCapture(final Observer observer) {
        Thread commandThread = (new Thread(new Runnable() {
            @Override
            public void run() {

                Looper.prepare();

                index = 0;
                isCaptureVerif = false;
                final TemplateList templateList = new TemplateList();
                MorphoInfo morphoInfo = ProcessInfo.getInstance().getMorphoInfo();
                ProcessInfo processInfo = ProcessInfo.getInstance();
                int timeout;
                int acquisitionThreshold = 0;
                int advancedSecurityLevelsRequired;
                TemplateType templateType;
                TemplateFVPType templateFVPType;
                int maxSizeTemplate = 255;
                EnrollmentType enrollType;
                LatentDetection latentDetection;
                Coder coderChoice;
                int detectModeChoice;

                boolean exportFVP = false, exportFP = false;
                timeout = processInfo.getTimeout();

                if(processInfo.isFingerprintQualityThreshold())	{
                    acquisitionThreshold = processInfo.getFingerprintQualityThresholdvalue();
                }

                templateType = ((CaptureInfo) morphoInfo).getTemplateType();
                templateFVPType = ((CaptureInfo) morphoInfo).getTemplateFVPType();

                if (templateType != TemplateType.MORPHO_NO_PK_FP) {
                    exportFP = true;
                    if (templateType == TemplateType.MORPHO_PK_MAT || templateType == TemplateType.MORPHO_PK_MAT_NORM || templateType == TemplateType.MORPHO_PK_PKLITE)	{
                        maxSizeTemplate = 1;
                    } else {
                        maxSizeTemplate = 255;
                    }
                } else {
                    if (MorphoInfo.m_b_fvp == false) {
                        templateType = TemplateType.MORPHO_PK_COMP;
                    }
                    maxSizeTemplate = 255;
                }

                if (templateFVPType != TemplateFVPType.MORPHO_NO_PK_FVP) {
                    exportFVP = true;
                }

                if (MorphoInfo.m_b_fvp) {
                    if (((CaptureInfo) morphoInfo).getCaptureType() != CaptureType.Verif) {
                        templateFVPType = TemplateFVPType.MORPHO_PK_FVP;
                    } else {
                        templateFVPType = TemplateFVPType.MORPHO_PK_FVP_MATCH;
                    }
                } else {
                    templateFVPType = TemplateFVPType.MORPHO_NO_PK_FVP;
                }

                if (((CaptureInfo) morphoInfo).getCaptureType() == CaptureType.Enroll) {
                    enrollType = EnrollmentType.THREE_ACQUISITIONS;
                } else {
                    isCaptureVerif = true;
                    currentCaptureBitmapId = R.id.imageView2;
                    enrollType = EnrollmentType.ONE_ACQUISITIONS;
                }

                if (((CaptureInfo) morphoInfo).isLatentDetect()) {
                    latentDetection = LatentDetection.LATENT_DETECT_ENABLE;
                } else {
                    latentDetection = LatentDetection.LATENT_DETECT_DISABLE;
                }

                coderChoice = processInfo.getCoder();

                detectModeChoice = DetectionMode.MORPHO_ENROLL_DETECT_MODE.getValue();

                if (processInfo.isForceFingerPlacementOnTop()) {
                    detectModeChoice |= DetectionMode.MORPHO_FORCE_FINGER_ON_TOP_DETECT_MODE.getValue();
                }

                if (processInfo.isWakeUpWithLedOff()) {
                    detectModeChoice |= MorphoWakeUpMode.MORPHO_WAKEUP_LED_OFF.getCode();
                }

                advancedSecurityLevelsRequired = 0;
                if (((CaptureInfo) morphoInfo).getCaptureType() != CaptureType.Verif) {
                    if (processInfo.isAdvancedSecLevCompReq()) {
                        advancedSecurityLevelsRequired = 1;
                    } else {
                        advancedSecurityLevelsRequired = 0;
                    }
                } else {
                    advancedSecurityLevelsRequired = 0xFF;
                    if (processInfo.isAdvancedSecLevCompReq()) {
                        advancedSecurityLevelsRequired = 1;
                    }
                }

                int callbackCmd = ProcessInfo.getInstance().getCallbackCmd();

                int nbFinger = ((CaptureInfo) morphoInfo).getFingerNumber();
                final String idUser = ((CaptureInfo) morphoInfo).getIDNumber();

                int ret = morphoDevice.setStrategyAcquisitionMode(ProcessInfo.getInstance().getStrategyAcquisitionMode());

                if(ret == ErrorCodes.MORPHO_OK) {
                    ret = morphoDevice.capture(timeout, acquisitionThreshold, advancedSecurityLevelsRequired,
                            nbFinger, templateType, templateFVPType, maxSizeTemplate, enrollType,
                            latentDetection, coderChoice, detectModeChoice, CompressionAlgorithm.MORPHO_NO_COMPRESS, 0, templateList, callbackCmd, observer);
                }

                ProcessInfo.getInstance().setCommandBioStart(false);

                getAndWriteFFDLogs();

                String message = "";
                try	{
                    if (ret == ErrorCodes.MORPHO_OK) {
                        int NbTemplateFVP = templateList.getNbFVPTemplate();
                        final int NbTemplate = templateList.getNbTemplate();
                        final TemplateType finalTemplateType = templateType;
                        if (MorphoInfo.m_b_fvp) {
                            if (NbTemplateFVP > 0) {
                                TemplateFVP t = templateList.getFVPTemplate(0);
                                message += "Advanced Security Levels Compatibility: " + (t.getAdvancedSecurityLevelsCompatibility() == true ? "Yes" : "NO") + "\n";
                                for (int i = 0; i < NbTemplateFVP; i++)	{
                                    t = templateList.getFVPTemplate(i);
                                    message += "Finger #" + (i + 1) + " - Quality Score: " + t.getTemplateQuality() + "\n";
                                }
                            }
                        } else {
                            if (NbTemplate > 0)	{
                                for (int i = 0; i < NbTemplate; i++)	{
                                    Template t = templateList.getTemplate(i);
                                    message += "Finger #" + (i + 1) + " - Quality Score: " + t.getTemplateQuality() + "\n";
                                }
                            }
                        }

                        if (exportFVP) {
                            for (int i = 0; i < NbTemplateFVP; i++)	{
                                TemplateFVP t = templateList.getFVPTemplate(i);
                                FileOutputStream fos = new FileOutputStream(AppContext.RootPath+"TemplateFVP_" + idUser + "_f" + (i + 1) + templateFVPType.getExtension());
                                byte[] data = t.getData();
                                fos.write(data);
                                fos.close();
                                message += "Finger #" + (i + 1) + " - FVP Template successfully exported in file ["+AppContext.RootPath+"TemplateFVP_" + idUser + "_f" + (i + 1) + templateFVPType.getExtension()
                                        + "]\n";
                            }
                        }

                        if (exportFP) {
                            if (MorphoDevicePrivacyModeStatus.PRIVACY_MODE_DISABLED != ProcessInfo.getInstance().getPrivacyModeStatus()) {
                                // display alert dialog to choose whether template received shall be decrypted before being saved
                                final AlertDialog alertDialog = new AlertDialog.Builder(ProcessInfo.getInstance().getMorphoSample()).create();
                                alertDialog.setTitle(ProcessInfo.getInstance().getMorphoSample().getResources().getString(R.string.morphosample));
                                alertDialog.setMessage(ProcessInfo.getInstance().getMorphoSample().getResources().getString(R.string.privacyDataDecryptChoice));
                                alertDialog.setCancelable(false);
                                alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, ProcessInfo.getInstance().getMorphoSample().getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        // Data will be saved without being decrypted
                                        String message = "";
                                        for (int i = 0; i < NbTemplate; i++) {
                                            try {
                                                String file_name = AppContext.RootPath+"TemplateFP_" + idUser + "_f" + (i + 1) + "_pcrypt";
                                                file_name += finalTemplateType.getExtension();
                                                Template t = templateList.getTemplate(i);
                                                FileOutputStream fos = new FileOutputStream(file_name);
                                                fos.write(t.getData());
                                                fos.close();
                                                message += "Finger #" + (i + 1) + " - FP Template successfully exported in file [" + file_name + "]\n";
                                            } catch(FileNotFoundException e) {
                                                Log.i("CAPTURE", e.getMessage());
                                            } catch (IOException e)	{
                                                Log.i("CAPTURE", e.getMessage());
                                            }
                                        }

                                        final String alertMessage = message;
                                        mHandler.post(new Runnable()
                                        {
                                            @Override
                                            public synchronized void run()
                                            {
                                                alert(0, 0, "Capture", alertMessage, false);
                                            }
                                        });
                                        notifyEndProcess();
                                    }
                                });
                                alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, ProcessInfo.getInstance().getMorphoSample().getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        // Data will be decrypted before being saved
                                        byte[] privacyKey = ProcessInfo.getInstance().getMorphoSample().getPrivacyKey();
                                        if (null == privacyKey) {
                                            alert("En error occured while getting Privacy key");
                                        } else {
                                            String message = "";
                                            for (int i = 0; i < NbTemplate; i++) {
                                                try {
                                                    String file_name = AppContext.RootPath+"TemplateFP_" + idUser + "_f" + (i + 1);
                                                    file_name += finalTemplateType.getExtension();
                                                    Template t = templateList.getTemplate(i);
                                                    FileOutputStream fos = new FileOutputStream(file_name);
                                                    byte[] data = ProcessInfo.getInstance().getMorphoSample().decryptAndCheckPrivacyData(t.getData(), privacyKey);
                                                    fos.write(data);
                                                    fos.close();
                                                    message += "Finger #" + (i + 1) + " - FP Template successfully exported in file [" + file_name + "]\n";
                                                } catch(FileNotFoundException e) {
                                                    Log.i("CAPTURE", e.getMessage());
                                                } catch (IOException e)	{
                                                    Log.i("CAPTURE", e.getMessage());
                                                }
                                            }

                                            final String alertMessage = message;
                                            mHandler.post(new Runnable()
                                            {
                                                @Override
                                                public synchronized void run()
                                                {
                                                    alert(0, 0, "Capture", alertMessage, false);
                                                }
                                            });
                                            notifyEndProcess();
                                        }
                                    }
                                });
                                alertDialog.show();
                            } else {
                                for (int i = 0; i < NbTemplate; i++) {
                                    try {
                                        String file_name = AppContext.RootPath+"TemplateFP_" + idUser + "_f" + (i + 1);
                                        if (ProcessInfo.getInstance().getBioDataEncryptionState() == true) {
                                            file_name += "_crypt";
                                        }
                                        file_name += templateType.getExtension();
                                        Template t = templateList.getTemplate(i);
                                        FileOutputStream fos = new FileOutputStream(file_name);
                                        fos.write(t.getData());
                                        fos.close();
                                        message += "Finger #" + (i + 1) + " - FP Template successfully exported in file [" + file_name + "]\n";
                                    } catch(FileNotFoundException e) {
                                        Log.i("CAPTURE", e.getMessage());
                                    } catch (IOException e)	{
                                        Log.i("CAPTURE", e.getMessage());
                                    }
                                }

                                final String alertMessage = message;
                                mHandler.post(new Runnable()
                                {
                                    @Override
                                    public synchronized void run()
                                    {
                                        alert(0, 0, "Capture", alertMessage, false);
                                    }
                                });
                                notifyEndProcess();
                            }
                        }
                        notifyEndProcess();
                    } else {
                        final String alertMessage = message;
                        final int finalRet = ret;
                        mHandler.post(new Runnable()
                        {
                            @Override
                            public synchronized void run()
                            {
                                alert(finalRet, 0, "Capture", alertMessage, false);
                            }
                        });
                        notifyEndProcess();
                    }

                    Looper.loop();

                } catch (Exception e) {
                    Log.i("CAPTURE", e.getMessage());
                }
            }
        }));

        commandThread.start();
    }

    private void notifyEndProcess()
    {
        mHandler.post(new Runnable()
        {
            @Override
            public synchronized void run()
            {
                try
                {
                    ProcessInfo.getInstance().getMorphoSample().stopProcess();
                }
                catch (Exception e)
                {
                    Log.d("notifyEndProcess", e.getMessage());
                }
            }
        });

    }

    @SuppressWarnings("deprecation")
    private void updateSensorProgressBar(int level)
    {
        try
        {
            ProgressBar progressBar = (ProgressBar) findViewById(R.id.vertical_progressbar);

            final float[] roundedCorners = new float[] { 5, 5, 5, 5, 5, 5, 5, 5 };
            ShapeDrawable pgDrawable = new ShapeDrawable(new RoundRectShape(roundedCorners, null, null));

            int color = Color.GREEN;

            if (level <= 25)
            {
                color = Color.RED;
            }
            else if (level <= 75)
            {
                color = Color.YELLOW;
            }
            pgDrawable.getPaint().setColor(color);
            ClipDrawable progress = new ClipDrawable(pgDrawable, Gravity.LEFT, ClipDrawable.HORIZONTAL);
            progressBar.setProgressDrawable(progress);
            progressBar.setBackgroundDrawable(getResources().getDrawable(android.R.drawable.progress_horizontal));
            progressBar.setProgress(level);
        }
        catch (Exception e)
        {
            e.getMessage();
        }
    }

    private void updateSensorMessage(String sensorMessage)
    {
        try
        {
            TextView tv = (TextView) findViewById(R.id.textViewMessage);
            tv.setText(sensorMessage);
        }
        catch (Exception e)
        {
            e.getMessage();
        }
    }

    private void updateImage(Bitmap bitmap, int id)
    {
        try
        {
            ImageView iv = (ImageView) findViewById(id);
            iv.setImageBitmap(bitmap);
        }
        catch (Exception e)
        {
            e.getMessage();
        }
    }

    @SuppressLint("NewApi")
    private void updateImageBackground(int id, int level) {
        ImageView iv = (ImageView) findViewById(id);
        Drawable drawable = null;
        if (level <= 25) {
            drawable = getResources().getDrawable(R.drawable.red_border);
        } else if (level <= 75) {
            drawable = getResources().getDrawable(R.drawable.yellow_border);
        } else {
            drawable = getResources().getDrawable(R.drawable.green_border);
        }
        if(iv != null) {
            iv.setBackground(drawable);
        }
    }

    @Override
    public synchronized void update(Observable o, Object arg)
    {
        try
        {
            // convert the object to a callback back message.
            CallbackMessage message = (CallbackMessage) arg;

            int type = message.getMessageType();

            switch (type)
            {

                case 1:
                    // message is a command.
                    Integer command = (Integer) message.getMessage();

                    // Analyze the command.
                    switch (command)
                    {
                        case 0:
                            strMessage = "move-no-finger";
                            break;
                        case 1:
                            strMessage = "move-finger-up";
                            break;
                        case 2:
                            strMessage = "move-finger-down";
                            break;
                        case 3:
                            strMessage = "move-finger-left";
                            break;
                        case 4:
                            strMessage = "move-finger-right";
                            break;
                        case 5:
                            strMessage = "press-harder";
                            break;
                        case 6:
                            strMessage = "move-latent";
                            break;
                        case 7:
                            strMessage = "remove-finger";
                            break;
                        case 8:
                            strMessage = "finger-ok";
                            // switch live acquisition ImageView
                            if (isCaptureVerif)
                            {
                                isCaptureVerif = false;
                                index = 4; //R.id.imageView5;
                            }
                            else
                            {
                                index++;
                            }

                            switch (index)
                            {
                                case 1:
                                    currentCaptureBitmapId = R.id.imageView2;
                                    break;
                                case 2:
                                    currentCaptureBitmapId = R.id.imageView3;
                                    break;
                                case 3:
                                    currentCaptureBitmapId = R.id.imageView4;
                                    break;
                                case 4:
                                    currentCaptureBitmapId = R.id.imageView5;
                                    break;
                                case 5:
                                    currentCaptureBitmapId = R.id.imageView6;
                                    break;
                                default:
                                case 0:
                                    currentCaptureBitmapId = R.id.imageView1;
                                    break;
                            }
                            break;
                    }

                    mHandler.post(new Runnable()
                    {
                        @Override
                        public synchronized void run()
                        {
                            updateSensorMessage(strMessage);
                        }
                    });

                    break;
                case 2:
                    // message is a low resolution image, display it.
                    byte[] image = (byte[]) message.getMessage();

                    MorphoImage morphoImage = MorphoImage.getMorphoImageFromLive(image);
                    int imageRowNumber = morphoImage.getMorphoImageHeader().getNbRow();
                    int imageColumnNumber = morphoImage.getMorphoImageHeader().getNbColumn();
                    final Bitmap imageBmp = Bitmap.createBitmap(imageColumnNumber, imageRowNumber, Config.ALPHA_8);

                    imageBmp.copyPixelsFromBuffer(ByteBuffer.wrap(morphoImage.getImage(), 0, morphoImage.getImage().length));
                    mHandler.post(new Runnable()
                    {
                        @Override
                        public synchronized void run()
                        {
                            updateImage(imageBmp, currentCaptureBitmapId);
                        }
                    });
                    break;
                case 3:
                    // message is the coded image quality.
                    final Integer quality = (Integer) message.getMessage();
                    mHandler.post(new Runnable()
                    {
                        @Override
                        public synchronized void run()
                        {
                            updateSensorProgressBar(quality);
                            updateImageBackground(currentCaptureBitmapId, quality);
                        }
                    });
                    break;
                //case 4:
                //byte[] enrollcmd = (byte[]) message.getMessage();
            }
        }
        catch (Exception e)
        {
            alert(e.getMessage());
        }
    }
}

