package models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.URL;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

@Entity
@ApiModel(description = "La quantité associée à un ingrédient")
@JsonIgnoreProperties({ "hash", "salt" })
public class Utilisateur {
    @Id
    @ApiModelProperty(value = "Le pseudo de l'utilisateur")
    @NotBlank(message = "Le username ne peut être null, vide ou contenir uniquement des caractères blancs")
    private String username;

    @ApiModelProperty(value = "Le hash du mot de passe de l'utilisateur", hidden = true)
    @NotBlank(message = "Le hash ne peut être null, vide ou contenir uniquement des caractères blancs")
    private String hash;

    @ApiModelProperty(value = "Le salt de l'utilisateur", hidden = true)
    @NotBlank(message = "Le salt ne peut être null, vide ou contenir uniquement des caractères blancs")
    private String salt;

    @ApiModelProperty(value = "Le nom complet de l'utilisateur")
    @NotBlank(message = "Le fullname ne peut être null, vide ou contenir uniquement des caractères blancs")
    private String fullname;

    @ApiModelProperty(value = "La biographie de l'utilisateur")
    @Length(max = 255)
    private String biographie;

    @ApiModelProperty(value = "La date de naissance")
    private LocalDate dateNaissance;

    @ApiModelProperty(value = "L'adresse email de l'utilisateur'")
    @Length(max = 255)
    @Email
    private String email;

    @ApiModelProperty(value = "La date d'inscription de l'utilisateur")
    @NotNull
    private LocalDateTime dateInscription;

    @ApiModelProperty(value = "La date de la dernière modification")
    @NotNull
    private LocalDateTime dateModification;

    @ApiModelProperty(value = "Le lien vers une photo de l'utilisateur'")
    @Length(max = 255)
    @URL
    private String photo;

    @ApiModelProperty(value = "La liste des recettes de l'utilisateur")
    @OneToMany(mappedBy = "auteur")
    @LazyCollection(LazyCollectionOption.FALSE)
    @Valid
    private Collection<Recette> recettes;

    @ApiModelProperty(value = "La liste des commentaires de l'utilisateur")
    // Here we cannot declare mappedBy = "auteur" because there is a double bidirectional relation
    @OneToMany(mappedBy = "auteur")
    @LazyCollection(LazyCollectionOption.FALSE)
    @Valid
    private Collection<Commentaire> commentaires;

    @ApiModelProperty(value = "La liste des commentaires de l'utilisateur")
    @OneToMany(mappedBy = "auteur")
    @LazyCollection(LazyCollectionOption.FALSE)
    @Valid
    private Collection<Note> notes;

    public Utilisateur(String username,
                       String hash,
                       String salt) {
        this.username = username;
        this.hash = hash;
        this.salt = salt;
        this.commentaires = new ArrayList<>();
        this.recettes = new ArrayList<>();
        this.notes = new ArrayList<>();
    }

    public void remove(EntityManager em) {
        removeCommentaires(em);
        removeNotes(em);
        // Recettes must be removed last
        removeRecettes(em);
        em.remove(this);
    }

    private void removeCommentaires(EntityManager em) {
        for (Iterator<Commentaire> iterator = commentaires.iterator(); iterator.hasNext();) {
            Commentaire commentaire = iterator.next();
            Recette r = em.find(Recette.class, commentaire.getRecetteId());
            r.removeCommentaire(commentaire);
            iterator.remove();
            em.remove(commentaire);
        }
    }

    private void removeNotes(EntityManager em) {
        for (Iterator<Note> iterator = notes.iterator(); iterator.hasNext();) {
            Note note = iterator.next();
            Recette r = em.find(Recette.class, note.getRecetteId());
            r.removeNote(note);
            iterator.remove();
            em.remove(note);
        }
    }

    private void removeRecettes(EntityManager em) {
        for (Iterator<Recette> iterator = recettes.iterator(); iterator.hasNext();) {
            Recette recette = iterator.next();
            Recette r = em.find(Recette.class, recette.getId());
            iterator.remove();
            em.remove(recette);
        }
    }

    public void addRecette(Recette recette) {
        this.recettes.add(recette);
    }

    public void removeRecette(Recette recette) {
        this.recettes.remove(recette);
    }

    public void addCommentaire(Commentaire commentaire) {
        this.commentaires.add(commentaire);
    }

    public void removeCommentaire(Commentaire commentaire) {
        this.commentaires.remove(commentaire);
    }

    public void addNote(Note note) {
        this.notes.add(note);
    }

    public void removeNote(Note note) {
        this.notes.remove(note);
    }

    public Utilisateur() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getBiographie() {
        return biographie;
    }

    public void setBiographie(String biographie) {
        this.biographie = biographie;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public LocalDateTime getDateInscription() {
        return dateInscription;
    }

    public void setDateInscription(LocalDateTime dateInscription) {
        this.dateInscription = dateInscription;
    }

    public Collection<Commentaire> getCommentaires() {
        return commentaires;
    }

    public void setCommentaires(Collection<Commentaire> commentaires) {
        this.commentaires = commentaires;
    }

    public void setRecettes(Collection<Recette> recettes) {
        this.recettes = recettes;
    }

    public Collection<Recette> getRecettes() {
        return recettes;
    }

    public LocalDateTime getDateModification() {
        return dateModification;
    }

    public void setDateModification(LocalDateTime dateModification) {
        this.dateModification = dateModification;
    }

    public LocalDate getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(LocalDate dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    public Collection<Note> getNotes() {
        return notes;
    }

    public void setNotes(Collection<Note> notes) {
        this.notes = notes;
    }
}