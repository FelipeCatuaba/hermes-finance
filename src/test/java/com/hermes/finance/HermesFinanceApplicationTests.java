package com.hermes.finance;

import com.hermes.finance.config.TestJwtDecoderConfig;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.mockito.Mockito.mockStatic;

@SpringBootTest
@Import(TestJwtDecoderConfig.class)
class HermesFinanceApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void mainShouldDelegateToSpringApplicationRun() {
        try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
            springApplication.when(() -> SpringApplication.run(HermesFinanceApplication.class, new String[]{}))
                .thenReturn(null);

            HermesFinanceApplication.main(new String[]{});

            springApplication.verify(() -> SpringApplication.run(HermesFinanceApplication.class, new String[]{}));
        }
    }
}
