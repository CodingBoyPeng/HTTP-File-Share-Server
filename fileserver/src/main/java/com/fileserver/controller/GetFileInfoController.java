package com.fileserver.controller;

import com.alibaba.fastjson.JSONObject;
import com.fileserver.pojo.Document;
import com.fileserver.service.FileService;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 获取文件信息接口
 */
public class GetFileInfoController extends HttpServlet{
    private static final Logger logger = LogManager.getLogger(GetFileInfoController.class);
    private static final long serialVersionUID = 1L;
    private JSONObject resultJson;
    private FileService fileService = new FileService();

    @Override
    public void doGet(HttpServletRequest request,HttpServletResponse response) throws IOException {
        logger.info("Get File Info");
        String uuid = request.getParameter("uuid");
        if (uuid.isEmpty()) {
            logger.error("Get File Info Failed: The UUID Is Empty");
        }

        Document result = null;
        try {
            result = fileService.getDocument(uuid);
        } catch (SQLException e) {
            logger.error("Get File Info Error: {}", e);
            e.printStackTrace();
        }

        resultJson = (JSONObject) JSONObject.toJSON(result);
        response.setContentType("text/html;charset=utf-8");
        response.getWriter().write(resultJson.toJSONString());
    }

}
