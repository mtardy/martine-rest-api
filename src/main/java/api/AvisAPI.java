package api;

import Erreurs.InvalidAuthorizationException;
import io.swagger.annotations.*;
import models.Commentaire;
import models.Note;
import models.Recette;
import models.Utilisateur;
import org.hibernate.validator.constraints.Length;
import utils.PasswordUtils;

import javax.ejb.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
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
        try {
            Optional<Utilisateur> u = PasswordUtils.authentifierUtilisateur(authorization, em);
            if (u.isPresent()) {
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

            } else {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

        } catch (NotFoundException e) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        } catch (InvalidAuthorizationException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
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
        try {
            Optional<Utilisateur> u = PasswordUtils.authentifierUtilisateur(authorization, em);
            if (u.isPresent()) {
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

            } else {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

        } catch (NotFoundException e) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        } catch (InvalidAuthorizationException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
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
        try {
            Optional<Utilisateur> u = PasswordUtils.authentifierUtilisateur(authorization, em);
            if (u.isPresent()) {
                Utilisateur utilisateur = u.get();
                Recette recette = em.find(Recette.class, id);
                if (recette != null) {
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
            } else {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
        } catch (NotFoundException e) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        } catch (InvalidAuthorizationException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
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
        try {
            Optional<Utilisateur> u = PasswordUtils.authentifierUtilisateur(authorization, em);
            if (u.isPresent()) {
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

            } else {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

        } catch (NotFoundException e) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        } catch (InvalidAuthorizationException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
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
