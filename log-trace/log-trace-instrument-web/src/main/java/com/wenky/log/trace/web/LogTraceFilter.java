package com.wenky.log.trace.web;

import com.wenky.log.trace.ContextScope;
import com.wenky.log.trace.Tracer;
import com.wenky.log.trace.Tracing;
import com.wenky.log.trace.propagation.Propagation;
import com.wenky.log.trace.propagation.TraceContext;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author zhongwenjian
 * @date 2021/6/26
 */
public class LogTraceFilter implements Filter {

    private final TraceContext.Extractor<HttpServletRequest> extractor;

    private final Tracer tracer;

    public LogTraceFilter(Tracing tracing){
        Propagation.Getter<HttpServletRequest, String> getter = HttpServletRequest::getHeader;
        extractor = tracing.propagation().extractor(getter);
        tracer = tracing.tracer();
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        TraceContext context = extractor.extract(request);
        ContextScope scope = tracer.newScope(context);
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            scope.finish();
        }
    }

}
