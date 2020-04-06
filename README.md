# 基于HTTP协议的文件分享服务

​	面试官您好，我是彭峰，这是我在项目开发过程中遇到的问题做出的解决方案和总结，希望能够占用您的几分钟的宝贵时间阅读。

## 1. 项目简介                                                      

​	文件服务器是一个前后端分离的项目，项目采用C/S架构；客户端Client与服务端Server采用HTTP协议进行通讯；实现文件上传、下载、显示上传文件信息和显示上传功能。

## 2.运行效果

**主界面**：展示项目前台界面效果如下

![image-20200406224342760](README.assets/image-20200406224342760.png)

**上传文件**：文件上传后会获得上传文件的元数据，并将上传的文件加密处理，并保存在YYYYMMDD目录下效果如下

![image-20200406225255965](README.assets/image-20200406225255965.png)

**下载文件**：将当前上传文件进行下载，并解密，效果如下所示

![image-20200406225503112](README.assets/image-20200406225503112.png)

## 3.技术分析

### 3.1 数据加解密

​	在我们实际开发过程中，我们都需要对敏感数据进行安全处理。数据安全处理的方法有很多，例如常见的MD5(信息摘要算法)，Base64，SHA256，SHA512。其实这些严格意义上来讲不能算加解密算法，因为数据加解密肯定涉及到秘钥。MD5其实是一种通过哈希算法，将数据运算为固定长度值；Base64其实就是改变数据的编码格式；而SHA-256，SHA-512就是通过哈希函数将消息压缩成摘要，再通过哈希函数将数据形成一个新的哈希散列。

加密分为两种：对称加密和非对称加密。

#### 3.1.1 对称加密

​	对称加密就是数据加解密使用同一秘钥。常见的对称加密算法：DES，3DES，AES。因为项目用到了AES，所以这里只介绍AES。

**特点**：算法公开、计算量小、加密速度快、加密效率高 

**AES**：AES加密算法采用分组密码体制，每个分组数据的长度为128位16个字节，密钥长度可以是128位16个字节、192位或256位，一共有四种加密模式，我们通常采用需要初始向量IV的CBC模式，初始向量的长度也是128位16个字节。其加解密流程如下图：

![image-20200331234422488](README.assets/image-20200331234422488.png)

#### 3.1.2 非对称加密

 	公开密钥与私有密钥是一对，如果用公开密钥对数据进行加密，只有用对应的私有密钥才能解密；如果用私有密钥对数据进行加密，那么只有用对应的公开密钥才能解密。 常见的非对称加密算法有：RSA，DSA。

**基本流程**： 甲方生成一对密钥并将其中的一把作为公用密钥向其它方公开；得到该公用密钥的乙方使用该密钥对机密信息进行加密后再发送给甲方；甲方再用自己保存的另一把专用密钥对加密后的信息进行解密。甲方只能用其专用密钥解密由其公用密钥加密后的任何信息。 

**特点**：安全，但是速度较慢

### 3.2 Derby数据库

​	Derby是一个由Java语言编写的内置数据库。其体积小、易安装。 它的特性却非常丰富。它可以支持关系数据库中的所有企业级的特性，包括崩溃恢复、事务回滚和提交、行 / 表级锁、视图、主键 / 外键约束、触发器、子查询表达式等 

**优缺点**

* Derby是嵌入式数据库，桌面应用也可以用它来保存配置或其他数据，其做到与文件格式无关
* 支持临时表，索引，视图，外键，事物等
* 安全性良好

**连接方式**：有内嵌模式和网络模式两种。

#### 3.2.1 内嵌连接模式

​	内嵌模式中，Derby数据库与应用程序共享同一个JVM，通常由应用程序负责启动和停止，对除启动它的应用程序外的其它应用程序不可见，即其它应用程序不可访问它；  通过Java应用程序访问内嵌模式Derby数据库时，应用程序有责任需要在程序结束时关闭Derby数据库。

```java
// JDBC连接：
// 内嵌形式加载数据驱动
Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
// D:\\summer位置使用数据库位置，可以使用相对路径，也可以使用绝对路径
Connetction conn = DriverManager.getConnection("jdbc:derby:D:\\summer;create=true'");
```

#### 3.2.2 网络连接模式

​	网络模式，Derby数据库独占一个JVM，做为服务器上的一个独立进程运行。在这种模式下，允许有多个应用程序来访问同一个Derby数据库，通过本地IP加默认端口1527(默认端口)进行访问。													      首先我们应运行derby包的lib下的startNetWorkServer开启Derby服务器。效果如下图所示：

![image-20200401151212616](README.assets/image-20200401151212616.png)

![image-20200401151310875](readme.assets/image-20200401151310875.png)

```java
// 网络模式加载数据驱动
Class.forName("org.apache.derby.jdbc.ClientDriver").newInstance();
Connection conn = DriverManager.getConnection("jdbc:derby://localhost:1527/summer");
```

网络模式和内嵌模式的不同出在于：
A． 数据库连接URL的不同；
B． 应用程序退出时无须关闭Derby数据库；
C． 数据库驱动的不同； 

## 4.参考链接

[博客1]:https://www.cnblogs.com/wkfvawl/p/12091358.html	"Derby数据库的使用"
[博客2]:https://blog.csdn.net/weixin_42518062/article/details/104534608	"SpringBoot实现文件上传下载"
[博客3]:https://blog.csdn.net/gnail_oug/article/details/80324120?depth_1-utm_source=distribute.pc_relevant.none-task&utm_source=distribute.pc_relevant.none-task	"SpringBoot文件上传"
[博客4]:https://www.cnblogs.com/jpfss/p/11014737.html	"数据加解密服务"
[博客5]:https://www.jianshu.com/p/3840b344b27c?utm_campaign=maleskine&amp;utm_content=note&amp;utm_medium=seo_notes&amp;utm_source=recommendation	"AES算法"



## 5.总结

​	在开发文件上传下载服务器中，我遇到了很多问题。

1.命令行的Derby数据库版本与项目Derby依赖包的版本不兼容，只需将其版本一致就行

2.在使用jdbc链接Derby数据库时，我们构建的语句应该加上表的模式

```sql
// 用户创建的表为APP模式
SELECT * FROM APP.FILE ORDER BY FILE.CREATE_TIME DESC
```

3.使用`MultipartFile`的`transferTo`方法自动加上默认路径问题，导致找不到对应的盘符或文件。效果如下图所示:

![image-20200401233816069](README.assets/image-20200401233816069.png)

解决方案：添加`commons io`包，调用` FileUtils.copyInputStreamToFile(file.getInputStream(), tempFile)`将目标文件通过输入流传给目标文件。

4.跨域解决方案：跨域方式有三种，分别是Jsonp，Nginx反向代理，CORS。

**Jsonp**：很有局限性，只能跨Get请求，对其他类型请求无能为力，所以不建议使用。

**CORS**： 跨域资源共享，浏览器必须先以 OPTIONS 请求方式发送一个预请求，从而获知服务器端对跨源请求所支持 HTTP 方法。在确认服务器允许该跨源请求的情况下，以实际的 HTTP 请求方法发送那个真正的请求。 

本项目采用CORS跨域，解决方式如下

![image-20200406230933282](README.assets/image-20200406230933282.png)

