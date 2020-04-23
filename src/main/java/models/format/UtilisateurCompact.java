package models.format;

import models.Commentaire;
import models.Utilisateur;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;

public class UtilisateurCompact {
    private String username;
    private String fullname;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    private String biographie;
    private String email;
    private String photo;
    private Collection<Commentaire> commentaires;
    private Collection<Integer> recettesIds;

    public UtilisateurCompact(Utilisateur u) {
        this.username = u.getUsername();
        this.fullname = u.getFullname();
        this.dateCreation = u.getDateInscription();
        this.dateModification = u.getDateModification();
        this.biographie = u.getBiographie();
        this.email = u.getEmail();
        this.photo = u.getPhoto();
        this.commentaires = u.getCommentaires();
        recettesIds = new ArrayList<>();
        u.getRecettes().forEach(recette -> {
            recettesIds.add(recette.getId());
        });
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

    public Collection<Commentaire> getCommentaires() {
        return commentaires;
    }

    public void setCommentaires(Collection<Commentaire> commentaires) {
        this.commentaires = commentaires;
    }

    public Collection<Integer> getRecettesIds() {
        return recettesIds;
    }

    public void setRecettesIds(Collection<Integer> recettesIds) {
        this.recettesIds = recettesIds;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public LocalDateTime getDateModification() {
        return dateModification;
    }

    public void setDateModification(LocalDateTime dateModification) {
        this.dateModification = dateModification;
    }
}
