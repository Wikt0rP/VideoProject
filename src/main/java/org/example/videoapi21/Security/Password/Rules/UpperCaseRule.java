package org.example.videoapi21.Security.Password.Rules;

public class UpperCaseRule implements PasswordRule{
    @Override
    public int check(String password, String username) {
        char[] passwordArray = password.toCharArray();
        for(char c : passwordArray){
            if(c >= 65 && c <= 90){
                return 1;
            }
        }
        return 0;
    }
}
