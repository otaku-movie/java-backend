package com.example.backend.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

@RestController
public class OrderController {

  private static final Random random = new Random();

  public static String generateOrderId(long userId, int seatCount, String seatCoordinates) {
    // 获取当前时间戳（毫秒级）
    long timestamp = System.currentTimeMillis();

    // 获取用户ID前后各一位
    String userIdStr = String.format("%02d", userId);
    char firstChar = userIdStr.charAt(0);
    char lastChar = userIdStr.charAt(userIdStr.length() - 1);

    // 生成5位随机数
    int randomInt = random.nextInt(100000);
    String randomStr = String.format("%05d", randomInt);

    // 组合订单号
    return timestamp + "" + firstChar + lastChar + seatCount + seatCoordinates + randomStr;
  }


  @PostMapping("/api/order/save")
  public void createOrder() {
    long userId = 1234;
    int seatCount = 5;
    String seatCoordinates = "0123";
    int totalOrders = 1000000;  // 生成10万订单号
    Set<String> orderIds = new HashSet<>();

    for (int i = 0; i < totalOrders; i++) {
      String orderId = generateOrderId(userId, seatCount, seatCoordinates);
      orderIds.add(orderId);
    }

    int duplicates = totalOrders - orderIds.size();
    System.out.println("Total Orders: " + totalOrders);
    System.out.println("Unique Orders: " + orderIds.size());
    System.out.println("Duplicate Orders: " + duplicates);
    System.out.println("Duplicate Rate: " + ((double) duplicates / totalOrders) * 100 + "%");
  }
}
