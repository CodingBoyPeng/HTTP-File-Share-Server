package com.codeboy.fileclient.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import com.codeboy.fileclient.tools.BaseErrorCode;
import com.codeboy.fileclient.tools.ErrorException;
import com.codeboy.fileclient.tools.RandomUtil;
import com.codeboy.fileclient.tools.api.ApiUtils;
import com.codeboy.fileclient.tools.encrypt.AESFile;
import com.codeboy.fileclient.tools.encrypt.RSAEncryptUtil;
import com.codeboy.fileclient.tools.fileutil.FileUtils;
import com.codeboy.fileclient.tools.fileutil.MultipartFileToFile;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.BadPaddingException;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;


/**
 * @Author: PengFeng
 * @Description:
 * @Date: Created in 23:46 2020/4/2
 */
@RestController
@RequestMapping(value = "file")
public class DocumentController {
    private static  final Logger logger = LogManager.getLogger(DocumentController.class);
    JSONObject object = null;
    CloseableHttpResponse response = null;

    @Value(value = "${rsa.privatekey}")
    private String privateKey;
    
    /**
     * 文件上传接口
     * @param file
     * @return
     * @throws Exception
     */
    @PostMapping(value = "upload")
    public ResponseEntity<?> uploadDocument(@RequestParam("file") MultipartFile file, HttpServletResponse httpServletResponse) throws Exception {
        logger.info("Upload File Client: File Name {}", file.getName());
        // 获得Http客户端
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        // 创建post请求
        HttpPost httpPost = new HttpPost("http://localhost:8088/api/file/upload");
        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
        // 文件名其实是放在请求头的Content-Disposition里面进行传输的，如其值为form-data; name="files"; filename="头像.jpg"
        multipartEntityBuilder.addBinaryBody("file", MultipartFileToFile.multipartFileToFile(file), ContentType.DEFAULT_BINARY, URLEncoder.encode(file.getOriginalFilename(), "utf-8"));
        HttpEntity httpEntity = multipartEntityBuilder.build();
        httpPost.setEntity(httpEntity);
        String sid = RandomUtil.generateStr(6);             // 请求头X-SID：随机生成6位随机字符串
        httpPost.setHeader("X-SID", sid);                 // 设置请求头X-SID
        String signature = RSAEncryptUtil.sign(sid, privateKey); // 使用RSA私钥对SID随机字符串进行签名，等待服务端验签
        httpPost.setHeader("X-Signature", signature);     // 使用RSA对SID签名
        // 响应模型
        response = httpClient.execute(httpPost);
        // 校验响应码, 如不为200则表示请求失败
        if (response.getStatusLine().getStatusCode() != 200) {
            logger.info("Upload File Client Failed");
            return ApiUtils.errorOf(BaseErrorCode.UPLOAD_ERROR);
        }

        // 获取请求实体
        HttpEntity responseEntity = response.getEntity();
        logger.info("Upload File Client Error: HTTP Response Status{}", response.getStatusLine());
        // 将响应实体转化字符串
        String responseStr = EntityUtils.toString(responseEntity, StandardCharsets.UTF_8);
        // 将响应字符串转化JSON数组
        JSONObject jsonResponse = JSON.parseObject(responseStr);
        // 获取响应的UUID
        String uuid = jsonResponse.getString("uuid");
        // 请求参数
        StringBuffer params = new StringBuffer();
        params.append("uuid=" + uuid);
        // 创建GET请求，获取文件元数据
        String responseData = getResponseData(params);
        JSONObject responseFileJson = JSON.parseObject(responseData);
        release(httpClient, response);
        return ApiUtils.responseOf(responseFileJson);
    }

    /**
     * 文件下载接口
     * @param uuid
     */
    @GetMapping(value = "download")
    public void downloadDocument(@RequestParam(value = "uuid") String uuid, HttpServletResponse httpServletResponse) throws Exception {

        try {
            logger.info("Download File Client: Download File By Uuid {}", uuid);
            // 请求参数
            StringBuffer params = new StringBuffer();
            params.append("uuid=" + uuid);
            // 获取data集合
            String data = getResponseData(params);
            String digitalEnvelope = JSON.parseObject(data).getString("secretKey");
            // 使用RSA私钥对其进行解密，得到随机对称密钥
            String randomKey = RSAEncryptUtil.decrypt(digitalEnvelope, privateKey);
            logger.info("随机生成秘钥: " + randomKey);
            // 用对称密钥进行文件解密
            String fileName = JSON.parseObject(data).getString("name");   // 获取加密文件名
            String fileType = JSON.parseObject(data).getString("type");   // 获取加密文件类型
            String sid = RandomUtil.generateStr(6);
            String signature = RSAEncryptUtil.sign(sid, privateKey);
            // 2.调服务器文件下载接口
            // 创建Get请求
            HttpGet httpGetDownload = new HttpGet("http://localhost:8088/api/file/download" + "?" + params);
            httpGetDownload.setHeader("X-SID", sid);                  // 设置请求头X-SID
            httpGetDownload.setHeader("X-Signature", signature);      // 使用RSA对SID签名
            // 客服端向服务器发起Get请求
            CloseableHttpClient httpClient = HttpClientBuilder.create().build();
            CloseableHttpResponse downLoadResponse = httpClient.execute(httpGetDownload);
            StatusLine statusLine = downLoadResponse.getStatusLine();
            // 获取请求响应码
            int statusCode = statusLine.getStatusCode();
            // 校验状态码, 如果状态码不为200，则请求失败
            if (statusCode != 200) {
                logger.warn("Request Failed");
                throw new ErrorException(BaseErrorCode.DOWNLOAD_ERROR);
            }

            // 获取接口返回的响应流
            HttpEntity entity = downLoadResponse.getEntity();
            InputStream fileInput = entity.getContent();
            // 新建临时文件,将响应流输入到文件中
            File tmpFile = new File("E:\\tmp\\tmp" + "." + fileType);
            // 检测目录是否存在
            if (!tmpFile.getParentFile().exists()) {
                tmpFile.getParentFile().mkdir(); // 如果不存在，则新建目录
            }
            // 将响应流写入文件中
            OutputStream out = new FileOutputStream(tmpFile);
            byte buf[]=new byte[1024];
            int len;
            while((len=fileInput.read(buf))>0)
                out.write(buf,0,len);
            out.close();
            fileInput.close();
            // 新建解密文件，用于下载
            File decryptFile = new File("E:\\tmp\\tmp2" + "." + fileType);
            // 检测目录是否存在
            if (!decryptFile.getParentFile().exists()) {
                decryptFile.getParentFile().mkdir(); // 如果不存在，则新建目录
            }
            // 对临时文件进行解密
            AESFile.decryptFile(tmpFile.getPath(), decryptFile.getPath(), randomKey);
            httpServletResponse.reset();
            httpServletResponse.setCharacterEncoding("UTF-8");
            httpServletResponse.setContentType("application/octet-stream");
            //3.设置content-disposition响应头控制浏览器以下载的形式打开文件
            httpServletResponse.addHeader("Content-Disposition","attachment;filename=" + fileName);
            int length = 0;
            byte[] buffer = new byte[1024];
            OutputStream outputStream = httpServletResponse.getOutputStream();
            InputStream stream = new FileInputStream(decryptFile.getAbsolutePath());
            while ((length = stream.read(buffer)) > 0) {
                //将缓冲区的数据输出到客户端浏览器
                outputStream.write(buffer,0,length);
            }
            outputStream.close();
            stream.close();
            // 删除临时文件, 避免资源浪费
            logger.info("Delete Tmp File: Avoid Waste Of Resources");
            FileUtils.deleteFile(decryptFile.getPath());
            FileUtils.deleteFile(tmpFile.getPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 请求获取文件元数据接口
     * @param params
     * @return
     * @throws IOException
     */
    public String getResponseData(StringBuffer params) throws IOException {
        logger.info("Get File Info");
        // 获得Http客户端(可以理解为:你得先有一个浏览器;注意:实际上HttpClient与浏览器是不一样的)
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        // 1.创建Get请求，获取文件信息元数据
        HttpGet httpGet = new HttpGet("http://localhost:8088/api/file/get" + "?" + params);
        // 设置ContentType(注:如果只是传普通参数的话,ContentType不一定非要用application/json)
        httpGet.setHeader("Content-Type", "application/json;charset=utf8");
        String sid = RandomUtil.generateStr(16);             // 请求头X-SID：随机生成16位随机字符串
        httpGet.setHeader("X-SID", sid);                  // 设置请求头X-SID
        String signature = RSAEncryptUtil.sign(sid, privateKey); // 使用RSA私钥对SID随机字符串进行签名，等待服务端验签
        httpGet.setHeader("X-Signature", signature);      // 使用RSA对SID签名
        logger.info("Request Header: X-SID {}, X-Signature {}", sid, signature);
        logger.info("Request Server Get File Interface");
        // 客服端向服务器发起Get请求
        response = httpClient.execute(httpGet);
        // 校验响应码，如不为200则请求失败，并返回对应的错误码
        if (response.getStatusLine().getStatusCode() != 200) {
            logger.info("Request File Server Fail: Get File Info Failed");
            throw new ErrorException(BaseErrorCode.DOWNLOAD_ERROR);
        }

        // 从相应模型中获取响应实体
        HttpEntity responseEntity = response.getEntity();
        // 将响应实体转化字符串
        String responseStr = EntityUtils.toString(responseEntity, StandardCharsets.UTF_8);

        return responseStr;
    }

    /**
     * 获取文件列表
     * @return
     */
    @GetMapping("list")
    public ResponseEntity<?> listFile() {
        logger.info("Get File List Client");
        // 获得Http客户端(可以理解为:你得先有一个浏览器;注意:实际上HttpClient与浏览器是不一样的)
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        // 创建Get请求
        HttpGet httpGet = new HttpGet("http://localhost:8088/api/file/list");
        try {
            // 由客户端执行(发送)Get请求
            response = httpClient.execute(httpGet);
            // 从响应模型中获取响应实体
            HttpEntity responseEntity = response.getEntity();
            StatusLine statusLine = response.getStatusLine();
            // 响应码
            int statusCode = statusLine.getStatusCode();
            // 校验状态码
            if (statusCode != 200) {
                logger.warn("Request Failed");
                throw new ErrorException(BaseErrorCode.INVALID_REQUEST);
            }

            if (responseEntity != null) {
                object = JSON.parseObject(EntityUtils.toString(responseEntity));
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                // 释放资源
                if (httpClient != null) {
                    httpClient.close();
                }
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return ApiUtils.responseOf(object);
    }

    /**
     * 释放资源
     */
    public static void release (CloseableHttpClient httpClient, CloseableHttpResponse response) {
        logger.info("Release IO");
        // 释放资源
        try {
            if (httpClient != null) {
                httpClient.close();
            }
            if (response != null) {
                response.close();
            }
        } catch (IOException e) {
            logger.error("Release IO Exception");
            e.printStackTrace();
        }
    }
}
