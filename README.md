<p align="center">
   <a href="https://721806280.github.io/lark-notice-plugin-doc">
       <img src="src/main/webapp/images/logo.png" alt="logo">
   </a>
</p>

<p align="center">
   <img src="https://img.shields.io/badge/JDK-17-success" alt="JDK">
   <img src="https://img.shields.io/badge/Jenkins-2.479-blue.svg" alt="License">
   <img src="https://img.shields.io/badge/License-MIT-blue.svg" alt="License">
   <img src="https://img.shields.io/badge/Author-xm.z-success" alt="Author">
</p>

<h1 align="center">Lark - 云雀Jenkins构建通知插件</h1>

## 🌐 插件简介

`lark-notice-plugin` 是一个用于  `Jenkins` 的 `构建通知机器人` 通知插件，可以将 `Jenkins`
构建过程以及结果通知推送到 `Lark`、`飞书`、`钉钉` 协作平台。
可配置多个的通知时机，包括 `构建启动时`、`构建中断`、`构建失败`、`构建成功时`、`构建不稳定`等。
支持多种不同类型的消息，包括 `文本消息`、`图片消息`， `群名片消息`、`富文本消息`、`卡片消息`；
同时该插件还提供了`自定义模板`和`变量`的功能，使您能够根据自己的需求来定制通知消息的内容和格式。

## 📝 使用文档

> 请参考 [官方文档](https://721806280.github.io/lark-notice-plugin-doc)

## 🧑‍💻 开发服务

在 `IDEA` 右侧 `maven` 控制面板中添加 `hpi:run` 到启动配置：

### 远程调试(Remote JVM DEBUG)

#### 1. 命令行模式启动

> 1. `set MAVEN_OPTS=-Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=5005,suspend=n`
> 2. `mvn hpi:run -Djetty.port=8080`

#### 2. IDEA启动

> 1. 打开 `idea` 中 `maven` 面板，`Plugins` 下的 `hpi` 插件
> 2. `hpi:run` 右键选择 `Modify Run Configuration..` 设置
> 3. 点击 `Modify Options` 后选择 `Add VM Options` 选项
> 4. 在 `VM options` 处输入: `-Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=5005,suspend=n`
> 5. 点击运行按钮开始执行 `maven` 命令(勿使用`Debug`模式运行)

#### 3. Remote JVM DEBUG

> 1. 打开 `Run/Debug Configurations` 并点击 `Add New Configuration` 后选择 `Remote JVM DEBUG`
> 2. 配置端口后 `Run` 运行调试
> 3. 启动后控制台打印信息: `Listening for transport dt_socket at address: 5005`

## ✅ 开发约定

1. 使用 [Alibaba Java Coding Guidelines](https://plugins.jetbrains.com/plugin/10046-alibaba-java-coding-guidelines/)
   校验代码规范。
2. 使用 [Google Style Guide](https://github.com/google/styleguide) 统一代码风格。

> `IDEA`
>
下载 [intellij-java-google-style.xml](https://github.com/google/styleguide/blob/gh-pages/intellij-java-google-style.xml)
> 通过 `Settings` -> `Editor` `Code Style` 进行导入。

## 🔍️ 相关链接

- [Plugin tutorial](https://wiki.jenkins.io/display/JENKINS/Plugin+tutorial#Plugintutorial-SettingUpEnvironment)
- [Jenkins 插件开发之旅：两天内从 idea 到发布(上篇)](https://jenkins-zh.cn/wechat/articles/2019/05/2019-05-06-jenkins-plugin-develop-within-two-days-part01/)
- [Jenkins 插件开发之旅：两天内从 idea 到发布(下篇)](https://jenkins-zh.github.io/wechat/articles/2019/05/2019-05-08-jenkins-plugin-develop-within-two-days-part02/)

## 💚 鸣谢

感谢 JetBrains 提供的免费开源 License

[![JetBrains](docs/img/jetbrains.png)](https://www.jetbrains.com/?from=lark-notice-plugin)