package aletz.io.flutter;

public class UserInfo {

    private String bio;
    private String email;
    private String phone;
    private String photoURL;
    private String title;
    private String username;

    public UserInfo() {}

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
