package com.example.backend.constants;

/**
 * 消息键常量类 - 按端和模块组织
 * 
 * <p>设计原则：</p>
 * <ul>
 *   <li>按端组织：App 端、Admin 端、Common（通用）区分不同的错误消息风格</li>
 *   <li>模块化组织：按业务模块分组，快速定位接口</li>
 *   <li>引用而非重复：模块类引用通用常量，避免重复定义</li>
 * </ul>
 * 
 * <p>使用规则：</p>
 * <ul>
 *   <li>/api/app/* → 使用 MessageKeys.App.*</li>
 *   <li>/api/admin/* → 使用 MessageKeys.Admin.*</li>
 *   <li>/api/movie/*, /api/user/* → 使用 MessageKeys.Common.*</li>
 * </ul>
 * 
 * <p>使用示例：</p>
 * <pre>
 * // App 端
 * MessageUtils.getMessage(MessageKeys.App.Movie.GET_SUCCESS);
 * 
 * // Admin 端
 * MessageUtils.getMessage(MessageKeys.Admin.Movie.SAVE_SUCCESS);
 * 
 * // 通用接口
 * MessageUtils.getMessage(MessageKeys.Common.Movie.GET_SUCCESS);
 * </pre>
 */
public final class MessageKeys {
    
    private MessageKeys() {
        throw new UnsupportedOperationException("Utility class");
    }

    // ==================== 通用消息键常量（所有模块共享） ====================
    
    /**
     * 通用成功消息键
     */
    public static final class Success {
        private Success() {}
        /** 保存成功 - 通用 */
        public static final String SAVE = "success.save";
        /** 删除成功 - 通用 */
        public static final String REMOVE = "success.remove";
        /** 获取成功 - 通用 */
        public static final String GET = "success.get";
        /** 登录成功 - 用户相关 */
        public static final String LOGIN = "success.login";
        /** 退出成功 - 用户相关 */
        public static final String LOGOUT = "success.logout";
        /** 上传成功 - 上传相关 */
        public static final String UPLOAD = "success.uploadSuccess";
        /** 成功 - 通用 */
        public static final String GENERAL = "success.success";
        /** 发送成功 - 邮件/消息相关 */
        public static final String SEND = "success.send";
        /** 操作成功 - 通用 */
        public static final String ACTION = "success.action";
    }
    
    /**
     * 通用错误消息键
     */
    public static final class Error {
        private Error() {}
        /** 系统错误 - 通用 */
        public static final String SYSTEM = "error.systemError";
        /** 用户不存在 - 用户相关 */
        public static final String USER_NOT_FOUND = "error.userNotFound";
        /** 登录已过期 - 认证相关 */
        public static final String LOGIN_EXPIRED = "error.loginExpired";
        /** 参数错误 - 通用 */
        public static final String PARAMETER = "error.parameterError";
        /** 重复错误 - 通用 */
        public static final String REPEAT = "error.repeat";
        /** 没有权限 - 权限相关 */
        public static final String NOT_PERMISSION = "error.notPermission";
        /** 时间冲突 - 时间相关 */
        public static final String TIME_CONFLICT = "error.timeConflict";
        /** 邮箱已存在 - 用户相关 */
        public static final String EMAIL_REPEAT = "error.emailRepeat";
        /** 上传失败 - 上传相关 */
        public static final String UPLOAD = "error.uploadError";
        /** 不能为空 - 验证相关 */
        public static final String REQUIRED = "error.get";
    }
    
    /**
     * 通用重复提示键（用于 error.repeat 的参数）
     */
    public static final class Repeat {
        private Repeat() {}
        /** 路径 - 菜单管理 */
        public static final String PATH = "repeat.path";
        /** 代码 - 通用 */
        public static final String CODE = "repeat.code";
        /** 角色名称 - 角色管理 */
        public static final String ROLE_NAME = "repeat.roleName";
        /** 电影票类型名称 - 电影票类型管理 */
        public static final String MOVIE_TICKET_TYPE_NAME = "repeat.movieTicketTypeName";
        /** 影厅名称 - 影厅管理 */
        public static final String THEATER_HALL_NAME = "repeat.theaterHallName";
        /** 职位名称 - 职位管理 */
        public static final String POSITION_NAME = "repeat.positionName";
        /** 语言代码 - 语言管理 */
        public static final String LANGUAGE_CODE = "repeat.languageCode";
        /** 品牌名称 - 品牌管理 */
        public static final String BRAND_NAME = "repeat.brandName";
        /** 标签名称 - 标签管理 */
        public static final String MOVIE_TAG_NAME = "repeat.movieTagName";
        /** 场次标签名称 - 场次标签管理 */
        public static final String SHOW_TIME_TAG_NAME = "repeat.showTimeTagName";
    }

    // ==================== 按端组织的消息键（推荐使用） ====================
    
    /**
     * App 端消息键
     * 
     * <p>相关接口：/api/app/*</p>
     * <p>特点：用户友好的错误消息，适合移动端和 Web 前端</p>
     */
    public static final class App {
        private App() {}
        
        /**
         * App 端 - 电影模块
         * 
         * <p>相关接口：</p>
         * <ul>
         *   <li>GET /api/app/movie/nowShowing - 正在热映</li>
         *   <li>GET /api/app/movie/comingSoon - 即将上映</li>
         *   <li>GET /api/app/movie/staff - 演职员</li>
         *   <li>POST /api/app/movie/showTime - 场次查询</li>
         * </ul>
         */
        public static final class Movie {
            private Movie() {}
            /** 获取成功 - GET /api/app/movie/* */
            public static final String GET_SUCCESS = Success.GET;
            /** 参数错误 - POST /api/app/movie/showTime */
            public static final String PARAMETER_ERROR = Error.PARAMETER;
        }
        
        /**
         * App 端 - 用户模块
         * 
         * <p>相关接口：POST /api/user/login, /api/user/logout, /api/user/register</p>
         */
        public static final class User {
            private User() {}
            /** 登录成功 - POST /api/user/login */
            public static final String LOGIN_SUCCESS = Success.LOGIN;
            /** 退出成功 - POST /api/user/logout */
            public static final String LOGOUT_SUCCESS = Success.LOGOUT;
            /** 登录已过期 - 认证相关 */
            public static final String LOGIN_EXPIRED = Error.LOGIN_EXPIRED;
            /** 用户不存在 - 用户相关 */
            public static final String NOT_FOUND = Error.USER_NOT_FOUND;
            /** 邮箱已存在 - POST /api/user/register */
            public static final String EMAIL_REPEAT = Error.EMAIL_REPEAT;
        }
        
        /**
         * App 端 - 订单模块
         * 
         * <p>相关接口：POST /api/app/order/*</p>
         */
        public static final class Order {
            private Order() {}
            /** 订单已支付 - 订单相关 */
            public static final String PAY_ERROR = "error.order.payError";
        }
    }
    
    /**
     * Admin 端消息键
     * 
     * <p>相关接口：/api/admin/*</p>
     * <p>特点：详细的错误消息，适合管理后台</p>
     */
    public static final class Admin {
        private Admin() {}
        
        /** 保存成功 - 通用保存接口 */
        public static final String SAVE_SUCCESS = Success.SAVE;
        /** 删除成功 - 通用删除接口 */
        public static final String REMOVE_SUCCESS = Success.REMOVE;
        /** 获取成功 - 通用获取接口 */
        public static final String GET_SUCCESS = Success.GET;
        /** 参数错误 - 通用参数验证 */
        public static final String PARAMETER_ERROR = Error.PARAMETER;
        /** 没有权限 - 权限验证 */
        public static final String NOT_PERMISSION = Error.NOT_PERMISSION;
        /** 重复错误 - 保存时重复检查 */
        public static final String REPEAT_ERROR = Error.REPEAT;
        
        /**
         * Admin 端 - 电影模块
         * 
         * <p>相关接口：</p>
         * <ul>
         *   <li>POST /api/admin/movie/save</li>
         *   <li>DELETE /api/admin/movie/remove</li>
         * </ul>
         */
        public static final class Movie {
            private Movie() {}
            /** 保存成功 - POST /api/admin/movie/save */
            public static final String SAVE_SUCCESS = Success.SAVE;
            /** 删除成功 - DELETE /api/admin/movie/remove */
            public static final String REMOVE_SUCCESS = Success.REMOVE;
            /** 重复错误 - POST /api/admin/movie/save */
            public static final String REPEAT_ERROR = Error.REPEAT;
        }
        
        /**
         * Admin 端 - 用户管理
         * 
         * <p>相关接口：</p>
         * <ul>
         *   <li>POST /api/admin/user/list</li>
         *   <li>POST /api/admin/user/save</li>
         *   <li>DELETE /api/admin/user/remove</li>
         * </ul>
         */
        public static final class User {
            private User() {}
            /** 保存成功 - POST /api/admin/user/save */
            public static final String SAVE_SUCCESS = Success.SAVE;
            /** 删除成功 - DELETE /api/admin/user/remove */
            public static final String REMOVE_SUCCESS = Success.REMOVE;
            /** 获取成功 - GET /api/admin/user/role */
            public static final String GET_SUCCESS = Success.GET;
            /** 参数错误 - 通用参数验证 */
            public static final String PARAMETER_ERROR = Error.PARAMETER;
            /** 邮箱已存在 - POST /api/admin/user/save */
            public static final String EMAIL_REPEAT = Error.EMAIL_REPEAT;
        }
        
        /**
         * 重复提示键集合
         * 用于生成重复错误消息：MessageUtils.getMessage(Error.REPEAT, MessageUtils.getMessage(Admin.Repeat.ROLE_NAME))
         */
        public static final class Repeat {
            private Repeat() {}
            /** 角色名称 - POST /api/admin/role/save */
            public static final String ROLE_NAME = MessageKeys.Repeat.ROLE_NAME;
            /** 路径 - POST /api/admin/menu/save */
            public static final String PATH = MessageKeys.Repeat.PATH;
            /** 电影票类型名称 - POST /api/admin/movieTicketType/save */
            public static final String MOVIE_TICKET_TYPE_NAME = MessageKeys.Repeat.MOVIE_TICKET_TYPE_NAME;
            /** 影厅名称 - POST /api/admin/theaterHall/save */
            public static final String THEATER_HALL_NAME = MessageKeys.Repeat.THEATER_HALL_NAME;
            /** 职位名称 - POST /api/admin/position/save */
            public static final String POSITION_NAME = MessageKeys.Repeat.POSITION_NAME;
            /** 语言代码 - POST /api/admin/language/save */
            public static final String LANGUAGE_CODE = MessageKeys.Repeat.LANGUAGE_CODE;
            /** 品牌名称 - POST /api/admin/brand/save */
            public static final String BRAND_NAME = MessageKeys.Repeat.BRAND_NAME;
            /** 标签名称 - POST /api/admin/tag/save */
            public static final String MOVIE_TAG_NAME = MessageKeys.Repeat.MOVIE_TAG_NAME;
            /** 场次标签名称 - POST /api/admin/showTimeTag/save */
            public static final String SHOW_TIME_TAG_NAME = MessageKeys.Repeat.SHOW_TIME_TAG_NAME;
        }
    }
    
    /**
     * 通用模块消息键（不区分端）
     * 
     * <p>相关接口：/api/movie/*, /api/user/* 等公共接口</p>
     */
    public static final class Common {
        private Common() {}
        
        /**
         * 通用 - 电影模块
         * 
         * <p>相关接口：</p>
         * <ul>
         *   <li>GET /api/movie/list - 电影列表</li>
         *   <li>GET /api/movie/detail - 电影详情</li>
         *   <li>GET /api/movie/staff - 演职员</li>
         *   <li>GET /api/movie/character - 角色</li>
         *   <li>GET /api/movie/version/list - 版本列表</li>
         * </ul>
         */
        public static final class Movie {
            private Movie() {}
            /** 获取成功 - GET /api/movie/* */
            public static final String GET_SUCCESS = Success.GET;
            
            /**
             * 评论相关
             * 
             * <p>相关接口：POST /api/movie/comment/rate</p>
             */
            public static final class Comment {
                private Comment() {}
                /** 用户已评分 - 评论接口 */
                public static final String USER_RATED = "error.comment.userRated";
            }
        }
        
        /**
         * 通用 - 用户模块
         * 
         * <p>相关接口：POST /api/user/login, /api/user/logout, /api/user/register</p>
         */
        public static final class User {
            private User() {}
            /** 登录成功 - POST /api/user/login */
            public static final String LOGIN_SUCCESS = Success.LOGIN;
            /** 退出成功 - POST /api/user/logout */
            public static final String LOGOUT_SUCCESS = Success.LOGOUT;
            /** 发送成功 - POST /api/verify/send */
            public static final String SEND_SUCCESS = Success.SEND;
            /** 登录已过期 - 认证相关 */
            public static final String LOGIN_EXPIRED = Error.LOGIN_EXPIRED;
            /** 用户不存在 - 用户相关 */
            public static final String NOT_FOUND = Error.USER_NOT_FOUND;
            /** 邮箱已存在 - POST /api/user/register, POST /api/user/update */
            public static final String EMAIL_REPEAT = Error.EMAIL_REPEAT;
            
            /**
             * 验证码相关
             * 
             * <p>相关接口：POST /api/verify/send</p>
             */
            public static final class VerifyCode {
                private VerifyCode() {}
                /** 验证码标题 - 邮件发送 */
                public static final String TITLE = "email.verifyCode.title";
                /** 验证码内容 - 邮件发送 */
                public static final String CONTENT = "email.verifyCode.content";
            }
        }
    }
    
    
    /**
     * 上传模块消息键（通用，不区分端）
     * 
     * <p>相关接口：</p>
     * <ul>
     *   <li>POST /api/upload</li>
     *   <li>DELETE /api/upload/remove</li>
     * </ul>
     */
    public static final class Upload {
        private Upload() {}
        
        /** 上传成功 - POST /api/upload */
        public static final String SUCCESS = Success.UPLOAD;
        /** 上传失败 - POST /api/upload */
        public static final String ERROR = Error.UPLOAD;
        /** 删除成功 - DELETE /api/upload/remove */
        public static final String REMOVE_SUCCESS = Success.REMOVE;
    }
    
    
    /**
     * 验证器消息键
     * 
     * <p>用于 @Valid 注解的参数验证</p>
     */
    public static final class Validator {
        private Validator() {}
        
        /**
         * 登录验证
         * 
         * <p>相关接口：POST /api/user/login</p>
         */
        public static final class Login {
            private Login() {}
            /** 密码不能为空 */
            public static final String PASSWORD_REQUIRED = "validator.login.password.required";
            /** 邮箱不能为空 */
            public static final String EMAIL_REQUIRED = "validator.login.email.required";
            /** 邮箱格式错误 */
            public static final String EMAIL_NOT_EMAIL = "validator.login.email.notEmail";
        }
        
        /**
         * 注册验证
         * 
         * <p>相关接口：POST /api/user/register</p>
         */
        public static final class Register {
            private Register() {}
            /** 用户名不能为空 */
            public static final String NAME_REQUIRED = "validator.register.name.required";
            /** 密码不能为空 */
            public static final String PASSWORD_REQUIRED = "validator.register.password.required";
            /** 邮箱不能为空 */
            public static final String EMAIL_REQUIRED = "validator.register.email.required";
            /** 邮箱格式错误 */
            public static final String EMAIL_NOT_EMAIL = "validator.register.email.notEmail";
        }
        
        /**
         * 保存用户验证
         * 
         * <p>相关接口：POST /api/admin/user/save</p>
         */
        public static final class SaveUser {
            private SaveUser() {}
            /** 密码不能为空 */
            public static final String PASSWORD_REQUIRED = "validator.saveUser.password.required";
            /** 验证码已过期 */
            public static final String CODE_EXPIRED = "validator.saveUser.code.expired";
            /** 验证码错误 */
            public static final String CODE_ERROR = "validator.saveUser.code.error";
        }
    }
}
