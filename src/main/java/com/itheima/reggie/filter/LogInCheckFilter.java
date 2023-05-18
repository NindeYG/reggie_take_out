package com.itheima.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@WebFilter(filterName = "LogInCheckFilter")
public class LogInCheckFilter implements Filter {

    //路径匹配器。支持通配符
    public static final AntPathMatcher PATH_MATCHER=new AntPathMatcher();
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest=(HttpServletRequest) servletRequest;
        HttpServletResponse httpServletResponse=(HttpServletResponse) servletResponse;
        //获取请求的url
        String requestUrI=httpServletRequest.getRequestURI();
        //定义可放行的url
        String[] urls=new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/user/sendMsg",
                "/user/login"
        };

        log.info("拦截到请求：{}",requestUrI);
        //判断本次请求是否要放行
        boolean check = check(urls, requestUrI);
        //无需处理，直接放行
        if (check){
            //放行
            log.info("本次请求 {} 无需处理", requestUrI);
            filterChain.doFilter(httpServletRequest,httpServletResponse);
            return;
        }
        //不能放行，需要进行处理，判断登陆状态
        if(httpServletRequest.getSession().getAttribute("employee") != null){
            //已登录
            log.info("用户 {} 已登录", httpServletRequest.getSession().getAttribute("employee"));
            Long empId = (Long) httpServletRequest.getSession().getAttribute("employee");
            BaseContext.setCurrentId(empId);
            filterChain.doFilter(httpServletRequest,httpServletResponse);
            return;
        }
        //移动端登录判断
        if(httpServletRequest.getSession().getAttribute("user") != null){
            //已登录
            log.info("用户 {} 已登录", httpServletRequest.getSession().getAttribute("user"));
            Long empId = (Long) httpServletRequest.getSession().getAttribute("user");
            BaseContext.setCurrentId(empId);
            filterChain.doFilter(httpServletRequest,httpServletResponse);
            return;
        }
        //未登录，进行拦截,返回的信息需要与前端匹配
        log.info("用户未登录");
        httpServletResponse.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));

    }

    /*路径匹配，检查本次是否要放行*/
    public boolean check(String[] urls,String requestURI){
        for (String url : urls) {
            val match = PATH_MATCHER.match(url, requestURI);
            if (match){
                return true;
            }
        }
        return false;
    }
}
