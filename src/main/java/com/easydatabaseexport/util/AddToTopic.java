package com.easydatabaseexport.util;

import com.easydatabaseexport.log.LogManager;
import lombok.extern.java.Log;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSimpleField;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STOnOff;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * AddToTopic
 *
 * @author lzy
 * @date 2021/3/21 22:25
 **/
@Log
public class AddToTopic {

    /*public static void main(String[] args) throws Exception {
        generateTOC("C:\\Users\\admin\\Desktop\\1.doc","C:\\Users\\admin\\Desktop\\2.doc");
    }*/

    public static void generateTOC(String sourcePath, String targetPath) throws IOException {
        FileInputStream fileInputStream = null;
        OutputStream out = null;
        try {
            fileInputStream = new FileInputStream(sourcePath);
            XWPFDocument document = new XWPFDocument(fileInputStream);
            String findText = "目录哈哈";
            String replaceText = "";
            for (XWPFParagraph p : document.getParagraphs()) {
                for (XWPFRun r : p.getRuns()) {
                    int pos = r.getTextPosition();
                    String text = r.getText(pos);
                    if (text != null && text.contains(findText)) {
                        text = text.replace(findText, replaceText);
                        r.setText(text, 0);
                        addField(p, "TOC \\o \"1-3\" \\h \\z \\u");
                        // addField(p, "TOC \\h");
                        break;
                    }
                }
            }
            out = new FileOutputStream(targetPath);
            document.write(out);
        } catch (Exception e) {
            LogManager.writeLogFile(e, log);
        } finally {
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }

    private static void addField(XWPFParagraph paragraph, String fieldName) {
        CTSimpleField ctSimpleField = paragraph.getCTP().addNewFldSimple();
        ctSimpleField.setInstr(fieldName);
        ctSimpleField.setDirty(STOnOff.TRUE);
        ctSimpleField.addNewR().addNewT().setStringValue("<>");
    }

}
