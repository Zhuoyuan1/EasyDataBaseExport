package com.easydatabaseexport.ui.export;

import com.easydatabaseexport.common.CommonConstant;
import com.easydatabaseexport.common.EnvironmentConstant;
import com.easydatabaseexport.ui.component.JCheckBoxTree;
import com.easydatabaseexport.ui.export.config.ExportFileType;
import com.easydatabaseexport.util.AddToTopic;
import com.easydatabaseexport.util.FileOperateUtil;
import com.easydatabaseexport.util.WordReporter;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.awt.event.ActionListener;
import java.io.File;

/**
 * WordActionListener
 *
 * @author lzy
 * @date 2022/11/10 9:34
 **/
@Log4j
@Deprecated
public class WordActionListener extends AbstractActionListener implements ActionListener {

    public WordActionListener(final JCheckBoxTree.CheckNode root) {
        super.root = root;
        super.suffix = ExportFileType.WORD.getSuffix();
    }

    /**
     * Word导出
     **/
    @SneakyThrows
    @Override
    public boolean export(File file) {
        String filePath = FileOperateUtil.getSavePath() + CommonConstant.templateDir + File.separator;
        if (indexMap.size() > 0) {
            filePath += EnvironmentConstant.TEMPLATE_FILE.get(0);
        } else {
            filePath += EnvironmentConstant.TEMPLATE_FILE.get(1);
        }
        WordReporter wordReporter = new WordReporter();
        wordReporter.setTempLocalPath(filePath);
        wordReporter.init();
        wordReporter.exportWORD(listMap, indexMap, 0);
        XWPFDocument document = wordReporter.generate(file.getAbsolutePath());
        //添加目录
        AddToTopic.generateTOC(document, file.getAbsolutePath());
        return Boolean.TRUE;
    }

}
