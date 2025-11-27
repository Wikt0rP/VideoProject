package org.example.videoapi21.Security.Password.Rules;

public class SpecialCharacterRule implements PasswordRule{
    @Override
    public int check(String password, String username) {
        char[] passwordArray = password.toCharArray();
        for(char c : passwordArray){
            if((c >= 33 && c <= 47) || (c >= 58 && c <= 64)
                    || (c >= 91 && c <= 96) || (c >= 123 && c <= 126)){
                return 1; 
            }
        }
        return 0;
    }
}
