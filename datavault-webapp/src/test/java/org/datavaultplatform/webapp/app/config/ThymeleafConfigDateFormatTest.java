package org.datavaultplatform.webapp.app.config;

import org.datavaultplatform.webapp.config.GlobalDateTimeFormatInterceptor;
import org.datavaultplatform.webapp.test.ProfileStandalone;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

/**
 * Tests that the date/datetime formats specified within templates and
 * setup in GlobalDateTimeFormatInterceptor work as expected.
 *
 * @see GlobalDateTimeFormatInterceptor
 */
@WebMvcTest
@ProfileStandalone
public class ThymeleafConfigDateFormatTest extends BaseThymeleafTest{

    @Autowired
    MockMvc mvc;

    @Test
    void testDateTimeFormatting() throws Exception {
        String html = mvc.perform(MockMvcRequestBuilders.get("/test/dates").locale(Locale.UK))
                .andDo(print())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Document doc = Jsoup.parse(html);

        String noFormat = lookupDivText(doc, "no-format");

        // note: the built-in datetime format is sensitive to JDK version changes !!!!
        String builtInDateTimeFormat = lookupDivText(doc, "built-in-datetime-format");

        String customDateFormat = lookupDivText(doc, "custom-date-format");
        String customDateTimeFormat = lookupDivText(doc, "custom-datetime-format");

        String dateFormatISO = lookupDivText(doc, "date-format-iso");
        String dateFormatMM = lookupDivText(doc, "date-format-mm");
        String dateFormatMMM = lookupDivText(doc, "date-format-mmm");
        String globalTimeFormat = lookupDivText(doc, "global-time-format");
        String globalDateTimeFormat = lookupDivText(doc, "global-datetime-format");

        System.out.printf("noFormat[%s]%n", noFormat);
        System.out.printf("builtInDateTimeFormat[%s]%n", builtInDateTimeFormat);
        System.out.printf("customDateFormat[%s]%n", customDateFormat);
        System.out.printf("customDateTimeFormat[%s]%n", customDateTimeFormat);

        System.out.printf("dateFormatISO[%s]%n", dateFormatISO);
        System.out.printf("dateFormatMM[%s]%n", dateFormatMM);
        System.out.printf("dateFormatMMM[%s]%n", dateFormatMMM);
        System.out.printf("globalTimeFormat[%s]%n", globalTimeFormat);
        System.out.printf("globalDateTimeFormat[%s]%n", globalDateTimeFormat);

        assertEquals("Fri Feb 16 10:11:12 GMT 2024", noFormat);
        assertEquals("16 February 2024 at 10:11:12 GMT", builtInDateTimeFormat);
        assertEquals("16/Feb/2024", customDateFormat);
        assertEquals("16/Feb/2024 10:11:12", customDateTimeFormat);

        assertEquals("2024-02-16", dateFormatISO);
        assertEquals("16/02/2024", dateFormatMM);
        assertEquals("16 Feb 2024", dateFormatMMM);
        assertEquals("10:11:12", globalTimeFormat);
        assertEquals("16-Feb-2024 10:11:12", globalDateTimeFormat);
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        Date myDate() {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, 2024);
            cal.set(Calendar.MONTH, Calendar.FEBRUARY);
            cal.set(Calendar.DAY_OF_MONTH, 16);
            cal.set(Calendar.HOUR_OF_DAY, 10);
            cal.set(Calendar.MINUTE, 11);
            cal.set(Calendar.SECOND, 12);
            return cal.getTime();
        }

        @Controller
        static class TestDateFormattingController {

            @Autowired
            Date myDateTime;

            @RequestMapping("/test/dates")
            public ModelAndView renderTestDatePage() {
                ModelAndView result = new ModelAndView("test/dates");
                result.addObject("myDateTime", myDateTime);
                return result;
            }
        }
    }

}
