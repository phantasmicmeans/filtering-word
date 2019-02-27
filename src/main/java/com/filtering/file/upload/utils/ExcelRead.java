package com.filtering.file.upload.utils;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExcelRead {

    public static List<Map<String, String>> read(ExcelReadOption excelReadOption) {

        //확장자 대로 workbook에 저장.
        Workbook wb = ExcelFileType.getWorkbook(excelReadOption.getFilePath());
        Sheet sheet = wb.getSheetAt(0);

        /**
         * Sheet에서 유효한 데이터가 있는 행의 개수를 가져온다.
         */
        int numOfRows = sheet.getPhysicalNumberOfRows();
        int numOfCells = 0;

        Row row = null;
        Cell cell = null;
        String cellName = "";

        Map<String, String> map = null; //각 row마다 값을 저장할 객체. put("A", "이름") 이런식
        List<Map<String, String>> result = new ArrayList<>();

        for(int rowIndex = excelReadOption.getStartRow() -1; rowIndex < numOfRows; rowIndex ++) {
            /** 워크북에서 가져온 시트에서 rowIndex에 해당하는 Row를 가져온다.
             * 하나의 Row는 여러개의 Cell을 가진다.
             */
            row = sheet.getRow(rowIndex);
            if(row != null) {

                numOfCells = row.getLastCellNum();  //가져온 Row의 Cell의 개수를 구한다.

                map = new HashMap<>(); //데이터를 담을 맵 객체 초기화

                for(int cellIndex = 0; cellIndex < numOfCells; cellIndex++) {
                    cell = row.getCell(cellIndex); //row에서 cellindex에 해당하는 cell을 가져온다.
                    cellName = ExcelCellRef.getName(cell, cellIndex); //현재 cell의 이름 A,B,C,D

                    if(!excelReadOption.getOutputColumns().contains(cellName)) {
                        continue;
                    }

                    map.put(cellName,ExcelCellRef.getValue(cell)); //cell의 이름을 키로 데이터를 담는다.
                }
                result.add(map);
            }

        }
        return result;
    }
}

















