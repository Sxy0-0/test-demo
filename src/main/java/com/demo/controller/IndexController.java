package com.demo.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.demo.entity.IndexTest;
import com.demo.feign.DetailPhoneClient;
import com.demo.mapper.IndexMapper;
import com.demo.properties.DemoProperties;
import com.demo.service.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IndexController {

    @Autowired
    private Publisher publisher;
    @Autowired
    private DemoProperties demoProperties;
    @Autowired
    private IndexMapper indexMapper;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private DetailPhoneClient detailPhoneClient;

    private static final String cacheName = "cacheTest";

    @GetMapping("/index1")
    public String index(){
        return demoProperties.getName() + demoProperties.getTitle();
    }

    @GetMapping("/index")
    public String index1(){
        String test = (String)redisTemplate.opsForValue().get("test");
        if (test != null){
            System.out.println("redis取值");
            return test;
        }
        String name = indexMapper.selectById(1).getName();
        redisTemplate.opsForValue().set("test",name);
        return name;
    }

    @GetMapping("setIndex/{id}")
    public String setIndexById(@PathVariable int id){
        IndexTest indexTest = new IndexTest();
        indexTest.setId(id);
        indexTest.setName(id+"name");

        redisTemplate.opsForValue().set("key-" + id ,indexTest);

        return "成功";
    }

    @GetMapping("getIndex/{id}")
    public String getIndexById(@PathVariable int id){
        String s = redisTemplate.opsForValue().get("key-" + id) + "";
        System.out.println(s);
        IndexTest o = (IndexTest)redisTemplate.opsForValue().get("key-"+id);
        if (o == null){
            return "结果为空";
        }else{
            return o.toString();
        }
    }

    @GetMapping("set/{id}")
    @Cacheable(value = cacheName , key = "#id + '-'+#name",unless = "#result == null")
    public IndexTest testIndex(@PathVariable int id){
        IndexTest indexTest = new IndexTest();
        indexTest.setId(id);
        indexTest.setName(id+"name");

        return (IndexTest)indexTest;
    }

    @GetMapping("cacheEvict")
    @CacheEvict(value = cacheName,allEntries = true,beforeInvocation = true)
    public void testCacheEvict(){
        System.out.println("清空缓存");
        return;
    }

    @GetMapping("getPhoneCity/{phone}")
    public String getPhoneCity(@PathVariable String phone){
        String cityByPhone = getCityByPhone(phone);
        return cityByPhone;
    }
    @GetMapping("publish")
    public String publish(){
        publisher.publish("测试");
        return "完成";
    }


    /**
     * 用手机号查询所属地域
     *
     * @param phone 手机号
     * @return 所属地域
     */
    private String getCityByPhone(String phone) {
        JSONObject detailByPhone = detailPhoneClient.getDetailByPhone(phone);
        System.out.println("查询手机号所属地区：" + detailByPhone);
        // 获取手机号信息的数据
        if (!detailByPhone.containsKey("response")) {
            return null;
        }
        JSONObject response = detailByPhone.getJSONObject("response");
        if (response != null){
            JSONObject jsonObject = response.getJSONObject(phone);
            String location = jsonObject.getString("location");
            return location;
        }
        if (!response.containsKey(phone)) {
            return null;
        }
        JSONObject phoneDetail = response.getJSONObject(phone);
        if (!phoneDetail.containsKey("detail")) {
            return null;
        }
        JSONObject detail = phoneDetail.getJSONObject("detail");
        if (!detail.containsKey("area")) {
            return null;
        }
        JSONArray area = detail.getJSONArray("area");
        if (area.size() == 0) {
            return null;
        }
        JSONObject cityJsonObject = area.getJSONObject(0);
        if (!cityJsonObject.containsKey("city")) {
            return null;
        }
        return cityJsonObject.getString("city");
    }

}
