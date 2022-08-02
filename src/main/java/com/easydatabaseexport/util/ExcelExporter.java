package com.easydatabaseexport.util;

import javax.swing.JTable;
import javax.swing.table.TableModel;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * ExcelExporter
 *
 * @author lzy
 * @date 2021/2/28 18:09
 **/
public class ExcelExporter {
    /**
     * 导出JTable到excel
     */
    public void exportTable(JTable table, File file) throws IOException {
        TableModel model = table.getModel();
        BufferedWriter bWriter = new BufferedWriter(new FileWriter(file));
        for (int i = 0; i < model.getColumnCount(); i++) {
            bWriter.write(model.getColumnName(i));
            bWriter.write("\t");
        }
        bWriter.newLine();
        for (int i = 0; i < model.getRowCount(); i++) {
            for (int j = 0; j < model.getColumnCount(); j++) {
                bWriter.write(model.getValueAt(i, j).toString());
                bWriter.write("\t");
            }
            bWriter.newLine();
        }
        bWriter.close();
    }
}
