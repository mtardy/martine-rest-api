package models;

import io.swagger.annotations.ApiModel;
import org.hibernate.validator.constraints.Length;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Entity
@ApiModel(description="Un commentaire sur une recette")
public class Commentaire extends Avis {
    @NotBlank
    @Length(min=1, max=512)
    @Column(length = 512)
    private String texte;

    public Commentaire() {
    }

    public Commentaire(int recetteId,
                       String auteurUsername, LocalDateTime date,
                       @NotBlank @Length(min = 1, max = 512) String texte) {
        super(recetteId, auteurUsername, date);
        this.texte = texte;
    }

    @Override
    public void removeReferences(Recette r, Utilisateur u) {
        r.removeCommentaire(this);
        u.removeCommentaire(this);
    }

    public String getTexte() {
        return texte;
    }

    public void setTexte(String texte) {
        this.texte = texte;
    }
}
