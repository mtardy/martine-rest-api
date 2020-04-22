package models;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Entity
@ApiModel(description = "Un élement d'une recette est composé d'un ingrédient et de sa quantité")
public class Element {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @ApiModelProperty(hidden = true)
    private int id;

    @ManyToOne(optional = false)
    @ApiModelProperty(required = true)
    @NotNull(message = "L'ingrédient d'un élément ne peut pas être null")
    @Valid
    private Ingredient ingredient;

    @ManyToOne(optional = false)
    @Valid
    private Quantite quantite;

    public Ingredient getIngredient() {
        return ingredient;
    }

    public void setIngredient(Ingredient ingredient) {
        this.ingredient = ingredient;
    }

    public Quantite getQuantite() {
        return quantite;
    }

    public void setQuantite(Quantite quantite) {
        this.quantite = quantite;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
