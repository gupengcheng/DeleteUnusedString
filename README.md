# DeleteUnusedString-过滤项目无用String.xml字符串的工具
delete project res values strings.xml unused string item
项目开发过程中，会产生很多无用的字符串，既使项目文件臃肿，也影响编译后的apk大小和编译效率，所以有必要对项目无用的资源文件进行清理，这里简单介绍以下项目中进行的string.xml无用string资源的清理流程，也顺便介绍一下自己写的清理xml文件的工具使用说明。
## 一.Android studio检查无用字符串
在android studio中，Analyze->Run inspection by name->unused resources可以找到无用的资源文件，如图所示

## 二.將检查到的所有无用的资源名字全部复制粘贴到一个txt文件中

如图所示，举例文件名为unused.txt

## 三.运行工程

由于是java项目，请用Eclipse打开

UNUSED_STRING_FILE_PATH变量存储的是第二步txt文件的路径

UNUSED_STRING_FILE_NAME存储的是第二步txt文件的文件名

PROJECT_RES_PATH存储的需要删除无用资源的工程的res目录，比如UserCare为例，就是/data/project/userCare/master/client/UserCare/res

运行项目，res目录下所有子目录下的strings.xml里的无用的资源就被删除掉了。



网上也有其他工具可以完成删除无用资源的目的，具体的可以再去了解，之所以自己去写，而没有用第三方的工具是因为，第三方的工具不好控制删除的资源类别，以及link生成result.xml需要配置
