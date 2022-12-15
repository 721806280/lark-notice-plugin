## 系统说明

- **飞书 Jenkins 插件**
- **飞书 Jenkins 插件**
- **飞书 Jenkins 插件**

## 开发服务

在 `IDEA` 右侧 `maven` 控制面板中添加 `hpi:run` 到启动配置：

## 远程调试(Remote JVM DEBUG)

```
set MAVEN_OPTS=-Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=5005,suspend=n
mvn hpi:run -Djetty.port=8080

# IDEA 新建 Remote JVM DEBUG 并配置端口后启动进行调试
```

## 开发约定

1. 使用 [Alibaba Java Coding Guidelines](https://plugins.jetbrains.com/plugin/10046-alibaba-java-coding-guidelines/)
   校验代码规范。
2. 使用 [Google Style Guide](https://github.com/google/styleguide) 统一代码风格。

> `IDEA`
> 下可以下载 [intellij-java-google-style.xml](https://github.com/google/styleguide/blob/gh-pages/intellij-java-google-style.xml)
> ，然后在 `Settings` -> `Editor` `Code Style` 进行导入。

## 使用示例

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

> 1. 按钮颜色 `type` 的取值范围:  primary | danger | default

```
pipeline {
    agent any
    stages {
        stage('text'){
            steps {
                echo '发送卡片消息...'
            }
            post {
                success {
                    feiShuTalk (
                        robot: 'f72aa1bb-0f0b-47c7-8387-272d266dc25c',
                        type: 'INTERACTIVE',
                        title: '📢 Jenkins 构建通知',
                        text: [
                            '📋 **任务名称**：[demo](http://127.0.0.1:8080/jenkins/job/demo/)',
                            '🔢 **任务编号**：[#9](http://127.0.0.1:8080/jenkins/job/demo/9/)',
                            '🌟 **构建状态**: <font color="green">成功</font>',
                            '🕐 **构建用时**:  2 ms and counting',
                            '👤 **执  行 者**:  Started by user anonymous',
                            '<at id=all></at>'
                        ],
                        buttons: [
                           [
                              title: '更改记录',
                              actionUrl: 'http://127.0.0.1:8080/jenkins/job/pipeline/1/changes'
                           ],
                           [
                              title: '控制台',
                              type: 'danger',
                              actionUrl: 'http://127.0.0.1:8080/jenkins/job/pipeline/1/console'
                           ]
                        ]
                    )
                }
            }
        }
    }
}

```

## 参考文档

1. [Plugin tutorial](https://wiki.jenkins.io/display/JENKINS/Plugin+tutorial#Plugintutorial-SettingUpEnvironment)
2. [Jenkins 插件开发之旅：两天内从 idea 到发布(上篇)](https://jenkins-zh.cn/wechat/articles/2019/05/2019-05-06-jenkins-plugin-develop-within-two-days-part01/)
3. [Jenkins 插件开发之旅：两天内从 idea 到发布(下篇)](https://jenkins-zh.github.io/wechat/articles/2019/05/2019-05-08-jenkins-plugin-develop-within-two-days-part02/)

## 参考项目

1. [钉钉插件](https://github.com/jenkinsci/dingtalk-plugin)

---
