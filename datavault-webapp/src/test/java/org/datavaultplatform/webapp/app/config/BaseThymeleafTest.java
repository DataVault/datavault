package org.datavaultplatform.webapp.app.config;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.webapp.config.GlobalDateTimeFormatInterceptor;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ui.ModelMap;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
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

    final void outputHtml(String html) {
        outputHtml(html, true);
    }

    @SuppressWarnings("SameParameterValue")
    final void outputHtml(String html, boolean output) {
        if (output) {
            log.info(html);
        }
    }

}
