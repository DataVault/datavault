package org.datavaultplatform.webapp.config;


import org.springframework.ui.ModelMap;
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
        addGlobalDateTimeFormats(modelAndView.getModelMap());
    }

    public static void addGlobalDateTimeFormats(ModelMap modelMap){
        modelMap.put("globalDateFormat", "'['yyyy-MM-dd']'");
        modelMap.put("globalTimeFormat", "'['HH:mm:ss']'");
        // From John Pinto : 17-Jan-2024 14:17:48
        modelMap.put("globalDateTimeFormat", "dd-MMM-yyyy HH:mm:ss");
    }

}

