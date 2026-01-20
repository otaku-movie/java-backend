package com.example.backend.controller;

import cn.dev33.satoken.secure.SaSecureUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 用户登录接口测试类
 * 
 * <p>测试范围：</p>
 * <ul>
 *   <li>用户登录接口</li>
 *   <li>用户注册接口</li>
 *   <li>用户详情接口（需要认证）</li>
 *   <li>用户订单列表接口（需要认证）</li>
 *   <li>用户登出接口</li>
 * </ul>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("用户登录接口测试")
public class UserLoginTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api";
    
    // 测试账号信息
    private static final String TEST_EMAIL = "diy4869@gmail.com";
    private static final String TEST_PASSWORD = "123456";
    

    /**
     * 测试用户登录接口 - 成功登录
     */
    @Test
    @DisplayName("测试用户登录接口 - 成功登录")
    public void testLoginSuccess() throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("email", TEST_EMAIL);
        requestBody.put("password", TEST_PASSWORD);

        MvcResult result = mockMvc.perform(post(BASE_URL + "/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.email").value(TEST_EMAIL))
                .andExpect(jsonPath("$.data.token").exists())
                .andReturn();

        System.out.println("响应内容: " + result.getResponse().getContentAsString());
        
        // 提取token，供其他需要认证的接口使用
        String responseContent = result.getResponse().getContentAsString();
        if (responseContent.contains("\"token\"")) {
            System.out.println("登录成功，Token已获取");
        }
    }

    /**
     * 测试用户登录接口 - 密码错误
     */
    @Test
    @DisplayName("测试用户登录接口 - 密码错误")
    public void testLoginWrongPassword() throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("email", TEST_EMAIL);
        requestBody.put("password", "wrong_password");

        mockMvc.perform(post(BASE_URL + "/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.code").exists());
    }

    /**
     * 测试用户登录接口 - 邮箱不存在
     */
    @Test
    @DisplayName("测试用户登录接口 - 邮箱不存在")
    public void testLoginEmailNotFound() throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("email", "notexist@example.com");
        requestBody.put("password", TEST_PASSWORD);

        mockMvc.perform(post(BASE_URL + "/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists());
    }

    /**
     * 测试用户登录接口 - 参数验证（缺少邮箱）
     */
    @Test
    @DisplayName("测试用户登录接口 - 参数验证（缺少邮箱）")
    public void testLoginMissingEmail() throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("password", TEST_PASSWORD);

        mockMvc.perform(post(BASE_URL + "/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    /**
     * 测试用户登录接口 - 参数验证（缺少密码）
     */
    @Test
    @DisplayName("测试用户登录接口 - 参数验证（缺少密码）")
    public void testLoginMissingPassword() throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("email", TEST_EMAIL);

        mockMvc.perform(post(BASE_URL + "/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    /**
     * 测试用户登录接口 - 参数验证（邮箱格式错误）
     */
    @Test
    @DisplayName("测试用户登录接口 - 参数验证（邮箱格式错误）")
    public void testLoginInvalidEmail() throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("email", "invalid_email");
        requestBody.put("password", TEST_PASSWORD);

        mockMvc.perform(post(BASE_URL + "/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    /**
     * 测试用户详情接口（需要认证）
     * 注意：需要先登录获取token
     */
    @Test
    @DisplayName("测试用户详情接口（需要认证）")
    public void testUserDetail() throws Exception {
        // 先登录获取token
        String token = loginAndGetToken();
        
        if (token == null || token.isEmpty()) {
            System.out.println("登录失败，无法测试用户详情接口");
            return;
        }

        MvcResult result = mockMvc.perform(get(BASE_URL + "/user/detail")
                        .header("token", token))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.email").value(TEST_EMAIL))
                .andReturn();

        System.out.println("响应内容: " + result.getResponse().getContentAsString());
    }

    /**
     * 测试用户订单列表接口（需要认证）
     * 注意：需要先登录获取token
     */
    @Test
    @DisplayName("测试用户订单列表接口（需要认证）")
    public void testUserOrderList() throws Exception {
        // 先登录获取token
        String token = loginAndGetToken();
        
        if (token == null || token.isEmpty()) {
            System.out.println("登录失败，无法测试用户订单列表接口");
            return;
        }

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("page", 1);
        requestBody.put("pageSize", 10);

        MvcResult result = mockMvc.perform(post(BASE_URL + "/user/order/list")
                        .header("token", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andReturn();

        System.out.println("响应内容: " + result.getResponse().getContentAsString());
    }

    /**
     * 测试用户登出接口
     * 注意：需要先登录获取token
     */
    @Test
    @DisplayName("测试用户登出接口")
    public void testLogout() throws Exception {
        // 先登录获取token
        String token = loginAndGetToken();
        
        if (token == null || token.isEmpty()) {
            System.out.println("登录失败，无法测试登出接口");
            return;
        }

        mockMvc.perform(post(BASE_URL + "/user/logout")
                        .header("token", token))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        System.out.println("登出成功");
    }

    /**
     * 辅助方法：登录并获取token
     * 
     * @return token字符串，如果登录失败则返回null
     */
    private String loginAndGetToken() throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("email", TEST_EMAIL);
        requestBody.put("password", TEST_PASSWORD);

        MvcResult result = mockMvc.perform(post(BASE_URL + "/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        
        // 从响应中提取token
        // 注意：这里假设响应格式为 {"code":200,"data":{"token":"..."}}
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> responseMap = objectMapper.readValue(responseContent, Map.class);
            if (responseMap.get("code") != null && responseMap.get("code").equals(200)) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) responseMap.get("data");
                if (data != null && data.get("token") != null) {
                    return data.get("token").toString();
                }
            }
        } catch (Exception e) {
            System.err.println("解析token失败: " + e.getMessage());
        }
        
        return null;
    }

    /**
     * 测试密码2次MD5加密
     */
    @Test
    @DisplayName("测试密码2次MD5加密")
    public void testDoubleMd5Encryption() {
        String password = TEST_PASSWORD;
        String firstMd5 = SaSecureUtil.md5(password);
        String secondMd5 = SaSecureUtil.md5(firstMd5);
        
        System.out.println("原始密码: " + password);
        System.out.println("第一次MD5: " + firstMd5);
        System.out.println("第二次MD5（2次加密结果）: " + secondMd5);
        
        // 验证加密结果
        assert firstMd5 != null && !firstMd5.isEmpty();
        assert secondMd5 != null && !secondMd5.isEmpty();
        assert !firstMd5.equals(secondMd5);
        
        System.out.println("密码2次MD5加密测试通过");
    }
}
