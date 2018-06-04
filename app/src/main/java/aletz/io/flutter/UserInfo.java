package aletz.io.flutter;

public class UserInfo {

    private String bio;
    private String photoURL;
    private String title;
    private String username;

    public UserInfo() {}

    public UserInfo(String bio, String photoURL, String title, String username) {
        this.bio = bio;
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

}
