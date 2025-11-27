package org.example.videoapi21.Security.Password.Rules;

import java.util.Locale;

public class NoUsernameRule implements PasswordRule{
    @Override
    public int check(String password, String username) {

        if(!password.toLowerCase(Locale.ROOT).contains(username.toLowerCase(Locale.ROOT))){
            return 1;
        }
        return 0;
    }
}
