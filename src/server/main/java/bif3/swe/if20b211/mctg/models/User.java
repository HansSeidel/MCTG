package bif3.swe.if20b211.mctg.models;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class User {
    private String username;
    private String password;
    private String token;

    public User(String username, String password){
        this.username = username;
        this.password = this.getHashedPassword256(password);
    }

    private String getHashedPassword256(String password){
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(
                    password.getBytes(StandardCharsets.UTF_8));
            return encodedhash.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}
