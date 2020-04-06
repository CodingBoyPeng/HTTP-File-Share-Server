package com.fileserver.controller;

import com.alibaba.fastjson.JSONObject;
import com.fileserver.pojo.Document;
import com.fileserver.service.FileService;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.TreeMap;

/**
 * @Author: PengFeng
 * @Description:   最近10次上传浏览接口
 * @Date: Created in 15:02 2020/4/5
 */
public class GetFileInfoListController extends HttpServlet {
    private static final Logger logger = LogManager.getLogger(GetFileInfoListController.class);
    private static final long serialVersionUID = 1L;
    private JSONObject resultJson;
    private FileService fileService = new FileService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        logger.info("Get File List");
        try {
            Map<Integer, Document> fileMap = fileService.getDocumentList();
            Map<Integer, Document> response = new TreeMap<>();
            logger.info("Get File List Success");
            for (int i = 0; i < fileMap.size(); i++) {
                if (response.size() >= 10) {
                    break;
                }
                response.put(Integer.valueOf(i + 1), fileMap.get(Integer.valueOf(i + 1)));
            }

            resultJson = (JSONObject) JSONObject.toJSON(response);
            resp.setContentType("text/html;charset=utf-8");
            resp.getWriter().write(resultJson.toJSONString());
        } catch (SQLException | IOException e) {
            logger.error("Get File List Error: " + e);
        }
    }
}
