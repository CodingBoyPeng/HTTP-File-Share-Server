package com.fileserver.filter;

import com.fileserver.controller.DownloadFileController;
import com.fileserver.tools.BaseErrorCode;
import com.fileserver.tools.ErrorException;
import com.fileserver.tools.encrypt.RSAEncryptUtil;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Author: PengFeng
 * @Description:  过滤器过滤请求
 * @Date: Created in 17:26 2020/4/5
 */
public class FileFilter implements Filter {
    private static final Logger logger = LogManager.getLogger(FileFilter.class);
    private static final String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDF3oU2/CybT+ndVJt//dFaRdXeHt9++utehKQpH3hwB1rEzGMf+y76j++PgJvduXKpD2th/kyv7jaO2ItVSlarQvGzm/C0QqLtVJP+5m+UszhdptEMo+JD0CO86pHvBOZsnusJqf2ugHj4Cy6mBeMYQ261D6MSeUesYrWQeV3c2QIDAQAB";

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        logger.info("Start Request FileFilter");
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        StringBuffer url = request.getRequestURL();
        // 如果请求的是显示最近10次上传列表，则跳过
        if (url.toString().contains("list")) {
            chain.doFilter(servletRequest, servletResponse);
            return;
        }

        // 如果请求头中X-SID参数和X-Signature为空，则返回不合法请求状态码403
        String sid = request.getHeader("X-SID");
        String signature = request.getHeader("X-Signature");
        if (!StringUtils.isEmpty(sid) && !StringUtils.isEmpty(signature)) {
            logger.info("Start Verify Request");
            // 使用公钥验签
            boolean verifyResult = RSAEncryptUtil.verify(sid, signature, publicKey);
            if (!verifyResult) {
                logger.error("Verify Signature Failed");
                throw new ErrorException(BaseErrorCode.INVALID_REQUEST);
            }

            logger.info("Verify Signature  Success");
            chain.doFilter(servletRequest, servletResponse);
            return;
        }

        logger.error("Verify Signature Failed");
        throw new ErrorException(BaseErrorCode.INVALID_REQUEST);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void destroy() {

    }
}
