package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.URL;
import utils.QueryUtils;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.ws.rs.QueryParam;
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

    @ApiModelProperty(value = "L'auteur de la recette", hidden = true)
    @JsonIgnore
    @ManyToOne
    private Utilisateur auteur;

    @ApiModelProperty(value = "La date de création de la recette", hidden = true)
    private LocalDateTime dateCreation;

    @ApiModelProperty(value = "La date de dernière modification de la recette", hidden = true)
    private LocalDateTime dateModification;

    @ApiModelProperty(value = "La description de la recette", example = "Ceci est la recette de mon grand-père...")
    private String description;

    @ApiModelProperty(value = "Lien vers une photo de la recette", example = "http://photo.com/photo1.jpg")
    @URL
    private String photo;

    @ApiModelProperty(value = "Les étapes de préparation de la recette", example = "Casser les oeufs...")
    @Column(length = 512)
    @Length(max = 512)
    private String preparation;

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

    @ApiModelProperty(hidden = true)
    @OneToMany
    @LazyCollection(LazyCollectionOption.FALSE)
    @Valid
    private Collection<Note> notes;

    @ApiModelProperty(hidden = true)
    public Float getNote() {
        int note = 0;
        for (Note n : notes) {
            note += n.getValeur();
        }
        if (notes.size() == 0) {
            return null;
        } else {
            return (float) note/notes.size();
        }
    }

    public Recette() {
        this.commentaires = new ArrayList<>();
        this.notes = new ArrayList<>();
    }

    @ApiModelProperty(value = "Le username de l'auteur de la recette", hidden = true)
    public String getAuteurUsername() {
        return auteur.getUsername();
    }

    @ApiModelProperty(value = "Le fullname de l'auteur de la recette", hidden = true)
    public String getAuteurFullname() {
        return auteur.getFullname();
    }

    public void remove(EntityManager em) {
        removeCommentaires(em);
        removeNotes(em);
        em.remove(this);
    }

    private void removeCommentaires(EntityManager em) {
        for (Iterator<Commentaire> iterator = commentaires.iterator(); iterator.hasNext();) {
            Commentaire commentaire = iterator.next();
            Utilisateur u = QueryUtils.trouverUtilisateur(commentaire.getAuteurUsername(), em).get();
            u.removeCommentaire(commentaire);
            iterator.remove();
            em.remove(commentaire);
        }
    }

    private void removeNotes(EntityManager em) {
        for (Iterator<Note> iterator = notes.iterator(); iterator.hasNext();) {
            Note note = iterator.next();
            Utilisateur u = QueryUtils.trouverUtilisateur(note.getAuteurUsername(), em).get();
            u.removeNote(note);
            iterator.remove();
            em.remove(note);
        }
    }

    public String getNom() {
        return nom;
    }

    public void addNote(Note note) {
        notes.add(note);
    }

    public void removeNote(Note note) {
        notes.remove(note);
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

    public LocalDateTime getDateModification() {
        return dateModification;
    }

    public void setDateModification(LocalDateTime dateModification) {
        this.dateModification = dateModification;
    }

    public String getPreparation() {
        return preparation;
    }

    public void setPreparation(String preparation) {
        this.preparation = preparation;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public Collection<Note> getNotes() {
        return notes;
    }

    public void setNotes(Collection<Note> notes) {
        this.notes = notes;
    }

    public Utilisateur getAuteur() {
        return auteur;
    }

    public void setAuteur(Utilisateur auteur) {
        this.auteur = auteur;
    }
}
