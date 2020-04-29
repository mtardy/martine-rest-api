package models.format;

import models.Utilisateur;

public class UtilisateurCompact {
    private Utilisateur u;

    public UtilisateurCompact(Utilisateur u) {
        this.u = u;
    }

    public String getUsername() {
        return u.getUsername();
    }

    public String getFullname() {
        return u.getFullname();
    }
}
