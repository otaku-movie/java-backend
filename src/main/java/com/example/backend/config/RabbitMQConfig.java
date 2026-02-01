package com.example.backend.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class RabbitMQConfig implements ApplicationListener<ContextRefreshedEvent> {
  public static final String QUEUE_NAME = "demo.queue";
  public static final String EXCHANGE_NAME = "demo.exchange";
  public static final String ROUTING_KEY = "demo.key";

  // 选座延迟取消相关配置（使用 TTL + DLX 方案）
  public static final String SEAT_SELECTION_CANCEL_QUEUE = "seat.selection.cancel.queue";
  public static final String SEAT_SELECTION_CANCEL_DELAY_QUEUE = "seat.selection.cancel.delay.queue";
  public static final String SEAT_SELECTION_CANCEL_EXCHANGE = "seat.selection.cancel.exchange";
  public static final String SEAT_SELECTION_CANCEL_DLX_EXCHANGE = "seat.selection.cancel.dlx.exchange";
  public static final String SEAT_SELECTION_CANCEL_ROUTING_KEY = "seat.selection.cancel";

  // 订单创建相关配置
  public static final String ORDER_CREATE_QUEUE = "order.create.queue";
  public static final String ORDER_CREATE_EXCHANGE = "order.create.exchange";
  public static final String ORDER_CREATE_ROUTING_KEY = "order.create";
  
  // 订单超时相关配置（使用 TTL + DLX 方案）
  public static final String ORDER_TIMEOUT_QUEUE = "order.timeout.queue";
  public static final String ORDER_TIMEOUT_DELAY_QUEUE = "order.timeout.delay.queue";
  public static final String ORDER_TIMEOUT_EXCHANGE = "order.timeout.exchange";
  public static final String ORDER_TIMEOUT_DLX_EXCHANGE = "order.timeout.dlx.exchange";
  public static final String ORDER_TIMEOUT_ROUTING_KEY = "order.timeout";

  @Autowired
  private ConnectionFactory connectionFactory;
  
  @Autowired
  private ApplicationContext applicationContext;

  @Bean
  public Queue queue() {
    return new Queue(QUEUE_NAME, true);
  }

  @Bean
  public DirectExchange exchange() {
    return new DirectExchange(EXCHANGE_NAME);
  }

  @Bean
  public Binding binding(Queue queue, DirectExchange exchange) {
    return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);
  }

  /**
   * 选座延迟取消 - 死信交换机（DLX）
   * 当延迟队列中的消息过期后，会被发送到这个交换机
   */
  @Bean
  public DirectExchange seatSelectionCancelDlxExchange() {
    return new DirectExchange(SEAT_SELECTION_CANCEL_DLX_EXCHANGE, true, false);
  }

  /**
   * 选座延迟取消 - 实际处理队列
   * 绑定到死信交换机，接收过期后的消息
   */
  @Bean
  public Queue seatSelectionCancelQueue() {
    return QueueBuilder.durable(SEAT_SELECTION_CANCEL_QUEUE).build();
  }

  @Bean
  public Binding seatSelectionCancelBinding(Queue seatSelectionCancelQueue, DirectExchange seatSelectionCancelDlxExchange) {
    return BindingBuilder.bind(seatSelectionCancelQueue)
        .to(seatSelectionCancelDlxExchange)
        .with(SEAT_SELECTION_CANCEL_ROUTING_KEY);
  }

  /**
   * 选座延迟取消 - 延迟队列（带 TTL）
   * 消息发送到这个队列，设置 TTL，过期后发送到死信交换机
   */
  @Bean
  public Queue seatSelectionCancelDelayQueue() {
    Map<String, Object> args = new HashMap<>();
    // 设置死信交换机
    args.put("x-dead-letter-exchange", SEAT_SELECTION_CANCEL_DLX_EXCHANGE);
    // 设置死信路由键
    args.put("x-dead-letter-routing-key", SEAT_SELECTION_CANCEL_ROUTING_KEY);
    // 注意：TTL 在发送消息时通过消息属性设置，这里不设置队列级别的 TTL
    return QueueBuilder.durable(SEAT_SELECTION_CANCEL_DELAY_QUEUE).withArguments(args).build();
  }

  /**
   * 选座延迟取消 - 普通交换机
   * 用于接收消息并路由到延迟队列
   */
  @Bean
  public DirectExchange seatSelectionCancelExchange() {
    return new DirectExchange(SEAT_SELECTION_CANCEL_EXCHANGE, true, false);
  }

  @Bean
  public Binding seatSelectionCancelDelayBinding(Queue seatSelectionCancelDelayQueue, DirectExchange seatSelectionCancelExchange) {
    return BindingBuilder.bind(seatSelectionCancelDelayQueue)
        .to(seatSelectionCancelExchange)
        .with(SEAT_SELECTION_CANCEL_ROUTING_KEY);
  }

  // 订单创建队列配置
  @Bean
  public Queue orderCreateQueue() {
    return QueueBuilder.durable(ORDER_CREATE_QUEUE).build();
  }

  @Bean
  public DirectExchange orderCreateExchange() {
    return new DirectExchange(ORDER_CREATE_EXCHANGE);
  }

  @Bean
  public Binding orderCreateBinding(Queue orderCreateQueue, DirectExchange orderCreateExchange) {
    return BindingBuilder.bind(orderCreateQueue).to(orderCreateExchange).with(ORDER_CREATE_ROUTING_KEY);
  }

  // 订单超时处理队列配置（使用 TTL + DLX 方案）
  /**
   * 订单超时 - 死信交换机（DLX）
   * 当延迟队列中的消息过期后，会被发送到这个交换机
   */
  @Bean
  public DirectExchange orderTimeoutDlxExchange() {
    return new DirectExchange(ORDER_TIMEOUT_DLX_EXCHANGE, true, false);
  }

  /**
   * 订单超时 - 实际处理队列
   * 绑定到死信交换机，接收过期后的消息
   */
  @Bean
  public Queue orderTimeoutQueue() {
    return QueueBuilder.durable(ORDER_TIMEOUT_QUEUE).build();
  }

  @Bean
  public Binding orderTimeoutBinding(Queue orderTimeoutQueue, DirectExchange orderTimeoutDlxExchange) {
    return BindingBuilder.bind(orderTimeoutQueue)
        .to(orderTimeoutDlxExchange)
        .with(ORDER_TIMEOUT_ROUTING_KEY);
  }

  /**
   * 订单超时 - 延迟队列（带 TTL）
   * 消息发送到这个队列，设置 TTL，过期后发送到死信交换机
   */
  @Bean
  public Queue orderTimeoutDelayQueue() {
    Map<String, Object> args = new HashMap<>();
    // 设置死信交换机
    args.put("x-dead-letter-exchange", ORDER_TIMEOUT_DLX_EXCHANGE);
    // 设置死信路由键
    args.put("x-dead-letter-routing-key", ORDER_TIMEOUT_ROUTING_KEY);
    return QueueBuilder.durable(ORDER_TIMEOUT_DELAY_QUEUE).withArguments(args).build();
  }

  /**
   * 订单超时 - 普通交换机
   * 用于接收消息并路由到延迟队列
   */
  @Bean
  public DirectExchange orderTimeoutExchange() {
    return new DirectExchange(ORDER_TIMEOUT_EXCHANGE, true, false);
  }

  @Bean
  public Binding orderTimeoutDelayBinding(Queue orderTimeoutDelayQueue, DirectExchange orderTimeoutExchange) {
    return BindingBuilder.bind(orderTimeoutDelayQueue)
        .to(orderTimeoutExchange)
        .with(ORDER_TIMEOUT_ROUTING_KEY);
  }

  // 配置消息转换器，支持 JSON 序列化
  @Bean
  public MessageConverter messageConverter() {
    return new Jackson2JsonMessageConverter();
  }

  /**
   * 应用启动后，清理并重新创建队列和绑定，确保配置正确
   * 解决 RabbitMQ 中存在旧绑定导致消息立即被消费的问题
   */
  @Override
  public void onApplicationEvent(ContextRefreshedEvent event) {
    try {
      // 从 ApplicationContext 获取 RabbitAdmin，避免循环依赖
      RabbitAdmin rabbitAdmin = applicationContext.getBean(RabbitAdmin.class);
      
      log.info("【RabbitMQ 配置检查】开始清理并重新创建队列和绑定...");
      
      // 删除可能存在的错误绑定（处理队列直接绑定到普通交换机）
      try {
        Binding wrongBinding = BindingBuilder
            .bind(QueueBuilder.durable(SEAT_SELECTION_CANCEL_QUEUE).build())
            .to(new DirectExchange(SEAT_SELECTION_CANCEL_EXCHANGE))
            .with(SEAT_SELECTION_CANCEL_ROUTING_KEY);
        rabbitAdmin.removeBinding(wrongBinding);
        log.warn("【RabbitMQ 配置检查】已删除错误的绑定: {} -> {}", 
            SEAT_SELECTION_CANCEL_QUEUE, SEAT_SELECTION_CANCEL_EXCHANGE);
      } catch (Exception e) {
        // 绑定不存在是正常的，忽略错误
        log.debug("【RabbitMQ 配置检查】错误绑定不存在（这是正常的）: {}", e.getMessage());
      }
      
      // 重新声明队列和绑定（确保配置正确）
      // 创建队列和交换机实例
      Queue delayQueue = QueueBuilder.durable(SEAT_SELECTION_CANCEL_DELAY_QUEUE)
          .withArgument("x-dead-letter-exchange", SEAT_SELECTION_CANCEL_DLX_EXCHANGE)
          .withArgument("x-dead-letter-routing-key", SEAT_SELECTION_CANCEL_ROUTING_KEY)
          .build();
      Queue processQueue = QueueBuilder.durable(SEAT_SELECTION_CANCEL_QUEUE).build();
      DirectExchange normalExchange = new DirectExchange(SEAT_SELECTION_CANCEL_EXCHANGE, true, false);
      DirectExchange dlxExchange = new DirectExchange(SEAT_SELECTION_CANCEL_DLX_EXCHANGE, true, false);
      
      // 声明队列和交换机
      rabbitAdmin.declareQueue(delayQueue);
      rabbitAdmin.declareQueue(processQueue);
      rabbitAdmin.declareExchange(normalExchange);
      rabbitAdmin.declareExchange(dlxExchange);
      
      // 创建并声明绑定
      Binding delayBinding = BindingBuilder.bind(delayQueue)
          .to(normalExchange)
          .with(SEAT_SELECTION_CANCEL_ROUTING_KEY);
      Binding processBinding = BindingBuilder.bind(processQueue)
          .to(dlxExchange)
          .with(SEAT_SELECTION_CANCEL_ROUTING_KEY);
      
      rabbitAdmin.declareBinding(delayBinding);
      rabbitAdmin.declareBinding(processBinding);
      
      log.info("【RabbitMQ 配置检查】队列和绑定已重新创建完成");
      log.info("  - 延迟队列: {} (绑定到: {})", 
          SEAT_SELECTION_CANCEL_DELAY_QUEUE, SEAT_SELECTION_CANCEL_EXCHANGE);
      log.info("  - 处理队列: {} (绑定到: {})", 
          SEAT_SELECTION_CANCEL_QUEUE, SEAT_SELECTION_CANCEL_DLX_EXCHANGE);
    } catch (Exception e) {
      log.error("【RabbitMQ 配置检查】清理和重新创建队列时出错: {}", e.getMessage(), e);
    }
  }
}
