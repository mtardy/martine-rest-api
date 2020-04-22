package models.format;

import models.Utilisateur;

public class UtilisateurCompact {
    private String username;
    private String fullname;
    private String biographie;
    private String email;
    private String photo;

    public UtilisateurCompact(Utilisateur u) {
        this.username = u.getUsername();
        this.fullname = u.getFullname();
        this.biographie = u.getBiographie();
        this.email = u.getEmail();
        this.photo = u.getPhoto();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getBiographie() {
        return biographie;
    }

    public void setBiographie(String biographie) {
        this.biographie = biographie;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }
}
