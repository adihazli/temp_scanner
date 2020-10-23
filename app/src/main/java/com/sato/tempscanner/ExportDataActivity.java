package com.sato.tempscanner;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.sato.tempscanner.Sqlite.SqliteDBHelper;
import com.sato.tempscanner.Utilities.CommonUtils;
import com.sato.tempscanner.Utilities.SqliteDbName;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

public class ExportDataActivity extends AppCompatActivity {

    CommonUtils commonUtils = new CommonUtils();
    SharedPreferences sharedpreferences;
    ArrayList<ArrayList> csvData = new ArrayList<>();
    HashMap<String, String> tableElement;
    String deviceID,location;
    TextView txtStart,txtEnd;
    DatePickerDialog picker;
    EditText etEndDate, etStartDate;
    String fileDateLoc, fileName, start, end;
    Button btnExport;
    String langSetOkBtn,langSetStartDate,langSetEndDate,langSetExportCSVBtn,langSetExportLbl,langSetExportMsgAlert;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export_data);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Export Data");

        btnExport = findViewById(R.id.btnExport);
        txtEnd = findViewById(R.id.txtEnd);
        txtStart = findViewById(R.id.txtStart);
        etEndDate = findViewById(R.id.etEndDate);
        etStartDate = findViewById(R.id.etStartDate);

        sharedpreferences = getSharedPreferences(getString(R.string.MyPref), Context.MODE_PRIVATE);
        deviceID = sharedpreferences.getString(getString(R.string.DeviceName), "");
        location = sharedpreferences.getString(getString(R.string.Location), "");

        setInitialDate();
        getEndDate();
        getStartDate();
        setFileName();
        exportFile();
        setTextBasedOnLanguage();

        tableElement = checkTablePragma(SqliteDbName.ScanDataV2);

    }

    public void setTextBasedOnLanguage(){

        langSetOkBtn = getString(R.string.okBtn);
        langSetStartDate = getString(R.string.startDate);
        langSetEndDate = getString(R.string.endDate);
        langSetExportCSVBtn= getString(R.string.exportCsvBtn);
        langSetExportLbl= getString(R.string.exportScannedData);
        langSetExportMsgAlert= getString(R.string.exportMsgAlert);

//        btnExport.setText(langSetExportCSVBtn);
//        txtEnd.setText(langSetEndDate);
//        txtStart.setText(langSetStartDate);

    }

    public void setInitialDate(){

        String today = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(new Date());
        String startDate = getCalculatedDate( "yyyy/MM/dd", -40);

        etStartDate.setText(startDate);
        etEndDate.setText(today);

        end = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(new Date());
        start = getCalculatedDate( "yyyy/MM/dd", -40);

    }

    public static String getCalculatedDate( String dateFormat, int days) {

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat s = new SimpleDateFormat(dateFormat);
        cal.add(Calendar.DAY_OF_YEAR, days);

        return s.format(new Date(cal.getTimeInMillis()));
    }

    public void getEndDate() {

        etEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Calendar cldr = Calendar.getInstance();
                int day = cldr.get(Calendar.DAY_OF_MONTH);
                int month = cldr.get(Calendar.MONTH);
                int year = cldr.get(Calendar.YEAR);
                // date picker dialog
                picker = new DatePickerDialog(ExportDataActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                                String setMonth = " ";
                                String setDate = " ";
                                if (dayOfMonth < 10) {
                                    setDate = "0" + dayOfMonth;
                                } else {
                                    setDate = String.valueOf(dayOfMonth);
                                }

                                if (monthOfYear < 9) {
                                    setMonth = "0" + (monthOfYear + 1);
                                } else {
                                    setMonth = String.valueOf(monthOfYear + 1);
                                }

                                end = year + "/" + setMonth + "/" + setDate ;

                                etEndDate.setText(end);

                            }
                        }, year, month, day);
                picker.show();
            }
        });
    }

    public void getStartDate() {

        etStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Calendar cldr = Calendar.getInstance();
                int day = cldr.get(Calendar.DAY_OF_MONTH);
                int month = cldr.get(Calendar.MONTH);
                int year = cldr.get(Calendar.YEAR);
                // date picker dialog
                picker = new DatePickerDialog(ExportDataActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                                String setMonth = " ";
                                String setDate = " ";
                                if (dayOfMonth < 10) {
                                    setDate = "0" + dayOfMonth;
                                } else {
                                    setDate = String.valueOf(dayOfMonth);
                                }

                                if (monthOfYear < 9) {
                                    setMonth = "0" + (monthOfYear + 1);
                                } else {
                                    setMonth = String.valueOf(monthOfYear + 1);
                                }
                                start = year + "/" + setMonth + "/" + setDate ;

                                etStartDate.setText(start);
                            }
                        }, year, month, day);
                picker.show();
            }
        });
    }

    public void setFileName() {

        Date date = new Date();
        String day = (String) DateFormat.format("dd", date);
        String month = (String) DateFormat.format("MM", date);
        String year = (String) DateFormat.format("yyyy", date);
        String hour = (String) DateFormat.format("hh", date);
        String min = (String) DateFormat.format("mm", date);
        String ss = (String) DateFormat.format("ss", date);
        String ampm = (String) DateFormat.format("aa", date);

        fileDateLoc = year + month + day +"_"+hour+"."+min+"."+ss+ampm+".csv";

        sqliteDeleteData(SqliteDbName.TempScanDataV2);
    }

    public void exportFile() {
        btnExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String start = etStartDate.getText().toString().trim();
                String end = etEndDate.getText().toString().trim();

                if (start.isEmpty() || end.isEmpty()) {
                    commonUtils.message(ExportDataActivity.this, "Please select date.");
                } else {

                    if(start.length() == 10 &&  end.length() ==10){
                        AsyncGetUserData getUserData = new AsyncGetUserData();
                        getUserData.execute();
                    }else  {
                        commonUtils.message(ExportDataActivity.this, "Please select start & end date.");
                    }

                }
            }
        });
    }

    public class AsyncGetUserData extends AsyncTask<Object, String, ArrayList> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(ExportDataActivity.this);
            progressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected ArrayList doInBackground(Object... objects) {
            System.out.println(start);
            System.out.println(end);
            return getScanData(SqliteDbName.ScanDataV2, location,start, end);
        }

        @Override
        protected void onPostExecute(ArrayList arrayList) {

            if (progressDialog.isShowing()) {
                progressDialog.dismiss();

                csvData.clear();
                if (arrayList.size() > 0) {
                    for (int i = 0; i < arrayList.size(); i++) {
                        csvData.add(populateData((HashMap) arrayList.get(i), tableElement));
                    }
                    System.out.println(csvData);
                }

                populateTempDB(SqliteDbName.TempScanDataV2, csvData);

                AsyncExportCSV asyncExportCSV = new AsyncExportCSV(SqliteDbName.TempScanDataV2,fileDateLoc);
                asyncExportCSV.execute();

            }
        }
    }

    public class AsyncExportCSV extends AsyncTask<String, String, String> {
        String file;
        String tempDbName;

        public AsyncExportCSV(String tempDbName,String file){
            this.tempDbName =tempDbName;
            this.file = file;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... String) {

            String result= fileDateLoc;
            SqliteDBHelper sqliteDBHelper = new SqliteDBHelper(ExportDataActivity.this, getString(R.string.appdb_name), getFilesDir().getPath());
            sqliteDBHelper.exportToCSV(tempDbName,fileDateLoc);

            return result;
        }

        @Override
        protected void onPostExecute(String string) {
            File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File saveFile = new File(dir, string);
            MediaScannerConnection.scanFile(ExportDataActivity.this, new String[] { dir.getAbsolutePath()}, null, null);
            scanFile(saveFile.getAbsolutePath());

        }
    }

    private void scanFile(String path) {
        MediaScannerConnection.scanFile(ExportDataActivity.this,
                new String[] { path }, null,
                new MediaScannerConnection.OnScanCompletedListener() {

                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("TAG", "Finished scanning " + path);
                    }
                });
    }

    public void populateTempDB(String strTableName, ArrayList arrayList) {
        SqliteDBHelper sqliteDBHelper = new SqliteDBHelper(ExportDataActivity.this, getString(R.string.appdb_name), getFilesDir().getPath());

        if (sqliteDBHelper.checkTableExistence(strTableName)) {

            insertDataIntoSqlite(sqliteDBHelper, strTableName, arrayList);

            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(ExportDataActivity.this);
            alertDialog.setIcon(ExportDataActivity.this.getResources().getDrawable(R.drawable.icon_info, ExportDataActivity.this.getTheme()));
            alertDialog.setTitle(langSetExportLbl);
            alertDialog.setMessage(langSetExportMsgAlert);
            alertDialog.setPositiveButton( langSetOkBtn, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    reload();
                }
            });
            alertDialog.create().show();

        } else {
            System.out.println("Please create table for temp table");
        }
    }

    public void insertDataIntoSqlite(SqliteDBHelper sqliteDBHelper, String strTableName, ArrayList arrayList){
        if (arrayList.size() > 0) {
            for (int i = 0; i < arrayList.size(); i++) {
               sqliteDBHelper.insertTableData(strTableName, setContVal((ArrayList) arrayList.get(i)));
            }
        }
    }

    public ContentValues setContVal(ArrayList thisList) {
        ContentValues contentValues = new ContentValues();
        for (int i = 0 ; i < thisList.size(); i++) {
            Set<String> keySet = ((HashMap)thisList.get(i)).keySet();
            for (String strKey: keySet) {
                contentValues.put(strKey, String.valueOf(((HashMap) thisList.get(i)).get(strKey)));
            }
        }
        return contentValues;
    }

    public ArrayList populateData(HashMap hashMapPO, HashMap hashMapReceiving) {
        ArrayList thisArray = new ArrayList();
        HashMap<String, String> thisData = new HashMap<>();

        for (int i = 0 ;i < hashMapReceiving.size(); i++) {
            hashMapPO.get(String.valueOf(hashMapReceiving.get(String.valueOf(i))));
            thisData.put(String.valueOf(hashMapReceiving.get(String.valueOf(i))), String.valueOf(hashMapPO.get(String.valueOf(hashMapReceiving.get(String.valueOf(i))))));
        }
        thisArray.add(thisData);
        return thisArray;
    }

    public void sqliteDeleteData(String strTable) {
        SqliteDBHelper sqliteDBHelper = new SqliteDBHelper(ExportDataActivity.this,getString(R.string.appdb_name), getFilesDir().getPath());
        sqliteDBHelper.sqlStatement("delete from " + strTable);
    }

    public ArrayList getScanData(String strTableName,String location, String start, String end) {

        ArrayList arrayList = new ArrayList();

        SqliteDBHelper sqliteDBHelper = new SqliteDBHelper(ExportDataActivity.this, getString(R.string.appdb_name), getFilesDir().getPath());
        if (sqliteDBHelper.checkTableExistence(strTableName)) {
            //String sqlStatment = "select * from " + strTableName+ " where Location = '"+location+"'and ScanDateIN between '"+start+" 00:00:00' and '"+end+" 23:59:59'";
            String sqlStatment = "select * from " + strTableName+ " where ScanDateIN between '"+start+" 00:00:00' and '"+end+" 23:59:59'";
            System.out.println(sqlStatment);
            arrayList = sqliteDBHelper.getTableDataFromSqlite(sqlStatment);
        } else {
            System.out.println("Table Don't Exist");
        }
        return arrayList;
    }

    public HashMap checkTablePragma(String strTable) {
        SqliteDBHelper sqliteDBHelper = new SqliteDBHelper(ExportDataActivity.this, getString(R.string.appdb_name), getFilesDir().getPath());
        ArrayList arrayList = sqliteDBHelper.sqlStatement("pragma table_info("+strTable+")");
        HashMap<String, String> thisHash = new HashMap<>();
        for (int i = 0; i < arrayList.size(); i++) { thisHash.put(String.valueOf(i), String.valueOf(((HashMap)arrayList.get(i)).get("name"))); }

        return thisHash;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        startActivity(new Intent(ExportDataActivity.this, MainActivity.class));
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

    public void reload() {
        Intent intent = getIntent();
        overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();
        overridePendingTransition(0, 0);
        startActivity(intent);

    }

}
