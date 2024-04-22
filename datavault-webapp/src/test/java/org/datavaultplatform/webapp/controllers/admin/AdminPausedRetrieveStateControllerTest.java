package org.datavaultplatform.webapp.controllers.admin;


import org.datavaultplatform.common.dto.PausedRetrieveStateDTO;
import org.datavaultplatform.webapp.app.DataVaultWebApp;
import org.datavaultplatform.webapp.services.RestService;
import org.datavaultplatform.webapp.test.AddTestProperties;
import org.datavaultplatform.webapp.test.ProfileDatabase;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest(classes = DataVaultWebApp.class)
@AutoConfigureMockMvc
@ProfileDatabase
@TestPropertySource(properties = "logging.level.org.springframework.security=DEBUG")
@AddTestProperties
class AdminPausedRetrieveStateControllerTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2007-12-03T10:15:30.00Z"), ZoneOffset.UTC);

    @Autowired
    MockMvc mvc;

    @MockBean
    RestService mService;

    @Test
    @WithMockUser(roles = "USER")
    public void testShowRecent() throws Exception {
        checkShowRecent(false);
    }

    @Test
    @WithMockUser(roles = {"IS_ADMIN","USER"})
    public void testShowRecentAsIS_ADMIN() throws Exception {
        checkShowRecent(true);
    }

    private void checkShowRecent(boolean hasIsAdminRole) throws Exception {
        PausedRetrieveStateDTO ps1 = new PausedRetrieveStateDTO(false, LocalDateTime.now(CLOCK));
        PausedRetrieveStateDTO ps2 = new PausedRetrieveStateDTO(true, LocalDateTime.now(CLOCK).minusDays(1));
        PausedRetrieveStateDTO ps3 = new PausedRetrieveStateDTO(false, LocalDateTime.now(CLOCK).minusDays(2));
        List<PausedRetrieveStateDTO> list = Arrays.asList(ps3, ps2, ps1);

        when(mService.getPausedRetrieveStateHistory(10)).thenReturn(list);

        MvcResult result = mvc.perform(get("/admin/paused/retrieve/history"))
                .andDo(print())
                .andReturn();
        assertThat(result.getModelAndView().getViewName()).isEqualTo("admin/paused/retrieve/history");
        assertThat(result.getModelAndView().getModel().get("pausedStates")).isEqualTo(list);

        verify(mService).getPausedRetrieveStateHistory(10);
        verifyNoMoreInteractions(mService);

        String rawHtml = result.getResponse().getContentAsString();
        Document doc = Jsoup.parse(rawHtml);
        checkHasHtmlElement(doc, "tglPsBtn", hasIsAdminRole);
        checkHasHtmlElement(doc, "tglPsSpan", !hasIsAdminRole);
    }

    private void checkHasHtmlElement(Document doc, String id, boolean expected) {
        Element elem = doc.getElementById(id);
        boolean exists = elem != null;

        assertThat(exists ).isEqualTo(expected);

    }

    @Test
    @WithMockUser(roles = {"IS_ADMIN"})
    public void testToggleState() throws Exception {
        MvcResult result = mvc.perform(post("/admin/paused/retrieve/toggle").with(csrf())).andDo(print()).andReturn();
        assertThat(result.getModelAndView().getViewName()).isEqualTo("redirect:/admin/paused/retrieve/history");

        verify(mService).toggleRetrievePausedState();
        verifyNoMoreInteractions(mService);
    }
}