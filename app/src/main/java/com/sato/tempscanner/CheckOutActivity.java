package com.sato.tempscanner;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.sato.tempscanner.Sqlite.SqliteDBHelper;
import com.sato.tempscanner.Utilities.CommonUtils;
import com.sato.tempscanner.Utilities.SqliteDbName;

import java.util.ArrayList;
import java.util.HashMap;

public class CheckOutActivity extends AppCompatActivity {

    CommonUtils commonUtils = new CommonUtils();
    ListView userList;
    Button btnReturnChckOut,btnChckOut;
    ArrayAdapter adapter;
    String today;
    ArrayList<String> userName;
    ArrayList<String> userID;
    ArrayList<String> scanInDate;
    ArrayList userDetail = new ArrayList<>();
    CustomAdapter customAdapter = new CustomAdapter();
    String selectedName,selectedIDNo,selectedDate;
    String langSetCheckOutMessage,langSetCheckOutAlert,langSetNameTxt,langSetIDTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_out);

        userList = findViewById(R.id.userList);
        btnReturnChckOut = findViewById(R.id.btnReturnChckOut);
        btnChckOut = findViewById(R.id.btnChckOut);

        btnReturnChckOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CheckOutActivity.this, MainActivity.class));
                CheckOutActivity.this.finish();
            }
        });

        btnChckOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CheckOutActivity.this, ScanCheckOutActivity.class));
                CheckOutActivity.this.finish();
            }
        });

        userName = new ArrayList<>();
        userID = new ArrayList<>();
        scanInDate = new ArrayList<>();

       // adapter = new ArrayAdapter<String>(this,R.layout.support_simple_spinner_dropdown_item,userName);
//        AsyncForceCheckOutUser asyncForceCheckOutUser = new AsyncForceCheckOutUser();
//        asyncForceCheckOutUser.execute();

        AsyncGetCheckOutData asyncGetCheckOutData = new AsyncGetCheckOutData();
        asyncGetCheckOutData.execute();

        userListClick();
        setTextBasedOnLanguage();
    }

    private class CustomAdapter extends BaseAdapter{
        @Override
        public int getCount() {
            return userName.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View view1 = getLayoutInflater().inflate(R.layout.row_data,null);
            //getting view in row_data
            TextView txtUserNameRowData = view1.findViewById(R.id.txtUserNameRowData);
            TextView txtIdNoRowData = view1.findViewById(R.id.txtIdNoRowData);

            txtUserNameRowData.setText(userName.get(i));
            txtIdNoRowData.setText(userID.get(i));
            return view1;
        }
    }

    public void setTextBasedOnLanguage(){

        langSetCheckOutMessage = getString(R.string.checkOutMessageAlert);
        langSetCheckOutAlert = getString(R.string.checkOutAlert);
        langSetNameTxt = getString(R.string.nameLbl);
        langSetIDTxt = getString(R.string.idNoLbl);
    }

    public void userListClick(){

        userList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                selectedName = userName.get(position).trim();
                selectedIDNo = userID.get(position).trim();
                selectedDate = scanInDate.get(position).trim();
                System.out.println(scanInDate.get(position));
                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(CheckOutActivity.this);
                alertDialog.setIcon(CheckOutActivity.this.getResources().getDrawable(R.drawable.ico_warn, CheckOutActivity.this.getTheme()));
                alertDialog.setTitle( langSetCheckOutMessage);
                alertDialog.setMessage(langSetNameTxt+" "+selectedName+"\n"+ langSetIDTxt+" "+ selectedIDNo);
                alertDialog.setPositiveButton( "OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

//                        AsyncCheckOutUser asyncCheckOutUser = new AsyncCheckOutUser();
//                        asyncCheckOutUser.execute();

                        AsyncForceCOUser asyncForceCOUser = new AsyncForceCOUser();
                        asyncForceCOUser.execute();
                    }
                });
                alertDialog.create().show();
            }
        });

    }

    public void reload() {
        Intent intent = getIntent();
        overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();
        overridePendingTransition(0, 0);
        startActivity(intent);

    }

    public class AsyncGetCheckOutData extends AsyncTask<Object, String, ArrayList> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(CheckOutActivity.this);
            progressDialog.setMessage("Loading visitor to check out..");
            progressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected ArrayList doInBackground(Object... objects) {
            return getScanData(SqliteDbName.ScanDataV2);
           // return getScanData(SqliteDbName.ScanData);
        }

        @Override
        protected void onPostExecute(ArrayList arrayList) {

            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
                if (arrayList.size() > 0) {
                    for (int i = 0; i < arrayList.size(); i++) {
                        final int x = i;

                        userName.add(String.valueOf(((HashMap)arrayList.get(x)).get("Name")));
                        userID.add(String.valueOf(((HashMap)arrayList.get(x)).get("IDNo")));
                        scanInDate.add(String.valueOf(((HashMap)arrayList.get(x)).get("ScanDateIN")));

                        userList.setAdapter(customAdapter);

                    }
                }
            }
        }
    }

    public ArrayList getScanData(String strTableName) {

        ArrayList arrayList = new ArrayList();

        SqliteDBHelper sqliteDBHelper = new SqliteDBHelper(CheckOutActivity.this, getString(R.string.appdb_name), getFilesDir().getPath());
        if (sqliteDBHelper.checkTableExistence(strTableName)) {
            String sqlStatment = "select * from " + strTableName+ " where LTRIM(RTRIM([ScanDateOut])) = ''";
            arrayList = sqliteDBHelper.getTableDataFromSqlite(sqlStatment);
        } else {
            System.out.println("Table Don't Exist");
        }
        return arrayList;
    }

    public class AsyncForceCOUser extends AsyncTask<Object, String, String> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Object... objects) {
            String result = " ";
            result = forceCheckOut(SqliteDbName.ScanDataV2);
            return result;
        }

        @Override
        protected void onPostExecute(String result) {

            if(!result.equals(false) ){
                System.out.println("All user checked out");
                commonUtils.message(CheckOutActivity.this, langSetCheckOutAlert);
                Handler handler5 = new Handler();
                handler5.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(CheckOutActivity.this, MainActivity.class));
                        CheckOutActivity.this.finish();
                    }
                }, 100);
            }else{
                commonUtils.message(CheckOutActivity.this, "Error Check Out User!");
            }
        }
    }

    public String forceCheckOut(String strTableName){
        String result = "";
        today = commonUtils.getDateTime();
        SqliteDBHelper sqliteDBHelper = new SqliteDBHelper(CheckOutActivity.this, getString(R.string.appdb_name), getFilesDir().getPath());
        if (sqliteDBHelper.checkTableExistence(strTableName)) {

            boolean update =  sqliteDBHelper.forceUpdate(strTableName,selectedName,selectedIDNo,selectedDate,today);

            result = String.valueOf(update);
        } else {
            result = "Table Don't Exist";
        }

        return result;
    }



//    public HashMap checkTablePragma(String strTable) {
//        SqliteDBHelper sqliteDBHelper = new SqliteDBHelper(CheckOutActivity.this, getString(R.string.appdb_name), getFilesDir().getPath());
//        ArrayList arrayList = sqliteDBHelper.sqlStatement("pragma table_info("+strTable+")");
//        HashMap<String, String> thisHash = new HashMap<>();
//        for (int i = 0; i < arrayList.size(); i++) { thisHash.put(String.valueOf(i), String.valueOf(((HashMap)arrayList.get(i)).get("name"))); }
//
//        return thisHash;
//    }
//
//    public ContentValues scannedContVal() {
//        ContentValues contentValues = new ContentValues();
//
//        today = commonUtils.getDateTime();
//        // System.out.println(today);
//        contentValues.put("DeviceId", "Admin");
//        contentValues.put("ScanDateOut", today);
//
//        return contentValues;
//    }

//    public String forceCheckOutTime(String strTableName, ContentValues contentValues){
//        String result = "";
//
//        SqliteDBHelper sqliteDBHelper = new SqliteDBHelper(CheckOutActivity.this, getString(R.string.appdb_name), getFilesDir().getPath());
//        if (sqliteDBHelper.checkTableExistence(strTableName)) {
//
//            int update =  sqliteDBHelper.updateTableData(strTableName,contentValues,"substr(ScanDateIN,1,10) <>'"+today.substring(0,10)+"' and ScanDateOut =''",null);
//
//            result = String.valueOf(update);
//        } else {
//            result = "Table Don't Exist";
//        }
//
//        return result;
//    }
//
//    public String setCheckOutTime(String strTableName, ContentValues contentValues, String Name, String IdNo,String date){
//        String result = "";
//
//        SqliteDBHelper sqliteDBHelper = new SqliteDBHelper(CheckOutActivity.this, getString(R.string.appdb_name), getFilesDir().getPath());
//        if (sqliteDBHelper.checkTableExistence(strTableName)) {
//
//            int update =  sqliteDBHelper.updateTableData(strTableName,contentValues,"Name ='"+Name+"' and IDNo ='"+IdNo+"' and substr(ScanDateIN,1,10) ='"+date+"'",null);
//            System.out.println(update);
//            result = String.valueOf(update);
//        } else {
//            result = "Table Don't Exist";
//        }
//
//        return result;
//    }

//    public class AsyncCheckOutUser extends AsyncTask<Object, String, String> {
//        ProgressDialog progressDialog;
//
//        @Override
//        protected void onPreExecute() {
//            progressDialog = new ProgressDialog(CheckOutActivity.this);
//            progressDialog.show();
//            super.onPreExecute();
//        }
//
//        @Override
//        protected String doInBackground(Object... objects) {
//            String result = " ";
//            result = setCheckOutTime(SqliteDbName.ScanData,scannedContVal(),selectedName,selectedIDNo,selectedDate);
//            return result;
//        }
//
//        @Override
//        protected void onPostExecute(String result) {
//
//            if (progressDialog.isShowing()) {
//                progressDialog.dismiss();
//
//                if(Integer.parseInt(result) > 0){
//                    commonUtils.message(CheckOutActivity.this, langSetCheckOutAlert);
//                    Handler handler5 = new Handler();
//                    handler5.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            startActivity(new Intent(CheckOutActivity.this, MainActivity.class));
//                            CheckOutActivity.this.finish();
//                        }
//                    }, 100);
//                    //reload();
//                }else {
//                    commonUtils.message(CheckOutActivity.this, "Error check out user");
//                }
//
//
//            }
//        }
//    }
//
//    public class AsyncForceCheckOutUser extends AsyncTask<Object, String, String> {
//        ProgressDialog progressDialog;
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//        }
//
//        @Override
//        protected String doInBackground(Object... objects) {
//            String result = " ";
//            result = forceCheckOutTime(SqliteDbName.ScanData,scannedContVal());
//            return result;
//        }
//
//        @Override
//        protected void onPostExecute(String result) {
//
//            if(Integer.parseInt(result) > 0){
//                System.out.println("All user checked out");
//                //reload();
//            }
//        }
//    }
}
