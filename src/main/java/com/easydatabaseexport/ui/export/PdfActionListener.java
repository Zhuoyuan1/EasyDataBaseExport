package com.easydatabaseexport.ui.export;

import com.easydatabaseexport.common.CommonConstant;
import com.easydatabaseexport.entities.IndexInfoVO;
import com.easydatabaseexport.entities.TableParameter;
import com.easydatabaseexport.ui.AbstractActionListener;
import com.easydatabaseexport.ui.component.JCheckBoxTree;
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

    /**
     * 表结构和表索引数据组装
     **/
    @Override
    public boolean dataAssemble() {
        return dataAssembleAndJudge(root);
    }

    @SneakyThrows
    @Override
    public void export(File file) {
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
        PdfFont font = PdfFontFactory.createFont("STSong-Light", "UniGB-UCS2-H", pdf);
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
                Paragraph tableParagraph = new Paragraph(tableName).setFont(font);
                outline = createOutline(outline, pdf, tableName, tableParagraph);
                document.add(tableParagraph);
                //索引Table
                if (indexMap.size() > 0) {
                    Table table = new Table(CommonConstant.INDEX_HEAD_NAMES.length);
                    process(table, CommonConstant.INDEX_HEAD_NAMES, font, true);
                    String name = parameterMap.getKey().split("\\[")[0];
                    List<IndexInfoVO> indexInfoVOList = indexMap.get(name);
                    for (int j = 0; j < indexInfoVOList.size(); j++) {
                        process(table, getIndexValues(indexInfoVOList.get(j)), font, false);
                    }
                    document.add(table);
                }
                document.add(new Paragraph());
                //字段Table
                List<TableParameter> exportList = parameterMap.getValue();
                Table table = new Table(CommonConstant.COLUMN_HEAD_NAMES.length);
                //标题、内容
                process(table, CommonConstant.COLUMN_HEAD_NAMES, font, true);
                for (int i = 0; i < exportList.size(); i++) {
                    process(table, getColumnValues((i + 1) + "", exportList.get(i)), font, false);
                }
                document.add(table);
                //分页
                document.add(new AreaBreak());
            }
        }
        //删除最后一页
        pdf.removePage(pdf.getLastPage());
        document.close();
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

    //设置表格内容
    public static <T> void process(Table table, T[] line, PdfFont font, boolean isHeader) {
        for (T s : line) {
            if (Objects.isNull(s)) {
                return;
            }
            Cell cell = new Cell().add(new Paragraph(s.toString()).setFont(font));
            if (isHeader) {
                table.addHeaderCell(cell);
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
