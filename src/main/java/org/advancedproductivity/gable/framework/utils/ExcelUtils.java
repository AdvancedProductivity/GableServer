package org.advancedproductivity.gable.framework.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ExcelUtils {

    public static void main(String[] args) {
        String s = "{\n\t\"operationDir\": \"/config/Persistence\"\n}";
        JsonNode node = null;
        try {
            node = new ObjectMapper().readTree(s);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
//        String fileName = "C:\\Users\\Administrator\\Desktop\\test3.xlsx";
//        try {
//            ArrayNode read = read(fileName, 0, FileUtils.openInputStream(FileUtils.getFile(fileName)));
//            System.out.println(read.toPrettyString());
//            System.out.println("size: " + read.size());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
    private static String XLS = ".xls";
    private static String XLSX =".xlsx";

    public static ArrayNode read(String fileName, String sheetName, InputStream is) throws Exception {
        Workbook workbook = null;
        if (org.apache.commons.lang3.StringUtils.endsWith(fileName, XLS)) {
            workbook = new HSSFWorkbook(is);
        }else if (org.apache.commons.lang3.StringUtils.endsWith(fileName, XLSX)) {
            workbook = new XSSFWorkbook(is);
        } else {
            throw new Exception(fileName + " can not handle");
        }
        Sheet sheet = workbook.getSheet(sheetName);
        if (sheet == null) {
            throw new Exception(sheetName + " can not find");
        }
        return readSheetWithFirstLineIsHeader(sheet, is);
    }

    public static ArrayNode read(String fileName, int sheetIndex, InputStream is) throws Exception {
        Workbook workbook = null;
        if (org.apache.commons.lang3.StringUtils.endsWith(fileName, XLS)) {
            workbook = new HSSFWorkbook(is);
        }else if (org.apache.commons.lang3.StringUtils.endsWith(fileName, XLSX)) {
            workbook = new XSSFWorkbook(is);
        } else {
            throw new Exception(fileName + " can not handle");
        }
        Sheet sheet = workbook.getSheetAt(sheetIndex);
        if (sheet == null) {
            throw new Exception(sheetIndex + " can not find");
        }
        return readSheetWithFirstLineIsHeader(sheet, is);
    }

    private static ArrayNode readSheetWithFirstLineIsHeader(Sheet sheet, InputStream is) {
        //获取最大行数
        int rownum = sheet.getPhysicalNumberOfRows();
        //获取第一行
        Row row = sheet.getRow(0);
        List<String> header = new ArrayList<>();
        for (Cell cell : row) {
            String s = getCellFormatValue(cell).toString();
            header.add(s);
        }
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode result = mapper.createArrayNode();
        for (int i = 1; i < rownum; i++) {
            Row tmp = sheet.getRow(i);
            if (tmp == null) {
                continue;
            }
            ObjectNode item = mapper.createObjectNode();
            int emptyCount = 0;
            for (int j = 0; j < header.size(); j++) {
                Cell cell = tmp.getCell(j);
                String value = getCellFormatValue(cell).toString();
                if (StringUtils.isEmpty(value)) {
                    emptyCount++;
                }
                item.put(header.get(j), value);
            }
            // 如果某一行全部为空 就忽略这一行
            if (emptyCount != header.size()) {
                result.add(item);
            }
        }
        return result;
    }


    private static Object getCellFormatValue(Cell cell){
        Object cellValue = "";
        if(cell!=null){
            //判断cell类型
            switch(cell.getCellType()){
                case Cell.CELL_TYPE_NUMERIC:{
                    cellValue = String.valueOf(cell.getNumericCellValue());
                    break;
                }
                case Cell.CELL_TYPE_FORMULA:{
                    //判断cell是否为日期格式
                    if(DateUtil.isCellDateFormatted(cell)){
                        //转换为日期格式YYYY-mm-dd
                        cellValue = cell.getDateCellValue();
                    }else{
                        //数字
                        cellValue = String.valueOf(cell.getNumericCellValue());
                    }
                    break;
                }
                case Cell.CELL_TYPE_STRING:{
                    cellValue = cell.getRichStringCellValue().getString();
                    break;
                }
                default:
                    cellValue = "";
            }
        }else{
            cellValue = "";
        }
        return cellValue;
    }
}
