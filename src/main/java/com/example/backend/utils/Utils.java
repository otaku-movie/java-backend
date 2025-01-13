package com.example.backend.utils;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.annotation.TableField;
import com.example.backend.config.MinIOConfig;
import io.minio.PutObjectArgs;
import io.minio.errors.*;
import org.springframework.cglib.core.Local;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    public static Integer getUserId () {
        String token = StpUtil.getTokenValue();
        Object id = StpUtil.getLoginIdByToken(token);

        return Integer.valueOf((String) id);
    }
    public static Map<String, String> parseRedisKey(String key) {
        Map<String, String> result = new HashMap<>();
        // 匹配 `{key:value}` 格式的内容
        Pattern pattern = Pattern.compile("\\{(\\w+):(\\w+)}");
        Matcher matcher = pattern.matcher(key);

        // 遍历匹配结果
        while (matcher.find()) {
            String field = matcher.group(1); // 获取键
            String value = matcher.group(2); // 获取值
            result.put(field, value);
        }
        return result;
    }
    public static String getRowName(int rowIndex) {
        StringBuilder rowName = new StringBuilder();
        rowIndex++; // 从 1 开始
        while (rowIndex > 0) {
            rowIndex--; // 调整索引范围到 0 开始
            rowName.insert(0, (char) ('A' + (rowIndex % 26)));
            rowIndex /= 26;
        }
        return rowName.toString();
    }

    public static File MultipartToFile(MultipartFile multipartFile) throws IOException {
        File file = new File(System.getProperty("java.io.tmpdir") + File.separator + multipartFile.getOriginalFilename());
        multipartFile.transferTo(file);

        return file;
    }
    public static void upload (InputStream stream, String path, String mime) throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        MinIOConfig minIOConfig = new MinIOConfig();


        minIOConfig.getMinioClient()
            .putObject(
              PutObjectArgs.builder()
                .bucket(minIOConfig.bucket)
                .object(path)
                .stream(stream, stream.available(), -1)
                .contentType(mime)
                .build()
            );
    }
    public static <T> List<String> excludeKeys(Class<T> entityClass, List<String> excludeFields)  {
        // 获取实体类的所有字段
        Field[] fields = entityClass.getDeclaredFields();
        List<String> allFields = new ArrayList<>();
        for (Field field : fields) {
            // 获取字段上 @TableField 注解的 value 值，如果存在
            TableField tableField = field.getAnnotation(TableField.class);
            if (tableField != null) {
                allFields.add(tableField.value());
            } else {
                allFields.add(field.getName());
            }
        }

        // 移除需要排除的字段
        allFields.removeAll(excludeFields);

        return  allFields;
    }
    public static Date getTimestamp (String date, String format) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(format);

        return sdf.parse(date);
    }
    public static String format (Date date, String format) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(format);

        return sdf.format(date);
    }
    public static LocalDate stringToDate (String date, String format) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return LocalDate.parse(date, formatter);

    }
}

