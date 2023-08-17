package com.easydatabaseexport.util;

import com.easydatabaseexport.log.LogManager;
import lombok.extern.log4j.Log4j;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSimpleField;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * AddToTopic
 *
 * @author lzy
 * @date 2021/3/21 22:25
 **/
@Log4j
public class AddToTopic {

    /*public static void main(String[] args) throws Exception {
        generateTOC("C:\\Users\\admin\\Desktop\\1.doc","C:\\Users\\admin\\Desktop\\2.doc");
    }*/

    public static void generateTOC(XWPFDocument document, String targetPath) throws IOException {
        OutputStream out = null;
        try {
            String findText = "目录哈哈";
            String replaceText = "";
            for (XWPFParagraph p : document.getParagraphs()) {
                for (XWPFRun r : p.getRuns()) {
                    int pos = r.getTextPosition();
                    String text = r.getText(pos);
                    if (text != null && text.contains(findText)) {
                        text = text.replace(findText, replaceText);
                        r.setText(text, 0);
                        addField(p);
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
            if (out != null) {
                out.close();
            }
        }
    }

    private static void addField(XWPFParagraph paragraph) {
        CTSimpleField ctSimpleField = paragraph.getCTP().addNewFldSimple();
        ctSimpleField.setInstr("TOC \\o \"1-3\" \\h \\z \\u");
        ctSimpleField.setDirty(true);
        ctSimpleField.addNewR().addNewT().setStringValue("<>");
    }

}
