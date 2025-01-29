package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.web.app.service.ApartmentInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ApartmentInfoServiceImplTest {
    @Autowired
    private ApartmentInfoService apartmentInfoService;
    @Test
    void test01() {
        System.out.println(apartmentInfoService.getItemById(9L));
    }

}