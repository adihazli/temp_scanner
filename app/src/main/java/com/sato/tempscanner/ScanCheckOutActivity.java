package com.sato.tempscanner;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.idata.ise.scanner.decoder.CamDecodeAPI;
import com.idata.ise.scanner.decoder.DecodeResult;
import com.idata.ise.scanner.decoder.DecodeResultListener;
import com.sato.tempscanner.Sqlite.SqliteDBHelper;
import com.sato.tempscanner.Utilities.CommonUtils;
import com.sato.tempscanner.Utilities.SqliteDbName;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ScanCheckOutActivity extends AppCompatActivity implements DecodeResultListener {

    TextView scannedNameTxtSCO,scannedIDTxtSCO,scannedHPTxtSCO,scannedPICTxtSCO,scannedReasonxtSCO,visitorLblSCO;
    Button btnReturnChckOutSCO,btnChckOutSCO;
    CommonUtils commonUtils = new CommonUtils();
    String qrLabelDetails = "QRType!Name!Gender!IDType!IDNo!PhoneNo!CompanyID!PIC!Reason!CompanyName!Address!Country!Symptom!Question3!Question4!Venue";

    HashMap<String, String> hashScannedData;
    String today;
    String scannedName = " ",scannedUserID,scannedCountry,scannedGender , scannedPhoneNo, scannedPIC, scannedReason, scannedVenue,location,compID,deviceID;
    SharedPreferences sharedpreferences;
    String langSetCheckOutAlert,langSetQrCancelAlert;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_check_out);

        sharedpreferences = getSharedPreferences(getString(R.string.MyPref), Context.MODE_PRIVATE);
        deviceID = sharedpreferences.getString(getString(R.string.DeviceName), "");
        compID = sharedpreferences.getString(getString(R.string.CompanyID), "");
        location = sharedpreferences.getString(getString(R.string.Location), "");

        btnReturnChckOutSCO = findViewById(R.id.btnReturnChckOutSCO);
        btnChckOutSCO = findViewById(R.id.btnChckOutSCO);
        scannedNameTxtSCO = findViewById(R.id.scannedNameTxtSCO);
        scannedIDTxtSCO = findViewById(R.id.scannedIDTxtSCO);
        scannedHPTxtSCO = findViewById(R.id.scannedHPTxtSCO);
        scannedPICTxtSCO = findViewById(R.id.scannedPICTxtSCO);
        scannedReasonxtSCO = findViewById(R.id.scannedReasonxtSCO);
        visitorLblSCO = findViewById(R.id.visitorLblSCO);

        visitorLblSCO.setPaintFlags(visitorLblSCO.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        btnPressed();
        startScanning();
        setTextBasedOnLanguage();
    }
    public void setTextBasedOnLanguage() {

        langSetQrCancelAlert = getString(R.string.qrScanCancelAlert);
        langSetCheckOutAlert = getString(R.string.checkOutAlert);
    }

    public void btnPressed(){

        btnReturnChckOutSCO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ScanCheckOutActivity.this, CheckOutActivity.class));
                ScanCheckOutActivity.this.finish();
            }
        });

        btnChckOutSCO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AsyncCheckOutUser asyncCheckOutUser = new AsyncCheckOutUser();
                asyncCheckOutUser.execute();

            }
        });
    }

    private int scaning = 0;

    public void  startScanning(){
        if (0==scaning){
            scaning = 1;
            CamDecodeAPI.getInstance(ScanCheckOutActivity.this).ScanBarcode(
                    ScanCheckOutActivity.this);
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
        CamDecodeAPI.getInstance(ScanCheckOutActivity.this)
                .SetOnDecodeListener(ScanCheckOutActivity.this);

    }

    @Override
    public void onDecodeResult(DecodeResult decodeResult) {
        scaning = 0;

        if (null!=decodeResult){

            String result = new String(decodeResult.getBarcodeData());
            System.out.println(result);
            hashScannedData = doSubstringData(result,qrLabelDetails);

            if(hashScannedData.size()>0){
                scannedIDTxtSCO.setText(hashScannedData.get("IDNo"));
                scannedUserID = hashScannedData.get("IDNo");
                scannedNameTxtSCO.setText(hashScannedData.get("Name"));
                scannedName = hashScannedData.get("Name");
                scannedHPTxtSCO.setText(hashScannedData.get("PhoneNo"));
                scannedPhoneNo = hashScannedData.get("PhoneNo");
                scannedPICTxtSCO.setText(hashScannedData.get("PIC"));
                scannedPIC = hashScannedData.get("PIC");
                scannedVenue = hashScannedData.get("Venue");
                scannedCountry= hashScannedData.get("Country");
                if(Integer.parseInt(hashScannedData.get("Gender")) == 1){
                    scannedGender = "Female" ;
                }else {
                    scannedGender = "Male" ;
                }
                scannedReasonxtSCO.setText(hashScannedData.get("Reason"));
                scannedReason = hashScannedData.get("Reason");
            }else {

                Handler handler4 = new Handler();
                handler4.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(ScanCheckOutActivity.this, CheckOutActivity.class));
                        ScanCheckOutActivity.this.finish();
                    }
                }, 10);
            }



        }else {
            commonUtils.message(ScanCheckOutActivity.this, langSetQrCancelAlert);

            Handler handler4 = new Handler();
            handler4.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(ScanCheckOutActivity.this, CheckOutActivity.class));
                    ScanCheckOutActivity.this.finish();
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
            commonUtils.message(ScanCheckOutActivity.this, "QR code doesn't match with MYQRID");
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

    public class AsyncCheckOutUser extends AsyncTask<Object, String, String> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(ScanCheckOutActivity.this);
            progressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Object... objects) {
            String result = " ";
            System.out.println(scannedName +" : "+ scannedUserID);
            result = setCheckOutTime(SqliteDbName.ScanDataV2,scannedContVal(),scannedName,scannedUserID);
            //result = setCheckOutDateTime(SqliteDbName.ScanData,scannedContVal(),scannedName,scannedUserID);
            System.out.println(result);
            return result;
        }

        @Override
        protected void onPostExecute(String result) {

            if (progressDialog.isShowing()) {
                progressDialog.dismiss();

                if(Integer.parseInt(result) > 0){
                    commonUtils.message(ScanCheckOutActivity.this, langSetCheckOutAlert);
                    Handler handler5 = new Handler();
                    handler5.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startActivity(new Intent(ScanCheckOutActivity.this, MainActivity.class));
                            ScanCheckOutActivity.this.finish();
                        }
                    }, 100);
                    //reload();
                }else {
                    commonUtils.message(ScanCheckOutActivity.this, "Error check out user");
                }


            }
        }
    }

    public ContentValues scannedContVal() {
        ContentValues contentValues = new ContentValues();

        today = commonUtils.getDateTime();
        System.out.println(today);
        contentValues.put("ScanDateOut", today);

        return contentValues;
    }

    public String setCheckOutTime(String strTableName, ContentValues contentValues, String Name, String IdNo){
        String result = "";

        SqliteDBHelper sqliteDBHelper = new SqliteDBHelper(ScanCheckOutActivity.this, getString(R.string.appdb_name), getFilesDir().getPath());
        if (sqliteDBHelper.checkTableExistence(strTableName)) {
            System.out.println(Name + IdNo + today.substring(0,10) );
            int update = sqliteDBHelper.updateTableData(strTableName,contentValues,"Name ='"+Name+"' and IDNo ='"+IdNo+"' and substr(ScanDateIN,1,10) ='"+today.substring(0,10)+"' and ScanDateOut = ' '",null);

            result = String.valueOf(update);
        } else {
            result = "Table Don't Exist";
        }

        return result;
    }

    public String setCheckOutDateTime(String strTableName, String Name, String IdNo){
        String result = "";
        today = commonUtils.getDateTime();
        SqliteDBHelper sqliteDBHelper = new SqliteDBHelper(ScanCheckOutActivity.this, getString(R.string.appdb_name), getFilesDir().getPath());
        if (sqliteDBHelper.checkTableExistence(strTableName)) {

            boolean update =  sqliteDBHelper.visitorUpdate(strTableName,Name,IdNo,today);

            result = String.valueOf(update);
        } else {
            result = "Table Don't Exist";
        }

        return result;
    }
}
