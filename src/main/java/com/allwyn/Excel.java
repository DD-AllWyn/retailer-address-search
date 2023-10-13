package com.allwyn;

import com.allwyn.framework.SerenityScenario;
import com.codoid.products.exception.FilloException;
import com.codoid.products.fillo.Connection;
import com.codoid.products.fillo.Fillo;
import com.codoid.products.fillo.Recordset;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import org.apache.commons.text.similarity.FuzzyScore;

import java.util.Arrays;
import java.util.stream.Collectors;

public class Excel {
    public static Recordset getDataFromExcelSheet(String prmWorkBookName, String prmSheetName) {
        try {
            Fillo fillo = new Fillo();
            Connection excelConnection = fillo.getConnection(prmWorkBookName);
            String strQuery = "select * from " + prmSheetName;
            Recordset recordset = excelConnection.executeQuery(strQuery);
            return recordset;
        } catch (Exception filoException) {
            filoException.printStackTrace();
        }
        return null;
    }

    public static Recordset getFilteredDataFromExcelSheet(String prmWorkBookName, String prmSheetName, String prmFilterColumn, String prmFilterValue) {
        try {
            Fillo fillo = new Fillo();
            Connection excelConnection = fillo.getConnection(prmWorkBookName);
            String strQuery = "select * from " + prmSheetName + " where " + prmFilterColumn + "= '" + prmFilterValue + "'";
            System.out.println(strQuery);
            Recordset recordset = excelConnection.executeQuery(strQuery);
            System.out.println("NUMBER OF RECORDS RETURNED " + recordset.getCount());
            return recordset;
        } catch (Exception filoException) {
            filoException.printStackTrace();
        }
        return null;
    }

    public static int getRowCountBySheetName(String prmWorkBookName, String prmSheetName) {

        try {
            Fillo fillo = new Fillo();
            Connection excelConnection = fillo.getConnection(prmWorkBookName);
            String strQuery = "select * from " + prmSheetName;
            Recordset recordset = excelConnection.executeQuery(strQuery);

            return recordset.getCount();
        } catch (Exception filoException) {
            filoException.printStackTrace();
        }
        return 0;
    }
}
