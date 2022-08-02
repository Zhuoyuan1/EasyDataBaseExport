<div align="center">
   <img alt="EasyDataBaseExport" src="https://gitee.com/lzy549876/EasyDataBaseExport/raw/main/image/logo.png">
   <a href="https://www.likethewind.top">
      <h1 align="center" style="color:#4da7fd">
         <b>EasyDataBaseExport</b>
      </h1>
   </a>
</div>

<p align="center">
    <img alt="IntelliJ IDEA 2021" src="https://img.shields.io/badge/IntelliJ IDEA-2021-blue.svg">
    <img alt="java" src="https://img.shields.io/badge/language-java-brightgreen.svg">
    <img src="https://img.shields.io/badge/JDK-1.8.0_151+-yellow.svg" alt="JDK Version">
    <img alt="release" src="https://img.shields.io/badge/release-0.1.1-green">
</p> 

## 表结构导出Excel、导出Word
该工具主要用于导出excel、word，方便快速生成《数据库结构文档》，同时可以快速查看表的结构和相关信息  
该工具永久免费，欢迎大家体验！  
下载地址：右侧【发行版】中下载最新版   
其他历史版本：[下载](https://wwr.lanzoui.com/b02c7c0cb) 【密码:1111】

### 使用手册  
[《EasyDataBaseExport说明文档》](https://www.likethewind.top/)

### 运行环境要求  
 *  1.8.0_151+ （建议最好1.8.0_151以上的版本，因为151版本为JVM启用无限制强度管辖策略）

### 数据库支持  
- &#10004; MySQL
- &#10004; Oracle
- &#10004; SqlServer
- &#10004; PostgreSQL
- &#10004; 达梦
- &#10004; 人大金仓
- &#10004; DB2


### 文档支持  
- &#10004; Word
- &#10004; Excel


### 功能速览
> 注：图中涉及资源均来源于网络，仅用于展示工具功能，版权归其作者所有。

+ 连接

![在这里插入图片描述](https://github.com/Zhuoyuan1/MySQLToWordOrExcel/blob/main/image/login.jpg?raw=true)

+ 主页基本功能展示

![在这里插入图片描述](https://gitee.com/lzy549876/EasyDataBaseExport/raw/main/image/home.jpg)

#### 导出自定义

> `Word`可以导出表的 `主键`和`索引`  
> `Excel`可以导出分 `单sheet`和`多sheet`

配置位置如下：
![在这里插入图片描述](https://img-blog.csdnimg.cn/8f7ecf0270fc46d69fad54fc4c3a9956.gif)

### 项目结构  

```md
EasyDataBaseExport
└─ src/main/java/com/easydatabaseexport
    ├── `common` _(**公共信息**)_
    ├── `core` _(**核心包，包含核心数据源和接口**)_
    ├── `database` _(**所有数据库的数据源和实现**)_
    ├── `entities` _(**实体**)_
    ├── `enums` _(**枚举**)_
    ├── `exception` _(**全局异常**)_
    ├── `factory` _(**工厂**)_
    ├── `log` _(**全局异常拦截日志**)_
    ├── `navicat` _(**navicat处理**)_
    ├── `ui` _(**界面**)_
    └── `util` _(**工具**)_
```

### 运行方式  

* 1.双击
* 2.java -jar *.jar
* 3.运行`EasyDataBaseExportMain`的main方法

### 编译和打包  
![img.png](https://gitee.com/lzy549876/EasyDataBaseExport/raw/main/image/maven.png)  
双击运行`assembly:assembly`进行打包

注：由于采用的部分驱动未上传maven仓库  
若打包报错，提示找不到包，请先运行 `mvn install`  
![img.png](https://gitee.com/lzy549876/EasyDataBaseExport/raw/main/image/install.png)
### 下载  

蓝奏云：[https://wwr.lanzoui.com/b02c7c0cb](https://wwr.lanzoui.com/b02c7c0cb) 密码:1111  
项目地址：[https://gitee.com/lzy549876/EasyDataBaseExport](https://gitee.com/lzy549876/EasyDataBaseExport)

> 做这个工具的初衷是为了方便导出数据结构到word、excel。开发不易，希望多多支持，帮忙点star，谢谢各位！！！

### 联系方式    

QQ: 963565242       
Mail: 963565242@qq.com