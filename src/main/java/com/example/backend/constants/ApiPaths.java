package com.example.backend.constants;

/**
 * API 路径常量类 - 按端和模块组织
 * 
 * <p>设计原则：</p>
 * <ul>
 *   <li>按端组织：App 端、Admin 端、Common（通用）区分不同的接口</li>
 *   <li>模块化组织：按业务模块分组，快速定位接口</li>
 *   <li>统一管理：所有 API 路径集中管理，便于修改和维护</li>
 * </ul>
 * 
 * <p>使用规则：</p>
 * <ul>
 *   <li>/api/app/* → 使用 ApiPaths.App.*</li>
 *   <li>/api/admin/* → 使用 ApiPaths.Admin.*</li>
 *   <li>/api/movie/*, /api/user/* 等 → 使用 ApiPaths.Common.*</li>
 * </ul>
 * 
 * <p>使用示例：</p>
 * <pre>
 * // 在 Controller 中使用
 * @GetMapping(ApiPaths.App.Movie.NOW_SHOWING)
 * public RestBean&lt;List&lt;MovieResponse&gt;&gt; nowShowing() { ... }
 * 
 * // 在代码中引用
 * String url = ApiPaths.Admin.Movie.SAVE;
 * </pre>
 */
public final class ApiPaths {
    
    private ApiPaths() {
        throw new UnsupportedOperationException("Utility class");
    }

    // ==================== 基础路径常量 ====================
    
    /** API 基础路径 */
    public static final String BASE = "/api";
    
    /** App 端基础路径 */
    public static final String APP_BASE = BASE + "/app";
    
    /** Admin 端基础路径 */
    public static final String ADMIN_BASE = BASE + "/admin";
    
    /** 通用基础路径 */
    public static final String COMMON_BASE = BASE;

    // ==================== App 端 API 路径 ====================
    
    /**
     * App 端 API 路径
     * 
     * <p>相关接口：/api/app/*</p>
     */
    public static final class App {
        private App() {}
        
        /**
         * App 端 - 电影模块
         */
        public static final class Movie {
            private Movie() {}
            /** 正在热映 - GET /api/app/movie/nowShowing */
            public static final String NOW_SHOWING = APP_BASE + "/movie/nowShowing";
            /** 即将上映 - GET /api/app/movie/comingSoon */
            public static final String COMING_SOON = APP_BASE + "/movie/comingSoon";
            /** 演职员 - GET /api/app/movie/staff */
            public static final String STAFF = APP_BASE + "/movie/staff";
            /** 场次查询 - POST /api/app/movie/showTime */
            public static final String SHOW_TIME = APP_BASE + "/movie/showTime";
        }
        
        /**
         * App 端 - 影院模块
         */
        public static final class Cinema {
            private Cinema() {}
            /** 影院场次 - POST /api/app/cinema/movie/showTime */
            public static final String MOVIE_SHOW_TIME = APP_BASE + "/cinema/movie/showTime";
        }

        /**
         * App 端 - 预售券模块（C 端查看预售券详情，无需登录）
         */
        public static final class Presale {
            private Presale() {}
            /** 预售券详情 - GET /api/app/presale/detail?id= */
            public static final String DETAIL = APP_BASE + "/presale/detail";
        }
    }

    // ==================== Admin 端 API 路径 ====================
    
    /**
     * Admin 端 API 路径
     * 
     * <p>相关接口：/api/admin/*</p>
     */
    public static final class Admin {
        private Admin() {}
        
        /**
         * Admin 端 - 电影模块
         */
        public static final class Movie {
            private Movie() {}
            /** 保存电影 - POST /api/admin/movie/save */
            public static final String SAVE = ADMIN_BASE + "/movie/save";
            /** 删除电影 - DELETE /api/admin/movie/remove */
            public static final String REMOVE = ADMIN_BASE + "/movie/remove";
            /** 重映管理 - POST /api/admin/movie/reRelease/save */
            public static final String RE_RELEASE_SAVE = ADMIN_BASE + "/movie/reRelease/save";
            /** 电影等级 - POST /api/admin/movie/level/save */
            public static final String LEVEL_SAVE = ADMIN_BASE + "/movie/level/save";
            /** 电影等级删除 - DELETE /api/admin/movie/level/remove */
            public static final String LEVEL_REMOVE = ADMIN_BASE + "/movie/level/remove";
        }
        
        /**
         * Admin 端 - 用户管理
         */
        public static final class User {
            private User() {}
            /** 用户列表 - POST /api/admin/user/list */
            public static final String LIST = ADMIN_BASE + "/user/list";
            /** 保存用户 - POST /api/admin/user/save */
            public static final String SAVE = ADMIN_BASE + "/user/save";
            /** 删除用户 - DELETE /api/admin/user/remove */
            public static final String REMOVE = ADMIN_BASE + "/user/remove";
            /** 配置角色 - POST /api/admin/user/configRole */
            public static final String CONFIG_ROLE = ADMIN_BASE + "/user/configRole";
            /** 获取用户角色 - GET /api/admin/user/role */
            public static final String ROLE = ADMIN_BASE + "/user/role";
        }
        
        /**
         * Admin 端 - 角色管理
         */
        public static final class Role {
            private Role() {}
            /** 角色列表 - POST /api/admin/permission/role/list */
            public static final String LIST = ADMIN_BASE + "/permission/role/list";
            /** 权限列表 - GET /api/admin/permission/role/permissionList */
            public static final String PERMISSION_LIST = ADMIN_BASE + "/permission/role/permissionList";
            /** 权限详情 - GET /api/admin/permission/role/permission */
            public static final String PERMISSION = ADMIN_BASE + "/permission/role/permission";
            /** 配置权限 - POST /api/admin/permission/role/config */
            public static final String CONFIG = ADMIN_BASE + "/permission/role/config";
            /** 角色详情 - GET /api/admin/permission/role/detail */
            public static final String DETAIL = ADMIN_BASE + "/permission/role/detail";
            /** 删除角色 - DELETE /api/admin/permission/role/remove */
            public static final String REMOVE = ADMIN_BASE + "/permission/role/remove";
            /** 保存角色 - POST /api/admin/permission/role/save */
            public static final String SAVE = ADMIN_BASE + "/permission/role/save";
        }
        
        /**
         * Admin 端 - 菜单管理
         */
        public static final class Menu {
            private Menu() {}
            /** 菜单列表 - POST /api/admin/permission/menu/list */
            public static final String LIST = ADMIN_BASE + "/permission/menu/list";
            /** 菜单详情 - GET /api/admin/permission/menu/detail */
            public static final String DETAIL = ADMIN_BASE + "/permission/menu/detail";
            /** 删除菜单 - DELETE /api/admin/permission/menu/remove */
            public static final String REMOVE = ADMIN_BASE + "/permission/menu/remove";
            /** 保存菜单 - POST /api/admin/permission/menu/save */
            public static final String SAVE = ADMIN_BASE + "/permission/menu/save";
        }
        
        /**
         * Admin 端 - 按钮管理
         */
        public static final class Button {
            private Button() {}
            /** 按钮列表 - POST /api/admin/permission/button/list */
            public static final String LIST = ADMIN_BASE + "/permission/button/list";
            /** 按钮详情 - GET /api/admin/permission/button/detail */
            public static final String DETAIL = ADMIN_BASE + "/permission/button/detail";
            /** 删除按钮 - DELETE /api/admin/permission/button/remove */
            public static final String REMOVE = ADMIN_BASE + "/permission/button/remove";
            /** 保存按钮 - POST /api/admin/permission/button/save */
            public static final String SAVE = ADMIN_BASE + "/permission/button/save";
        }
        
        /**
         * Admin 端 - API 管理
         */
        public static final class Api {
            private Api() {}
            /** API 列表 - POST /api/admin/permission/api/list */
            public static final String LIST = ADMIN_BASE + "/permission/api/list";
            /** API 详情 - GET /api/admin/permission/api/detail */
            public static final String DETAIL = ADMIN_BASE + "/permission/api/detail";
            /** 删除 API - DELETE /api/admin/permission/api/remove */
            public static final String REMOVE = ADMIN_BASE + "/permission/api/remove";
            /** 保存 API - POST /api/admin/permission/api/save */
            public static final String SAVE = ADMIN_BASE + "/permission/api/save";
        }
        
        /**
         * Admin 端 - 角色管理
         */
        public static final class Character {
            private Character() {}
            /** 删除角色 - DELETE /api/admin/character/remove */
            public static final String REMOVE = ADMIN_BASE + "/character/remove";
            /** 保存角色 - POST /api/admin/character/save */
            public static final String SAVE = ADMIN_BASE + "/character/save";
        }
        
        /**
         * Admin 端 - 演职员管理
         */
        public static final class Staff {
            private Staff() {}
            /** 删除演职员 - DELETE /api/admin/staff/remove */
            public static final String REMOVE = ADMIN_BASE + "/staff/remove";
            /** 保存演职员 - POST /api/admin/staff/save */
            public static final String SAVE = ADMIN_BASE + "/staff/save";
        }
        
        /**
         * Admin 端 - 影院管理
         */
        public static final class Cinema {
            private Cinema() {}
            /** 保存影院 - POST /api/admin/cinema/save */
            public static final String SAVE = ADMIN_BASE + "/cinema/save";
            /** 删除影院 - DELETE /api/admin/cinema/remove */
            public static final String REMOVE = ADMIN_BASE + "/cinema/remove";
            /** 影院规格 - POST /api/admin/cinema/spec/save */
            public static final String SPEC_SAVE = ADMIN_BASE + "/cinema/spec/save";
            /** 影院规格删除 - DELETE /api/admin/cinema/spec/remove */
            public static final String SPEC_REMOVE = ADMIN_BASE + "/cinema/spec/remove";
            /** 电影票类型列表（只读）- POST /api/cinema/ticketType/list */
            public static final String TICKET_TYPE_LIST = BASE + "/cinema/ticketType/list";
            /** 影院票种单条保存（有id更新/无id插入）- POST /api/admin/cinema/ticketType/save */
            public static final String TICKET_TYPE_SAVE = ADMIN_BASE + "/cinema/ticketType/save";
            /** 影院票种单条删除 - DELETE /api/admin/cinema/ticketType/remove */
            public static final String TICKET_TYPE_REMOVE = ADMIN_BASE + "/cinema/ticketType/remove";
            /** 影院票种排序 - POST /api/admin/cinema/ticketType/reorder */
            public static final String TICKET_TYPE_REORDER = ADMIN_BASE + "/cinema/ticketType/reorder";
            /** 票价配置列表 - POST /api/cinema/priceConfig/list */
            public static final String PRICE_CONFIG_LIST = BASE + "/cinema/priceConfig/list";
            /** 票价配置保存 - POST /api/admin/cinema/priceConfig/save */
            public static final String PRICE_CONFIG_SAVE = ADMIN_BASE + "/cinema/priceConfig/save";
            /** 票价配置删除 - DELETE /api/admin/cinema/priceConfig/remove */
            public static final String PRICE_CONFIG_REMOVE = ADMIN_BASE + "/cinema/priceConfig/remove";
        }
        
        /**
         * Admin 端 - 影厅管理
         */
        public static final class TheaterHall {
            private TheaterHall() {}
            /** 保存影厅 - POST /api/admin/theater/hall/save */
            public static final String SAVE = ADMIN_BASE + "/theater/hall/save";
            /** 删除影厅 - DELETE /api/admin/theater/hall/remove */
            public static final String REMOVE = ADMIN_BASE + "/theater/hall/remove";
        }
        
        /**
         * Admin 端 - 职位管理
         */
        public static final class Position {
            private Position() {}
            /** 删除职位 - DELETE /api/admin/position/remove */
            public static final String REMOVE = ADMIN_BASE + "/position/remove";
            /** 保存职位 - POST /api/admin/position/save */
            public static final String SAVE = ADMIN_BASE + "/position/save";
        }
        
        /**
         * Admin 端 - 语言管理
         */
        public static final class Language {
            private Language() {}
            /** 删除语言 - DELETE /api/admin/language/remove */
            public static final String REMOVE = ADMIN_BASE + "/language/remove";
            /** 保存语言 - POST /api/admin/language/save */
            public static final String SAVE = ADMIN_BASE + "/language/save";
        }
        
        /**
         * Admin 端 - 品牌管理
         */
        public static final class Brand {
            private Brand() {}
            /** 删除品牌 - DELETE /api/admin/brand/remove */
            public static final String REMOVE = ADMIN_BASE + "/brand/remove";
            /** 保存品牌 - POST /api/admin/brand/save */
            public static final String SAVE = ADMIN_BASE + "/brand/save";
        }
        
        /**
         * Admin 端 - 标签管理
         */
        public static final class Tag {
            private Tag() {}
            /** 删除标签 - DELETE /api/admin/movieTag/remove */
            public static final String REMOVE = ADMIN_BASE + "/movieTag/remove";
            /** 保存标签 - POST /api/admin/movieTag/save */
            public static final String SAVE = ADMIN_BASE + "/movieTag/save";
        }
        
        /**
         * Admin 端 - 场次标签管理
         */
        public static final class ShowTimeTag {
            private ShowTimeTag() {}
            /** 删除场次标签 - DELETE /api/admin/showTimeTag/remove */
            public static final String REMOVE = ADMIN_BASE + "/showTimeTag/remove";
            /** 保存场次标签 - POST /api/admin/showTimeTag/save */
            public static final String SAVE = ADMIN_BASE + "/showTimeTag/save";
        }
        
        /**
         * Admin 端 - 场次管理
         */
        public static final class ShowTime {
            private ShowTime() {}
            /** 删除场次 - DELETE /api/admin/movie_show_time/remove */
            public static final String REMOVE = ADMIN_BASE + "/movie_show_time/remove";
            /** 保存场次 - POST /api/admin/movie_show_time/save */
            public static final String SAVE = ADMIN_BASE + "/movie_show_time/save";
        }
        
        /**
         * Admin 端 - 退款管理
         */
        public static final class Refund {
            private Refund() {}
            /** 退款列表 - POST /api/admin/refund/list */
            public static final String LIST = ADMIN_BASE + "/refund/list";
            /** 处理退款 - POST /api/admin/refund/process */
            public static final String PROCESS = ADMIN_BASE + "/refund/process";
        }
        
        /**
         * Admin 端 - 订单管理
         */
        public static final class Order {
            private Order() {}
            /** 订单列表 - POST /api/admin/movieOrder/list */
            public static final String LIST = ADMIN_BASE + "/movieOrder/list";
            /** 删除订单 - DELETE /api/admin/movieOrder/remove */
            public static final String REMOVE = ADMIN_BASE + "/movieOrder/remove";
        }
        
        /**
         * Admin 端 - 活动管理
         */
        public static final class Promotion {
            private Promotion() {}
            /** 活动详情 - GET /api/admin/promotion/detail */
            public static final String DETAIL = ADMIN_BASE + "/promotion/detail";
            /** 活动列表 - POST /api/admin/promotion/list */
            public static final String LIST = ADMIN_BASE + "/promotion/list";
            /** 保存活动 - POST /api/admin/promotion/save */
            public static final String SAVE = ADMIN_BASE + "/promotion/save";
            /** 删除活动 - DELETE /api/admin/promotion/remove */
            public static final String REMOVE = ADMIN_BASE + "/promotion/remove";
        }

        /**
         * Admin 端 - 预售券管理
         */
        public static final class Presale {
            private Presale() {}
            /** 预售券详情 - GET /api/admin/presale/detail */
            public static final String DETAIL = ADMIN_BASE + "/presale/detail";
            /** 预售券列表 - POST /api/admin/presale/list */
            public static final String LIST = ADMIN_BASE + "/presale/list";
            /** 保存预售券 - POST /api/admin/presale/save */
            public static final String SAVE = ADMIN_BASE + "/presale/save";
            /** 删除预售券 - DELETE /api/admin/presale/remove */
            public static final String REMOVE = ADMIN_BASE + "/presale/remove";
        }
        
        /**
         * Admin 端 - 字典管理
         */
        public static final class Dict {
            private Dict() {}
            /** 字典项保存 - POST /api/admin/dict/item/save */
            public static final String ITEM_SAVE = ADMIN_BASE + "/dict/item/save";
            /** 保存字典 - POST /api/admin/dict/save */
            public static final String SAVE = ADMIN_BASE + "/dict/save";
            /** 删除字典 - DELETE /api/admin/dict/remove */
            public static final String REMOVE = ADMIN_BASE + "/dict/remove";
        }
        
        /**
         * Admin 端 - 应用版本管理
         */
        public static final class AppVersion {
            private AppVersion() {}
            /** 版本列表 - POST /api/admin/app/versionList */
            public static final String LIST = ADMIN_BASE + "/app/versionList";
        }
        
        /**
         * Admin 端 - 图表统计
         */
        public static final class Chart {
            private Chart() {}
            /** 图表数据 - GET /api/admin/chart */
            public static final String DATA = ADMIN_BASE + "/chart";
        }
    }

    // ==================== 通用 API 路径 ====================
    
    /**
     * 通用 API 路径（不区分端）
     * 
     * <p>相关接口：/api/movie/*, /api/user/* 等公共接口</p>
     */
    public static final class Common {
        private Common() {}
        
        /**
         * 通用 - 电影模块
         */
        public static final class Movie {
            private Movie() {}
            /** 电影列表 - POST /api/movie/list */
            public static final String LIST = COMMON_BASE + "/movie/list";
            /** 电影详情 - GET /api/movie/detail */
            public static final String DETAIL = COMMON_BASE + "/movie/detail";
            /** 电影规格 - GET /api/movie/spec */
            public static final String SPEC = COMMON_BASE + "/movie/spec";
            /** 演职员 - GET /api/movie/staff */
            public static final String STAFF = COMMON_BASE + "/movie/staff";
            /** 角色 - GET /api/movie/character */
            public static final String CHARACTER = COMMON_BASE + "/movie/character";
            /** 版本列表 - GET /api/movie/version/list */
            public static final String VERSION_LIST = COMMON_BASE + "/movie/version/list";
        }
        
        /**
         * 通用 - 用户模块
         */
        public static final class User {
            private User() {}
            /** 用户登录 - POST /api/user/login */
            public static final String LOGIN = COMMON_BASE + "/user/login";
            /** 用户注册 - POST /api/user/register */
            public static final String REGISTER = COMMON_BASE + "/user/register";
            /** 更新用户信息 - POST /api/user/updateUserInfo */
            public static final String UPDATE_INFO = COMMON_BASE + "/user/updateUserInfo";
            /** 用户详情 - GET /api/user/detail */
            public static final String DETAIL = COMMON_BASE + "/user/detail";
            /** 订单列表 - POST /api/user/orderList */
            public static final String ORDER_LIST = COMMON_BASE + "/user/orderList";
            /** 用户退出 - POST /api/user/logout */
            public static final String LOGOUT = COMMON_BASE + "/user/logout";
        }
        
        /**
         * 通用 - 角色模块
         */
        public static final class Character {
            private Character() {}
            /** 角色列表 - POST /api/character/list */
            public static final String LIST = COMMON_BASE + "/character/list";
            /** 角色详情 - GET /api/character/detail */
            public static final String DETAIL = COMMON_BASE + "/character/detail";
        }
        
        /**
         * 通用 - 演职员模块
         */
        public static final class Staff {
            private Staff() {}
            /** 演职员列表 - POST /api/staff/list */
            public static final String LIST = COMMON_BASE + "/staff/list";
            /** 演职员详情 - GET /api/staff/detail */
            public static final String DETAIL = COMMON_BASE + "/staff/detail";
        }
        
        /**
         * 通用 - 字典模块
         */
        public static final class Dict {
            private Dict() {}
            /** 字典列表 - POST /api/dict/list */
            public static final String LIST = COMMON_BASE + "/dict/list";
            /** 字典详情 - GET /api/dict/detail */
            public static final String DETAIL = COMMON_BASE + "/dict/detail";
            /** 指定字典 - GET /api/dict/specify */
            public static final String SPECIFY = COMMON_BASE + "/dict/specify";
        }
        
        /**
         * 通用 - 品牌模块
         */
        public static final class Brand {
            private Brand() {}
            /** 品牌列表 - POST /api/brand/list */
            public static final String LIST = COMMON_BASE + "/brand/list";
            /** 品牌详情 - GET /api/brand/detail */
            public static final String DETAIL = COMMON_BASE + "/brand/detail";
        }
        
        /**
         * 通用 - 语言模块
         */
        public static final class Language {
            private Language() {}
            /** 语言列表 - POST /api/language/list */
            public static final String LIST = COMMON_BASE + "/language/list";
            /** 语言详情 - GET /api/language/detail */
            public static final String DETAIL = COMMON_BASE + "/language/detail";
        }
        
        /**
         * 通用 - 职位模块
         */
        public static final class Position {
            private Position() {}
            /** 职位列表 - POST /api/position/list */
            public static final String LIST = COMMON_BASE + "/position/list";
            /** 职位详情 - GET /api/position/detail */
            public static final String DETAIL = COMMON_BASE + "/position/detail";
        }
        
        /**
         * 通用 - 影院模块
         */
        public static final class Cinema {
            private Cinema() {}
            /** 影院列表 - POST /api/cinema/list */
            public static final String LIST = COMMON_BASE + "/cinema/list";
            /** 影院详情 - GET /api/cinema/detail */
            public static final String DETAIL = COMMON_BASE + "/cinema/detail";
            /** 影院价格策略（促销）列表，按优先级排序，供 app 端价格计算 - GET /api/cinema/promotions */
            public static final String PROMOTIONS = COMMON_BASE + "/cinema/promotions";
            /** 影院规格 - GET /api/cinema/spec */
            public static final String SPEC = COMMON_BASE + "/cinema/spec";
            /** 正在上映 - GET /api/cinema/screening */
            public static final String SCREENING = COMMON_BASE + "/cinema/screening";
            /** 即将上映 - GET /api/cinema/movieShowing */
            public static final String MOVIE_SHOWING = COMMON_BASE + "/cinema/movieShowing";
        }
        
        /**
         * 通用 - 影厅模块
         */
        public static final class TheaterHall {
            private TheaterHall() {}
            /** 影厅列表 - POST /api/theater/hall/list */
            public static final String LIST = COMMON_BASE + "/theater/hall/list";
            /** 影厅详情 - GET /api/theater/hall/detail */
            public static final String DETAIL = COMMON_BASE + "/theater/hall/detail";
            /** 座位详情 - GET /api/theater/hall/seat/detail */
            public static final String SEAT_DETAIL = COMMON_BASE + "/theater/hall/seat/detail";
            /** 保存座位 - POST /api/theater/hall/seat/save */
            public static final String SEAT_SAVE = COMMON_BASE + "/theater/hall/seat/save";
        }
        
        /**
         * 通用 - 规格模块
         */
        public static final class Spec {
            private Spec() {}
            /** 规格列表 - POST /api/cinema/spec/list */
            public static final String LIST = COMMON_BASE + "/cinema/spec/list";
            /** 规格详情 - GET /api/cinema/spec/detail */
            public static final String DETAIL = COMMON_BASE + "/cinema/spec/detail";
        }
        
        /**
         * 通用 - 电影标签模块
         */
        public static final class MovieTag {
            private MovieTag() {}
            /** 标签列表 - POST /api/movieTag/list */
            public static final String LIST = COMMON_BASE + "/movieTag/list";
            /** 标签详情 - GET /api/movieTag/detail */
            public static final String DETAIL = COMMON_BASE + "/movieTag/detail";
        }
        
        /**
         * 通用 - 场次标签模块
         */
        public static final class ShowTimeTag {
            private ShowTimeTag() {}
            /** 场次标签列表 - POST /api/showTimeTag/list */
            public static final String LIST = COMMON_BASE + "/showTimeTag/list";
            /** 场次标签详情 - GET /api/showTimeTag/detail */
            public static final String DETAIL = COMMON_BASE + "/showTimeTag/detail";
        }
        
        /**
         * 通用 - 重映模块
         */
        public static final class ReRelease {
            private ReRelease() {}
            /** 重映列表 - POST /api/movie/reRelease/list */
            public static final String LIST = COMMON_BASE + "/movie/reRelease/list";
            /** 重映详情 - GET /api/movie/reRelease/detail */
            public static final String DETAIL = COMMON_BASE + "/movie/reRelease/detail";
            /** 删除重映 - DELETE /api/movie/reRelease/remove */
            public static final String REMOVE = COMMON_BASE + "/movie/reRelease/remove";
        }
        
        /**
         * 通用 - 电影等级模块
         */
        public static final class Level {
            private Level() {}
            /** 等级列表 - POST /api/movie/level/list */
            public static final String LIST = COMMON_BASE + "/movie/level/list";
            /** 等级详情 - GET /api/movie/level/detail */
            public static final String DETAIL = COMMON_BASE + "/movie/level/detail";
        }
        
        /**
         * 通用 - 场次模块
         */
        public static final class ShowTime {
            private ShowTime() {}
            /** 场次列表 - POST /api/movie_show_time/list */
            public static final String LIST = COMMON_BASE + "/movie_show_time/list";
            /** 场次详情 - GET /api/movie_show_time/detail */
            public static final String DETAIL = COMMON_BASE + "/movie_show_time/detail";
            /** 保存选座 - POST /api/movie_show_time/select_seat/save */
            public static final String SELECT_SEAT_SAVE = COMMON_BASE + "/movie_show_time/select_seat/save";
            /** 取消选座 - POST /api/movie_show_time/select_seat/cancel */
            public static final String SELECT_SEAT_CANCEL = COMMON_BASE + "/movie_show_time/select_seat/cancel";
            /** 用户选座 - GET /api/movie_show_time/user_select_seat */
            public static final String USER_SELECT_SEAT = COMMON_BASE + "/movie_show_time/user_select_seat";
            /** 选座列表 - GET /api/movie_show_time/select_seat/list */
            public static final String SELECT_SEAT_LIST = COMMON_BASE + "/movie_show_time/select_seat/list";
            /** 场次可用票种列表（App 选票页）- POST /api/movie_show_time/ticketType/list */
            public static final String TICKET_TYPE_LIST = COMMON_BASE + "/movie_show_time/ticketType/list";
        }
        
        /**
         * 通用 - 评论模块
         */
        public static final class Comment {
            private Comment() {}
            /** 评论列表 - POST /api/movie/comment/list */
            public static final String LIST = COMMON_BASE + "/movie/comment/list";
            /** 评论详情 - GET /api/movie/comment/detail */
            public static final String DETAIL = COMMON_BASE + "/movie/comment/detail";
            /** 保存评论 - POST /api/movie/comment/save */
            public static final String SAVE = COMMON_BASE + "/movie/comment/save";
            /** 删除评论 - DELETE /api/movie/comment/remove */
            public static final String REMOVE = COMMON_BASE + "/movie/comment/remove";
            /** 点赞评论 - POST /api/movie/comment/like */
            public static final String LIKE = COMMON_BASE + "/movie/comment/like";
            /** 点踩评论 - POST /api/movie/comment/dislike */
            public static final String DISLIKE = COMMON_BASE + "/movie/comment/dislike";
            /** 同步点赞和点踩 - POST /api/movie/comment/syncLikeAndDislikeToDatabase */
            public static final String SYNC_LIKE_DISLIKE = COMMON_BASE + "/movie/comment/syncLikeAndDislikeToDatabase";
        }
        
        /**
         * 通用 - 回复模块
         */
        public static final class Reply {
            private Reply() {}
            /** 回复列表 - POST /api/movie/reply/list */
            public static final String LIST = COMMON_BASE + "/movie/reply/list";
            /** 回复详情 - GET /api/movie/reply/detail */
            public static final String DETAIL = COMMON_BASE + "/movie/reply/detail";
            /** 保存回复 - POST /api/movie/reply/save */
            public static final String SAVE = COMMON_BASE + "/movie/reply/save";
            /** 删除回复 - DELETE /api/movie/reply/remove */
            public static final String REMOVE = COMMON_BASE + "/movie/reply/remove";
            /** 点赞回复 - POST /api/movie/reply/like */
            public static final String LIKE = COMMON_BASE + "/movie/reply/like";
            /** 点踩回复 - POST /api/movie/reply/dislike */
            public static final String DISLIKE = COMMON_BASE + "/movie/reply/dislike";
        }
        
        /**
         * 通用 - 退款模块
         */
        public static final class Refund {
            private Refund() {}
            /** 按订单号查退款列表 - GET /api/refund/list */
            public static final String LIST = COMMON_BASE + "/refund/list";
            /** 我的退款列表(分页) - POST /api/refund/myList */
            public static final String MY_LIST = COMMON_BASE + "/refund/myList";
            /** 退款详情 - GET /api/refund/detail */
            public static final String DETAIL = COMMON_BASE + "/refund/detail";
            /** 申请退款 - POST /api/refund/apply */
            public static final String APPLY = COMMON_BASE + "/refund/apply";
            /** 取消申请 - POST /api/refund/cancel */
            public static final String CANCEL = COMMON_BASE + "/refund/cancel";
        }
        
        /**
         * 通用 - 订单模块
         */
        public static final class Order {
            private Order() {}
            /** 创建订单 - POST /api/movieOrder/create */
            public static final String CREATE = COMMON_BASE + "/movieOrder/create";
            /** 订单详情 - GET /api/movieOrder/detail */
            public static final String DETAIL = COMMON_BASE + "/movieOrder/detail";
            /** 支付订单 - POST /api/movieOrder/pay */
            public static final String PAY = COMMON_BASE + "/movieOrder/pay";
            /** 取消订单 - POST /api/movieOrder/cancel */
            public static final String CANCEL = COMMON_BASE + "/movieOrder/cancel";
            /** 订单超时 - POST /api/movieOrder/timeout */
            public static final String TIMEOUT = COMMON_BASE + "/movieOrder/timeout";
            /** 我的票 - POST /api/movieOrder/myTickets */
            public static final String MY_TICKETS = COMMON_BASE + "/movieOrder/myTickets";
            /** 生成二维码 - GET /api/movieOrder/generatorQRcode */
            public static final String GENERATOR_QR_CODE = COMMON_BASE + "/movieOrder/generatorQRcode";
        }
        
        /**
         * 通用 - 支付方式模块
         */
        public static final class PaymentMethod {
            private PaymentMethod() {}
            /** 支付方式列表 - GET /api/paymentMethod/list */
            public static final String LIST = COMMON_BASE + "/paymentMethod/list";
        }
        
        /**
         * 通用 - 地区模块
         */
        public static final class Areas {
            private Areas() {}
            /** 地区树 - GET /api/areas/tree */
            public static final String TREE = COMMON_BASE + "/areas/tree";
        }
        
        /**
         * 通用 - 信用卡模块
         */
        public static final class CreditCard {
            private CreditCard() {}
            /** 信用卡基础路径 */
            public static final String BASE = COMMON_BASE + "/creditCard";
            /** 信用卡列表 - GET /api/creditCard/list */
            public static final String LIST = BASE + "/list";
            /** 信用卡详情 - GET /api/creditCard/detail */
            public static final String DETAIL = BASE + "/detail";
            /** 保存信用卡 - POST /api/creditCard/save */
            public static final String SAVE = BASE + "/save";
            /** 更新信用卡 - POST /api/creditCard/update */
            public static final String UPDATE = BASE + "/update";
            /** 删除信用卡 - DELETE /api/creditCard/delete */
            public static final String DELETE = BASE + "/delete";
            /** 设置默认信用卡 - POST /api/creditCard/setDefault */
            public static final String SET_DEFAULT = BASE + "/setDefault";
        }
    }
    
    /**
     * 验证相关 API 路径
     */
    public static final class Verify {
        private Verify() {}
        /** 发送验证码 - POST /api/verify/sendCode */
        public static final String SEND_CODE = COMMON_BASE + "/verify/sendCode";
        /** 验证码图片 - POST /api/verify/captcha */
        public static final String CAPTCHA = COMMON_BASE + "/verify/captcha";
        /** 校验验证码 - POST /api/verify/checkCaptcha */
        public static final String CHECK_CAPTCHA = COMMON_BASE + "/verify/checkCaptcha";
        /** 邮件模板预览 - GET /api/verify/emailPreview */
        public static final String EMAIL_PREVIEW = COMMON_BASE + "/verify/emailPreview";
    }
    
    /**
     * 上传相关 API 路径
     */
    public static final class Upload {
        private Upload() {}
        /** 上传文件 - POST /api/upload */
        public static final String UPLOAD = COMMON_BASE + "/upload";
        /** 删除文件 - DELETE /api/deleteFile */
        public static final String DELETE = COMMON_BASE + "/deleteFile";
    }
    
    /**
     * 定时任务 API 路径
     */
    public static final class Scheduled {
        private Scheduled() {}
        /** 更新电影状态 - POST /api/scheduled/updateMovieState */
        public static final String UPDATE_MOVIE_STATE = COMMON_BASE + "/scheduled/updateMovieState";
        /** 更新电影上映状态 - POST /api/scheduled/updateMovieScreeningState */
        public static final String UPDATE_MOVIE_SCREENING_STATE = COMMON_BASE + "/scheduled/updateMovieScreeningState";
        /** 更新电影订单状态 - POST /api/scheduled/updateMovieOrderState */
        public static final String UPDATE_MOVIE_ORDER_STATE = COMMON_BASE + "/scheduled/updateMovieOrderState";
        /** 更新场次定时公开/开放购票状态 - POST /api/scheduled/updateShowTimePublishState */
        public static final String UPDATE_SHOWTIME_PUBLISH_STATE = COMMON_BASE + "/scheduled/updateShowTimePublishState";
    }
    
    /**
     * 开发工具 API 路径
     */
    public static final class Dev {
        private Dev() {}
        /** 开发工具基础路径 */
        public static final String BASE_PATH = COMMON_BASE + "/dev";
        /** 检查消息键 - GET /api/dev/check-message-keys */
        public static final String CHECK_MESSAGE_KEYS = BASE_PATH + "/check-message-keys";
        /** 消息键报告 - GET /api/dev/message-keys-report */
        public static final String MESSAGE_KEYS_REPORT = BASE_PATH + "/message-keys-report";
    }
}
