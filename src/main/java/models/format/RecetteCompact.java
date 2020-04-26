package models.format;

import models.Recette;
import java.time.LocalDateTime;

public class RecetteCompact {
    private int id;
    private String nom;
    private String auteurUsername;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    private String description;
    private String photo;

    public RecetteCompact(Recette r) {
        this.id = r.getId();
        this.nom = r.getNom();
        this.auteurUsername = r.getAuteurUsername();
        this.dateCreation = r.getDateCreation();
        this.dateModification = r.getDateModification();
        this.description = r.getDescription();
        this.photo = r.getPhoto();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getAuteurUsername() {
        return auteurUsername;
    }

    public void setAuteurUsername(String auteurUsername) {
        this.auteurUsername = auteurUsername;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }
}
