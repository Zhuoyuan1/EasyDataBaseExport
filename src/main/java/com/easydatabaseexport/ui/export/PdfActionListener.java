package com.easydatabaseexport.ui.export;

import com.easydatabaseexport.common.CommonConstant;
import com.easydatabaseexport.entities.IndexInfoVO;
import com.easydatabaseexport.entities.TableParameter;
import com.easydatabaseexport.ui.component.JCheckBoxTree;
import com.easydatabaseexport.ui.export.config.ExportFileType;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.font.otf.GlyphLine;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfOutline;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.navigation.PdfDestination;
import com.itextpdf.kernel.pdf.navigation.PdfExplicitDestination;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.Property;
import com.itextpdf.layout.renderer.DrawContext;
import com.itextpdf.layout.renderer.ParagraphRenderer;
import com.itextpdf.layout.splitting.DefaultSplitCharacters;
import lombok.SneakyThrows;

import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * PdfActionListener
 *
 * @author lzy
 * @date 2022/11/10 9:25
 **/
public class PdfActionListener extends AbstractActionListener implements ActionListener {

    public PdfActionListener(final JCheckBoxTree.CheckNode root) {
        super.root = root;
        super.suffix = ExportFileType.PDF.getSuffix();
    }

    @SneakyThrows
    @Override
    public boolean export(File file) {
        Map<String, List<Map.Entry<String, List<TableParameter>>>> allMap = listMap.entrySet()
                .stream().collect(Collectors.groupingBy(v -> v.getKey().split("---")[0]));
        PdfWriter pdfWriter = new PdfWriter(file);
        PdfDocument pdf = new PdfDocument(pdfWriter);
        Document document = new Document(pdf, PageSize.A4.rotate());
        document.setProperty(Property.SPLIT_CHARACTERS, new DefaultSplitCharacters() {
            @Override
            public boolean isSplitCharacter(GlyphLine text, int glyphPos) {
                //return super.isSplitCharacter(text, glyphPos);//覆盖当前
                return true;//解决word-break: break-all;不兼容的问题，解决纯英文或数字不自动换行的问题
            }
        });
        URL url = PdfActionListener.class.getClassLoader().getResource("simsun.ttc");
        if (null == url) {
            throw new Exception("字体初始化失败！");
        }
        /** 为什么是 ,1 呢？ 请查看关键代码 => int ttcSplit = baseName.toLowerCase().indexOf(".ttc,");
         * @see com.itextpdf.io.font.FontProgramFactory#createFont(String, boolean) */
        PdfFont font = PdfFontFactory.createFont(url.toExternalForm() + ",1", PdfEncodings.IDENTITY_H, pdf);
        //设置文档属性
        pdf.getDocumentInfo().setAuthor("像风一样");
        pdf.getDocumentInfo().setTitle("EasyDatabaseExport");
        pdf.getDocumentInfo().setCreator("像风一样");
        pdf.getDocumentInfo().setKeywords("EasyDatabaseExport");
        //遍历数据
        for (Map.Entry<String, List<Map.Entry<String, List<TableParameter>>>> myMap : allMap.entrySet()) {
            PdfOutline outline = null;
            //数据库名
            String database = myMap.getKey();
            String title = "数据库：" + database;
            Paragraph p = new Paragraph(title).setFont(font).setFontSize(16).setBold();
            outline = createOutline(null, pdf, title, p);
            document.add(p);
            for (Map.Entry<String, List<TableParameter>> parameterMap : myMap.getValue()) {
                //表名
                String tableName = parameterMap.getKey().split("---")[1];
                Paragraph tableParagraph = new Paragraph(tableName).setFont(font).setBold();
                outline = createOutline(outline, pdf, tableName, tableParagraph);
                document.add(tableParagraph);
                //索引Table
                if (indexMap.size() > 0) {
                    Table table = new Table(CommonConstant.INDEX_HEAD_NAMES.length == 0 ? 1 : CommonConstant.INDEX_HEAD_NAMES.length);
                    process(table, CommonConstant.INDEX_HEAD_NAMES, font, true);
                    String name = parameterMap.getKey().split("\\[")[0];
                    List<IndexInfoVO> indexInfoVOList = indexMap.get(name);
                    if (!indexInfoVOList.isEmpty()) {
                        for (IndexInfoVO indexInfoVO : indexInfoVOList) {
                            process(table, getIndexValues(indexInfoVO), font, false);
                        }
                    } else {
                        process(table, getIndexValues(new IndexInfoVO()), font, false);
                    }
                    document.add(table);
                }
                document.add(new Paragraph());
                //字段Table
                List<TableParameter> exportList = parameterMap.getValue();
                Table table = new Table(CommonConstant.COLUMN_HEAD_NAMES.length == 0 ? 1 : CommonConstant.COLUMN_HEAD_NAMES.length);
                //标题、内容
                process(table, CommonConstant.COLUMN_HEAD_NAMES, font, true);
                for (TableParameter tableParameter : exportList) {
                    process(table, getColumnValues(tableParameter), font, false);
                }
                document.add(table);
                //分页
                document.add(new AreaBreak());
            }
        }
        //删除最后一页
        pdf.removePage(pdf.getLastPage());
        document.close();
        return Boolean.TRUE;
    }

    public PdfOutline createOutline(PdfOutline outline, PdfDocument pdf, String title, Paragraph p) {
        if (outline == null) {
            outline = pdf.getOutlines(false);
            outline = outline.addOutline(title);
            return outline;
        }
        OutlineRenderer renderer = new OutlineRenderer(p, title, outline);
        p.setNextRenderer(renderer);
        return outline;
    }

    /**
     * 设置表格内容
     */
    public static <T> void process(Table table, T[] line, PdfFont font, boolean isHeader) {
        for (T s : line) {
            if (Objects.isNull(s)) {
                return;
            }
            Cell cell = new Cell().add(new Paragraph(s.toString()).setFont(font));
            if (isHeader) {
                table.addHeaderCell(cell.setBold());
            } else {
                table.addCell(cell);
            }
        }
    }

    static class OutlineRenderer extends ParagraphRenderer {
        protected PdfOutline parent;
        protected String title;

        public OutlineRenderer(
                Paragraph modelElement, String title, PdfOutline parent) {
            super(modelElement);
            this.title = title;
            this.parent = parent;
        }

        @Override
        public void draw(DrawContext drawContext) {
            super.draw(drawContext);
            Rectangle rect = getOccupiedAreaBBox();
            PdfDestination dest =
                    PdfExplicitDestination.createFitH(
                            drawContext.getDocument().getLastPage(),
                            rect.getTop());
            PdfOutline outline = parent.addOutline(title);
            outline.addDestination(dest);
        }
    }
}
