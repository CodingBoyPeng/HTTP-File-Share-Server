package com.fileserver.controller;

import com.alibaba.fastjson.JSONObject;
import com.fileserver.service.FileService;
import com.fileserver.tools.RandomUtil;
import com.fileserver.tools.ShortUUID;
import com.fileserver.tools.encrypt.AESFile;
import com.fileserver.tools.encrypt.RSAEncryptUtil;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: PengFeng
 * @Description: 文件上传接口
 * @Date: Created in 13:43 2020/4/5
 */
public class UploadFileController extends HttpServlet {
    private static final Logger logger = LogManager.getLogger(UploadFileController.class);
    private FileService fileService = new FileService();
    private static final String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDF3oU2/CybT+ndVJt//dFaRdXeHt9++utehKQpH3hwB1rEzGMf+y76j++PgJvduXKpD2th/kyv7jaO2ItVSlarQvGzm/C0QqLtVJP+5m+UszhdptEMo+JD0CO86pHvBOZsnusJqf2ugHj4Cy6mBeMYQ261D6MSeUesYrWQeV3c2QIDAQAB";
    private JSONObject resultJson;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        logger.info("Upload File");
        // 创建缓存
        DiskFileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload sfu = new ServletFileUpload(factory);
        //解决文件名称中文问题
        sfu.setHeaderEncoding("utf-8");
        // 解析
        try {
            List<FileItem> items = sfu.parseRequest(req);
            for(FileItem item:items){
                if(item.isFormField()){
                    continue;
                }else{
                    //文件信息
                    //判断只有文件才需要进行保存处理
                    logger.warn("Upload File Start");
                    // 获取上传文件名和文件后缀
                    String fileOldName = item.getName();
                    String suffixName = fileOldName.substring(fileOldName.lastIndexOf("."));
                    System.out.println("上传文件名： " + fileOldName + "文件后缀" + suffixName);
                    // 获取文件类型
                    String[] strArray = fileOldName.split("\\.");
                    int suffixIndex = strArray.length - 1;
                    String fileType = strArray[suffixIndex];
                    // 文件重命名：使用UUID对文件进行重命名
                    String uuid = ShortUUID.randomShortUUID();
                    String fileName = uuid;
                    Date uploadTime = new Date();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                    // 设置文件存储路径，将上传的文件指定存放到指定位置
                    // 存放位置： E：\\ yyyyMMDD目录下
                    String filePath = "E:\\" + sdf.format(uploadTime) + "\\";
                    String path = filePath + fileName + suffixName;
                    File dest = new File(path);
                    // 请求响应体：Response
                    Map<String, Object> response = new HashMap<>();
                    // 检验是否存在目录
                    if (!dest.getParentFile().exists()) {
                        dest.getParentFile().mkdir(); // 如果不存在，则新建目录
                    }
                    FileUtils.copyInputStreamToFile(item.getInputStream(), dest);
                    // 生成AES随机密钥
                    String randomKey = RandomUtil.generateStr(16);
                    // 使用AES加密算法对文件进行加密
                    AESFile.encryptFile(path, null, randomKey);
                    // 使用RSA公钥对随机产生的对称秘钥加密
                    String signature = RSAEncryptUtil.encrypt(randomKey, publicKey);
                    System.out.println("AES Random Key:" + randomKey + "   Signature:" + signature);
                    fileService.saveDocument(item, uploadTime, path, uuid, fileType, signature);
                    logger.warn("Upload File Success");
                    // 返回上传的文件UUID
                    Map<String, Object> resultUuid = new HashMap<>();
                    resultUuid.put("uuid", uuid);

                    resultJson = (JSONObject) JSONObject.toJSON(resultUuid);
                    resp.setContentType("text/html;charset=utf-8");
                    resp.getWriter().write(resultJson.toString());
                }
            }
        } catch (FileUploadException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
