package com.mhyy.sms.controller;

import com.mhyy.sms.service.SmsService;
import com.mhyy.sms.test.ResponseVO;
import com.mhyy.sms.test.TestVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.xml.ws.Response;

@RestController
@RequestMapping("/sms")
public class SmsContorller {

    @Autowired
    private SmsService service;

    @RequestMapping("/send-msg-code")
    public void sendSms(@RequestParam String phoneNumber){
        service.sendSms(phoneNumber);
    }
}
