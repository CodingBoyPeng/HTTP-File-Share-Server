package com.fileserver.tools.encrypt;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * @Author: PengFeng
 * @Description:    使用AES对文件加解密
 * @Date: Created in 12:26 2020/4/6
 */

public class AESFile {
    private static KeyGenerator kgen;
    private static SecureRandom secureRandom;
    private static SecretKeySpec key;
    private static Cipher cipher;

    /**
     * AES加密算法
     */
    public AESFile() {
    }
    private static void init(String keyWord){
        try {
            kgen = KeyGenerator.getInstance("AES");
            secureRandom = SecureRandom.getInstance("SHA1PRNG" );
            secureRandom.setSeed(keyWord.getBytes());
            kgen.init(128,secureRandom);
            SecretKey secretKey = kgen.generateKey();
            byte[] enCodeFormat = secretKey.getEncoded();
            key = new SecretKeySpec(enCodeFormat, "AES");
            cipher = Cipher.getInstance("AES");// 创建密码器
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
    }

/********************************对文件进行加密解密*********************************************************/
    /**
     * @param fileName  加密的文件路径
     * @param fileNameEncrypt 加密后的文件路径 当fileNameEncrypt为null时加密文件代替原文件
     * @param keyWord 加密密钥
     *
     */
    public static void encryptFile(String fileName,String fileNameEncrypt, String keyWord){
        try {
            boolean replace = false;
            if(fileNameEncrypt==null || fileNameEncrypt.equals("")){
                fileNameEncrypt = fileName+".tem";
                replace = true;
            }
            init(keyWord);
            cipher.init(Cipher.ENCRYPT_MODE, key);// 初始化
            File file = new File(fileNameEncrypt);
            if (!file.exists())
                file.createNewFile();
            FileInputStream fileinputstream=new FileInputStream(fileName);
            FileOutputStream out = new FileOutputStream(file);
            byte bytes[] = new byte[131072]; //因在断点下载时用到，不能修改 除非 断点下载处修改
            byte enbytes[] = null;
            int line = 0;
            while ((line = fileinputstream.read(bytes)) != -1) {
                enbytes = cipher.doFinal(Arrays.copyOfRange(bytes, 0, line));
                out.write(enbytes, 0, enbytes.length);
            }
            out.close();
            fileinputstream.close();

            if(replace){
                File oldFile  = new File(fileName);
                oldFile.delete();
                File newFile = new File(fileNameEncrypt);
                newFile.renameTo(oldFile);
            }

        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IOException e) {
// TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**解密
     * @param keyWord 解密密钥
     * @return  byte[]
     */
    public static void decryptFile(String encryptFileName,String decryptFileName, String keyWord) {
        try {
            boolean replace = false;
            if(decryptFileName==null || decryptFileName.equals("")){
                decryptFileName = encryptFileName+".tem";
                replace = true;
            }
            init(keyWord);
            cipher.init(Cipher.DECRYPT_MODE, key);// 初始化
            File file = new File(decryptFileName); //解密后的文件
            if (!file.exists())
                file.createNewFile();
            FileInputStream fileinputstream=new FileInputStream(encryptFileName); //要解密的文件
            FileOutputStream out = new FileOutputStream(file);
            byte bytes[] = new byte[131080]; //因在断点下载时用到，不能修改 除非 断点下载处修改
            byte enbytes[] = null;
            int line = 0;
            while ((line = fileinputstream.read(bytes)) != -1) {
                enbytes = cipher.doFinal(Arrays.copyOfRange(bytes, 0, line));
                out.write(enbytes, 0, enbytes.length);
            }
            out.close();
            fileinputstream.close();

            if(replace){
                File oldFile  = new File(encryptFileName);
                oldFile.delete();
                File newFile = new File(decryptFileName);
                newFile.renameTo(oldFile);
            }
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IOException e) {
// TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

/********************************对文件进行加密解密结束*****************************************************/

    /**将二进制转换成16进制
     * @param buf
     * @return  String
     */
    public static String parseByte2HexStr(byte buf[]) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < buf.length; i++) {
            String hex = Integer.toHexString(buf[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase());
        }
        return sb.toString();
    }
    /**将16进制转换为二进制
     * @param hexStr
     * @return  byte[]
     */
    public static byte[] parseHexStr2Byte(String hexStr) {
        if (hexStr.length() < 1)
            return null;
        byte[] result = new byte[hexStr.length()/2];
        for (int i = 0;i< hexStr.length()/2; i++) {
            int high = Integer.parseInt(hexStr.substring(i*2, i*2+1), 16);
            int low = Integer.parseInt(hexStr.substring(i*2+1, i*2+2), 16);
            result[i] = (byte) (high * 16 + low);
        }
        return result;
    }

}