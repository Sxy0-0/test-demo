package com.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RedisListener implements MessageListener {


    @Override
    public void onMessage(Message message, byte[] pattern) {
        log.info("从消息通道={}监听到消息",new String(pattern));
        log.info("从消息通道={}监听到消息",new String(message.getChannel()));
        log.info("元消息={}",new String(message.getBody()));

        RedisSerializer serializer=new GenericJackson2JsonRedisSerializer();
        log.info("\"反序列化后的消息={}",serializer.deserialize(message.getBody()));

    }

}
