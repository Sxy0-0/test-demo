package com.demo.controller;

import com.demo.mapper.IndexMapper;
import com.demo.properties.DemoProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IndexController {

    @Autowired
    private DemoProperties demoProperties;
    @Autowired
    private IndexMapper indexMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    @GetMapping("/index1")
    public String index(){
        return demoProperties.getName() + demoProperties.getTitle();
    }

    @GetMapping("/index")
    public String index1(){
        String name = indexMapper.selectById(1).getName();
        redisTemplate.opsForValue().set("test",name);
        return name;
    }

}
