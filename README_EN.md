## 🌐 Plugin Introduction

`feishu-notification-plugin` is
a [FeiShu robot notification](https://open.feishu.cn/document/ukTMukTMukTM/ucTM5YjL3ETO24yNxkjN) plugin for `Jenkins`,
which can push the `Jenkins` build process and result notifications to the `Feishu` collaboration platform.
Multiple notification timings can be configured,
including `build startup`, `build interruption`, `build failure`, `build success`, `build unstable`, etc.
Support many different types of messages,
including `text message`, `picture message`, `group business card message`, `rich text message`, `card message`;
At the same time, the plug-in also provides the functions of `custom template` and `variable`, enabling you to customize
the content and format of notification messages according to your needs.

## 📝 Using documentation

> Please refer to [official document](https://721806280.github.io/feishu-notification-plugin-doc)

#### TEXT message

```
pipeline {
     agent any
     stages {
         stage('text'){
             steps {
                 echo 'Send text message...'
             }
             post {
                 success {
                     feiShuTalk (
                         robot: 'f72aa1bb-0f0b-47c7-8387-272d266dc25c',
                         type: 'TEXT',
                         text: [
                             "New Update Reminder",
                             '<at user_id="all">everyone</at>'
                         ]
                     )
                 }
             }
         }
     }
}
```

#### Group business card message

```
pipeline {
     agent any
     stages {
         stage('text'){
             steps {
                 echo 'Send group business card message...'
             }
             post {
                 success {
                     feiShuTalk (
                         robot: 'f72aa1bb-0f0b-47c7-8387-272d266dc25c',
                         type: 'SHARE_CHAT',
                         shareChatId: 'oc_f5b1a7eb27ae2c7b6adc2a74faf339ff'
                     )
                 }
             }
         }
     }
}
```

#### Picture Message

```
pipeline {
     agent any
     stages {
         stage('text'){
             steps {
                 echo 'Send picture message...'
             }
             post {
                 success {
                     feiShuTalk (
                         robot: 'f72aa1bb-0f0b-47c7-8387-272d266dc25c',
                         type: 'IMAGE',
                         imageKey: 'img_ecffc3b9-8f14-400f-a014-05eca1a4310g'
                     )
                 }
             }
         }
     }
}
```

#### Rich text messages

```
pipeline {
     agent any
     stages {
         stage('text'){
             steps {
                 echo 'Send rich text message...'
             }
             post {
                 success {
                     feiShuTalk (
                         robot: 'f72aa1bb-0f0b-47c7-8387-272d266dc25c',
                         type: 'POST',
                         title: 'Project update notification',
                         post: [
                             [
                                 [
                                     "tag": "text",
                                     "text": "Item has been updated: "
                                 ],
                                 [
                                     "tag": "a",
                                     "text": "Please check",
                                     "href": "https://www.example.com/"
                                 ],
                                 [
                                     "tag": "at",
                                     "user_id": "ou_xxxxxx",
                                     "user_name": "tom"
                                 ]
                             ],
                             [
                                 [
                                     "tag": "text",
                                     "text": "Item has been updated:"
                                 ],
                                 [
                                     "tag": "at",
                                     "user_id": "all",
                                     "user_name": "Everyone"
                                 ]
                             ]
                         ]
                     )
                 }
             }
         }
     }
}

```

#### Card message

> 1. The value range of the button color `type`: primary | danger | default
> 2. Value range of font color `color`: green: green text | red: red text | grey: gray text | default: black text on
     white background

```
pipeline {
     agent any
     stages {
         stage('text'){
             steps {
                 echo "Send card message..."
             }
             post {
                 success {
                     feiShuTalk (
                         robot: "f72aa1bb-0f0b-47c7-8387-272d266dc25c",
                         type: "INTERACTIVE",
                         title: "📢 Jenkins Build Notifications",
                         text: [
                             "📋 **JOB NAME**: [${JOB_NAME}](${JOB_URL})",
                             "🔢 **Task ID**: [${BUILD_DISPLAY_NAME}](${BUILD_URL})",
                             "🌟 **Build Status**: <font color='green'>Success</font>",
                             "🕐 **Build Duration**: ${currentBuild.duration} ms",
                             "👤 **Performer**: Started by user anonymous",
                             "<at id=all></at>"
                         ],
                         buttons: [
                            [
                               title: "Change Record",
                               url: "${BUILD_URL}changes"
                            ],
                            [
                               title: "Console",
                               type: "danger",
                               url: "${BUILD_URL}console"
                            ]
                         ]
                     )
                 }
             }
         }
     }
}

```

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