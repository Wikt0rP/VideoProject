package org.example.videoapi21.Security.Password.Rules;

public class DigitRule implements PasswordRule{

    @Override
    public int check(String password, String username) {
        char[] passwordArray = password.toCharArray();
        for(char c : passwordArray){
            if(c >= 48 && c <= 57){
                return 1;
            }
        }
        return 0;
    }
}
