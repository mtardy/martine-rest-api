package models.format;

import models.Utilisateur;

public class UtilisateurCompact {
    private String username;

    public UtilisateurCompact(Utilisateur u) {
        this.username = u.getUsername();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
