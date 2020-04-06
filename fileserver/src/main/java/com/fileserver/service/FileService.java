package com.fileserver.service;

import com.fileserver.pojo.Document;
import com.fileserver.tools.BaseErrorCode;
import com.fileserver.tools.ErrorException;
import org.apache.commons.fileupload.FileItem;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.sql.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * @Author: PengFeng
 * @Description: 主要包含文件上传，下载方法
 * @Date: Created in 16:19 2020/3/31
 */

public class FileService {

    private static final Logger logger = LogManager.getLogger(FileService.class);
    private static final String url = "jdbc:derby:file;create=true";

    /**
     * 保存文件上传信息
     * @param file      上传文件
     * @param time      上传时间
     * @param address   上传文件地址
     * @param uuid      文件流水号
     * @param type      文件类型
     * @param signature 签名
     */
    public void saveDocument(FileItem file, Date time, String address, String uuid, String type, String signature) {
        logger.info("Upload Document Service");
        String name = file.getName();   // 上传文件名
        Long size = file.getSize();     // 文件大小
        Timestamp createTime = new Timestamp(time.getTime()); // 文件上传时间

        // 获取数据库连接
        try {
            // 使用内嵌形式访问Derby数据库
            Connection conn = DriverManager.getConnection(url);
            String sql = "INSERT INTO APP.FILE (UUID, NAME, TYPE, SIZE, ADDRESS, SECRET, CREATE_TIME) VALUES ('" +
                    uuid + "', '" +       // 文件流水号
                    name + "', '" +       // 文件名
                    type + "', " +       // 文件类型
                    size + ", '" +       // 文件大小
                    address + "', '" +    // 保存地址
                    signature + "', '" +   // 签名信封
                    createTime + "')";   // 上传时间
            PreparedStatement ps = conn.prepareStatement(sql);
            // 执行更新语句
            int resultSet = ps.executeUpdate();
            if (resultSet <= 0){
                logger.info("Insert Document Data Error");
                throw new ErrorException(BaseErrorCode.UPLOAD_ERROR);
            }

            // DriverManager.getConnection("jdbc:derby:;shutdown=true");
            // 关闭流
            release(null, ps, conn);
        } catch (SQLException e) {
            logger.info("Insert Document Error");
            throw new ErrorException(BaseErrorCode.UPLOAD_ERROR);
        }
    }

    /**
     * 下载文件
     * @param uuid  文件流水号
     * @return
     * @throws SQLException
     */
    public Document downloadDocument(String uuid) throws SQLException {
        logger.info("Download Document: Down Document Service By Uuid");
        Document result = getDocument(uuid);
        if (result == null) {
            logger.error("Download Document Service Error");
            throw new ErrorException(BaseErrorCode.DOWNLOAD_ERROR);
        }

        return result;
    }

    /**
     * 获取指定文件元数据
     * @param uuid  文件流水号
     * @return
     * @throws SQLException
     */
    public Document getDocument(String uuid) throws SQLException {
        logger.info("Get Document Info Service: Uuid");
        Connection conn = DriverManager.getConnection(url);
        String sql = "SELECT * FROM APP.FILE WHERE uuid = '" + uuid+ "'";
        PreparedStatement ps = conn.prepareStatement(sql);
        // 执行查询语句
        ResultSet resultSet = ps.executeQuery();
        Map<Integer, Document> response = new HashMap<>();

        // DriverManager.getConnection("jdbc:derby:file;shutdown=true");

        return dealResult(resultSet, response).get(1);
    }

    /**
     * 获取最近10次上传文件元数据集
     * @return
     */
    public Map<Integer, Document> getDocumentList() throws SQLException {
        logger.info("Get Document List Recently 10");
        Connection conn = DriverManager.getConnection(url);
        String sql = "SELECT * FROM APP.FILE ORDER BY FILE.CREATE_TIME DESC";
        PreparedStatement ps = conn.prepareStatement(sql);
        // 执行查询语句
        ResultSet resultSet = ps.executeQuery();
        // 响应数据集
        Map<Integer, Document> response = new TreeMap<>();

        // DriverManager.getConnection("jdbc:derby:file;shutdown=true"); // 关闭数据库链接
        return  dealResult(resultSet, response);
    }

    /**
     * 对响应结果集进行公共处理
     * @param resultSet 结果集
     * @param response  响应集
     * @return
     * @throws SQLException
     */
    private static Map<Integer, Document> dealResult(ResultSet resultSet, Map<Integer, Document> response) throws SQLException {
        // 结果集数据量
        Integer num = 0;
        while(resultSet.next()) {
            Document document = new Document();
            document.setUuid(resultSet.getString("uuid"));
            document.setName(resultSet.getString("name"));
            document.setType(resultSet.getString("type"));
            document.setSize(resultSet.getInt("size"));
            document.setCreateTime(resultSet.getTimestamp("create_time"));
            document.setSaveAddress(resultSet.getString("address"));
            document.setSecretKey(resultSet.getString("secret"));
            response.put(++num, document);
        }
        return response;
    }

    /**
     * 关闭Statement和Connection
     * @param statement
     * @param conn
     */
    private static void release(ResultSet rs, PreparedStatement statement, Connection conn) {
        //关闭ResultSet对象
        if(rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        //关闭Statement对象
        if(statement != null) {
            try {
                statement.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //关闭conn对象
        if(conn != null) {
            try {
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
