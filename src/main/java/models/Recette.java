package models;

import io.swagger.annotations.ApiModelProperty;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Collection;

@Entity
public class Recette {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @ApiModelProperty(hidden = true)
    private int id;

    @ApiModelProperty(value = "Le nom de la recette", example = "Pizza", required = true)
    @NotBlank(message = "Le nom d'une recette ne peut pas être null, vide ou composé uniquement de caractères blancs")
    private String nom;

    @ApiModelProperty(value = "La description de la recette", example = "Ceci est la recette de mon grand-père...")
    private String description;

    // ManyToMany au lieu de OneToMany car un même élément peut appartenir à plusieurs recettes.
    @ManyToMany(fetch = FetchType.EAGER)
    @NotNull(message = "Les éléments d'une recette ne peuvent pas être null")
    @Valid
    private Collection<Element> elements;


    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Collection<Element> getElements() {
        return elements;
    }

    public void setElements(Collection<Element> listeElements) {
        this.elements = listeElements;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
