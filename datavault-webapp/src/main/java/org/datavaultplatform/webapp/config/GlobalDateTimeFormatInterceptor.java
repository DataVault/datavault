package org.datavaultplatform.webapp.config;


import org.datavaultplatform.common.util.DateTimeUtils;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.annotation.Nullable;

public class GlobalDateTimeFormatInterceptor implements HandlerInterceptor {

    public static final String FMT_GLOBAL_DATE_TIME = "globalDateTimeFormat";
    public static final String FMT_GLOBAL_TIME = "globalTimeFormat";
    public static final String FMT_DATE_ISO = "dateFormatISO";

    public static final String FMT_DATE_DD_MMM_YYYY = "dateFormatddMMMyyyy";
    public static final String FMT_DATE_DD_MM_YYYY = "dateFormatddMMyyyy";
    private static final String PREFIX_REDIRECT = "redirect:";
    private static final String PREFIX_FORWARD = "forward:";

    @Override
    public void postHandle(
             HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) {
        if (modelAndView == null) {
            return;
        }
        if (modelAndView.getViewName() == null) {
            return;
        }
        if (modelAndView.getViewName().startsWith(PREFIX_REDIRECT)) {
            return;
        }
        if (modelAndView.getViewName().startsWith(PREFIX_FORWARD)) {
            return;
        }
        addGlobalDateTimeFormats(modelAndView.getModelMap());
    }

    public static void addGlobalDateTimeFormats(ModelMap modelMap){
        // From John Pinto : 17-Jan-2024 14:17:48
        modelMap.put(FMT_GLOBAL_DATE_TIME, DateTimeUtils.GLOBAL_DATETIME_FORMAT);

        modelMap.put(FMT_GLOBAL_TIME, DateTimeUtils.GLOBAL_TIME_FORMAT);

        modelMap.put(FMT_DATE_ISO, DateTimeUtils.ISO_DATE_FORMAT);
        modelMap.put(FMT_DATE_DD_MMM_YYYY, DateTimeUtils.DATE_FORMAT_DD_MMM_YYYY);
        modelMap.put(FMT_DATE_DD_MM_YYYY, DateTimeUtils.DATE_FORMAT_DD_MM_YYYY);
    }

}

