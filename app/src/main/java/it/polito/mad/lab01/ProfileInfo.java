package it.polito.mad.lab01;

/**
 * Created by eugeniogallea on 20/03/18.
 */

public class ProfileInfo {
    private static String email = null;
    private static String username = null;
    private static String location = null;
    private static String description = null;

    private static final ProfileInfo instance = new ProfileInfo();

    private ProfileInfo(){}

    public static ProfileInfo getInstance(){
        return instance;
    }

    public static String getEmail() {
        return email;
    }

    public static void setEmail(String email) {
        ProfileInfo.email = email;
    }

    public static String getUsername() {
        return username;
    }

    public static void setUsername(String username) {
        ProfileInfo.username = username;
    }

    public static String getLocation() {
        return location;
    }

    public static void setLocation(String location) {
        ProfileInfo.location = location;
    }

    public static String getDescription() {
        return description;
    }

    public static void setDescription(String description) {
        ProfileInfo.description = description;
    }

}
