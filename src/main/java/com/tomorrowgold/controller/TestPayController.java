package com.tomorrowgold.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.tomorrowgold.common.constant.Constants;
import com.tomorrowgold.facade.TestPayFacade;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Administrator on 2018/6/30.
 */
@RestController
public class TestPayController {

    @Reference(version = Constants.DUBBO_SERVICE_VERSION)
    private TestPayFacade testPayFacade;

    @RequestMapping("/testPay")
    public void testPay(){
        testPayFacade.pay();
    }
}
