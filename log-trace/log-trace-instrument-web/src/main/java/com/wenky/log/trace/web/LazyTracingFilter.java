package com.wenky.log.trace.web;

import com.wenky.log.trace.Tracing;
import org.springframework.beans.factory.BeanFactory;

import javax.servlet.*;
import java.io.IOException;

/**
 * @author zhongwenjian
 * @date 2021/6/27
 */
public class LazyTracingFilter implements Filter {

    private final BeanFactory beanFactory;

    private LogTraceFilter filter;
    public LazyTracingFilter(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        Filter filter = getFilter();
        if (filter != null){
            filter.doFilter(servletRequest, servletResponse, filterChain);
            return;
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    private Filter getFilter(){
        if (filter!= null){
            return filter;
        }
        try {
            Tracing tracing = beanFactory.getBean(Tracing.class);
            filter = new LogTraceFilter(tracing);
        }catch (Exception ignore){

        }
        return filter;
    }

}
