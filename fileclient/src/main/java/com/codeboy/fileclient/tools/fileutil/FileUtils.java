package com.codeboy.fileclient.tools.fileutil;

import java.io.*;

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
        }
        copyDirDetail(src,dest);
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

    // 清空已有的文件内容，以便下次重新写入新的内容
    public static void clearInfoForFile(String fileName) {
        File file =new File(fileName);
        try {
            if(!file.exists()) {
                file.createNewFile();
            }
            FileWriter fileWriter =new FileWriter(file);
            fileWriter.write("");
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}