package bif3.swe.if20b211.mctg.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class User {
    private String username;
    private String password;
    private String token;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public User(@JsonProperty("username") String username,@JsonProperty("password") String password){
        this.username = username;
        this.password = this.getHashedPassword256(password);
    }

    @JsonIgnore
    private String getHashedPassword256(String password){
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(
                    password.getBytes(StandardCharsets.UTF_8));
            BigInteger number = new BigInteger(1,encodedhash);
            StringBuilder hexString = new StringBuilder(number.toString(16));
            while (hexString.length() < 32){
                hexString.insert(0,'0');
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    @JsonIgnore
    public boolean isLoggedIn() {
        if(this.token == null) return false; //To prevent NullPointerException
        return !(token.isEmpty());
    }
    @JsonIgnore
    public void clearPassword() {
        this.password = null;
    }

    //Getter and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

}
