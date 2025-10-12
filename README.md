# 社区小铺系统 - 项目 README

## 项目简介
社区小铺系统是一款融合社区互动与商品交易双核心的综合性平台，面向大学生群体提供聊天交友、学术问答、二手交易等功能。系统采用前后端分离架构，后端基于 Spring Boot + MyBatis-Plus 构建，前端使用 Vue 3 + Vite + Tailwind CSS 开发，支持用户注册登录、帖子创作与互动、商品发布与交易、评价反馈及消息通知等全流程业务，同时具备完善的权限控制、信用体系与数据安全保障。


## 核心功能
系统围绕 “社区” 与 “交易” 两大板块设计，覆盖以下核心场景：

### 1. 社区互动板块
- **用户管理：** 支持手机号 / 邮箱 / 第三方账号（微信 / QQ / 支付宝）注册登录，个人资料维护、信用分管理及权限控制。
- **帖子管理：** 发布图文 / 富文本帖子，支持点赞、跟帖、收藏、置顶 / 加精（管理员），按热度 / 时间 / 类型筛选帖子。
- **互动功能：** 跟帖嵌套回复、@用户通知、帖子点赞统计，实时更新互动数据。

### 2. 商品交易板块
- **商品管理：** 发布二手商品（含多图上传、成色标注），支持商品搜索（关键词 / 价格区间 / 类别）、库存调整及上下架操作。
- **订单交易：** 完整交易流程（下单 - 支付 - 发货 - 收货），集成支付宝 / 微信支付，支持订单状态追踪与超时自动取消。
- **信用与评价：** 基于交易行为计算信用分（影响发布权限），交易完成后买卖双方互评，生成卖家 / 商品评分统计。

### 3. 通用支撑板块
- **消息通知：** 系统通知（订单状态变更）、私信互动、未读消息统计，支持标记已读 / 删除。
- **权限控制：** 基于角色的权限管理（普通用户 / 管理员），敏感操作身份校验（如修改密码、解绑第三方账号）。
- **数据安全：** 敏感信息脱敏（手机号 / OpenID）、密码 BCrypt 加密、接口请求校验与限流。



## 技术栈选型

### 1. 后端技术栈

| 技术 / 工具 | 版本 | 用途说明 |
| :---: | :--- | :--- |
| Spring Boot | 2.7.x | 微服务基础框架，提供自动配置、依赖管理，简化开发流程 |
| Spring Cloud Alibaba | 2021.0.4.0 | 服务注册发现（Nacos）、配置中心（Nacos Config）、熔断限流（Sentinel） |
| MyBatis-Plus | 3.x | ORM 框架，简化数据库 CRUD 操作，支持分页、枚举映射、逻辑删除 |
| MapStruct | 1.5.x | 实体（Entity）与数据传输对象（DTO）之间的高效映射，减少重复代码 |
| MySQL | 8.0 | 关系型数据库，存储用户、商品、订单等结构化数据 |
| Redis | 6.2.x | 缓存热点数据（商品详情、未读消息数），提升查询性能 |
| RabbitMQ | 3.11.x | 异步通信（如订单支付后通知卖家），解耦服务依赖 |
| JWT | 0.11.5 | 生成登录令牌，实现无状态身份认证 |


### 2. 前端技术栈
| 技术 / 工具 |	版本 | 用途说明 |
| :---: | :--- | :--- |
| Vue |	3.x | 前端框架，支持组件化开发、响应式设计，适配多终端 |
| Vite | 3.x | 前端构建工具，比 Webpack 更快的热更新与打包速度 |
| Tailwind CSS | 3.x | 实用优先的 CSS 框架，快速构建统一风格的 UI 界面 |
| Axios | 1.3.x | 发起 HTTP 请求，封装请求拦截（添加 Token）与响应处理（统一异常捕获） |
| Vue Router | 4.x | 前端路由管理，实现页面跳转与权限控制（未登录拦截） |
| Swagger/SpringDoc | 2.10.x | 自动生成接口文档，支持在线调试 |

## 项目结构
### 1. 后端项目结构（按业务模块划分）

``` plaintext
com.community_shop.backend
├── controller       # 控制器层：请求路由、参数校验、响应封装
│   ├── UserController.java        # 用户管理接口
│   ├── PostController.java        # 帖子管理接口
│   ├── ProductController.java     # 商品管理接口
│   ├── OrderController.java       # 订单管理接口
│   └── MessageController.java     # 消息通知接口
├── service          # 业务逻辑层：核心业务处理、事务控制
│   ├── base         # 基础服务实现
│   │   ├── UserServiceImpl.java
│   │   └── ProductServiceImpl.java
│   └── interfaces   # 服务接口定义
│       ├── UserService.java
│       └── ProductService.java
├── mapper           # 数据访问层：数据库操作接口
│   ├── UserMapper.java
│   ├── ProductMapper.java
│   └── OrderMapper.java
├── entity           # 实体类：与数据库表映射
│   ├── User.java
│   ├── Product.java
│   └── Order.java
├── dto              # 数据传输对象：请求/响应参数封装
│   ├── user         # 用户模块DTO
│   │   ├── LoginDTO.java
│   │   └── UserDetailDTO.java
│   ├── product      # 商品模块DTO
│   └── order        # 订单模块DTO
├── convert          # 转换器：Entity与DTO映射（基于MapStruct）
│   ├── UserConvert.java
│   └── ProductConvert.java
├── exception        # 异常处理：自定义异常、全局异常处理器
│   ├── BusinessException.java
│   └── GlobalExceptionHandler.java
├── config           # 配置类：框架配置、Bean定义
│   ├── MyBatisConfig.java
│   └── SwaggerConfig.java
└── util             # 工具类：脱敏、加密、日期处理等
    ├── DesensitizeUtils.java
    └── JwtUtils.java
```

### 2. 前端项目结构

```plaintext
src/
├── api              # 接口请求封装
│   ├── user.js      # 用户相关接口
│   ├── post.js      # 帖子相关接口
│   └── product.js   # 商品相关接口
├── components       # 通用组件
│   ├── common       # 基础组件（分页、弹窗）
│   ├── post         # 帖子相关组件
│   └── product      # 商品相关组件
├── views            # 页面组件
│   ├── Home.vue     # 首页（帖子推荐流）
│   ├── PostDetail.vue  # 帖子详情页
│   ├── ProductList.vue # 商品列表页
│   └── OrderConfirm.vue # 订单确认页
├── router           # 路由配置
│   └── index.js
├── store            # 状态管理（Vuex/Pinia）
│   └── index.js
├── utils            # 工具函数
│   ├── request.js   # Axios封装
│   └── auth.js      # 登录状态管理
└── styles           # 样式文件
    └── tailwind.css # Tailwind CSS配置
```


## 快速启动

### 1. 环境准备
- **JDK：** 21.x 及以上
- **MySQL：** 8.0 及以上（创建数据库 community_shop，执行 sql/init.sql 初始化表结构）
- **Redis：** 6.2.x 及以上（默认配置：localhost:6379，无密码）
- **RabbitMQ：** 3.11.x 及以上（默认配置：localhost:5672，账号密码 guest/guest）
- **Node.js：** 14.x 及以上（前端依赖安装与打包）

### 2. 后端启动步骤
1. **克隆代码仓库：**
``` bash
git clone https://github.com/your-username/community-shop.git
cd community-shop/backend
```

2. **配置数据库连接（修改 application.yml）：**
- 添加 `src/main/resources/application-dev.properties`，配置数据库、Redis、邮件等相关信息。

3. ***启动服务：***
- 使用 Maven 构建项目：
```bash
mvn clean package
```

- 运行 Spring Boot 应用：
```bash
java -jar target/ai_smart_course-0.0.1-SNAPSHOT.jar
```

- 或使用 IDE 直接运行 BackendApplication.java（主类）

### 3. 前端启动步骤
1. **进入前端目录：**
```bash
cd ../frontend
```

2. **安装依赖：**
```bash
npm install
```

3. **启动开发服务器（默认端口 8070）：**
```bash
npm run dev
```

4. **访问前端页面：** http://localhost:8070

## 核心业务流程
### 1. 帖子发布流程
1. 用户登录后点击 “发布帖子”，填写标题、内容并上传图片（可选）。
2. 前端校验表单（标题非空、内容长度≤2000 字），调用 /api/v1/posts/publish 接口。
3. 后端校验用户信用分（≥60 分可发布），通过 MapStruct 转换 DTO 为 Entity，保存至数据库。
4. 发布成功后返回帖子详情，前端跳转至帖子详情页；失败则返回错误提示（如信用分不足）。

### 2. 商品交易流程
1. 用户浏览商品列表，点击 “立即购买”，进入订单确认页（选择收货地址、数量）。
2. 点击 “提交订单”，调用 /api/v1/orders/create 接口，后端校验商品库存、用户信用分，创建订单并扣减库存。
3. 前端跳转至支付页，调用 /api/v1/payments/create 生成支付链接，用户完成支付。
4. 支付平台回调后端 /api/v1/payments/callback 接口，更新订单状态为 “已支付”，并发送消息通知卖家。
5. 卖家发货后更新订单状态为 “已发货”，买家确认收货后订单完成，触发评价流程。

## 扩展与维护
### 1. 新增业务模块
遵循 “Controller → Service → Mapper → Entity → DTO” 分层架构，新增模块需：
- 定义 Entity 映射数据库表，添加 MyBatis-Plus 注解。
- 编写 Mapper 接口继承 BaseMapper，扩展自定义查询方法。
- 定义 Service 接口与实现类，处理业务逻辑并添加事务控制。
- 编写 Controller 接口，添加参数校验与权限注解（如 @LoginRequired）。
- 定义 DTO 封装请求 / 响应参数，使用 JSR380 注解（如 @NotBlank）做参数校验。

### 2. 性能优化建议
- 缓存优化：高频查询接口（如商品详情、未读消息数）添加 Redis 缓存，设置合理过期时间。
- 数据库优化：核心表（如 order、post）添加索引（如 idx_user_id、idx_create_time），避免全表扫描。
- 异步处理：非实时需求（如消息通知、日志记录）使用 RabbitMQ 异步处理，提升接口响应速度。

### 3. 安全保障
- 接口限流：使用 Sentinel 对高频接口（登录、商品搜索）设置限流规则，防止恶意请求。
- 敏感数据保护：手机号、邮箱等信息通过 DesensitizeUtils 脱敏后返回（如手机号显示为 138****1234）。
- 权限校验：所有敏感操作（如修改订单状态、删除帖子）需校验用户身份与数据归属权，避免越权操作。

## 联系方式
- 项目维护者：Arrhenius401
- 邮箱：17268287727@163.com
- 仓库地址：
  - [前端仓库](https://github.com/Arrhenius401/community_shop_frontend "后端仓库")
  - [后端仓库](https://github.com/Arrhenius401/community_shop_backend "前端仓库")
- 如有问题或建议，欢迎提交 Issue 或 Pull Request！