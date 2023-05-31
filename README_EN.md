## 🌐 Plug-in introduction

`feishu-notification-plugin` is a `Jenkins` plugin for pushing `Jenkins` build notifications to the `Feishu` platform.
It supports many different types of messages, including `build started`, `build completed`, `build failed`, `build log`,
etc.
At the same time, the plug-in also provides the functions of `custom template` and `variable`, enabling you to customize
the content and format of notification messages according to your needs.

## 🧑‍💻 Development Services

Add `hpi:run` to the startup configuration in the `maven` control panel on the right side of `IDEA`:

### Remote debugging (Remote JVM DEBUG)

#### 1. Command line mode start

> 1. `set MAVEN_OPTS=-Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=5005,suspend=n`
> 2. `mvn hpi:run -Djetty.port=8080`

#### 2. IDEA starts

> 1. Open the `maven` panel in `idea`, the `hpi` plugin under `Plugins`
> 2. Right click on `hpi:run` and select `Modify Run Configuration..` to set
> 3. Click `Modify Options` and select `Add VM Options` option
> 4. Input at `VM options`: `-Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=5005,suspend=n`
> 5. Click the Run button to start executing the `maven` command (do not run in `Debug` mode)

#### 3. Remote JVM DEBUG

> 1. Open `Run/Debug Configurations` and click `Add New Configuration` and select `Remote JVM DEBUG`
> 2. After configuring the port, `Run` runs the debugger
> 3. The console prints information after startup: `Listening for transport dt_socket at address: 5005`

## ✅ Development Agreement

1. Use [Alibaba Java Coding Guidelines](https://plugins.jetbrains.com/plugin/10046-alibaba-java-coding-guidelines/)
   Verify code specifications.
2. Use the [Google Style Guide](https://github.com/google/styleguide) to unify the code style.

> `IDEA`
> download [intellij-java-google-style.xml](https://github.com/google/styleguide/blob/gh-pages/intellij-java-google-style.xml)
> Import via `Settings` -> `Editor` `Code Style`.

## 📝 Using documentation

> Please refer to [official document](https://721806280.github.io/feishu-notification-plugin)

## 🔍️ Related Links

- [Plugin tutorial](https://wiki.jenkins.io/display/JENKINS/Plugin+tutorial#Plugintutorial-SettingUpEnvironment)
- [Jenkins plug-in development journey: from idea to release in two days (Part 1)](https://jenkins-zh.cn/wechat/articles/2019/05/2019-05-06-jenkins-plugin-develop-
  within-two-days-part01/)
- [Jenkins plug-in development journey: from idea to release in two days (Part 2)](https://jenkins-zh.github.io/wechat/articles/2019/05/2019-05-08-jenkins-plugin-
  develop-within-two-days-part02/)

## 🍻 Open source recommendation

- `Dingtalk Plugin`: [https://github.com/jenkinsci/dingtalk-plugin](https://github.com/jenkinsci/dingtalk-plugin)

## 💚 Thanks

Thanks to JetBrains for the free open source license

[![JetBrains](docs/img/jetbrains.png)](https://www.jetbrains.com/?from=feishu-notification-plugin)