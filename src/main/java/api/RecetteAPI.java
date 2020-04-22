package api;

import io.swagger.annotations.*;
import models.Element;
import models.Ingredient;
import models.Quantite;
import models.Recette;
import models.format.RecetteCompact;

import javax.ejb.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Singleton
@Path("/recettes")
@Api(value = "Recettes")
public class RecetteAPI {

    @PersistenceContext
    EntityManager em;

    @GET
    @Path("/")
    @Produces({ "application/json" })
    @ApiOperation(value = "Lister et rechercher les recettes",
            notes = "Retourne une représentation compacte des recettes. " +
                    "Une requête sans paramètre retourne la totalité des recettes. " +
                    "Recherche insensible à la casse et utilisation de wildcard prise en charge.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Succès de l'opération")
    })
    public Response lister(@QueryParam("nom") String nom,
                           @QueryParam("ingrédient") String ingredient) {
        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<Recette> q = cb.createQuery(Recette.class);
        Root<Recette> r = q.from(Recette.class);
        q.select(r);

        List<Predicate> predicates = new ArrayList<>();

        if (nom != null) {
            predicates.add(cb.like(
                    cb.lower(r.get("nom")),
                    nom.toLowerCase().replace('*','%')));
        }

        if (ingredient != null) {
            predicates.add(cb.like(
                    r.join("elements").get("ingredient").get("nom"),
                    ingredient.toLowerCase().replace('*', '%')
            ));
        }

        // Halleluja
        q.where(cb.and(predicates.toArray(new Predicate[]{})));

        TypedQuery<Recette> query = em.createQuery(q);
        List<Recette> result = query.getResultList();

        List<RecetteCompact> resultCompact = new ArrayList<>();
        for (int i = 0; i < result.size(); i++) {
            resultCompact.add(new RecetteCompact(result.get(i)));
        }

        return Response.ok(resultCompact).build();
    }

    @GET
    @Path("/{id}")
    @Produces({ "application/json" })
    @ApiOperation(value = "Obtenir les données sur une recette")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Succès de l'opération"),
            @ApiResponse(code = 404, message = "Recette introuvable")
    })
    public Response obtenir(@ApiParam(value="identifiant de la recette") @PathParam("id") int id) {
        Recette r = em.find(Recette.class, id);
        if (r != null) {
            return Response.ok(r).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @POST
    @Path("/")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Ajouter une recette")
    @ApiResponses({
            @ApiResponse(code = 201, message = "Succès de l'ajout de la recette")
    })
    public Response ajouter(
            @Valid @NotNull(message = "La recette ne peut pas être null")
            @ApiParam(value = "Recette à ajouter", required = true) Recette recette
    ) {
        ArrayList<Element> listElements = (ArrayList<Element>) recette.getElements();
        boolean nouvelElement;

        for (int i=0; i<listElements.size(); i++) {
            nouvelElement = false;
            Element element = listElements.get(i);

            // INGREDIENT
            Ingredient ingredient = element.getIngredient();
            // Vérifie si l'ingrédient existe déjà
            // Dans ce cas c'est facile car la clé est le nom de l'ingrédient
            if (em.find(Ingredient.class, ingredient.getNom()) == null) {
                em.persist(ingredient);
                nouvelElement = true;
            } else {
                em.merge(ingredient);
            }

            // QUANTITE
            Quantite quantite = element.getQuantite();

            if (quantite == null) {
                // Remplacer par une quantité avec les propriétés à null
                element.setQuantite(new Quantite());
                quantite = element.getQuantite();
            }

            // Vérifie si la quantité existe déjà
            // Ici on crée une requête personnalisée et on lie l'objet si on le trouve
            try {
                // On essai de récupérer la quantité
                CriteriaBuilder cb = this.em.getCriteriaBuilder();
                CriteriaQuery<Quantite> q = cb.createQuery(Quantite.class);
                Root<Quantite> r = q.from(Quantite.class);
                q.select(r);

                Predicate nombrePredicate;
                Predicate quantitePredicate;

                if (quantite.getNombre() == null) {
                    nombrePredicate = cb.isNull(r.get("nombre"));
                } else {
                    nombrePredicate = cb.equal(r.get("nombre"), quantite.getNombre());
                }

                if (quantite.getUnite() == null) {
                    quantitePredicate = cb.isNull(r.get("unite"));
                } else {
                    quantitePredicate = cb.equal(r.get("unite"), quantite.getUnite());
                }

                TypedQuery<Quantite> query = em.createQuery(q.where(cb.and(nombrePredicate, quantitePredicate)));
                element.setQuantite(query.getSingleResult());
            } catch (NoResultException e) {
                em.persist(quantite);
                nouvelElement = true;
            }

            // Si jamais l'elément existait déjà
            if (nouvelElement) {
                em.persist(element);
            } else {
                // On essai de récupérer l'élement
                Element e = em.createQuery("SELECT e FROM Element e WHERE e.ingredient = :custIngredient AND e.quantite = :custQuantite", Element.class)
                        .setParameter("custIngredient", element.getIngredient())
                        .setParameter("custQuantite", element.getQuantite())
                        .getSingleResult();
                // On le lie dans la nouvelle liste d'élements
                listElements.set(i, e);
            }
        }

        em.persist(recette);
        return Response.status(Response.Status.CREATED).entity(recette).build();
    }

    @DELETE
    @Path("/{id}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Supprimer une recette", authorizations = {
            @Authorization(value = "basicAuth")
    })
    @ApiResponses({
            @ApiResponse(code = 204, message = "Succès de la suppression de la recette"),
            @ApiResponse(code = 404, message = "Recette introuvable")
    })
    public Response supprimer(@ApiParam(value="identifiant de la recette") @PathParam("id") int id) {
        Recette r = em.find(Recette.class, id);
        if (r != null) {
            em.remove(r);
            return Response.status(Response.Status.NO_CONTENT).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
