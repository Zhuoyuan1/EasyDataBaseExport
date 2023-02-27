package com.easydatabaseexport.ui.export.config;

/**
 * ExportFileType
 *
 * @author lzy
 * @date 2022/11/10 10:10
 **/
public enum ExportFileType {

    //word
    WORD(".docx"),
    EXCEL(".xlsx"),
    MARKDOWN(".md"),
    HTML(".html"),
    PDF(".pdf");

    private String suffix;

    ExportFileType(String suffix) {
        this.suffix = suffix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }
}
