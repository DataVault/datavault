package org.datavaultplatform.webapp.app.config;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.datavaultplatform.webapp.config.GlobalDateTimeFormatInterceptor;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ui.ModelMap;
import org.springframework.util.Assert;

import java.io.*;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class BaseThymeleafTest {

    protected Date now;

    @BeforeEach
    void setup() {
        this.now = new Date();
    }

    final ModelMap getModelMap() {
        ModelMap result = new ModelMap();
        GlobalDateTimeFormatInterceptor.addGlobalDateTimeFormats(result);
        return result;
    }

    final String lookupDivText(Document doc, String id) {
        return doc.selectXpath("//div[@id='" + id + "']")
                .stream()
                .map(Element::text)
                .findFirst()
                .orElseThrow(RuntimeException::new);
    }

    final void noFormFields(Document doc){
        Elements forms = doc.selectXpath("//form[1]");
        assertThat(forms).isEmpty();
    }

    final void checkTitle(Document doc, String expectedTitle) {
        List<TextNode> titles = doc.selectXpath("//head/title[1]/text()", TextNode.class);
        String title = titles.get(0).text();
        assertThat(title).isEqualTo(expectedTitle);
    }

    final String getFirstLine(ClassPathResource res) throws IOException {
        InputStreamReader rdr = new InputStreamReader(res.getInputStream());
        LineNumberReader lnr = new LineNumberReader(rdr);
        return lnr.readLine();
    }

    final String getFirstLine(String fileContents) {
        if (fileContents == null) {
            return null;
        } else {
            return Arrays.stream(fileContents.split("\n")).findFirst().orElse(null);
        }
    }
    

    @SuppressWarnings("SameParameterValue")
    final void outputHtml(String filename, Document doc) {
        String html = doc.html();
        log.info(html);
        
        outputTemplateHtmlToFile(filename,html);
    }

    public static final String BASE_DIR_ENV_NAME = "DV_LOCAL_TEST_TEMPLATE_OUTPUT_BASE_DIR";
    public static final String TEMPLATE_FILE_TYPE = "new";
    @SneakyThrows
    private void outputTemplateHtmlToFile(String filename, String html) {
        // for local 'human' testing to compare old and new template output
        String baseDirName = System.getProperty(BASE_DIR_ENV_NAME);
        if (StringUtils.isBlank(baseDirName)) {
            return;
        }
        File baseDir = new File(baseDirName);
        Assert.isTrue(baseDir.exists(), String.format("The dv test template output base dir [%s] does not exist", baseDir));
        Assert.isTrue(baseDir.isDirectory(), String.format("The dv test template output base dir [%s] is not a directory", baseDir));
        Assert.isTrue(baseDir.canWrite(), String.format("The dv test template output base dir [%s] is not writable", baseDir));
        File outputFile = new File(baseDir, filename + TEMPLATE_FILE_TYPE + ".html");
        try (FileWriter fw = new FileWriter(outputFile)) {
            fw.write(html);
            fw.write("\n");
        }
        log.info("Written template generated html to [{}]", outputFile.getCanonicalPath());
    }
}
