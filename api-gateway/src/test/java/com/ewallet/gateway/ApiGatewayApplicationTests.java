package com.ewallet.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "eureka.client.enabled=false",
    "spring.cloud.discovery.enabled=false",
    "spring.cloud.gateway.discovery.locator.enabled=false",
    "spring.data.redis.host=localhost",
    "spring.data.redis.port=6379"
})
class ApiGatewayApplicationTests {

    @Test
    void contextLoads() {
    }
}
