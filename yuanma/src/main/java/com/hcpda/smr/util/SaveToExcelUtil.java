package com.hcpda.smr.util;

import android.app.Activity;
import android.os.Environment;

import java.io.File;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

/**
 * Created by Administrator on 2017-9-7.
 */

public class SaveToExcelUtil {

    private WritableWorkbook wwb;
    private File excelFile;
    public static String TAG="FileUtils";
    public final static String PATH = Environment
            .getExternalStorageDirectory()
            + File.separator
            + "SMR"
            + File.separator;
    static {
        File filePath=new File(PATH);
        if(!filePath.exists()){
            filePath.mkdirs();
        }
    }





    public SaveToExcelUtil(String excelPath) {
        excelFile = new File(PATH+excelPath);
        createExcel(excelFile);
    }

    // 创建excel表.
    public void createExcel(File file) {
        WritableSheet ws = null;
        try {
            if (!file.exists()) {
                wwb = Workbook.createWorkbook(file);

                ws = wwb.createSheet("sheet1", 0);

                // 在指定单元格插入数据
                Label lbl1 = new Label(0, 0, "一维条码");
                Label lbl2 = new Label(1, 0, "UTag标签码");

                ws.addCell(lbl1);
                ws.addCell(lbl2);

                // 从内存中写入文件中
                wwb.write();
                wwb.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //将数据存入到Excel表中
    public void writeToExcel(Object... args) {

        try {
            Workbook oldWwb = Workbook.getWorkbook(excelFile);
            wwb = Workbook.createWorkbook(excelFile, oldWwb);
            WritableSheet ws = wwb.getSheet(0);
            // 当前行数
            int row = ws.getRows();
            Label lab1 = new Label(0, row, args[0] + "");
            Label lab2 = new Label(1, row, args[1] + "");
            ws.addCell(lab1);
            ws.addCell(lab2);


            // 从内存中写入文件中,只能刷一次.
            wwb.write();
            wwb.close();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }




}
