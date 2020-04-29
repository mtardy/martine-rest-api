package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@MappedSuperclass
public abstract class Avis {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @ApiModelProperty(hidden = true)
    private int id;

    @NotNull
    private int recetteId;

    @JsonIgnore
    @NotNull
    @ManyToOne
    private Utilisateur auteur;

    @ApiModelProperty(value = "Le username de l'auteur de l'avis", hidden = true)
    public String getAuteurUsername() {
        return auteur.getUsername();
    }

    @ApiModelProperty(value = "Le fullname de l'auteur de l'avis", hidden = true)
    public String getAuteurFullname() {
        return auteur.getFullname();
    }

    @NotNull
    private LocalDateTime date;

    public Avis() {
    }

    public Avis(int recetteId, LocalDateTime date) {
        this.date = date;
        this.recetteId = recetteId;
    }



    abstract void removeReferences(Recette r, Utilisateur u );

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRecetteId() {
        return recetteId;
    }

    public void setRecetteId(int recetteId) {
        this.recetteId = recetteId;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public Utilisateur getAuteur() {
        return auteur;
    }

    public void setAuteur(Utilisateur auteur) {
        this.auteur = auteur;
    }
}
