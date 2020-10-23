package com.sato.tempscanner.PrintingClass;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import java.util.HashMap;
import java.util.Map;

public class PrintTask extends AsyncTask<HashMap<String, String>, Void, Map<String,String>> {
    Context context;
    String printerUrl;
    public Map<String, String> resultMap = null;
    PrintTaskInterface printTaskInterface;
    public static HashMap<String, String> printMap = null;
    ProgressDialog progressDialog;

    public PrintTask(Context context, PrintTaskInterface printTaskInterface) {
        this.context = context;
        this.printTaskInterface = printTaskInterface;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Map<String, String> doInBackground(HashMap... mapData)  {
        Map<String,String> resultMap = null;
        try {
            resultMap = PrintHttpClient.get("http://localhost:8080/Printer/SendRawData", mapData[0]);
            return resultMap;
        } catch (PrintHttpClient.HttpClientException e) {
            e.printStackTrace();
            System.out.println("This e Error : " + e);
        }
        return resultMap;
    }

    @Override
    protected void onPostExecute(Map<String, String> objResult) {
        super.onPostExecute(objResult);
        Map thisResult = null;
        boolean isSuccess = false;

        try {
            if (objResult.containsKey("result") && objResult.get("result").equals("NG")) {
                System.out.println("Error Message : " + objResult.get("message"));
            } else if ((objResult.containsKey("result") && objResult.get("result").equals("OK"))&&(objResult.containsKey("function") && objResult.get("function").equals("/Printer/Port"))) {
                thisResult = objResult;
                isSuccess = true;
            } else if ((objResult.containsKey("result") && objResult.get("result").equals("OK"))&&(objResult.containsKey("function") && objResult.get("function").equals("/Printer/SetPort"))) {
                System.out.println("Data Set Successfully");
            } else {
                System.out.println("Data sent Successfully");
                isSuccess = true;
            }

        } catch(Exception e) {
            System.out.println("e : " + e);
        }

        if (printTaskInterface != null)  printTaskInterface.PrintResult(thisResult, isSuccess);
    }
}
