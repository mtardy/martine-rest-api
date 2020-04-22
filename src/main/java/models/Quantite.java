package models;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.PositiveOrZero;

@Entity
@ApiModel(description = "La quantité associée à un ingrédient")
public class Quantite {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @ApiModelProperty(hidden = true)
    private int id;

    @ApiModelProperty(value = "Le nombre associé à cette quantité", example = "4")
    @PositiveOrZero(message = "Le nombre de la quantité doit être positif ou égal à 0")
    private Integer nombre;

    @ApiModelProperty(value = "L'unité de la quantité", example = "cuillère")
    private String unite;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getNombre() {
        return nombre;
    }

    public void setNombre(Integer nombre) {
        this.nombre = nombre;
    }

    public String getUnite() {
        return unite;
    }

    public void setUnite(String unite) {
        this.unite = unite;
    }
}