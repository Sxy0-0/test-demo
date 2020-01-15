package com.demo.feign;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.support.spring.FastJsonContainer;
import org.springframework.stereotype.Component;

/**
 * @author songxy
 * @date 2020/1/3
 */
@Component
public class DetailPhoneFallback implements DetailPhoneClient {

    @Override
    public JSONObject getDetailByPhone(String tel) {
        return null;
    }

}
