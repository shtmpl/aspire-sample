package me.sample.config;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SpelConfiguration {
    @Bean
    public SpelExpressionParser expressionParser() {
        return new SpelExpressionParser();
    }
}
