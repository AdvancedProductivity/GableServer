
English  |  [简体中文](./README.md)

## ✨ 特性

- 🐱‍‍  Born for automated testing
- 🎨  Based on springboot, groovy, JSON defines input and JSON defines output
- 🌈  Support various unit tests (HTTP, database and other Custom  tests)
- ⚙️ Efficient organization operation management integration test
- 📦️  Multiple ways to verify output (JsonSchema, Groovy Script)
- 🛡️  Efficient Case Library management
- 🐱‍🚀️   Multi environment configuration management
- 🐱‍🏍️  0 middleware, no database, no redis, etc
- 🌹️  Interactive test report
- 🎉️  Interpolation expressions mock input fields
- 🌍️  International design, supporting Chinese and English languages

## As advertising: the author is looking for a partner -)
Until now, I am the only one in the design, development and maintenance of this project. I am eager to have others involved (especially front-end Engineers).
If you are interested, please email me.

## 🙌 Project Background

I am a `Java` back-end programmer. My development habit is to write an `HTTP` request interface and run it in `PostMan`.
Then intercept and debug in the `IDE`. At this point, `PostMan` is excellent and easy to use.

However, after I have accumulated a lot of `HTTP API`, they are left alone in the menu tree of `PostMan`.
So I'm thinking about whether I can create more usable scenarios for these interfaces,
because they will be more valuable to maintain only when there are more usable scenarios.

My most intuitive and urgent need is whether we can use these interfaces as materials to simulate and reproduce
our human operations, because our human operations essentially call these interfaces,
However, the interface has a certain correlation when calling 
(for example, create a data to obtain the ID, and then update it according to the ID). 
Later I learned that this is called automated testing.

`PostMan`在自动化测试上当然有所探索，`PostMan`有个集合的功能，但这个功能仅仅是把一堆接口拉到一个集合中一起跑几轮
（而且是单线程二层for循环的跑）。 根本就没有我上面说的接口关联性的设计。
Of course, `PostMan` has explored automated testing. 
Postman has a collection function, but this function only pulls a bunch of interfaces into a 
collection to run several rounds together
(and it's a single threaded two-tier for loop). 
There is no interface correlation design I mentioned above.


If we look at the commercialization of `PostMan`, 
the commercialization of `PostMan` is mainly in `API` collaborative design, 
and I understand that the `API` design is mainly in the early stage of development,
The product, Front-End, Back-End and Test agree on the request content, response content and various conditions of an interface.
Therefore, `PostMan` will have the function of document and `Mock`.
Therefore, in their commercial design, after an `API` is developed as planned, 
the degree of reuse is not so high, which is why they do not support performance testing.

If we can build the above `API` into an automated test case, 
every time "Jenkins" releases code or "VCS" merges code,
Run through the configured automated test cases to find out whether the code change or product software release has an impact on the old code,
which has greatly improved the efficiency of automated testing.
Therefore, I think `API` automated testing should be the top priority of `PostMan` in the second half of `API` life cycle management, 
but `PostMan` does not do well.

I think automated testing should meet two scenarios
- Like the collection function of `PostMan`, pull a bunch of interfaces without relevance together and run them again (from the perspective of efficiency, multithreading should be adopted because there is no relevance and no order is required).
- It should be able to design the interface relevance (the B interface depends on a field returned by the a interface) and use the interface to reproduce human operations.
- It should be able to integrate with `CI/CD`.

`Postman` as a commercial company, there will be disadvantages if it realizes the above requirements. 
Many enterprises have a little difficulty in letting a commercial company master its own interface list,
I'm afraid it's hard for them to master the calling logic between my interfaces.


Based on the above considerations, I started the `Gable` open source project alone. I hope it can:
- It can absorb the advantages of postman and efficiently test `HTTP` and other interfaces for developers
- It can meet my above needs, build automated testing and performance testing with high efficiency and low cost.
- Explore unit tests other than `Http`.
- Efficiency first, Cost second
- Open Source

Therefore, if you agree with the words I described above, you can consider joining the open source cause of `Gable`.

## 📦 Install

```bash
git clone https://github.com/AdvancedProductivity/GableServer.git
```

## 🍠 Run
```bash
mvn spring-boot:run
```

Browser open: `http://localhost:2110/`

## User Guide
Please See [here](https://github.com/AdvancedProductivity/GableServer/wiki)

## Front End Project Address

Please See [here](https://github.com/AdvancedProductivity/GableClient)


## ⌨️ Project Architecture

To help interested people quickly grasp key information, write this section.

The project is a front-end and back-end separated project, which can be used as CS architecture or BS architecture.

The back end of the project is an ordinary and simple `SpringBoot` project, 
which does not use the database (all persistent based on file system),
Middleware such as `redis` is not used (a `Guava Cache` is maintained in memory for caching), 
The front-end packaged files are stored in the directory `resource/static`, 
so the back-end jar files can be directly accessed after deployment `http://localhost:2110/`.

The front end of this project is an ordinary `Angular` project, which is based on the template [angular-electron](https://github.com/maximegris/angular-electron)
start-up,At the same time, the UI Library of  `AntDesign` is used, 
so the project can not only build resource files to access as web pages, 
but also package them into `Electron` clients(`Electron`client still has many bugs 😭)。

In terms of workflow, there are unit tests first. If multiple unit tests are connected in series, it is an integration test.

There are two types of unit tests, one is `HTTP` type test, and the other is `GroovyScript` test.

For `HTTP` type unit tests. After defining the input `json` of `HTTP` on the front end, 
click the run button to send the `json` to the `server` end,
The server end parses the input `json`, constructs an `OkHttpRequest`,
and then the `OkHttpClient` sends an `HTTP` request,
After get the `Resonse`, package it as `json` and return it to the front-end interface, which will be rendered.

For `GroovyScript` type unit tests. 
After the front end defines the input `json` of `GroovyScript` and the `Groovy` script code to be executed,
Send it to the `server` side. The `server` end maintains a `GroovyScriptEngine`. 
The `server` end will first write the code from the front end to the file,
Then write the input `json` parameters into a `Binding` object for `GroovyScript` to call by itself. 
There will also be a JSON object named `out` in the `Binding` object,
For `GroovyScript` to write the output to `out`. 
In theory, tests of `GroovyScript` type can do whatever you want.

In short, use `json` to describe the input and output of unit tests. Because the output is a `json` object,
So we can use `JsonSchema` to verify it.