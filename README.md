```
                                     ____                     ,--,
  ,---,                            ,'  , `.          ,---,  ,--.'|
,---.'|     ,---.    ,---.      ,-+-,.' _ |        ,---.'|  |  | :     ,---.
|   | :    '   ,'\  '   ,'\  ,-+-. ;   , ||        |   | :  :  : '    '   ,'\  ,----._,.
:   : :   /   /   |/   /   |,--.'|'   |  ||        :   : :  |  ' |   /   /   |/   /  ' /
:     |,-.   ; ,. .   ; ,. |   |  ,', |  |,        :     |,-'  | |  .   ; ,. |   :     |
|   : '  '   | |: '   | |: |   | /  | |--'         |   : '  |  | :  '   | |: |   | .\  .
|   |  / '   | .; '   | .; |   : |  | ,            |   |  / '  : |__'   | .; .   ; ';  |
'   : |: |   :    |   :    |   : |  |/             '   : |: |  | '.'|   :    '   .   . |
|   | '/ :\   \  / \   \  /|   | |`-'              |   | '/ ;  :    ;\   \  / `---`-'| |
|   :    | `----'   `----' |   ;/                  |   :    |  ,   /  `----'  .'__/\_: |
/    \  /                  '---'                   /    \  / ---`-'           |   :    :
`-'----'                                           `-'----'                    \   \  /
                                                                                `--`-'
```

> 基于蘑菇博客修改的`boom_blog`
>
> 在修改的同时、学习SpringCloud和Vue3相关知识
> 

## 目录介绍

- BoomBlog 是一款基于最新技术开发的多人在线、简洁的博客系统。
- boom_admin: 提供admin端API接口服务；
- boom_web：提供web端API接口服务；
- boom_picture： 图片服务，用于图片上传和下载；
- boom_sms：消息服务，用于更新ElasticSearch、Solr索引、邮件和短信发送
- boom_search：搜索服务，ElasticSearch和Solr作为检索工具，支持可插拔配置，默认使用SQL搜索
- boom_commons: 是公共模块，主要用于存放Entity实体类和Feign远程调用接口
- boom_utils: 是常用工具类；
- boom_xo: 是存放 VO、Service，Dao层的
- boom_base: 是一些Base基类
- boom_config: 是存放一些配置
- doc: 是boom博客的一些文档和数据库文件
- vue_boom_admin：VUE的后台管理页面
- vue_boom_web：VUE的门户网站