
[English](./README_EN.md) | 简体中文

## ✨ 特性

- 🐱‍‍  为自动化测试而生
- 🎨  基于SpringBoot,Groovy,json定义输入,json定义输出,良好的系统设计
- 🌈  支持各种单元测试(Http,数据库及其它自定义测试)
- ⚙️ 高效的组织运行管理集成测试
- 📦️ 多种方式校验输出(JsonSchema,Groovy脚本)
- 🛡️ 高效的用例库管理
- 🐱‍🚀️ 多环境配置管理
- 🐱‍🏍️ 0中间件,不需要数据库,不需要redis等
- 🌹️ 可交互的测试报告
- 🎉️ 插值表达式模拟输入字段
- 🌍️ 国际化设计，支持中英两种语言


## 🙌 项目背景

我是一名`Java`后端程序员，我的开发习惯是写好了一个`HTTP`请求的接口后，在`PostMan`里运行一下。然后在`IDE`进行拦截`debug`。 到这一步`PostMan`非常优秀好用。

但当我积累了很多`HTTP API`之后，它们被孤零零的扔在`PostMan`的菜单树中，如果这些接口不出问题的话，可能它们的使用是一次性的， 所以我在思考能不能把这些接口创建更过的可用场景，因为只有可用的场景多了，它们才会有维护的价值。

我最直观最迫切的需求是，能不能把这些接口作为材料，来模拟复现我们人的操作，因为我们人的操作本质上就是调用这些接口，
但是接口在调用时具有一定的相关性（比如创建一条数据获取id，再根据这个id去进行更新）。后来我了解到，这叫自动化测试。

`PostMan`在自动化测试上当然有所探索，`PostMan`有个集合的功能，但这个功能仅仅是把一堆接口拉到一个集合中一起跑几轮
（而且是单线程二层for循环的跑）。 根本就没有我上面说的接口关联性的设计。

再谈一下`PostMan`的商业化，`PostMan`的商业化主要是在`API`协作设计，我理解的`API`设计主要是在开发前期，
产品，前端，后端，测试一起约定好一个接口的请求内容，响应内容，接口的各种情况。所以`PostMan`会有文档的功能,会有`Mock`的功能。
所以在他们的商业设计中，一个接口按照计划开发完成之后，可再利用的程度本来就没有那么高，这也是为什么他们不支持性能测试的原因。

如果能把上面搞出来的`API`构建成一个自动化测试用例，每次`jenkins`发布代码或者`VCS`合并代码时，
都将配置的自动化测试用例跑一遍，来发现代码变更或者产品软件发布对旧业务有无影响,这对自动化测试来说是有巨大效率提升的。
所以我认为`API`自动化测试应该是`PostMan`是`API`生命周期管理的后半部分的重中之重,但是`PostMan`做的并不好。

我认为自动化测试应该满足两个场景
- 像`PostMan`的集合功能那样，把没有关联性的一堆接口拉到一起跑一遍(从效率的角度来讲，应该采用多线程，因为没有相关性不需要顺序)。
- 应该能够进行接口关联性的设计（b接口依赖a接口返回的某个字段），使用接口来复现人的操作。
- 应该能够和`CI/CD`集成。

`PostMan`作为一家商业公司，如果实现上面的需求会有劣势，很多企业，本身让一家商业公司掌握自己的接口列表就已经有点困难，
再让他们掌握我各个接口之间的调用逻辑，恐怕是一件很难接受的事情。

基于以上考虑,我独自启动了`Gable`这个开源项目，我希望它能：
- 能吸收PostMan的优点,高效的为开发人员测试`HTTP`等接口
- 能满足我上面的需求,高效率低成本的构建自动化测试,性能测试
- 探索除`Http`之外的其它单元测试
- 效率第一,成本第二
- 开源

所以，如果你认可上面我描述的文字，可以考虑加入`Gable`这个开源事业。

## 📦 安装

```bash
git clone https://github.com/AdvancedProductivity/GableServer.git
```

## 🍠 运行

```bash
mvn spring-boot:run
```

浏览器打开: `http://localhost:2110/`

## 用户操作指南

文档还在不断优化，可以先看一下

wiki文档请查看 [这里](https://github.com/AdvancedProductivity/GableServer/wiki)

如果访问github有困难，[这里](https://www.yuque.com/zhaoziqiang/wlnb68) 还有一份语雀的文档，可以点击查看

## 前端项目地址

请查看 [这里](https://github.com/AdvancedProductivity/GableClient)

## UI预览 【一览为快】

####   

![单元测试界面](imgForPreivew/UnitTest.png)

<p align="center">单元测试</p>

![单元测试界面](imgForPreivew/CaseManage.png)

<p align="center">用例管理</p>

![单元测试界面](imgForPreivew/CaseDiff.png)

<p align="center">用例差异</p>

![单元测试界面](imgForPreivew/IntegrateTest.png)

<p align="center">集成测试</p>

![单元测试界面](imgForPreivew/IntegrateHistory.png)

<p align="center">集成测试历史</p>

![单元测试界面](imgForPreivew/TestReport.png)

<p align="center">测试报告</p>

## ⌨️ 项目架构

为了帮助感兴趣的人快速掌握关键信息，写下这一节。

该项目是一个前后端分离的项目，既可以做为CS架构，也可以作为BS架构。

该项目的后端就是一个普通的简单的`SpringBoot`的项目,没有使用数据库(一切持久化基于文件的读写)， 没有使用`redis`这样的中间件(内存中维持了一个`Guava`的`Cache`做缓存)
,项目的`resource/static`
目录下存放了前端打包后的文件，所以可以后端的jar文件在部署后可以直接通过`http://localhost:2110/` 的形式访问

该项目的前端是普通的`Angular`项目,该项目基于模板[angular-electron](https://github.com/maximegris/angular-electron)
启动，同时使用了 `AntDesign` 的UI库.所以该项目既可以构建出资源文件作为Web网页来访问，也可以打包成`Electron`客户端
(`Electron`客户端还有很多bug😭)。

从业务来讲,先有单元测试，如果把多个单元测试串起来，就是一个集成测试。

单元测试分为两种，一种是`HTTP`类型的测试,一类是`GroovyScript`的脚本测试.

对于`HTTP`类型单元测试。在前端定义好了`HTTP`的输入`json`之后,点击运行按钮，会把`json`发送到`server`端,
`server`端解析输入`json`,构建出一个 `OkHttpRequest`然后由 `OkHttpClient` 发出`HTTP`请求,
拿到`Resonse`之后，再将其封装为`json`返回给前端界面，前端界面再进行渲染。

对于`GroovyScript`类型单元测试。在前端定义好了`GroovyScript`的输入`json`之后,以及要执行的`Groovy`脚本代码之后,
将其发送到`server`端,`server`端维持着一个`GroovyScriptEngine`,`server`端会先将前端传来的代码写入到文件，
然后把输入`json`参数写入到一个`Binding`对象中供`GroovyScript`自行调用，`Binding`对象中还会有一个名为`out`的json对象，
供`GroovyScript`自行将输出写入到`out`中，理论上，`GroovyScript`类型的测试可以做任何你想做的事。

总之，使用 `json` 来描述单元测试的输入和输出。因为输出是一个 `json` 对象，
所以可以使用`JsonSchema`来对其进行校验。
