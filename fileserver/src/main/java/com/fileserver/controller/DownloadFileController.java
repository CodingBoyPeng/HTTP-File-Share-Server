package com.fileserver.controller;

import com.alibaba.fastjson.JSONObject;
import com.fileserver.pojo.Document;
import com.fileserver.service.FileService;
import com.fileserver.tools.BaseErrorCode;
import com.fileserver.tools.ErrorException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.sql.SQLException;


/**
 * @Author: PengFeng
 * @Description: 下载文件接口
 * @Date: Created in 15:17 2020/4/5
 */
public class DownloadFileController extends HttpServlet {
    private static final Logger logger = LogManager.getLogger(DownloadFileController.class);
    private static final long serialVersionUID = 1L;
    private FileService fileService = new FileService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        logger.info("Download File By Uuid");
        String uuid = req.getParameter("uuid");
        if (uuid.isEmpty()) {
            logger.error("Download File Failed: The UUID Is Empty");
            throw new ErrorException(BaseErrorCode.DOWNLOAD_ERROR);
        }

        try {
            // 根据UUID查找对应的文件
            Document document = fileService.downloadDocument(uuid);
            // 如果文件找不到
            if (document == null) {
                logger.error("Download File Error: The File Not Found");
                throw new ErrorException(BaseErrorCode.DOWNLOAD_ERROR);
            }

            File file = new File(document.getSaveAddress()); // 获取指定文件
            if (!file.exists()) {
                logger.error("Download Document Error: The File Is Not Exists");
                throw new ErrorException(BaseErrorCode.DOWNLOAD_ERROR);
            }

            // 读取本地文件的输入流
            InputStream in = new FileInputStream(file);
            // 设置响应正文的MIME类型
            resp.setContentType("Content-Disposition;charset=GB2312");
            resp.setHeader("Content-Disposition", "attachment;" + " filename="+ new String(file.getAbsolutePath().getBytes(), "ISO-8859-1"));
            // 把本地文件发送给客户端
            OutputStream out = resp.getOutputStream();
            int byteRead = 0;
            byte[] buffer = new byte[512];
            while ((byteRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, byteRead);
            }
            in.close();
            out.flush();
            out.close();
        } catch (SQLException e) {
            logger.error("Download File Error: ", e);
            throw new ErrorException(BaseErrorCode.DOWNLOAD_ERROR);
        }
    }

}
