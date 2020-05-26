package martine.api;

import martine.erreurs.InvalidAuthorizationException;
import io.swagger.annotations.*;
import martine.models.*;
import martine.utils.QueryUtils;
import org.hibernate.validator.constraints.Length;
import martine.utils.SecurityUtils;

import javax.ejb.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Singleton
@Path("/recettes")
@Api(value = "Recettes avis")
public class AvisAPI {

    @PersistenceContext
    EntityManager em;

    @POST
    @Path("/{id}/commentaires")
    @Consumes({"application/x-www-form-urlencoded"})
    @Produces({"application/json"})
    @ApiOperation(value = "Ajouter un commentaire à une recette", authorizations = {
            @Authorization(value = "basicAuth")
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "Succès de l'ajout du commentaire"),
            @ApiResponse(code = 400, message = "Requête invalide"),
            @ApiResponse(code = 401, message = "L'authentification a échoué, mot de passe invalide"),
            @ApiResponse(code = 404, message = "Recette introuvable")
    })
    public Response ajouterCommentaire(
            @HeaderParam("authorization") String authorization,
            @ApiParam(value = "Identifiant de la recette") @PathParam("id") int id,
            @NotBlank @Length(min=1, max=512) @ApiParam(value = "Contenu du commentaire") @FormParam("texte") String texte
    ) {
        Optional<Response> authorizationErrors = SecurityUtils.handleAuthorization(authorization, em);
        if (authorizationErrors.isPresent()) {
            return authorizationErrors.get();
        } else {
            try {
                Optional<Utilisateur> u = QueryUtils.trouverUtilisateurAuthorization(authorization, em);
                // We can get directly because if authentication was successfull, user exists
                Utilisateur utilisateur = u.get();
                Recette recette = em.find(Recette.class, id);
                if (recette != null) {
                    LocalDateTime date = LocalDateTime.now(ZoneId.of("Europe/Paris"));
                    Commentaire commentaire = new Commentaire(recette, date, texte);
                    commentaire.setAuteur(utilisateur);
                    em.persist(commentaire);
                    recette.addCommentaire(commentaire);
                    utilisateur.addCommentaire(commentaire);
                    return Response.ok(commentaire).build();
                } else {
                    return Response.status(Response.Status.NOT_FOUND).build();
                }
            } catch (InvalidAuthorizationException e) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
        }
    }

    @DELETE
    @Path("/{id}/commentaires/{cid}")
    @Produces({"application/json"})
    @ApiOperation(value = "Supprimer un commentaire d'une recette", authorizations = {
            @Authorization(value = "basicAuth")
    })
    @ApiResponses({
            @ApiResponse(code = 204, message = "Succès de la suppression du commentaire"),
            @ApiResponse(code = 400, message = "Requête invalide"),
            @ApiResponse(code = 401, message = "L'authentification a échoué, mot de passe invalide"),
            @ApiResponse(code = 404, message = "Recette ou commentaire introuvable")
    })
    public Response supprimerCommentaire(
            @HeaderParam("authorization") String authorization,
            @ApiParam(value = "Identifiant de la recette") @PathParam("id") int id,
            @ApiParam(value = "Identifiant du commentaire") @PathParam("cid") int cid
    ) {
        Optional<Response> authorizationErrors = SecurityUtils.handleAuthorization(authorization, em);
        if (authorizationErrors.isPresent()) {
            return authorizationErrors.get();
        } else {
            try {
                Optional<Utilisateur> u = QueryUtils.trouverUtilisateurAuthorization(authorization, em);
                // We can get directly because if authentication was successfull, user exists
                Utilisateur utilisateur = u.get();
                Recette recette = em.find(Recette.class, id);
                if (recette != null) {
                    Commentaire commentaire = em.find(Commentaire.class, cid);
                    if (commentaire != null && commentaire.getRecetteId() == id) {
                        if (commentaire.getAuteurUsername().equals(utilisateur.getUsername())) {
                            commentaire.removeReferences(recette, utilisateur);
                            em.remove(commentaire);
                            return Response.status(Response.Status.NO_CONTENT).build();
                        } else {
                            return Response.status(Response.Status.UNAUTHORIZED).build();
                        }
                    } else {
                        return Response.status(Response.Status.NOT_FOUND).build();
                    }
                } else {
                    return Response.status(Response.Status.NOT_FOUND).build();
                }
            } catch (InvalidAuthorizationException e) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
        }
    }

    @GET
    @Path("/{id}/commentaires")
    @Produces({"application/json"})
    @ApiOperation(value = "Obtenir les commentaires d'une recette")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Succès de la requête"),
            @ApiResponse(code = 404, message = "Recette introuvable")
    })
    public Response obtenirCommentaires(@ApiParam(value = "Identifiant de la recette") @PathParam("id") int id) {
        Recette r = em.find(Recette.class, id);
        if (r != null) {
            Collection<Commentaire> commentaires = r.getCommentaires();
            return Response.ok(commentaires).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @POST
    @Path("/{id}/notes")
    @Consumes({"application/x-www-form-urlencoded"})
    @Produces({"application/json"})
    @ApiOperation(value = "Ajouter une note à une recette", authorizations = {
            @Authorization(value = "basicAuth")
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "Succès de l'ajout de la note"),
            @ApiResponse(code = 400, message = "Requête invalide"),
            @ApiResponse(code = 401, message = "L'authentification a échoué, mot de passe invalide"),
            @ApiResponse(code = 404, message = "Recette introuvable")
    })
    public Response ajouterNote(
            @HeaderParam("authorization") String authorization,
            @ApiParam(value = "Identifiant de la recette") @PathParam("id") int id,
            @NotNull @Min(0) @Max(10) @ApiParam(value = "valeur de la note, entier compris entre 0 et 10") @FormParam("valeur") int valeur
    ) {
        Optional<Response> authorizationErrors = SecurityUtils.handleAuthorization(authorization, em);
        if (authorizationErrors.isPresent()) {
            return authorizationErrors.get();
        } else {
            try {
                Optional<Utilisateur> u = QueryUtils.trouverUtilisateurAuthorization(authorization, em);
                // We can get directly because if authentication was successfull, user exists
                Utilisateur utilisateur = u.get();
                Recette recette = em.find(Recette.class, id);
                if (recette != null) {
                    // Try to find if this user already rated this recipe
                    TypedQuery<Note> req = em.createQuery("select n from Note n where n.recette = :recette and n.auteur = :auteur", Note.class);
                    req.setParameter("recette", recette);
                    req.setParameter("auteur", utilisateur);
                    List<Note> notes = req.getResultList();
                    // If yes, remove the previous rate and replace it with the new one
                    if (notes.size() != 0) {
                        Note n = notes.get(0);
                        n.removeReferences(recette, utilisateur);
                        em.remove(n);
                    }

                    LocalDateTime date = LocalDateTime.now(ZoneId.of("Europe/Paris"));
                    Note note = new Note(recette, date, valeur);
                    note.setAuteur(utilisateur);
                    em.persist(note);

                    recette.addNote(note);
                    utilisateur.addNote(note);
                    return Response.ok(note).build();
                } else {
                    return Response.status(Response.Status.NOT_FOUND).build();
                }
            } catch (InvalidAuthorizationException e) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
        }
    }

    @DELETE
    @Path("/{id}/notes/{nid}")
    @Produces({"application/json"})
    @ApiOperation(value = "Supprimer une note d'une recette", authorizations = {
            @Authorization(value = "basicAuth")
    })
    @ApiResponses({
            @ApiResponse(code = 204, message = "Succès de la suppression de la note"),
            @ApiResponse(code = 400, message = "Requête invalide"),
            @ApiResponse(code = 401, message = "L'authentification a échoué, mot de passe invalide"),
            @ApiResponse(code = 404, message = "Recette ou note introuvable")
    })
    public Response supprimerNote(
            @HeaderParam("authorization") String authorization,
            @ApiParam(value = "Identifiant de la recette") @PathParam("id") int id,
            @ApiParam(value = "Identifiant de la note") @PathParam("nid") int nid
    ) {
        Optional<Response> authorizationErrors = SecurityUtils.handleAuthorization(authorization, em);
        if (authorizationErrors.isPresent()) {
            return authorizationErrors.get();
        } else {
            try {
                Optional<Utilisateur> u = QueryUtils.trouverUtilisateurAuthorization(authorization, em);
                // We can get directly because if authentication was successfull, user exists
                Utilisateur utilisateur = u.get();
                Recette recette = em.find(Recette.class, id);
                if (recette != null) {
                    Note note = em.find(Note.class, nid);
                    if (note != null && note.getRecetteId() == id) {
                        if (note.getAuteurUsername().equals(utilisateur.getUsername())) {
                            note.removeReferences(recette, utilisateur);
                            em.remove(note);
                            return Response.status(Response.Status.NO_CONTENT).build();
                        } else {
                            return Response.status(Response.Status.UNAUTHORIZED).build();
                        }
                    } else {
                        return Response.status(Response.Status.NOT_FOUND).build();
                    }
                } else {
                    return Response.status(Response.Status.NOT_FOUND).build();
                }
            } catch (InvalidAuthorizationException e) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
        }
    }

    @GET
    @Path("/{id}/notes")
    @Produces({"application/json"})
    @ApiOperation(value = "Obtenir les notes d'une recette")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Succès de la requête"),
            @ApiResponse(code = 404, message = "Recette introuvable")
    })
    public Response obtenirNotes(@ApiParam(value = "Identifiant de la recette") @PathParam("id") int id) {
        Recette r = em.find(Recette.class, id);
        if (r != null) {
            Collection<Note> notes = r.getNotes();
            return Response.ok(notes).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
