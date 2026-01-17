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

    /**
     * 将30小时制时间转换为24小时制时间
     * 30小时制：24:00-29:59 表示第二天的 00:00-05:59
     * 
     * @param timeStr 时间字符串，格式：yyyy-MM-dd HH:mm（30小时制时，小时可以是24-29）
     * @return 转换后的24小时制时间字符串，格式：yyyy-MM-dd HH:mm
     */
    public static String convert30HourTo24Hour(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) {
            return timeStr;
        }
        
        try {
            // 解析时间字符串：yyyy-MM-dd HH:mm
            String[] parts = timeStr.split(" ");
            if (parts.length != 2) {
                return timeStr; // 格式不正确，直接返回
            }
            
            String datePart = parts[0]; // yyyy-MM-dd
            String timePart = parts[1]; // HH:mm
            
            String[] timeParts = timePart.split(":");
            if (timeParts.length != 2) {
                return timeStr; // 格式不正确，直接返回
            }
            
            int hour = Integer.parseInt(timeParts[0]);
            String minute = timeParts[1];
            
            // 如果小时小于24，直接返回原时间
            if (hour < 24) {
                return timeStr;
            }
            
            // 如果小时 >= 24，转换为第二天的对应时间
            LocalDate date = LocalDate.parse(datePart, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            LocalDate nextDay = date.plusDays(1);
            int convertedHour = hour - 24;
            
            // 格式化为 yyyy-MM-dd HH:mm
            return String.format("%s %02d:%s", 
                nextDay.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), 
                convertedHour, 
                minute);
        } catch (Exception e) {
            // 解析失败，返回原字符串
            return timeStr;
        }
    }

    /**
     * 将24小时制时间转换为30小时制时间
     * 30小时制：24:00-29:59 表示第二天的 00:00-05:59
     * 
     * @param timeStr 时间字符串，格式：yyyy-MM-dd HH:mm（24小时制）
     * @return 转换后的30小时制时间字符串，格式：yyyy-MM-dd HH:mm（如果是第二天 00:00-05:59，则显示为前一天的 24:00-29:59）
     */
    public static String convert24HourTo30Hour(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) {
            return timeStr;
        }
        
        try {
            // 解析时间字符串：yyyy-MM-dd HH:mm
            String[] parts = timeStr.split(" ");
            if (parts.length != 2) {
                return timeStr; // 格式不正确，直接返回
            }
            
            String datePart = parts[0]; // yyyy-MM-dd
            String timePart = parts[1]; // HH:mm
            
            String[] timeParts = timePart.split(":");
            if (timeParts.length != 2) {
                return timeStr; // 格式不正确，直接返回
            }
            
            int hour = Integer.parseInt(timeParts[0]);
            String minute = timeParts[1];
            
            // 如果小时 >= 6，直接返回原时间
            if (hour >= 6) {
                return timeStr;
            }
            
            // 如果小时 0-5，转换为前一天的 24-29
            LocalDate date = LocalDate.parse(datePart, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            LocalDate previousDay = date.minusDays(1);
            int convertedHour = hour + 24;
            
            // 格式化为 yyyy-MM-dd HH:mm
            return String.format("%s %02d:%s", 
                previousDay.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), 
                convertedHour, 
                minute);
        } catch (Exception e) {
            // 解析失败，返回原字符串
            return timeStr;
        }
    }

    /**
     * 从时间字符串中提取时间部分（HH:mm）
     * 支持格式：yyyy-MM-dd HH:mm:ss、yyyy-MM-dd HH:mm、HH:mm:ss、HH:mm
     * 
     * @param timeStr 时间字符串
     * @return 时间部分（HH:mm 格式），如果无法提取则返回 null
     */
    public static String extractTimePart(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) {
            return null;
        }
        
        // 格式：yyyy-MM-dd HH:mm:ss 或 yyyy-MM-dd HH:mm
        if (timeStr.length() >= 16) {
            return timeStr.substring(11, 16); // 提取 HH:mm
        }
        // 格式：HH:mm:ss 或 HH:mm
        else if (timeStr.length() >= 5) {
            return timeStr.substring(0, 5); // 提取 HH:mm
        }
        
        return null;
    }

    /**
     * 检查时间是否在指定范围内（只比较时间部分，不考虑日期）
     * 
     * @param timeStr 要检查的时间字符串
     * @param timeFrom 起始时间（HH:mm 格式或完整时间字符串）
     * @param timeTo 结束时间（HH:mm 格式或完整时间字符串）
     * @return 如果时间在范围内返回 true，否则返回 false
     */
    public static boolean isTimeInRange(String timeStr, String timeFrom, String timeTo) {
        if (timeStr == null) {
            return false;
        }
        
        String itemTime = extractTimePart(timeStr);
        if (itemTime == null) {
            return false;
        }
        
        // 提取起始时间和结束时间的时间部分
        String fromTime = timeFrom != null ? extractTimePart(timeFrom) : null;
        String toTime = timeTo != null ? extractTimePart(timeTo) : null;
        
        // 比较时间部分
        boolean matchesFrom = fromTime == null || itemTime.compareTo(fromTime) >= 0;
        boolean matchesTo = toTime == null || itemTime.compareTo(toTime) <= 0;
        
        return matchesFrom && matchesTo;
    }
}

