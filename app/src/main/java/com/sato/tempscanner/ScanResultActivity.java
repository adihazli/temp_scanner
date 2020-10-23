package com.sato.tempscanner;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.sato.tempscanner.PrintingClass.PrintTask;
import com.sato.tempscanner.PrintingClass.PrintTaskInterface;
import com.sato.tempscanner.Utilities.CommonUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ScanResultActivity extends AppCompatActivity {

    String scannedName,scannedTemp,scannedUserID,scannedCountry,scannedGender , scannedPhoneNo, scannedReason,scannedPIC, scannedVenue,date,time,qrcode,qrlen;
    TextView visitorlbl,scannedTempTxtSR,scannedIDTxtSR,scannedNameTxtSR,scannedHPTxtSR;
    Button completeBtn,reprintBtn;
    CommonUtils commonUtils = new CommonUtils();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_result);

        scannedTempTxtSR = findViewById(R.id.scannedTempTxtSR);
        scannedNameTxtSR = findViewById(R.id.scannedNameTxtSR);
        scannedIDTxtSR = findViewById(R.id.scannedIDTxtSR);
        scannedHPTxtSR = findViewById(R.id.scannedHPTxtSR);

        completeBtn = findViewById(R.id.completeBtn);
        reprintBtn = findViewById(R.id.reprintBtn);

        visitorlbl = findViewById(R.id.visitorlbl);
        visitorlbl.setPaintFlags(visitorlbl.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        completeBtnPress();
        reprintBtnPress();
        getIntentData();
        printLabel();


    }

    public void completeBtnPress(){
        completeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ScanResultActivity.this, MainActivity.class));
                ScanResultActivity.this.finish();
            }
        });
    }

    public void reprintBtnPress(){
        reprintBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                printLabel();
            }
        });
    }

    public void getIntentData(){
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            scannedName = extras.getString("Name");
            scannedUserID = extras.getString("IDNo");
            scannedCountry = extras.getString("Country");
            scannedGender = extras.getString("Gender");
            scannedPhoneNo = extras.getString("PhoneNo");
            scannedTemp = extras.getString("Temperature");
            scannedPIC = extras.getString("PIC");
            scannedReason = extras.getString("Reason");
            scannedVenue = extras.getString("Venue");
            qrcode = extras.getString("QrCode");

            DecimalFormat df = new DecimalFormat("0000");
            qrlen = String.valueOf( df.format(qrcode.length()));

            System.out.println(qrlen +":"+qrcode.length());

            String today =  extras.getString("ScanDateIN");
            try{
                date = commonUtils.formatDate(today.substring(0,11),"yyyy/MM/dd","dd/MM/yyyy") ;
            }catch (Exception e) {
                e.printStackTrace();
            }

            time = today.substring(11,16);



        }

        scannedTempTxtSR.setText(scannedTemp+" °C");
        scannedNameTxtSR.setText(scannedName);
        scannedIDTxtSR.setText(scannedUserID);
        scannedHPTxtSR.setText(scannedPhoneNo);
    }

    public String readSbpl(){


        //String sbplLabelName = "MurataLabel.prn";
       String sbplLabelName = "murataVisitorLabelPRN.prn";



        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File prnFile = new File(dir, sbplLabelName);

        String sbpl = " ";

        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(prnFile ));
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                sbpl= sbpl +line;
            }
            return sbpl;
        } catch (IOException e) {
            System.out.println("e :  " + e);
        }

        return sbpl;
    }

    public void printLabel(){

        String sbplLabel = readSbpl();

        sbplLabel = sbplLabel.replace("@@UserName@@",scannedName);
        sbplLabel = sbplLabel.replace("@@PhoneNO@@",scannedPhoneNo);
        sbplLabel = sbplLabel.replace("@@PIC@@",scannedPIC);
        sbplLabel = sbplLabel.replace("@@Time@@",time);
        sbplLabel = sbplLabel.replace("@@Date@@",date);
        sbplLabel = sbplLabel.replace("@@Temperature@@",scannedTemp);
        sbplLabel = sbplLabel.replace("@@QRCode@@",qrcode);
        sbplLabel = sbplLabel.replace("@@QRLEN@@",qrlen);

        System.out.println(sbplLabel);

        doPrint(sbplLabel);

    }

    public void doPrint(String sbplLabel) {
            PrintTask printTask = new PrintTask(ScanResultActivity.this, new PrintTaskInterface() {
                @Override
                public void PrintResult(Map result, Boolean isSuccess) {
                    System.out.println("isSuccess : " + isSuccess);
                }
            });
            printTask.execute(printerMapping(sbplLabel));
    }

    public HashMap printerMapping(String sbplLabel) {

        String encString ="";

        try {
            byte[] encodeValue = android.util.Base64.encode(sbplLabel.getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);
            encString = new String(encodeValue);
        } catch (Exception e) {
            e.printStackTrace();
        }

        final String finalEncString = encString;

        HashMap hashMap = new HashMap();
        hashMap.put("__send_data", finalEncString.trim());
        hashMap.put("__encoding", "base64");

        return hashMap;
    }
}
