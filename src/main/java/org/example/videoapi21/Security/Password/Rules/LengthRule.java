package org.example.videoapi21.Security.Password.Rules;

public class LengthRule implements PasswordRule{
    @Override
    public int check(String password, String username){
        return password.length() > 8 ? 1 : 0;
    }

}
