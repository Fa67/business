package com.eayun.common.tools;

import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jxl.CellView;
import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.format.ScriptStyle;
import jxl.format.UnderlineStyle;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ExportDataToExcel<T> {
    private static final Log log = LogFactory.getLog(ExportDataToExcel.class);

    public ExportDataToExcel() {

    }

    /**
     *@param data 需要导出的数据集合
     *@param os  输出流
     *@param sheetName 表名
     *@param sheetBookName 工作薄名称
     */
    public void exportData(List<T> data, OutputStream os, String sheetName) throws Exception {
        if (data == null || data.size() <= 0) {
            return;
        }
        if ("".equals(sheetName)) {
            sheetName = "sheet1";
        }
        try {
            WritableWorkbook workbook = Workbook.createWorkbook(os);
            WritableSheet ws = workbook.createSheet(sheetName, 0);
            T t = data.get(0);
            Field[] fields = t.getClass().getDeclaredFields();
            ws.mergeCells(0, 0, fields.length - 1, 0);
            Label label = new Label(0, 0, sheetName, setTitleStyle());
            ws.addCell(label);//创建表的第一行，表标题
            CellView cellView = new CellView();
            cellView.setAutosize(true); //宽度自适应
            Map<String, String> headers = createHeader(t); //创建表头

            int[] columnSize = new int[fields.length];
            for (int i = 0; i < fields.length; i++) {//添加表头
                String content = headers.get(fields[i].getName());
                Label header = new Label(i, 1, content, setStyle(13, true));
                columnSize[i] = content.length() + getChineseNum(content);
                ws.addCell(header);
            }

            for (int i = 0; i < data.size(); i++) {//添加数据
                T d = data.get(i);
                for (int j = 0; j < fields.length; j++) {
                    Field field = fields[j];
                    field.setAccessible(true);
                    String cString = "";
                    if (field.get(d) != null) {
                        cString = field.get(d).toString();
                    } else {
                        continue;
                    }
                    int chineseNum = getChineseNum(cString);
                    if (columnSize[j] < cString.length() + chineseNum) {
                        columnSize[j] = cString.length() + chineseNum;
                    }
                    Label content = new Label(j, i + 2, cString, setStyle(12, false));
                    if (!fieldIsDispaly(field)) {//导出字段是否显示，不显示则隐藏
                        ws.setColumnView(j, 0);
                    }
                    ws.addCell(content);
                }
            }
            // 设置各列宽，做到自适应宽度
            for (int i = 0; i < fields.length; i++) {
                ws.setColumnView(i, columnSize[i] + 5);//设置表头列的宽度
                if (!fieldIsDispaly(fields[i])) {//导出字段是否显示，不显示则隐藏
                    ws.setColumnView(i, 0);
                }
            }
            workbook.write();
            workbook.close();

        } catch (Exception ex) {
            log.error(ex);
            throw ex;
        }
    }

    /**
     * 得到汉字数量
     * 
     * @param context
     * @return
     */
    private int getChineseNum(String context) { ///统计context中是汉字的个数
        int lenOfChinese = 0;
        Pattern p = Pattern.compile("[\u4e00-\u9fa5]"); //汉字的Unicode编码范围
        Matcher m = p.matcher(context);
        while (m.find()) {
            lenOfChinese++;
        }
        return lenOfChinese;
    }

    /**
     * 创建Excel表头
     */
    private Map<String, String> createHeader(T t) throws Exception {
        Field[] fields = t.getClass().getDeclaredFields();
        Map<String, String> headers = new HashMap<String, String>();
        for (Field field : fields) {
            field.setAccessible(true);
            ExcelTitle title = field.getAnnotation(ExcelTitle.class);
            if (title == null || title.name() == null || "".equals(title.name())) {
                headers.put(field.getName(), field.getName());
            } else {
                headers.put(field.getName(), title.name());
            }
        }
        return headers;
    }

    private WritableCellFormat setStyle(int fontSize, boolean bold) throws Exception {
        WritableFont he = new WritableFont(WritableFont.createFont("宋体"),// 字体
            fontSize,// WritableFont.DEFAULT_POINT_SIZE, // 字号
            WritableFont.NO_BOLD, // 粗体
            false, // 斜体
            UnderlineStyle.NO_UNDERLINE, // 下划线
            Colour.BLACK, // 字体颜色
            ScriptStyle.NORMAL_SCRIPT);
        if (bold) {
            he.setBoldStyle(WritableFont.BOLD);
        }
        WritableCellFormat wcf = new WritableCellFormat(he);
        wcf.setAlignment(Alignment.CENTRE); // 设置对齐方式
        wcf.setBorder(Border.ALL, BorderLineStyle.THIN);
        //wcf.setBackground(Colour.ICE_BLUE);//设置背景颜色
        return wcf;
    }

    private WritableCellFormat setTitleStyle() throws Exception {
        WritableFont he = new WritableFont(WritableFont.createFont("宋体"),// 字体
            20,// WritableFont.DEFAULT_POINT_SIZE, // 字号
            WritableFont.BOLD, // 粗体
            false, // 斜体
            UnderlineStyle.NO_UNDERLINE, // 下划线
            Colour.BLACK, // 字体颜色
            ScriptStyle.NORMAL_SCRIPT);
        WritableCellFormat wcf = new WritableCellFormat(he);
        wcf.setAlignment(Alignment.CENTRE); // 设置对齐方式
        //wcf.setBackground(Colour.GRAY_25);//设置背景颜色
        return wcf;
    }

    /**
     *字段导出时是否显示
     */
    private boolean fieldIsDispaly(Field field) {
        boolean flag = true;
        ExcelTitle title = field.getAnnotation(ExcelTitle.class);
        if (title == null) {
            flag = true;
        } else if (title.display()) {
            flag = true;
        } else {
            flag = false;
        }
        return flag;
    }
}
