package com.fileserver.pojo;

import java.util.Date;

/**
 * @Author: PengFeng
 * @Description: 文件实体信息
 * @Date: Created in 16:08 2020/3/31
 */
public class Document {
    private String uuid;
    private String name;
    private String type;
    private Integer size;
    private Date createTime;
    private String saveAddress;
    private String secretKey;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getSaveAddress() {
        return saveAddress;
    }

    public void setSaveAddress(String saveAddress) {
        this.saveAddress = saveAddress;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
