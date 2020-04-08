package models.format;

import models.Recette;

public class RecetteCompact {
    private int id;
    private String nom;

    public RecetteCompact(Recette r) {
        this.id = r.getId();
        this.nom = r.getNom();
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
}
