package com.fix4home.fix4home.config;

import com.fix4home.fix4home.repository.UserRepository;
import com.fix4home.fix4home.service.ServiceService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

/**
 * Test configuration for mocking dependencies
 */
@TestConfiguration
@Profile("test")
public class TestConfig {

    @Bean
    @Primary
    public UserRepository mockUserRepository() {
        return Mockito.mock(UserRepository.class);
    }

    @Bean
    @Primary
    public ServiceService mockServiceService() {
        return Mockito.mock(ServiceService.class);
    }
}
