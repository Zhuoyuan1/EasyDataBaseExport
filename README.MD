## MySQL表结构导出Excel、导出Word

### version 0.0.5
* 删减  
【查询】删除dql查询功能（说实话市面上很工具sql查询功能都挺好用的，感觉sql查询功能有点多余...）
* 优化  
【界面】细节调整，两侧不可移动，但增加伸缩小按钮
【左侧】左侧的树可以直接选择库  
【细节】增加icon图标
* 新增  
【功能】word导出功能加入目录自动生成（Microsoft Office测试通过，WPS可以手动生成目录结构）注：打开word时会提醒更新目录，点击是!
* bug  
【导出】导出bug修复  
【word】word导出按顺序导出

### version 0.0.4
* 【连接问题】修复连接过长导致重连的问题
* 【界面】优化左侧树结构UI和整体UI修正
* 【代码】代码重构，优化加载速度
* 【导出】支持多表excel、word导出

### version 0.0.2
* 1.单库展示修改为多库展示，单节点 -> 树结构
* 2.word导出失败问题修复

### version 0.0.1
* 1.支持展示指定数据库的所有表
* 2.支持查看表信息展示
* 3.支持表字段展示
* 4.支持excel、word导出

### 功能速览
> 注：图中涉及资源均来源于网络，仅用于展示工具功能，版权归其作者所有。

+ 连接

![UploadFile](https://github.com/Zhuoyuan1/MySQLToWordOrExcel/blob/main/image/login.jpg?raw=true)

+ 主页基本功能展示

![MainPage](https://github.com/Zhuoyuan1/MySQLToWordOrExcel/blob/main/image/home.jpg?raw=true)


### 运行方式

* 1.双击
* 2.java -jar *.jar

> 做这个工具的初衷是为了方便导出数据结构到word、excel。开发不易，希望多多支持，帮忙点star，谢谢各位！！！