package models;

import io.swagger.annotations.ApiModelProperty;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

@Entity
public class Recette {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @ApiModelProperty(hidden = true)
    private int id;

    @ApiModelProperty(value = "Le nom de la recette", example = "Pizza", required = true)
    @NotBlank(message = "Le nom d'une recette ne peut pas être null, vide ou composé uniquement de caractères blancs")
    private String nom;

    @ApiModelProperty(value = "Le username de l'auteur de la recette", hidden = true)
    private String auteurUsername;

    @ApiModelProperty(value = "La description de la recette", example = "Ceci est la recette de mon grand-père...")
    private String description;

    @ApiModelProperty(value = "La date de création de la recette", hidden = true)
    private LocalDateTime dateCreation;

    // ManyToMany au lieu de OneToMany car un même élément peut appartenir à plusieurs recettes.
    @ManyToMany(fetch = FetchType.EAGER)
    @NotNull(message = "Les éléments d'une recette ne peuvent pas être null")
    @Valid
    private Collection<Element> elements;

    @ApiModelProperty(hidden = true)
    @OneToMany
    @LazyCollection(LazyCollectionOption.FALSE)
    @Valid
    private Collection<Commentaire> commentaires;

    public Recette() {
        this.commentaires = new ArrayList<Commentaire>();
    }

    public void remove(EntityManager em) {
        removeCommentaires(em);
        em.remove(this);
    }

    private void removeCommentaires(EntityManager em) {
        for (Iterator<Commentaire> iterator = commentaires.iterator(); iterator.hasNext();) {
            Commentaire commentaire = iterator.next();
            Utilisateur u = em.find(Utilisateur.class, commentaire.getAuteurUsername());
            u.removeCommentaire(commentaire);
            iterator.remove();
            em.remove(commentaire);
        }
    }

    public String getNom() {
        return nom;
    }

    public void addCommentaire(Commentaire commentaire) {
        commentaires.add(commentaire);
    }

    public void removeCommentaire(Commentaire commentaire) {
        commentaires.remove(commentaire);
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

    public Collection<Commentaire> getCommentaires() {
        return commentaires;
    }

    public void setCommentaires(Collection<Commentaire> commentaires) {
        this.commentaires = commentaires;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public String getAuteurUsername() {
        return auteurUsername;
    }

    public void setAuteurUsername(String auteurUsername) {
        this.auteurUsername = auteurUsername;
    }
}
