package com.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class Publisher {

    @Autowired
    private RedisTemplate redisTemplate;


    public void publish(Object msg){
        redisTemplate.convertAndSend("demo-channel",msg);
    }

}
