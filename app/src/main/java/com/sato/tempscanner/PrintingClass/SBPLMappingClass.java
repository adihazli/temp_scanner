package com.sato.tempscanner.PrintingClass;

import android.os.Environment;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class SBPLMappingClass {

    public static ArrayList thisSbplMapping(String itemCode, String percentOff, String normalPrice, String currentPrice, String printQuantity) {
        ArrayList printData = new ArrayList();
        HashMap thisHashMap = new HashMap();

        thisHashMap.put("itemCode", itemCode);
        thisHashMap.put("percentOff", percentOff);
        thisHashMap.put("normalPrice", normalPrice);
        thisHashMap.put("currentPrice", currentPrice);
        thisHashMap.put("printQuantity", printQuantity);

        ArrayList listOfSBPL = getSBPLPrint();
        for (int i = 0; i < listOfSBPL.size(); i++) {
            if (String.valueOf(listOfSBPL.get(i)).contains("^")) {
                int firstChar = String.valueOf(listOfSBPL.get(i)).indexOf("^");
                int secondChar = String.valueOf(listOfSBPL.get(i)).indexOf("^", firstChar + 1);

                if (thisHashMap.keySet().size() > 1) {
                    Set<String> keys = thisHashMap.keySet();
                    for (String strKeys : keys) {
                        if (String.valueOf(listOfSBPL.get(i)).substring(firstChar, secondChar + 1).contains(strKeys)) {
                            printData.add(sbplData(replaceCharAt(String.valueOf(listOfSBPL.get(i)), firstChar, secondChar, thisHashMap.get(strKeys).toString())));
                        }
                    }
                }
            } else {
                printData.add(sbplData(String.valueOf(listOfSBPL.get(i))));
            }
        }

        return printData;
    }

    public static String sbplData(String strData) {
        return "\u001B"+strData;
    }

    public static String simpleSbplPrint(ArrayList listData) {
        String strData = "";
        if (listData != null) {
            strData = "\u001BA";
            for (int i = 0;i < listData.size();i++) {
                strData = strData+listData.get(i).toString();
            }
            return strData+"\u001BZ";
        }
        return "";
    }

    public static ArrayList getSBPLPrint() {

        String strPath = "";
        String sbplLabelName ="";

        String path = Environment.getExternalStorageDirectory() + "/"+ strPath+"/";
        StringBuffer stringBuffer = null;
        ArrayList listOfSBPL = new ArrayList();

        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(path + sbplLabelName +".txt"));
            String line = "";
            while ((line = bufferedReader.readLine()) != null) { listOfSBPL.add(line); }
            return listOfSBPL;
        } catch (IOException e) { System.out.println("e :  " + e); }

        return listOfSBPL;
    }

    public static String replaceCharAt(String s, int firstPos, int secondPos, String c) {
        return s.substring(0, firstPos) + c + s.substring(secondPos + 1);
    }

}
