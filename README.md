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

#### Freestyle

> 请参考 [6.0 Jenkins 飞书通知](https://blog.csdn.net/qq_38765404/article/details/123497710/)

#### TEXT 消息

```
pipeline {
    agent any
    stages {
        stage('text'){
            steps {
                echo '发送文本消息...'
            }
            post {
                success {
                    feiShuTalk (
                        robot: 'f72aa1bb-0f0b-47c7-8387-272d266dc25c',
                        type: 'TEXT',
                        text: [
                            "新更新提醒",
                            '<at user_id="all">所有人</at>'
                        ]
                    )
                }
            }
        }
    }
}
```

#### 群名片消息

```
pipeline {
    agent any
    stages {
        stage('text'){
            steps {
                echo '发送群名片消息...'
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

#### 图片消息

```
pipeline {
    agent any
    stages {
        stage('text'){
            steps {
                echo '发送图片消息...'
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

#### 富文本消息

```
pipeline {
    agent any
    stages {
        stage('text'){
            steps {
                echo '发送富文本消息...'
            }
            post {
                success {
                    feiShuTalk (
                        robot: 'f72aa1bb-0f0b-47c7-8387-272d266dc25c',
                        type: 'POST',
                        title: '项目更新通知',
                        post: [
                            [
                                [
                                    "tag": "text",
                                    "text": "项目有更新: "
                                ],
                                [
                                    "tag": "a",
                                    "text": "请查看",
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
                                    "text": "项目有更新:"
                                ],
                                [
                                    "tag": "at",
                                    "user_id": "all",
                                    "user_name": "所有人"
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

#### 卡片消息

> 1. 按钮颜色 `type` 的取值范围： primary | danger | default
> 2. 字体颜色 `color` 的取值范围： green：绿色文本 | red：红色文本 | grey：灰色文本 | default：白底黑字样式

```
pipeline {
    agent any
    stages {
        stage('text'){
            steps {
                echo "发送卡片消息..."
            }
            post {
                success {
                    feiShuTalk (
                        robot: "f72aa1bb-0f0b-47c7-8387-272d266dc25c",
                        type: "INTERACTIVE",
                        title: "📢 Jenkins 构建通知",
                        text: [
                            "📋 **任务名称**：[${JOB_NAME}](${JOB_URL})",
                            "🔢 **任务编号**：[${BUILD_DISPLAY_NAME}](${BUILD_URL})",
                            "🌟 **构建状态**: <font color='green'>成功</font>",
                            "🕐 **构建用时**: ${currentBuild.duration} ms",
                            "👤 **执  行 者**: Started by user anonymous",
                            "<at id=all></at>"
                        ],
                        buttons: [
                           [
                              title: "更改记录",
                              url: "${BUILD_URL}changes"
                           ],
                           [
                              title: "控制台",
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

## 🔍️ 相关链接

- [Plugin tutorial](https://wiki.jenkins.io/display/JENKINS/Plugin+tutorial#Plugintutorial-SettingUpEnvironment)
- [Jenkins 插件开发之旅：两天内从 idea 到发布(上篇)](https://jenkins-zh.cn/wechat/articles/2019/05/2019-05-06-jenkins-plugin-develop-within-two-days-part01/)
- [Jenkins 插件开发之旅：两天内从 idea 到发布(下篇)](https://jenkins-zh.github.io/wechat/articles/2019/05/2019-05-08-jenkins-plugin-develop-within-two-days-part02/)

## 🍻 开源推荐

- `钉钉插件`：[https://github.com/jenkinsci/dingtalk-plugin](https://github.com/jenkinsci/dingtalk-plugin)

## 💚 鸣谢

感谢 JetBrains 提供的免费开源 License：

[![JetBrains](docs/img/jetbrains.png)](https://www.jetbrains.com/?from=feishu-talk-plugin)