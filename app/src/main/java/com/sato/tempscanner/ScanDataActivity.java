package com.sato.tempscanner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.idata.ise.scanner.decoder.CamDecodeAPI;
import com.idata.ise.scanner.decoder.DecodeResult;
import com.idata.ise.scanner.decoder.DecodeResultListener;
import com.idatachina.imeasuresdk.IMeasureSDK;
import com.sato.tempscanner.Sqlite.SqliteDBHelper;
import com.sato.tempscanner.Utilities.CommonUtils;
import com.sato.tempscanner.Utilities.SqliteDbName;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ScanDataActivity extends AppCompatActivity implements DecodeResultListener {

    CommonUtils commonUtils = new CommonUtils();
    IMeasureSDK mIMeasureSDK;
    Button btnReturn;
    String qrLabelDetails = "QRType!Name!Gender!IDType!IDNo!PhoneNo!CompanyID!PIC!Reason!CompanyName!Address!Country!Symptom!Question3!Question4!Venue";
    HashMap<String, String> hashScannedData;
    TextView txtUserID,txtTemp, scannedIDTxt,scannedNameTxt,scannedHPTxt,scannedPICTxt;
    ConstraintLayout layout;
    String today;
    Double roundTemp = 0.00;
    String qrcode, scannedQrType,scannedCompID,scannedName,scannedGender,scannedPhoneNo,scannedIDType,scannedUserID,scannedPIC,scannedReason,scannedVenue,scannedCompanyName,scannedAddress,scannedCountry,scannedSymptom,scannedQ3,scannedQ4,
            scannedTemp = " ", ScanDateIN ,ScanDateOut, location,compID,deviceID;
    SharedPreferences sharedpreferences;
    boolean printVisitor,printEmployee;
    SoundManager manager;
    String langSetOkBtn,langSetReturnBtn,langSetidNoLbl,langSetQrCancelAlert,langSetHiTempLbl,langSetLowTempLbl,langSetHiTempAlert,langSetLowTempAlert,langSetQrScanAlert,langSetOutOFRange,langSetOutOFRangeAlert;
    Integer retryScan = 0;
    Integer ScanResult = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_data);

        btnReturn = findViewById(R.id.btnReturn);
        txtUserID = findViewById(R.id.txtIdNo);
        scannedIDTxt = findViewById(R.id.scannedIDTxt);
        scannedNameTxt = findViewById(R.id.scannedNameTxt);
        scannedHPTxt = findViewById(R.id.scannedHPTxt);
        scannedPICTxt = findViewById(R.id.scannedPICTxt);
        txtTemp = findViewById(R.id.txtTemp);
        layout = findViewById(R.id.alertLayout);

        sharedpreferences = getSharedPreferences(getString(R.string.MyPref), Context.MODE_PRIVATE);
        deviceID = sharedpreferences.getString(getString(R.string.DeviceName), "");
        location = sharedpreferences.getString(getString(R.string.Location), "");
        compID = sharedpreferences.getString(getString(R.string.CompanyID), "");

        manager = new SoundManager(this);
        manager.initSound();

        setTextBasedOnLanguage();

        btnReturnPressed();
        startScanning();

        mIMeasureSDK = new IMeasureSDK(getBaseContext());
        mIMeasureSDK.init(initCallback);

    }

    public void setTextBasedOnLanguage(){

        langSetOkBtn = getString(R.string.okBtn);
        langSetReturnBtn = getString(R.string.returnBtn);
        langSetidNoLbl= getString(R.string.idNoLbl);
        langSetQrCancelAlert= getString(R.string.qrScanCancelAlert);
        langSetHiTempLbl= getString(R.string.HiTemp);
        langSetLowTempLbl= getString(R.string.LowTemp);
        langSetHiTempAlert= getString(R.string.tempHiAlert);
        langSetLowTempAlert= getString(R.string.tempLowAlert);
        langSetQrScanAlert= getString(R.string.qrScanAlert);
        langSetOutOFRange=getString(R.string.OutOfRangeLbl);
        langSetOutOFRangeAlert=getString(R.string.OutOfRangeAlert);

       // btnReturn.setText(langSetReturnBtn);
      //  txtUserID.setText(langSetidNoLbl);

    }

    private int scaning = 0;

    public void  startScanning(){
        if (0==scaning){
            scaning = 1;
            CamDecodeAPI.getInstance(ScanDataActivity.this).ScanBarcode(
                    ScanDataActivity.this);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        CamDecodeAPI.getInstance(this).Dispose();
    }

    @Override
    protected void onResume() {
        super.onResume();
        CamDecodeAPI.getInstance(ScanDataActivity.this)
                .SetOnDecodeListener(ScanDataActivity.this);

    }

    public void btnReturnPressed(){
        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ScanDataActivity.this, MainActivity.class));
                ScanDataActivity.this.finish();
            }
        });
    }

    @Override
    public void onDecodeResult(DecodeResult decodeResult) {
        scaning = 0;

        if (null!=decodeResult){

            String result = new String(decodeResult.getBarcodeData());
            System.out.println(result);
            qrcode = result;

            hashScannedData = doSubstringData(result,qrLabelDetails);

            if(hashScannedData.size()>0){
                scannedIDTxt.setText(hashScannedData.get("IDNo"));
                scannedUserID = hashScannedData.get("IDNo");
                scannedNameTxt.setText(hashScannedData.get("Name"));
                scannedName = hashScannedData.get("Name");
                scannedHPTxt.setText(hashScannedData.get("PhoneNo"));
                scannedPhoneNo = hashScannedData.get("PhoneNo");
                scannedPICTxt.setText(hashScannedData.get("PIC"));
                scannedPIC = hashScannedData.get("PIC");
                scannedVenue = hashScannedData.get("Venue");
                scannedCountry= hashScannedData.get("Country");
                if(Integer.parseInt(hashScannedData.get("Gender")) == 1){
                    scannedGender = "Female" ;
                }else {
                    scannedGender = "Male" ;
                }
                scannedReason = hashScannedData.get("Reason");

                scannedQrType = hashScannedData.get("QRType");
                scannedCompID = hashScannedData.get("CompanyID");
                scannedIDType = hashScannedData.get("IDType");
                scannedCompanyName = hashScannedData.get("CompanyName");
                scannedAddress = hashScannedData.get("Address");
                scannedSymptom = hashScannedData.get("Symptom");
                scannedSymptom = scannedSymptom.replace(","," & ");
                scannedQ3 = hashScannedData.get("Question3");
                scannedQ4 = hashScannedData.get("Question4");

            }else {

                Handler handler4 = new Handler();
                handler4.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(ScanDataActivity.this, MainActivity.class));
                        ScanDataActivity.this.finish();
                    }
                }, 500);
            }



        }else {
            commonUtils.message(ScanDataActivity.this, langSetQrCancelAlert);

            Handler handler4 = new Handler();
            handler4.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(ScanDataActivity.this, MainActivity.class));
                    ScanDataActivity.this.finish();
                }
            }, 10);
        }
    }

    public HashMap doSubstringData(String thisQRData, String strQrFormat) {
        List<String> labelDetails = Arrays.asList(strQrFormat.split("!"));
        List<String> QRDetails = Arrays.asList(thisQRData.split("!"));
        String thisSubstring = thisQRData;
        HashMap<String, String> hashMap = new HashMap();

        if(QRDetails.size() != 16){
            commonUtils.message(ScanDataActivity.this, "QR code doesn't match with MYQRID");
        }else {
            for (int i = 0; i < labelDetails.size(); i++) {
                if (thisSubstring.contains("!")) {
                    hashMap.put(labelDetails.get(i), thisSubstring.substring(0, thisSubstring.indexOf("!")));
                    thisSubstring = thisSubstring.substring(thisSubstring.indexOf("!") + 1, thisSubstring.length());
                } else { hashMap.put(labelDetails.get(i), thisSubstring); }
            }
        System.out.println("hash result"+hashMap);
        }
        return hashMap;
    }

    public ContentValues scannedContVal() {
        ContentValues contentValues = new ContentValues();

        today = commonUtils.getDateTime();

        contentValues.put("QRType", scannedQrType);
        contentValues.put("CompanyID", scannedCompID);
        contentValues.put("Name", scannedName);
        contentValues.put("IDType", scannedIDType);
        contentValues.put("IDNo", scannedUserID);
        contentValues.put("Gender", scannedGender);
        contentValues.put("PhoneNo", scannedPhoneNo);
        contentValues.put("CompanyName", scannedCompanyName);
        contentValues.put("Address", scannedAddress);
        contentValues.put("Temperature", scannedTemp);
        contentValues.put("PIC", scannedPIC);
        contentValues.put("Reason", scannedReason);
        contentValues.put("Venue", scannedVenue);
        contentValues.put("ScanDateIN", today);
        contentValues.put("ScanDateOut", " ");
        contentValues.put("Country", scannedCountry);
        contentValues.put("Symptom", scannedSymptom);
        contentValues.put("Question3", scannedQ3);
        contentValues.put("Question4", scannedQ4);
        contentValues.put("DeviceId", deviceID);

        System.out.println("contentValues : "  + contentValues);

        return contentValues;
    }

    public boolean insertDataIntoSqlite(SqliteDBHelper sqliteDBHelper, String strTableName){
        Boolean re = sqliteDBHelper.insertTableData(strTableName, scannedContVal());
       // System.out.println("result"+ re);
        return  re;
    }

    private IMeasureSDK.InitCallback initCallback = new IMeasureSDK.InitCallback() {
        @Override
        public void success() {
            Log.d("Scanner App", "success:Power on successfully");
            //Toast.makeText(getBaseContext(), "Power on successfully", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void failed(int code, String msg) {
            Log.d("Scanner App", "failed: Power on failed,"+msg);
            //Toast.makeText(getBaseContext(), "Power on failed[" + msg + "]", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void disconnect() {
            //Toast.makeText(getBaseContext(), "Service disconnect", Toast.LENGTH_SHORT).show();
            Log.d("Scanner App", "disconnect:Service disconnect");
            mIMeasureSDK.reconect();
        }
    };

    public class AsyncSaveScanData extends AsyncTask<String, String, String> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... String) {
            String result= " ";
            SqliteDBHelper sqliteDBHelper = new SqliteDBHelper(ScanDataActivity.this, getString(R.string.appdb_name), getFilesDir().getPath());
            Boolean re = insertDataIntoSqlite(sqliteDBHelper, SqliteDbName.ScanDataV2);

            if (re){
                result = "Scanned Data Inserted";
            }else {
                result = "Scanned Data failed to be Inserted";
            }
            return result;
        }

        @Override
        protected void onPostExecute(String string) {
            System.out.println(string);
        }
    }

    //Temperature measurement status
    private volatile int readStatus = notReading;
    private final static  int reading = 1;
    private final static  int notReading = 0;
    boolean tempFirstScan = false;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if ( keyCode == 600 || keyCode == 601 || keyCode == 602) {
            if (notReading==readStatus && tempFirstScan == false){
                readStatus = reading;
                tempFirstScan = true;
                mIMeasureSDK.read(new IMeasureSDK.TemperatureCallback() {
                    @Override
                    public void success(final double temp) {
                        readStatus = notReading;
                        runOnUiThread(new Runnable() {
                            @SuppressLint("ResourceAsColor")
                            @Override
                            public void run() {
                                DecimalFormat df = new DecimalFormat("#.##");
                                roundTemp = Double.valueOf(df.format(temp));

                                scannedTemp = String.valueOf(df.format(temp)) ;

                                if(!(hashScannedData.get("IDNo")).trim().isEmpty()) {

                                    txtTemp.setText(scannedTemp+"°C");

                                    if(roundTemp > 37.49){
                                        //System.out.println("Alert!");
                                        txtTemp.setTextColor(Color.parseColor("#FFFFFF"));
                                        layout.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.red_alert));

                                        Handler handler = new Handler();
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                System.out.println("Beep");
                                                manager.playSoundAndVibrate(true, true);
                                            }
                                        }, 1000);

                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                System.out.println("Beep");
                                                manager.playSoundAndVibrate(true, true);
                                            }
                                        }, 1250);
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                System.out.println("Beep");
                                                manager.playSoundAndVibrate(true, true);
                                            }
                                        }, 1500);

                                        if(retryScan < 2){
                                            retryScan++;
                                            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(ScanDataActivity.this);
                                            alertDialog.setIcon(ScanDataActivity.this.getResources().getDrawable(R.drawable.ico_warn, ScanDataActivity.this.getTheme()));
                                            alertDialog.setTitle( langSetHiTempLbl);
                                            alertDialog.setMessage( langSetHiTempAlert);
                                            alertDialog.setPositiveButton( langSetOkBtn, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    tempFirstScan = false;
                                                }
                                            });
                                            alertDialog.create().show();
                                        }else {
                                            retryScan = 0;

                                            AsyncSaveScanData asyncSaveScanData = new AsyncSaveScanData();
                                            asyncSaveScanData.execute();

                                            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(ScanDataActivity.this);
                                            alertDialog.setIcon(ScanDataActivity.this.getResources().getDrawable(R.drawable.ico_warn, ScanDataActivity.this.getTheme()));
                                            alertDialog.setTitle( langSetHiTempLbl);
                                            alertDialog.setMessage( langSetHiTempAlert);
                                            alertDialog.setPositiveButton( langSetOkBtn, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    Handler handler5 = new Handler();
                                                    handler5.postDelayed(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            startActivity(new Intent(ScanDataActivity.this, MainActivity.class));
                                                            ScanDataActivity.this.finish();
                                                        }
                                                    }, 1000);
                                                }
                                            });
                                            alertDialog.create().show();
                                        }




                                    }
                                    else if(roundTemp < 35.0){

                                        txtTemp.setTextColor(Color.parseColor("#FFFFFF"));
                                        layout.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.cold_alert));

                                        Handler handler = new Handler();
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                System.out.println("Beep");
                                                manager.playSoundAndVibrate(true, true);
                                            }
                                        }, 1000);

                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                System.out.println("Beep");
                                                manager.playSoundAndVibrate(true, true);
                                            }
                                        }, 1250);
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                System.out.println("Beep");
                                                manager.playSoundAndVibrate(true, true);
                                            }
                                        }, 1500);

                                        if(retryScan < 2){
                                            retryScan++;
                                            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(ScanDataActivity.this);
                                            alertDialog.setIcon(ScanDataActivity.this.getResources().getDrawable(R.drawable.ico_warn, ScanDataActivity.this.getTheme()));
                                            alertDialog.setTitle( langSetLowTempLbl);
                                            alertDialog.setMessage( langSetLowTempAlert);
                                            alertDialog.setPositiveButton( langSetOkBtn, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    tempFirstScan = false;
                                                }
                                            });
                                            alertDialog.create().show();
                                        }else {
                                            retryScan = 0;

                                            AsyncSaveScanData asyncSaveScanData = new AsyncSaveScanData();
                                            asyncSaveScanData.execute();

                                            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(ScanDataActivity.this);
                                            alertDialog.setIcon(ScanDataActivity.this.getResources().getDrawable(R.drawable.ico_warn, ScanDataActivity.this.getTheme()));
                                            alertDialog.setTitle( langSetLowTempLbl);
                                            alertDialog.setMessage( langSetLowTempAlert);
                                            alertDialog.setPositiveButton( langSetOkBtn, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    Handler handler5 = new Handler();
                                                    handler5.postDelayed(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            startActivity(new Intent(ScanDataActivity.this, MainActivity.class));
                                                            ScanDataActivity.this.finish();
                                                        }
                                                    }, 3000);
                                                }
                                            });
                                            alertDialog.create().show();

                                        }



                                    }
                                    else {
                                            retryScan = 0;

                                            txtTemp.setTextColor(Color.parseColor("#000000"));
                                            layout.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.green));

                                            AsyncSaveScanData asyncSaveScanData = new AsyncSaveScanData();
                                            asyncSaveScanData.execute();

                                            Handler handler5 = new Handler();
                                            handler5.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {

                                                    Intent i = new Intent(ScanDataActivity.this, ScanResultActivity.class);
                                                    i.putExtra("Name", scannedName);
                                                    i.putExtra("IDNo", scannedUserID);
                                                    i.putExtra("Country", scannedCountry);
                                                    i.putExtra("Gender", scannedGender);
                                                    i.putExtra("PhoneNo", scannedPhoneNo);
                                                    i.putExtra("Temperature", scannedTemp);
                                                    i.putExtra("ScanDateIN", today);
                                                    i.putExtra("DeviceId", deviceID);
                                                    i.putExtra("Reason", scannedReason);
                                                    i.putExtra("PIC", scannedPIC);
                                                    i.putExtra("Venue", scannedVenue);
                                                    i.putExtra("QrCode", qrcode);
                                                    ScanResult = 0;
                                                    startActivity(i);
                                                    ScanDataActivity.this.finish();
                                                }
                                            }, 100);
                                        }
                                        retryScan = 0;

                                }else {
                                    commonUtils.message(ScanDataActivity.this,langSetQrScanAlert);
                                }

                            }
                        });
                    }

                    @Override
                    public void failed(int code, final  String msg) {
                        readStatus = notReading;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(ScanDataActivity.this);
                                alertDialog.setIcon(ScanDataActivity.this.getResources().getDrawable(R.drawable.ico_warn, ScanDataActivity.this.getTheme()));
                                alertDialog.setTitle(langSetOutOFRange);
                                alertDialog.setMessage(langSetOutOFRangeAlert);
                                alertDialog.setPositiveButton(langSetOkBtn, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        tempFirstScan = false;
                                    }
                                });
                                alertDialog.create().show();
                                //Toast.makeText(getBaseContext(), "Failed[" + msg + "]", Toast.LENGTH_SHORT).show(); //Temperature measurement failed
                            }
                        });

                    }
                });
            }else{
                Toast.makeText(this,"Reading,please wait...",Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mIMeasureSDK.close();
    }
}
