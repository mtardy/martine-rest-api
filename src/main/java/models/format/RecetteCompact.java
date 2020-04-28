package models.format;

import models.Recette;

import java.time.LocalDateTime;

public class RecetteCompact {
    private Recette r;

    public RecetteCompact(Recette r) {
        this.r = r;
    }

    public int getId() {
        return r.getId();
    }

    public String getNom() {
        return r.getNom();
    }

    public String getAuteurUsername() {
        return r.getAuteurUsername();
    }

    public LocalDateTime getDateCreation() {
        return r.getDateCreation();
    }

    public LocalDateTime getDateModification() {
        return r.getDateModification();
    }

    public String getDescription() {
        return r.getDescription();
    }

    public Float getNote() {
        return r.getNote();
    }

    public String getPhoto() {
        return r.getPhoto();
    }
}
