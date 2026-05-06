package org.datavaultplatform.webapp.test;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.SneakyThrows;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Enumeration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

public class MvcUtils {

    /**
     * We've changed from AccessDeniedPage(/auth/denied) to AccessDeniedHandler which perfrorms forward to /auth/defined
     * so we need to use mockMvc twice - the first time to perform the request and the second time to perform the forward.
     * @param mockMvc
     * @param requestBuilder
     * @return
     */
    @SneakyThrows
    public static ResultActions performWithForward(MockMvc mockMvc, MockHttpServletRequestBuilder requestBuilder) {
        // 1. Initial Execution
        ResultActions action = mockMvc.perform(requestBuilder).andDo(print());
        MvcResult result = action.andReturn();
        String forwardedUrl = result.getResponse().getForwardedUrl();

        if (forwardedUrl != null) {
            // 2. Setup the Forward Request
            MockHttpServletRequestBuilder forwardRequest = get(forwardedUrl);
            

            // Carry over the session if it exists
            HttpSession session = result.getRequest().getSession(false);
            if (session != null) {
                forwardRequest.session((MockHttpSession) session);
            }

            HttpServletRequest originalRequest = result.getRequest();
            if(forwardedUrl.equals("/auth/denied")) {

                Throwable throwable = (Exception) originalRequest.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
                Integer statusCode = (Integer) originalRequest.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
                String requestUri = (String) originalRequest.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);

                if(throwable == null || statusCode == null || requestUri == null) {
                    throw new IllegalStateException("Unable to get error attributes from original request: " + originalRequest + "");
                }
            }
            Enumeration<String> attributeNames = originalRequest.getAttributeNames();

            while (attributeNames.hasMoreElements()) {
                String name = attributeNames.nextElement();

                if (name.startsWith("jakarta.servlet")) {
                    Object value = originalRequest.getAttribute(name);
                    System.out.println("Replicating attribute: " + name + "=" + value);
                    forwardRequest.with(req -> {
                        req.setAttribute(name, value);
                        return req;
                    });
                }
            }

            return mockMvc.perform(forwardRequest).andDo(print());
        }

        return action;
    }
}
