package aletz.io.flutter;
/**
 * Created by Andrew Letz on 6-1-18
 * Last modified by Andrew Letz on 6-4-18
 */

public class UserInfo {

    private String bio;
    private String email;
    private String phone;
    private String photoURL;
    private String title;
    private String username;

    public UserInfo() {}

    /**
     * Class used to keep track of a Flutter users information in the Firebase real time database
     * @param bio
     * @param email
     * @param phone
     * @param photoURL
     * @param title
     * @param username
     */
    public UserInfo(String bio, String email, String phone, String photoURL, String title, String username) {
        this.bio = bio;
        this.email = email;
        this.phone = phone;
        this.photoURL = photoURL;
        this.title = title;
        this.username = username;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getPhotoURL() {
        return photoURL;
    }

    public void setPhotoURL(String photoURL) {
        this.photoURL = photoURL;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }

    public void setPhone(String phone) { this.phone = phone; }
}
