package models;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@ApiModel(description="Un ingrédient d'une recette")
public class Ingredient {
    @Id
    @ApiModelProperty(value = "Le nom de l'ingrédient", example = "tomate", required = true)
    private String nom;

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    @Override
    public String toString() {
        return "Ingredient{" +
                "nom='" + nom + '\'' +
                '}';
    }
}