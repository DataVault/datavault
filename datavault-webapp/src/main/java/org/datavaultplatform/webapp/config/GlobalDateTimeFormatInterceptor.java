package org.datavaultplatform.webapp.config;


import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GlobalDateTimeFormatInterceptor implements HandlerInterceptor {

    @Override
    public void postHandle(
            HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        if (modelAndView == null) {
            return;
        }
        if (modelAndView.getViewName().startsWith("redirect:")) {
            return;
        }
        if (modelAndView.getViewName().startsWith("forward:")) {
            return;
        }
        modelAndView.getModelMap().put("globalDateFormat", "'['yyyy-MM-dd']'");
        modelAndView.getModelMap().put("globalTimeFormat", "'['HH:mm:ss']'");
        modelAndView.getModelMap().put("globalDateTimeFormat", "'['HH:mm:ss yyyy-MM-dd']'");
    }

}

