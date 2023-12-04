package com.mhyy.oauth2.filter;

import com.alibaba.fastjson.JSONObject;
import com.mhyy.common.response.ResponseCode;
import com.mhyy.common.response.ResponseVO;
import com.mhyy.oauth2.dao.OauthClientRepository;
import com.mhyy.oauth2.model.Oauth2Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;


@Component
@WebFilter(filterName = "OauthFilter")
public class OauthFilter implements Filter {
    private String filterPath;

    @Autowired
    private OauthClientRepository oauthClientRepository;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterPath = "/oauth/authorize";
    }

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws ServletException, IOException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String url = request.getRequestURI();
        if(url.equals(filterPath)) {
            Map<String, String[]> parameterMap = request.getParameterMap();
            if(!parameterMap.containsKey("redirect_url")) {
                ResponseVO<?> response = ResponseVO.fail(ResponseCode.BAD_REQUEST.getReturnCode()
                        , "redirect_url can not be blank!");
                returnJson(servletResponse, JSONObject.toJSONString(response));
                return;
            }
            //去db查询我们的 当前的 client id 的 redirect url 是什么，与参数进行匹配
            Oauth2Client client = oauthClientRepository.findByClientId(parameterMap.get("client_id")[0]);
            String redirectUrl = parameterMap.get("redirect_url")[0];
            if(!redirectUrl.equals(client.getRedirectUrl())) {
                ResponseVO<?> response = ResponseVO.fail(ResponseCode.BAD_REQUEST.getReturnCode()
                        , "redirect_url is not match!");
                returnJson(servletResponse, JSONObject.toJSONString(response));
                return;
            }
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    private void returnJson(ServletResponse servletResponse, String responseJson) {
        ServletOutputStream outputStream = null;
        servletResponse.setCharacterEncoding("UTF-8");
        servletResponse.setContentType("application/json; charset=utf-8");
        try {
            outputStream = servletResponse.getOutputStream();
            outputStream.write(responseJson.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new UnsupportedOperationException("Unknown issues when write the OauthFilter response!");
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {}
        }
    }
}
