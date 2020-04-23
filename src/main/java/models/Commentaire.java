package models;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@ApiModel(description="Un commentaire sur une recette")
public class Commentaire {
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

    @NotBlank
    @Length(min=1, max=512)
    @Column(length = 512)
    private String texte;

    public Commentaire() {
    }

    public Commentaire(int recetteId,
                       String auteurUsername, LocalDateTime date,
                       @NotBlank @Length(min = 1, max = 512) String texte) {
        this.auteurUsername = auteurUsername;
        this.date = date;
        this.recetteId = recetteId;
        this.texte = texte;
    }

    public void removeReferences(Recette r, Utilisateur u) {
        r.removeCommentaire(this);
        u.removeCommentaire(this);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public String getTexte() {
        return texte;
    }

    public void setTexte(String texte) {
        this.texte = texte;
    }

    public String getAuteurUsername() {
        return auteurUsername;
    }

    public void setAuteurUsername(String auteurUsername) {
        this.auteurUsername = auteurUsername;
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
}
