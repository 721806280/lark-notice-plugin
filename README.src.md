<!--@nrg.languages=en,zh-->
<!--@nrg.defaultLanguage=en-->

<div align="center">
   <p align="center">
      <a href="https://721806280.github.io/lark-notice-plugin-doc">
          <img src="src/main/webapp/images/logo.png" alt="logo">
      </a>
   </p>
   <p align="center">
      <img src="https://img.shields.io/badge/JDK-17-success" alt="JDK">
      <img src="https://img.shields.io/badge/Jenkins-2.528.3-blue.svg" alt="License">
      <img src="https://img.shields.io/badge/License-MIT-blue.svg" alt="License">
      <img src="https://img.shields.io/badge/Author-xm.z-success" alt="Author">
   </p>

   <div align="center">
      <a href="https://721806280.github.io/lark-notice-plugin-doc">
        <img src="https://readme-typing-svg.demolab.com?font=Fira+Code&weight=600&size=26&pause=1000&multiline=true&repeat=false&random=true&width=665&height=46&lines=Lark+-+Jenkins+builds+notification+plugins" alt="Typing SVG" /><!--en-->
         <img src="https://readme-typing-svg.demolab.com?font=Fira+Code&weight=600&size=26&pause=1000&multiline=true&repeat=false&random=true&width=435&height=46&lines=Lark+-+%E4%BA%91%E9%9B%80Jenkins%E6%9E%84%E5%BB%BA%E9%80%9A%E7%9F%A5%E6%8F%92%E4%BB%B6" alt="Typing SVG" /><!--zh-->
      </a>
   </div>
</div>

## 🌐 Plugin Introduction<!--en-->
## 🌐 插件简介<!--zh-->

`lark-notice-plugin` is a `build notification robot` notification plug-in for `Jenkins`, which can push the `Jenkins`<!--en-->
build process and result notifications to the `Lark`、`Feishu` and `DingTalk` collaboration platforms.<!--en-->
Multiple notification timings can be configured,<!--en-->
including `when the build starts`, `build interruption`, `build failure`, `when the build succeeds`,<!--en-->
`build instability`,<!--en-->
etc.<!--en-->
Supports many different types of messages,<!--en-->
including `text messages`, `picture messages`, `group business card messages`, `rich text messages`, `card messages`;<!--en-->
At the same time, the plug-in also provides the functions of `custom template` and `variables', allowing you to<!--en-->
customize the content and format of notification messages according to your own needs.<!--en-->
`lark-notice-plugin` 是一个用于  `Jenkins` 的 `构建通知机器人` 通知插件，可以将 `Jenkins`<!--zh-->
构建过程以及结果通知推送到 `Lark`、`飞书`、`钉钉` 协作平台。<!--zh-->
可配置多个的通知时机，包括 `构建启动时`、`构建中断`、`构建失败`、`构建成功时`、`构建不稳定`等。<!--zh-->
支持多种不同类型的消息，包括 `文本消息`、`图片消息`， `群名片消息`、`富文本消息`、`卡片消息`；<!--zh-->
同时该插件还提供了`自定义模板`和`变量`的功能，使您能够根据自己的需求来定制通知消息的内容和格式。<!--zh-->

## 📝 Using documentation<!--en-->
## 📝 使用文档<!--zh-->

> Please refer to [official document](https://721806280.github.io/lark-notice-plugin-doc)<!--en-->
> 请参考 [官方文档](https://721806280.github.io/lark-notice-plugin-doc)<!--zh-->

## 🔁 Retry Configuration<!--en-->
## 🔁 重试配置<!--zh-->

The plugin can retry failed webhook sends. Retries are disabled by default.<!--en-->
插件支持对发送失败的 Webhook 进行重试，默认不开启。<!--zh-->

- `enabled`: whether to enable retry<!--en-->
- `maxAttempts`: total attempts including the first one<!--en-->
- `initialDelayMs`: initial delay before the first retry<!--en-->
- `maxDelayMs`: maximum delay between retries<!--en-->
- `backoffMultiplier`: exponential backoff multiplier<!--en-->
- `jitterRatio`: random jitter ratio applied to delays<!--en-->
- `enabled`：是否启用重试<!--zh-->
- `maxAttempts`：最大尝试次数（包含第一次）<!--zh-->
- `initialDelayMs`：首次重试前的等待时间<!--zh-->
- `maxDelayMs`：重试等待时间上限<!--zh-->
- `backoffMultiplier`：退避倍数<!--zh-->
- `jitterRatio`：等待时间抖动比例<!--zh-->

Default values: `enabled=false`, `maxAttempts=1`, `initialDelayMs=500`, `maxDelayMs=5000`,<!--en-->
`backoffMultiplier=2.0`, `jitterRatio=0.2`.<!--en-->
默认值：`enabled=false`、`maxAttempts=1`、`initialDelayMs=500`、`maxDelayMs=5000`、<!--zh-->
`backoffMultiplier=2.0`、`jitterRatio=0.2`。<!--zh-->

Retry settings are configured per robot under robot settings.<!--en-->
重试设置在每个机器人配置中单独设置。<!--zh-->

## 🧑‍💻 Development Services<!--en-->
## 🧑‍💻 开发服务<!--zh-->

### Test commands<!--en-->
### 测试命令<!--zh-->

- Fast test suite: `mvn test`<!--en-->
- Full suite including JenkinsRule page and integration tests: `mvn test failsafe:integration-test failsafe:verify -Pjenkins-rule-tests`<!--en-->
- 快速测试集: `mvn test`<!--zh-->
- 包含 JenkinsRule 页面与集成测试的完整测试集: `mvn test failsafe:integration-test failsafe:verify -Pjenkins-rule-tests`<!--zh-->

Add `hpi:run` to the startup configuration in the `maven` control panel on the right side of `IDEA`:<!--en-->
在 `IDEA` 右侧 `maven` 控制面板中添加 `hpi:run` 到启动配置：<!--zh-->

### Remote debugging (Remote JVM DEBUG)<!--en-->
### 远程调试(Remote JVM DEBUG)<!--zh-->

#### 1. Command line mode start<!--en-->
#### 1. 命令行模式启动<!--zh-->

> 1. `set MAVEN_OPTS=-Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=5005,suspend=n`
> 2. `mvn hpi:run -Djetty.port=8080`

#### 2. IDEA starts<!--en-->
#### 2. IDEA启动<!--zh-->

> 1. Open the `maven` panel in `idea`, the `hpi` plugin under `Plugins`<!--en-->
> 2. Right click on `hpi:run` and select `Modify Run Configuration..` to set<!--en-->
> 3. Click `Modify Options` and select `Add VM Options` option<!--en-->
> 4. Input at `VM options`: `-Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=5005,suspend=n`<!--en-->
> 5. Click the Run button to start executing the `maven` command (do not run in `Debug` mode)<!--en-->
> 1. 打开 `idea` 中 `maven` 面板，`Plugins` 下的 `hpi` 插件<!--zh-->
> 2. `hpi:run` 右键选择 `Modify Run Configuration..` 设置<!--zh-->
> 3. 点击 `Modify Options` 后选择 `Add VM Options` 选项<!--zh-->
> 4. 在 `VM options` 处输入: `-Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=5005,suspend=n`<!--zh-->
> 5. 点击运行按钮开始执行 `maven` 命令(勿使用`Debug`模式运行)<!--zh-->

#### 3. Remote JVM DEBUG

> 1. Open `Run/Debug Configurations` and click `Add New Configuration` and select `Remote JVM DEBUG`<!--en-->
> 2. After configuring the port, `Run` runs the debugger<!--en-->
> 3. The console prints information after startup: `Listening for transport dt_socket at address: 5005`<!--en-->
> 1. 打开 `Run/Debug Configurations` 并点击 `Add New Configuration` 后选择 `Remote JVM DEBUG`<!--zh-->
> 2. 配置端口后 `Run` 运行调试<!--zh-->
> 3. 启动后控制台打印信息: `Listening for transport dt_socket at address: 5005`<!--zh-->

## ✅ Development Agreement<!--en-->
## ✅ 开发约定<!--zh-->

1. Use [Alibaba Java Coding Guidelines](https://plugins.jetbrains.com/plugin/10046-alibaba-java-coding-guidelines/)<!--en-->
   Verify code specifications.<!--en-->
2. Use the [Google Style Guide](https://github.com/google/styleguide) to unify the code style.<!--en-->
1. 使用 [Alibaba Java Coding Guidelines](https://plugins.jetbrains.com/plugin/10046-alibaba-java-coding-guidelines/)<!--zh-->
   校验代码规范。<!--zh-->
2. 使用 [Google Style Guide](https://github.com/google/styleguide) 统一代码风格。<!--zh-->

> `IDEA`
>
download [intellij-java-google-style.xml](https://github.com/google/styleguide/blob/gh-pages/intellij-java-google-style.xml)<!--en-->
> Import via `Settings` -> `Editor` `Code Style`.<!--en-->
下载 [intellij-java-google-style.xml](https://github.com/google/styleguide/blob/gh-pages/intellij-java-google-style.xml)<!--zh-->
> 通过 `Settings` -> `Editor` `Code Style` 进行导入。<!--zh-->

## 🔍️ Related Links<!--en-->
## 🔍️ 相关链接<!--zh-->

- [Plugin tutorial](https://wiki.jenkins.io/display/JENKINS/Plugin+tutorial#Plugintutorial-SettingUpEnvironment)
- [Jenkins plug-in development journey: from idea to release in two days (Part 1)](https://jenkins-zh.cn/wechat/articles/2019/05/2019-05-06-jenkins-plugin-develop-within-two-days-part01/)<!--en-->
- [Jenkins plug-in development journey: from idea to release in two days (Part 2)](https://jenkins-zh.github.io/wechat/articles/2019/05/2019-05-08-jenkins-plugin-develop-within-two-days-part02/)<!--en-->
- [Jenkins 插件开发之旅：两天内从 idea 到发布(上篇)](https://jenkins-zh.cn/wechat/articles/2019/05/2019-05-06-jenkins-plugin-develop-within-two-days-part01/)<!--zh-->
- [Jenkins 插件开发之旅：两天内从 idea 到发布(下篇)](https://jenkins-zh.github.io/wechat/articles/2019/05/2019-05-08-jenkins-plugin-develop-within-two-days-part02/)<!--zh-->

## ⭐ Star History<!--en-->
## ⭐ 星标历史<!--zh-->

[![Star History Chart](https://api.star-history.com/svg?repos=721806280/lark-notice-plugin&type=Date)](https://www.star-history.com/#721806280/lark-notice-plugin&Date)
