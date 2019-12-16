package com.demo.feign;

import com.alibaba.fastjson.JSONObject;
import com.demo.config.FeignConfig;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "test",
        url = "http://mobsec-dianhua.baidu.com/dianhua_api",
        configuration = FeignConfig.class
)
public interface DetailPhoneClient {

    @RequestMapping(value = "/open/location?tel={tel}&qq-pf-to=pcqq.c2c",method = RequestMethod.GET)
//    @Headers("Content-Type: text/html")
//    @RequestLine("GET /open/location?tel={tel}&qq-pf-to=pcqq.c2c")
    JSONObject getDetailByPhone(@RequestParam(value = "tel") String tel);

}
