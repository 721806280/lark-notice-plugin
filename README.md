## 🌐 系统说明

- **飞书 Jenkins 插件**
- **飞书 Jenkins 插件**
- **飞书 Jenkins 插件**

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

> `IDEA`下载 [intellij-java-google-style.xml](https://github.com/google/styleguide/blob/gh-pages/intellij-java-google-style.xml)
>  通过 `Settings` -> `Editor` `Code Style` 进行导入。

## 📝 使用文档

> 请参考 [官方文档](https://721806280.github.io/feishu-talk-plugin-doc)

## 🔍️ 相关链接

- [Plugin tutorial](https://wiki.jenkins.io/display/JENKINS/Plugin+tutorial#Plugintutorial-SettingUpEnvironment)
- [Jenkins 插件开发之旅：两天内从 idea 到发布(上篇)](https://jenkins-zh.cn/wechat/articles/2019/05/2019-05-06-jenkins-plugin-develop-within-two-days-part01/)
- [Jenkins 插件开发之旅：两天内从 idea 到发布(下篇)](https://jenkins-zh.github.io/wechat/articles/2019/05/2019-05-08-jenkins-plugin-develop-within-two-days-part02/)

## 🍻 开源推荐

- `钉钉插件`：[https://github.com/jenkinsci/dingtalk-plugin](https://github.com/jenkinsci/dingtalk-plugin)

## 💚 鸣谢

感谢 JetBrains 提供的免费开源 License

[![JetBrains](docs/img/jetbrains.png)](https://www.jetbrains.com/?from=feishu-talk-plugin)