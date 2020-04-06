package com.fileserver.tools;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author:Mr.wang
 * @date:2019/2/21 0021 上午 10:32
 */
public class FileUtils {
    //文件夹拷贝    不管是路径还是File对象都可直接使用
    //拷贝文件夹方法
    // String 对象
    public static void copyDir(String srcPath,String destPath){
        File src=new File(srcPath);
        File dest=new File(destPath);
        copyDir(src,dest);
    }
    //拷贝文件夹方法
    //File 对象
    public static void copyDir(File src,File dest){
        if(src.isDirectory()){//文件夹
            dest=new File(dest,src.getName());
        }copyDirDetail(src,dest);
    }


    //拷贝文件夹细节
    public static void copyDirDetail(File src,File dest){
        if(src.isFile()){   //文件直接复制
            try {
                FileUtils.copyFile(src,dest);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else if(src.isDirectory()){//目录
            //确保文件夹存在
            dest.mkdirs();
            //获取下一级目录|文件
            for (File sub:src.listFiles()){
                copyDirDetail(sub,new File(dest,sub.getName()));
            }
        }
    }

    //文件拷贝
    public static void copyFile(String srcPath, String destPath) throws IOException {
        File src=new File(srcPath);
        File dest=new File(destPath);
        copyFile(src,dest);
    }
    public static void copyFile(File srcPath, File destPath) throws IOException {

        //选择流
        InputStream is = new FileInputStream(srcPath);
        OutputStream os = new FileOutputStream(destPath);
        //拷贝  循环+读取+写出
        byte[] flush = new byte[1024];
        int len = 0;
        //读取
        while (-1 != (len = is.read(flush))) {
            //写出
            os.write(flush, 0, len);
        }
        os.flush();//强制刷出
        //关闭流  先打开后关闭
        os.close();
        is.close();
    }


    /**
     * 删除文件
     *
     * @param pathname
     * @return
     * @throws IOException
     */
    public static boolean deleteFile(String pathname){
        boolean result = false;
        File file = new File(pathname);
        if (file.exists()) {
            file.delete();
            result = true;
        }
        return result;
    }

    /**
     * 获取当前请求中的文件列表
     *
     * @param request
     * @return
     */
    protected static List<MultipartFile> getMultipartFileList(
            HttpServletRequest request) {
        List<MultipartFile> files = new ArrayList<MultipartFile>();
        try {
            CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(
                    request.getSession().getServletContext());
            if (request instanceof MultipartHttpServletRequest) {
                // 将request变成多部分request
                MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
                Iterator<String> iter = multiRequest.getFileNames();
                // 检查form中是否有enctype="multipart/form-data"
                if (multipartResolver.isMultipart(request) && iter.hasNext()) {
                    // 获取multiRequest 中所有的文件名
                    while (iter.hasNext()) {
                        // 一次遍历所有文件
                        // MultipartFile file =
                        // multiRequest.getFile(iter.next().toString());
                        // if (file != null) {
                        // files.add(file);
                        // }
                        // 适配名字重复的文件
                        List<MultipartFile> fileRows = multiRequest
                                .getFiles(iter.next().toString());
                        if (fileRows != null && fileRows.size() != 0) {
                            for (MultipartFile file : fileRows) {
                                if (file != null && !file.isEmpty()) {
                                    files.add(file);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return files;
    }
}