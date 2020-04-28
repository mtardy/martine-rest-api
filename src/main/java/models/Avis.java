package models;

import io.swagger.annotations.ApiModelProperty;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
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

    @NotNull
    private String auteurUsername;

    @NotNull
    private LocalDateTime date;

    public Avis() {
    }

    public Avis(int recetteId, String auteurUsername, LocalDateTime date) {
        this.auteurUsername = auteurUsername;
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

    public String getAuteurUsername() {
        return auteurUsername;
    }

    public void setAuteurUsername(String auteurUsername) {
        this.auteurUsername = auteurUsername;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }
}
