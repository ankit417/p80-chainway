package com.codniv.cookbook1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import com.codniv.cookbook1.info.MorphoInfo;
import com.codniv.cookbook1.info.ProcessInfo;
import com.codniv.cookbook1.info.subtype.SecurityOption;
import com.codniv.cookbook1.tools.DeviceDetectionMode;
import com.morpho.android.usb.USBManager;
//import com.morpho.morphosample.info.MorphoInfo;
//import com.morpho.morphosample.info.ProcessInfo;
//import com.morpho.morphosample.info.subtype.SecurityOption;
//import com.morpho.morphosample.pipe.MsoPipeImpl;
//import com.morpho.morphosample.tools.DeviceDetectionMode;
//import com.morpho.morphosample.tools.MorphoTools;
import com.morpho.morphosmart.pipe.ILog;
import com.morpho.morphosmart.pipe.SimpleLogger;
import com.morpho.morphosmart.sdk.ErrorCodes;
import com.morpho.morphosmart.sdk.FieldAttribute;
import com.morpho.morphosmart.sdk.IMsoSecu;
import com.morpho.morphosmart.sdk.MorphoDatabase;
import com.morpho.morphosmart.sdk.MorphoDevice;
import com.morpho.morphosmart.sdk.MorphoField;
import com.morpho.morphosmart.sdk.SecuConfig;
import com.morpho.morphosmart.sdk.TemplateType;
import com.morpho.morphosmart.sdk.MorphoDevice.MorphoDevicePrivacyModeStatus;
import com.rscja.deviceapi.UsbFingerprint;
import morpho.msosecu.sdk.api.MsoSecu;

public class MainActivity extends AppCompatActivity {

    MorphoDevice	morphoDevice;
    private int	sensorBus	= -1;
    private int	sensorAddress	= -1;
    private int	sensorFileDescriptor	= -1;
    private String	sensorName	= "";

    Button buttonConnection;

    private enum IntentRequestCode	{
        selectPrivacyKey
    }
    private IntentRequestCode requestCode;

    final static Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg)
        {
            Bundle bundle = msg.getData();
            String string = bundle.getString("KeyStatus");
            throw new RuntimeException(string);
        }
    };

    static {
        try {
            System.loadLibrary("MSO_Secu");
            Log.e("library","library loaded");
        } catch (UnsatisfiedLinkError e) {
            Log.e("MorphoSample", "Exception in loadLibrary: " + e);
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        if (MorphoFingerprint.isRebootSoft)
//        {
//            MorphoFingerprint.isRebootSoft = false;
//            finish();
//        }
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        Log.e("error","Start here");
        new InitTask().execute();
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        morphoDevice = new MorphoDevice();
        checkReadWritePermission();

    }

    public void grantPermission(View v)
    {
        USBManager.getInstance().initialize(this, "com.morpho.morphosample.USB_ACTION", true);
    }

    public void enumerate(View v) {
        TextView textViewSensorName = (TextView) findViewById(R.id.sensorName);

        Integer nbUsbDevice = new Integer(0);
        int ret = morphoDevice.initUsbDevicesNameEnum(nbUsbDevice);
        if (ret == ErrorCodes.MORPHO_OK) {
            if (nbUsbDevice > 0) {
                sensorName = morphoDevice.getUsbDeviceName(0);
                textViewSensorName.setText(sensorName);
//                buttonConnection.setEnabled(true);
            } else {
                final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                alertDialog.setTitle(this.getResources().getString(R.string.TMS));
                alertDialog.setMessage(getResources().getString(R.string.deviceNotDetected));
                alertDialog.setCancelable(false);
                alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, this.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //finish();
                    }
                });
                alertDialog.show();
            }

        } else {
            final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle(this.getResources().getString(R.string.TMS));
            alertDialog.setMessage(ErrorCodes.getError(ret, morphoDevice.getInternalError()));
            alertDialog.setCancelable(false);
            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, this.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    //finish();
                }
            });
            alertDialog.show();
        }

    }

    public void test(View v){
        Log.e("error","print this");
    }
//    @SuppressLint("UseValueOf")
    public void connection(View v)
    {
//        findViewById(R.id.btn_grantPermission).setEnabled(false);
//        findViewById(R.id.btn_enumerate).setEnabled(false);
//        findViewById(R.id.btn_ok).setEnabled(false);
//        findViewById(R.id.btn_cancel).setEnabled(false);

        Log.e("error","starting in connection");
        int ret = ErrorCodes.MORPHO_OK;
        final SecuConfig secuConfig = new SecuConfig();
        ret = morphoDevice.openUsbDevice(sensorName, 0);

        if (ret != ErrorCodes.MORPHO_OK) {
            final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle(this.getResources().getString(R.string.TMS));
            alertDialog.setMessage(ErrorCodes.getError(ret, morphoDevice.getInternalError()));
            alertDialog.setCancelable(false);
            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, this.getResources().getString(R.string.ok), new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int which)
                {
                    finish();
                }
            });
            alertDialog.show();
        } else {
            // Get device security configuration
            ret = morphoDevice.getSecuConfig(secuConfig);
            if(ret != ErrorCodes.MORPHO_OK)
                showMsgError(ret);

            boolean ok = true;

            // Check that certificates and keys are available on device in case of Offered security or Tunneling modes
            // Certificates and keys shall be stored in /sdcard/Keys folder
            if (secuConfig.isModeOfferedSecurity() || secuConfig.isModeTunneling()) {

//                File folder = new File(AppContext.RootPath+"Keys/");
//                if (!folder.isDirectory()) {
//                    ok = false;
//                    final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
//                    alertDialog.setTitle(this.getResources().getString(R.string.morphosample));
//                    alertDialog.setMessage(getResources().getString(R.string.setOpenSSLPath));
//                    alertDialog.setCancelable(false);
//                    alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, this.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int which) {
//                            finish();
//                        }
//                    });
//                    alertDialog.show();
//                }

            }

            if (ok) {
//                 Instantiate MsoSecu in case of security mode
                IMsoSecu msoSecu = new MsoSecu();
//                 Set host and root certificate paths
//                msoSecu.setOpenSSLPath(AppContext.RootPath+"Keys/");

                // Call offeredSecuOpen if device is configured with offered security mode
//                if(secuConfig.isModeOfferedSecurity())
//                {
//                    ret = morphoDevice.offeredSecuOpen(msoSecu);
//                    if(ret != ErrorCodes.MORPHO_OK) {
//                        ok = false;
//                        showMsgError(ret);
//                    }
//                }

                // Call tunnelingOpen if device is configured with Tunneling mode
//                if(secuConfig.isModeTunneling())
//                {
//                    // Read host certificate
//                    ArrayList<Byte> hostCertificate = new ArrayList<Byte>();
//                    msoSecu.getHostCertif(hostCertificate);
//                    ret = morphoDevice.tunnelingOpen(msoSecu, MorphoTools.toByteArray(hostCertificate));
//                    if(ret != ErrorCodes.MORPHO_OK) {
//                        ok = false;
//                        showMsgError(ret);
//                    }
//                }

                if (ok) {
                    Log.e("error","Going to add security options");
                    // Set security configuration
                    ArrayList<SecurityOption> securityOptions = new ArrayList<SecurityOption>();
                    securityOptions.add(new SecurityOption(secuConfig.isDownloadIsProtected(), "Download is protected with a signature"));
                    securityOptions.add(new SecurityOption(secuConfig.isModeTunneling(), "Mode Tunneling"));
                    securityOptions.add(new SecurityOption(secuConfig.isModeOfferedSecurity(), "Mode Offered Security"));
                    securityOptions.add(new SecurityOption(secuConfig.isAcceptsOnlySignedTemplates(), "Sensor accepts only signed templates"));
                    securityOptions.add(new SecurityOption(secuConfig.isExportScore(), "Export Score"));
                    ProcessInfo.getInstance().setSecurityOptions(securityOptions);

                    // Set other info data
                    ProcessInfo.getInstance().setMSOSerialNumber(sensorName);
                    ProcessInfo.getInstance().setMSOBus(sensorBus);
                    ProcessInfo.getInstance().setMSOAddress(sensorAddress);
                    ProcessInfo.getInstance().setMSOFD(sensorFileDescriptor);
                    ProcessInfo.getInstance().setMsoDetectionMode(DeviceDetectionMode.SdkDetection);
                    String productDescriptor = morphoDevice.getProductDescriptor();
                    java.util.StringTokenizer tokenizer = new java.util.StringTokenizer(productDescriptor, "\n");
                    if (tokenizer.hasMoreTokens())
                    {
                        String l_s_current = tokenizer.nextToken();
                        if (l_s_current.contains("FINGER VP") || l_s_current.contains("FVP"))
                        {
                            MorphoInfo.m_b_fvp = true;
                        }
                    }

                    // Check Privacy Mode status
                    MorphoDevicePrivacyModeStatus[] status = new MorphoDevicePrivacyModeStatus[1];
                    ret = morphoDevice.getPrivacyModeStatus(status);
                    if (ErrorCodes.MORPHO_OK != ret) {
                        ProcessInfo.getInstance().setPrivacyModeStatus(MorphoDevicePrivacyModeStatus.PRIVACY_MODE_DISABLED);
                    } else {
                        if (MorphoDevicePrivacyModeStatus.PRIVACY_MODE_DISABLED != status[0]) {
                            // Get Privacy Key that will be used for crypting/decrypting
                            final AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                            alertDialog.setTitle(MainActivity.this.getResources().getString(R.string.TMS));
//                            alertDialog.setMessage(ConnectionActivity.this.getResources().getString(R.string.selectPrivacyKey));
                            alertDialog.setMessage("Security related error");
                            alertDialog.setCancelable(false);
                            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.ok), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    try {
                                        Log.e("error","here inside onclick intent");
                                        MainActivity.this.requestCode = IntentRequestCode.selectPrivacyKey;
                                        Intent activityIntent = new Intent(MainActivity.this, MorphoFingerprint.class);
                                        startActivityForResult(activityIntent, 0);
                                    } catch (Exception e) {

                                    }
                                }
                            });
                            alertDialog.show();

                            // Wait for click event
                            try {
                                Looper.loop();
                            } catch (RuntimeException e) {
                                if (!e.getMessage().equalsIgnoreCase("Valid")) {
                                    if (secuConfig.isModeOfferedSecurity()) {
                                        morphoDevice.offeredSecuClose();
                                    }
                                    if (secuConfig.isModeTunneling()) {
                                        morphoDevice.tunnelingClose();
                                    }
                                    morphoDevice.closeDevice();
                                    if (e.getMessage().equalsIgnoreCase("Invalid")) {
                                        showMsgError("Invalid key error");
                                    } else if (e.getMessage().equalsIgnoreCase("NotSelected")) {
                                        showMsgError("Invalid key selected error");
                                    }
                                }
                            }
                        }

                        ProcessInfo.getInstance().setPrivacyModeStatus(status[0]);
                    }
                    Log.e("error","before closing morpho device");
                    morphoDevice.closeDevice();
                    Log.e("error","goint to start intent");
                    Intent dialogActivity = new Intent(MainActivity.this, MorphoFingerprint.class);
                    startActivity(dialogActivity);
                    finish();

                    // Check if database is OK
//                    final MorphoDatabase morphoDatabase = new MorphoDatabase();
//                    ret = morphoDevice.getDatabase(0, morphoDatabase);
//                    Log.i("MORPHO_USB", "morphoDevice.getDatabase = " + ret);
//                    if (ret != ErrorCodes.MORPHO_OK) {
//                        Log.e("error","error of some kind");
//                        if (ret == ErrorCodes.MORPHOERR_BASE_NOT_FOUND)	{
//                            // No database found => create it
//                            LayoutInflater factory = LayoutInflater.from(this);
//                            final View textEntryView = factory.inflate(R.layout.base_config, null);
//                            final EditText input1 = (EditText) textEntryView.findViewById(R.id.editTextMaximumnumberofrecord);
//                            final EditText input2 = (EditText) textEntryView.findViewById(R.id.editTextNumberoffingerperrecord);
//                            input1.setText("500");
//                            input2.setText("2");
//
//                            final RadioGroup radioEncryptDatabase = (RadioGroup) textEntryView.findViewById(R.id.radioEncryptDatabase);
//
//                            final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
//                            alertDialog.setTitle(this.getResources().getString(R.string.morphosample));
//                            alertDialog.setMessage("Data Base configuration : ");
//                            alertDialog.setCancelable(false);
//                            alertDialog.setView(textEntryView);
//                            alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, this.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener()
//                            {
//                                public void onClick(DialogInterface dialog, int which)
//                                {
//                                    finish();
//                                }
//                            });
//                            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, this.getResources().getString(R.string.ok), new DialogInterface.OnClickListener()
//                            {
//                                public void onClick(DialogInterface dialog, int which)
//                                {
//                                    Integer index = new Integer(0);
//                                    MorphoField morphoFieldFirstName = new MorphoField();
//                                    morphoFieldFirstName.setName("First");
//                                    morphoFieldFirstName.setMaxSize(15);
//                                    morphoFieldFirstName.setFieldAttribute(FieldAttribute.MORPHO_PUBLIC_FIELD);
//                                    morphoDatabase.putField(morphoFieldFirstName, index);
//                                    MorphoField morphoFieldLastName = new MorphoField();
//                                    morphoFieldLastName.setName("Last");
//                                    morphoFieldLastName.setMaxSize(15);
//                                    morphoFieldLastName.setFieldAttribute(FieldAttribute.MORPHO_PUBLIC_FIELD);
//                                    morphoDatabase.putField(morphoFieldLastName, index);
//
//                                    int maxRecord = Integer.parseInt(input1.getText().toString());
//                                    int maxNbFinger = Integer.parseInt(input2.getText().toString());
//                                    boolean encryptDatabase = false;
//
//                                    if(radioEncryptDatabase.getCheckedRadioButtonId() == R.id.radioButtonencryptDatabaseYes)
//                                    {
//                                        encryptDatabase = true;
//                                    }
//
//                                    final int ret = morphoDatabase.dbCreate(maxRecord, maxNbFinger, TemplateType.MORPHO_PK_COMP,0,encryptDatabase);
//                                    if (ret == ErrorCodes.MORPHO_OK) {
//                                        ProcessInfo.getInstance().setBaseStatusOk(true);
//                                        if (secuConfig.isModeOfferedSecurity()) {
//                                            morphoDevice.offeredSecuClose();
//                                        }
//                                        if (secuConfig.isModeTunneling()) {
//                                            morphoDevice.tunnelingClose();
//                                        }
//                                        morphoDevice.closeDevice();
//                                        Intent dialogActivity = new Intent(ConnectionActivity.this, MorphoSample.class);
//                                        startActivity(dialogActivity);
//                                        finish();
//                                    } else	{
//                                        Handler mHandler = new Handler();
//                                        mHandler.post(new Runnable()
//                                        {
//                                            @Override
//                                            public synchronized void run()
//                                            {
//                                                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
//                                                alertDialog.setTitle("DataBase : dbCreate");
//                                                String msg = getString(R.string.OP_FAILED) + "\n" +  getString(R.string.MORPHOERR_BADPARAMETER);
//                                                alertDialog.setMessage(msg);
//                                                alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.ok), new DialogInterface.OnClickListener()
//                                                {
//                                                    public void onClick(DialogInterface dialog, int which)
//                                                    {
//                                                    }
//                                                });
//                                                alertDialog.show();
//                                            }
//                                        });
//                                    }
//                                }
//                            });
//
//                            alertDialog.show();
//                        }
//                    } else {
//                        // Everything is OK, close com and display main GUI
//                        if (secuConfig.isModeOfferedSecurity()) {
//                            morphoDevice.offeredSecuClose();
//                        }
//                        if (secuConfig.isModeTunneling()) {
//                            morphoDevice.tunnelingClose();
//                        }
//                        morphoDevice.closeDevice();
//
//                        Intent dialogActivity = new Intent(MainActivity.this, MorphoFingerprint.class);
//                        startActivity(dialogActivity);
//                        finish();
//                    }
                }
            }
        }
    }

    private void showMsgError(String msg) {
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(this.getResources().getString(R.string.TMS));
        alertDialog.setMessage(msg);
        alertDialog.setCancelable(false);
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, this.getResources().getString(R.string.ok), new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                finish();
            }
        });
        alertDialog.show();

        // Wait for click event
        try {
            Looper.loop();
        } catch (RuntimeException e) {

        }
    }

    private void showMsgError(int ret) {
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(this.getResources().getString(R.string.TMS));
//        MorphoTabActivity activity = new MorphoTabActivity();
//        String msg = activity.convertToInternationalMessage(ret, morphoDevice.getInternalError(), getApplicationContext(), false);
        alertDialog.setMessage("error"+ret);
        alertDialog.setCancelable(false);
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, this.getResources().getString(R.string.ok), new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                finish();
            }
        });
        alertDialog.show();

        // Wait for click event
        try {
            Looper.loop();
        } catch (RuntimeException e) {

        }
    }
    public class InitTask extends AsyncTask<String, Integer, Boolean> {
        ProgressDialog mypDialog;

        @Override
        protected Boolean doInBackground(String... params) {

            UsbFingerprint.getInstance().FingerprintSwitchUsb();

            UsbFingerprint.getInstance().UsbToFingerprint();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            USBManager.getInstance().initialize(MainActivity.this, "com.morpho.morphosample.USB_ACTION", true);

            if(USBManager.getInstance().isDevicesHasPermission() == true)
            {
                return  false;
            }
            return  true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if(!result){
                Button buttonGrantPermission = (Button) findViewById(R.id.grant_permission);
                buttonGrantPermission.setEnabled(false);
            }
            mypDialog.cancel();

        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();

            mypDialog = new ProgressDialog(MainActivity.this);
            mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mypDialog.setMessage("init...");
            mypDialog.setCanceledOnTouchOutside(false);
            mypDialog.show();
        }

    }

    private void checkReadWritePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);

            }

            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

            }
        }
    }

}