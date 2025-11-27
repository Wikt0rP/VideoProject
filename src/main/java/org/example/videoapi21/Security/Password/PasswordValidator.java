package org.example.videoapi21.Security.Password;

import org.example.videoapi21.Security.Password.Rules.PasswordRule;

import java.util.List;

public class PasswordValidator {
    private final List<PasswordRule> rules;

    public PasswordValidator(List<PasswordRule> rules) {
        this.rules = rules;
    }

    public boolean isValidPassword(String password, String username, int minStrength){
        if(password.length() < 6){
            return false;
        }

        int score = 0;
        for(PasswordRule rule : rules){
            score += rule.check(password, username);
        }
        return score >= minStrength;
    }
}
