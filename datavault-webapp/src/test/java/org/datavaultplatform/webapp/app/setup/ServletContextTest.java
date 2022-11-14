package org.datavaultplatform.webapp.app.setup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import javax.servlet.ServletContext;
import org.datavaultplatform.webapp.test.ProfileStandalone;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ProfileStandalone
// Note : Spring allows second resolution for timeout but Tomcat only does minutes
@TestPropertySource(properties = "server.servlet.session.timeout=14m")
class ServletContextTest {

	@Value("${server.servlet.session.timeout}")
	String timeoutRaw;

	@Autowired
	MockMvc mvc;

	@Test
	void testServletConfig() throws Exception {
		assertEquals("14m", timeoutRaw);

		MvcResult result = mvc.perform(get("/test/hello")).andReturn();

		ServletContext ctx = result.getRequest().getServletContext();

		//check session timeout
		long timeoutMinutes = ctx.getSessionTimeout();
		assertEquals(14, timeoutMinutes);

		//check display name
		String displayName = ctx.getServletContextName();
		assertEquals("datavault-webapp", displayName);

		//check web app root key
		String webAppRootKey = ctx.getInitParameter("webAppRootKey");
		assertEquals("webapp.root", webAppRootKey);
	}

}
