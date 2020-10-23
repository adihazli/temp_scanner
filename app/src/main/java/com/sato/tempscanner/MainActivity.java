package com.sato.tempscanner;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.sato.tempscanner.Sqlite.SqliteDBHelper;
import com.sato.tempscanner.Utilities.CommonUtils;
import com.sato.tempscanner.Utilities.SqliteDbName;

import java.util.Date;

public class MainActivity extends AppCompatActivity {
    Button buttonScan,btnCheckOut,buttonExport;
    SharedPreferences sharedpreferences;
    Integer countPress;
    ImageView ivLogo;
    String langSetCheckInBtn,langSetExporBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buttonScan = findViewById(R.id.btnScan);
        buttonExport = findViewById(R.id.btnUpload);
        btnCheckOut = findViewById(R.id.btnCheckOut);
        ivLogo = findViewById(R.id.ivLogo);
        countPress = 0;


        sharedpreferences = getSharedPreferences(getString(R.string.MyPref), Context.MODE_PRIVATE);


        buttonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ScanDataActivity.class));
            }
        });

        btnCheckOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, CheckOutActivity.class));
            }
        });

        buttonExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ExportDataActivity.class));
            }
        });


        //setTextBasedOnLanguage();
        doSqliteDB();
        checkDeviceID();
        enterSettingPage();

    }

    public void setTextBasedOnLanguage(){

        langSetCheckInBtn = getString(R.string.checkInBtn);
        langSetExporBtn = getString(R.string.exportDataBtn);

        buttonScan.setText(langSetCheckInBtn);
        buttonExport.setText(langSetExporBtn);

    }

    public void enterSettingPage(){
        ivLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                countPress = countPress+ 1;
                System.out.println(countPress);
                if(countPress>6){
                    startActivity(new Intent(MainActivity.this, SettingActivity.class));
                    countPress = 0;
                }
            }
        });

    }

    public void checkDeviceID(){
        String deviceID = sharedpreferences.getString(getString(R.string.DeviceName), "");
        String compID = sharedpreferences.getString(getString(R.string.CompanyID)," ");
        if(deviceID.trim().isEmpty() || compID.trim().isEmpty()){
            startActivity(new Intent(MainActivity.this, SettingActivity.class));
        }

    }

    public void sqliteDeleteData(String strTable) {
        SqliteDBHelper sqliteDBHelper = new SqliteDBHelper(MainActivity.this,getString(R.string.appdb_name), getFilesDir().getPath());
        sqliteDBHelper.sqlStatement("delete from " + strTable);
    }

    public void doSqliteDB() {
        System.out.println("Check DB!");
        SqliteDBHelper sqliteDBHelper = new SqliteDBHelper(MainActivity.this, getString(R.string.appdb_name), getFilesDir().getPath());

        if (!sqliteDBHelper.checkTableExistence(SqliteDbName.ScanData)) {
            sqliteDBHelper.createDatabaseTable(SqliteDbName.ScanData,
                    " Name text, IDNo text, Country text, Gender Text, PhoneNo text, Temperature text, ScanDateIN text,ScanDateOut text, DeviceId text, PIC text, Reason text, Venue text");
            System.out.println("create " + SqliteDbName.ScanData);
        }

        if (!sqliteDBHelper.checkTableExistence(SqliteDbName.TempScanData)) {
            sqliteDBHelper.createDatabaseTable(SqliteDbName.TempScanData,
                    " Name text, IDNo text, Country text, Gender Text, PhoneNo text, Temperature text, ScanDateIN text,ScanDateOut text, DeviceId text, PIC text, Reason text, Venue text");
            System.out.println("create " + SqliteDbName.TempScanData);
        }

        if (!sqliteDBHelper.checkTableExistence(SqliteDbName.ScanDataV2)) {
            sqliteDBHelper.createDatabaseTable(SqliteDbName.ScanDataV2,
                    " QRType text, CompanyID text, Name text,Gender text,PhoneNo text,IDType text,IDNo text,Temperature text,ScanDateIN text,ScanDateOut text,PIC text,Reason text, Venue text, CompanyName text,Address text,Country text,Symptom text,Question3 text,Question4 text, DeviceId text ");
            System.out.println("create " + SqliteDbName.ScanDataV2);
        }else {
            System.out.println("Da ade");
        }

        if (!sqliteDBHelper.checkTableExistence(SqliteDbName.TempScanDataV2)) {
            sqliteDBHelper.createDatabaseTable(SqliteDbName.TempScanDataV2,
                    " QRType text, CompanyID text, Name text,Gender text,PhoneNo text,IDType text,IDNo text,Temperature text,ScanDateIN text,ScanDateOut text,PIC text,Reason text, Venue text, CompanyName text,Address text,Country text,Symptom text,Question3 text,Question4 text, DeviceId text ");
            System.out.println("create " + SqliteDbName.TempScanDataV2);
        }

//        if (!sqliteDBHelper.checkTableExistence(SqliteDbName.CheckInData)) {
//            sqliteDBHelper.createDatabaseTable(SqliteDbName.CheckInData,
//                    "ScanDateIN text, IDNo text,Temperature text,CompanyID text, Location text");
//            System.out.println("create " + SqliteDbName.CheckInData);
//        }

//        if (!sqliteDBHelper.checkTableExistence(SqliteDbName.TempCheckInData)) {
//            sqliteDBHelper.createDatabaseTable(SqliteDbName.TempCheckInData,
//                    "ScanDateIN text, IDNo text,Temperature text,CompanyID text, Location text");
//            System.out.println("create " + SqliteDbName.TempCheckInData);
//        }

    }
}
