package martine.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import martine.models.format.RecetteCompact;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@ApiModel(description="Un favori d'un utilisateur")
public class Favori {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @ApiModelProperty(hidden = true)
    private int id;

    @JsonIgnore
    @NotNull
    @ManyToOne
    private Recette recette;

    @ApiModelProperty(value = "La recette compacte du favori", hidden = true)
    public RecetteCompact getRecetteCompact() {
        return new RecetteCompact(recette);
    }

    @JsonIgnore
    @NotNull
    @ManyToOne
    private Utilisateur proprietaire;

    @NotNull
    private LocalDateTime date;

    public Favori() {
    }

    public Favori(Utilisateur proprietaire, Recette recette, LocalDateTime date) {
        this.proprietaire = proprietaire;
        this.recette = recette;
        this.date = date;
    }

    public Favori(Utilisateur proprietaire, Recette recette) {
        this.proprietaire = proprietaire;
        this.recette = recette;
        this.date = LocalDateTime.now(ZoneId.of("Europe/Paris"));
    }

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

    public Utilisateur getProprietaire() {
        return proprietaire;
    }

    public void setProprietaire(Utilisateur proprietaire) {
        this.proprietaire = proprietaire;
    }

    public Recette getRecette() {
        return recette;
    }

    public void setRecette(Recette recette) {
        this.recette = recette;
    }
}
