package com.example.backend.controller.dev;

import com.example.backend.constants.ApiPaths;
import com.example.backend.entity.RestBean;
import com.example.backend.utils.MessageKeysChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 开发工具控制器
 * 仅在开发环境使用，用于执行各种检查和工具
 * 
 * 注意：生产环境应该禁用此控制器
 */
@RestController
public class DevToolsController {

    private static final Logger log = LoggerFactory.getLogger(DevToolsController.class);

    /**
     * 检查消息键一致性
     * GET /api/dev/check-message-keys
     */
    @GetMapping(ApiPaths.Dev.CHECK_MESSAGE_KEYS)
    public RestBean<String> checkMessageKeys() {
        try {
            log.info("手动触发消息键一致性检查...");
            MessageKeysChecker.check();
            MessageKeysChecker.generateReport();
            return RestBean.success("检查完成，请查看日志", "消息键一致性检查完成");
        } catch (Exception e) {
            log.error("执行消息键检查时发生错误", e);
            return RestBean.error(500, "检查失败: " + e.getMessage());
        }
    }

    /**
     * 生成消息键一致性报告
     * GET /api/dev/message-keys-report
     */
    @GetMapping(ApiPaths.Dev.MESSAGE_KEYS_REPORT)
    public RestBean<String> generateMessageKeysReport() {
        try {
            log.info("生成消息键一致性报告...");
            MessageKeysChecker.generateReport();
            return RestBean.success("报告已生成，请查看日志", "消息键一致性报告已生成");
        } catch (Exception e) {
            log.error("生成报告时发生错误", e);
            return RestBean.error(500, "生成报告失败: " + e.getMessage());
        }
    }
}
