package org.example.videoapi21.Config;

import org.example.videoapi21.Security.Password.PasswordValidator;
import org.example.videoapi21.Security.Password.Rules.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class PasswordValidationConfig {
    @Bean
    public PasswordValidator passwordValidator() {
        return new PasswordValidator(List.of(
                new LengthRule(),
                new DigitRule(),
                new UpperCaseRule(),
                new NoUsernameRule(),
                new SpecialCharacterRule()
        ));
    }
}
