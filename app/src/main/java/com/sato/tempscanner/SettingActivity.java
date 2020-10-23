package com.sato.tempscanner;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.sato.tempscanner.Utilities.CommonUtils;

import java.util.Objects;

public class SettingActivity extends AppCompatActivity {

    SharedPreferences sharedpreferences;
    EditText etDevID,etComID,etEmail,etPass,etCompName,etLocation;
    Button buttonSave,buttonTestConnection;
    String deviceID,companyID,companyName,emailAddress,password,location;
    TextView txtDeviceID,txtComID,txtComName,txtLocation;
    Boolean autoPrintVisitor,autoPrintEmployee,connectWebService;
    Switch swWebServ,swPrintEmp,swPrintVisitor;
    CommonUtils commonUtils = new CommonUtils();
    String langSetDevIDLbl,langSetComIDLbl,langSetComNameLbl,langSetLocLbl,langSetSaveBtn,langSetSaveMsgAlert,langSetDevIDEt,langSetComIDEt,langSetComNameEt,langSetLocEt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Device Setting");
        sharedpreferences = getSharedPreferences(getString(R.string.MyPref),Context.MODE_PRIVATE);

        etDevID = findViewById(R.id.etDevID);
        etComID = findViewById(R.id.etComID);
        etCompName = findViewById(R.id.etCompName);
        etLocation = findViewById(R.id.etLocation);
        txtDeviceID = findViewById(R.id.txtDeviceID);
        txtComID = findViewById(R.id.txtComID);
        txtComName = findViewById(R.id.txtComName);
        txtLocation = findViewById(R.id.txtLocation);
        buttonSave = findViewById(R.id.btnSaveSetting);

        setSetting();
        saveSetting();
        setTextBasedOnLanguage();

    }

    public void setTextBasedOnLanguage(){

        langSetDevIDLbl = getString(R.string.deviceIdlbl);
        langSetComIDLbl = getString(R.string.comIdlbl);
        langSetComNameLbl = getString(R.string.comNamelbl);
        langSetLocLbl= getString(R.string.locationlbl);
        langSetSaveBtn= getString(R.string.saveBtn);
        langSetSaveMsgAlert= getString(R.string.saveSettingAlert);
        langSetDevIDEt = getString(R.string.deviceIdEt);
        langSetComIDEt = getString(R.string.comIdEt);
        langSetComNameEt = getString(R.string.comNameEt);
        langSetLocEt= getString(R.string.locationEt);

//        buttonSave.setText(langSetSaveBtn);
//        etDevID.setHint(langSetDevIDEt);
//        etComID.setHint(langSetComIDEt);
//        etCompName.setHint(langSetComNameEt);
//        etLocation.setHint(langSetLocEt);
//        txtDeviceID.setText(langSetDevIDLbl);
//        txtComID.setText(langSetComIDLbl);
//        txtComName.setText(langSetComNameLbl);
//        txtLocation.setText(langSetLocLbl);

    }


    public void setSetting(){

        deviceID = sharedpreferences.getString(getString(R.string.DeviceName), "");
        companyID = sharedpreferences.getString(getString(R.string.CompanyID), "");
        companyName = sharedpreferences.getString(getString(R.string.CompanyName), "");
        location = sharedpreferences.getString(getString(R.string.Location), "");

        etDevID.setText(deviceID);
        etComID.setText(companyID);
        etCompName.setText(companyName);
        etLocation.setText(location);

    }

    public void saveSetting(){
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                deviceID = etDevID.getText().toString().trim();
                companyID = etComID.getText().toString().trim();
                companyName = etCompName.getText().toString().trim();
                location = etLocation.getText().toString().trim();

                SharedPreferences.Editor editor = sharedpreferences.edit();

                editor.putString(getString(R.string.DeviceName), deviceID);
                editor.putString(getString(R.string.CompanyID), companyID);
                editor.putString(getString(R.string.CompanyName), companyName);
                editor.putString(getString(R.string.Location),location);

                editor.commit();

                commonUtils.message(SettingActivity.this,langSetSaveMsgAlert);
            }
        });
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        startActivity(new Intent(SettingActivity.this, MainActivity.class));
        finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if ( v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent( event );
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_BACK))
        {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

}
