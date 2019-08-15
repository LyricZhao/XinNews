### 评分细则
#### 基础要求
- 不崩溃
- 布局合理
- 分类列表
    - 添加和删除
    - 修改分类时有动态特效
- 新闻
    - 列表显示新闻、可以正确展示图片、视频
    - 看过的新闻本地存储、是否看过用灰色标记
    - \[?\] 上拉获取更多、下拉刷新
    - 显示新闻来源和时间
    - 关键词搜索
    - 历史记录
- 分享和收藏
    - 分享到微信、微博等，内容：摘要、链接和图片
    - 新闻推荐
- 其他
    - 夜间模式
    - 新闻屏蔽
    - 清新时报
    - \[ \] 写后台实现登录、注册
    
#### API    
- API Example: https://api2.newsminer.net/svc/news/queryNewsList?size=15&startDate=2019-07-01&endDate=2019-07-03&words=特朗普&categories=科技
- 测试连接: https://api2.newsminer.net/svc/news/queryNewsList
- 参数
    - size: 新闻篇数
    - startDate: 开始时间
    - endDate: 截止时间
    - words: 关键词
    - categories: 分类
        - 可选项: 娱乐、军事、教育、文化、健康、财经、体育、汽车、科技、社会

- 新闻信息
    - publishTime
    - keywords
        - score / word
    - language
    - video
    - title
    - content
    - newsId
    - publisher
    - category
    - mentioned
        - persons
        - organizations
        - locations
        - who
        - where
        
#### 数据库        
- NewsEntry
    - newsId (@PrimaryKey)
    - title
    - content
    - category
    - publishTime
    - keywords (in JSON)
    - images (in path list), 当浏览到的时候再加载
    - videos (in path list), 当进入页面时再加载
    - publisher
    - viewed
    - favorite
 
- CategoryEntry
    - name
    - keyword
    - category
    