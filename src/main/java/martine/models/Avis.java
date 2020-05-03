package martine.models;

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

    @JsonIgnore
    @NotNull
    @ManyToOne
    private Recette recette;

    @ApiModelProperty(value = "L'identifiant de la recette concernée", hidden = true)
    public int getRecetteId() {
        return recette.getId();
    }

    @ApiModelProperty(value = "Le nom de la recette concernée", hidden = true)
    public String getRecetteNom() {
        return recette.getNom();
    }

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

    public Avis(Recette recette, LocalDateTime date) {
        this.date = date;
        this.recette = recette;
    }

    abstract void removeReferences(Recette r, Utilisateur u );

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public Recette getRecette() {
        return recette;
    }

    public void setRecette(Recette recette) {
        this.recette = recette;
    }
}
