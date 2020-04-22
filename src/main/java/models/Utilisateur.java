package models;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.URL;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Entity
@ApiModel(description = "La quantité associée à un ingrédient")
public class Utilisateur {
    @Id
    @ApiModelProperty(value = "Le pseudo de l'utilisateur")
    @NotBlank(message = "Le username ne peut être null, vide ou contenir uniquement des caractères blancs")
    private String username;

    @ApiModelProperty(value = "Le hash du mot de passe de l'utilisateur")
    @NotBlank(message = "Le hash ne peut être null, vide ou contenir uniquement des caractères blancs")
    private String hash;

    @ApiModelProperty(value = "Le salt de l'utilisateur")
    @NotBlank(message = "Le salt ne peut être null, vide ou contenir uniquement des caractères blancs")
    private String salt;

    @ApiModelProperty(value = "Le nom complet de l'utilisateur")
    @NotBlank(message = "Le fullname ne peut être null, vide ou contenir uniquement des caractères blancs")
    private String fullname;

    @ApiModelProperty(value = "La biographie de l'utilisateur")
    @Length(max = 255)
    private String biographie;

    @ApiModelProperty(value = "L'adresse email de l'utilisateur'")
    @Length(max = 255)
    @Email
    private String email;

    @ApiModelProperty(value = "Le lien vers une photo de l'utilisateur'")
    @Length(max = 255)
    @URL
    private String photo;

    public Utilisateur(String username,
                       String hash,
                       String salt,
                       String fullname,
                       String biographie,
                       String email,
                       String photo) {
        this.username = username;
        this.hash = hash;
        this.salt = salt;
        this.fullname = fullname;
        this.biographie = biographie;
        this.email = email;
        this.photo = photo;
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
}