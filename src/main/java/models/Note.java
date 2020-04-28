package models;

import io.swagger.annotations.ApiModel;

import javax.persistence.Entity;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@ApiModel(description="Une note sur une recette")
public class Note extends Avis {
    @NotNull
    @Min(0)
    @Max(10)
    private int valeur;

    public Note() {
    }

    public Note(int recetteId, String auteurUsername, LocalDateTime date, @Min(0) @Max(10) int valeur) {
        super(recetteId, auteurUsername, date);
        this.valeur = valeur;
    }

    @Override
    public void removeReferences(Recette r, Utilisateur u) {
        r.removeNote(this);
        u.removeNote(this);
    }

    public int getValeur() {
        return valeur;
    }

    public void setValeur(int valeur) {
        this.valeur = valeur;
    }
}
