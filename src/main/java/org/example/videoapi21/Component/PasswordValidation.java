package org.example.videoapi21.Component;

import org.springframework.stereotype.Component;

@Component
public class PasswordValidation {

    //Logger logger = LoggerFactory.getLogger(PasswordValidation.class);

    /**
     * Check for password strength. Must be atleast 6 characters long.
     * <p>
     * Method checks for the following (1 point for each):
     * <p>
     * 1. Password length > 8
     * <p>
     * 2. Contains special character
     * <p>
     * 3. Does not contain username
     * <p>
     * 4. Contains digit
     * <p>
     * 5. Contains uppercase letter
     *
     *
     * @param password        Password to be checked.
     * @param username        Username related to the password.
     * @param passwordStrength Minimum strength of the password. (0-5)
     * @return true if password is valid, false otherwise.
     */
    public boolean isValidPassword(String password, String username, Integer passwordStrength){
        if(password.length() < 6){
            return false;
        }
        return rankPassword(password, username) >= passwordStrength;

    }
    private Integer rankPassword(String password, String username){
        Integer rank = 0;

        if(password.length() > 8){
            rank++;
        }
        if(containsSpecialCharacter(password)){
            rank++;
        }
        if(!password.toLowerCase().contains(username.toLowerCase())){
            rank++;
        }
        if(containsDigit(password)){
            rank++;
        }
        if(containsUpperCase(password)){
            rank++;
        }
        //logger.info("Password rank: " + rank);
        return rank;
    }
    private boolean containsSpecialCharacter(String password){
        char[] passwordArray = password.toCharArray();
        for(char c : passwordArray){
            if((c >= 33 && c <= 47) || (c >= 58 && c <= 64) || (c >= 91 && c <= 96) || (c >= 123 && c <= 126)){
                return true;
            }
        }
        return false;
    }
    private boolean containsDigit(String password){
        char[] passwordArray = password.toCharArray();
        for(char c : passwordArray){
            if(c >= 48 && c <= 57){
                return true;
            }
        }
        return false;
    }
    private boolean containsUpperCase(String password){
        char[] passwordArray = password.toCharArray();
        for(char c : passwordArray){
            if(c >= 65 && c <= 90){
                return true;
            }
        }
        return false;
    }



}
