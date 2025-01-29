package com.atguigu.lease;

import io.minio.MinioClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class Test111 {
    @Autowired
    MinioClient client;
    @Test
    void t1 () {

        Assertions.assertNotNull(client, "client IS NUll");
    }
}
