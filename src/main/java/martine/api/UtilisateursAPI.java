package martine.api;

import io.swagger.annotations.*;
import io.swagger.jaxrs.PATCH;
import martine.erreurs.UsernameIndisponibleErreur;
import martine.erreurs.UtilisateurMalformeErreur;
import martine.models.Favori;
import martine.models.Recette;
import martine.models.Utilisateur;
import martine.models.format.UtilisateurCompact;
import martine.utils.QueryUtils;
import martine.utils.SecurityUtils;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.URL;

import javax.ejb.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton
@Path("/users")
@Api(value = "Utilisateurs")
public class UtilisateursAPI {

    @PersistenceContext
    EntityManager em;

    private final int SALT_BYTE_LENGTH = 64;
    private final String regexString = "^[A-Za-z0-9]+(?:[_-][A-Za-z0-9]+)*$";

    @POST
    @Path("/")
    @Consumes({"application/x-www-form-urlencoded"})
    @Produces({"application/json"})
    @ApiOperation(value = "Créer un utilisateur", consumes = "application/x-www-form-urlencoded")
    @ApiResponses({
            @ApiResponse(code = 201, message = "Succès de la création de l'utilisateur")
    })
    public Response ajouter(
            @Length(min = 1, max = 255)
            @NotBlank(message = "Le username ne peut être null, vide ou contenir uniquement des caractères blancs")
            @ApiParam(value = "Le pseudo pour s'identifier, il doit être unique et convenir à l'expression régulière ^[A-Za-z0-9]+(?:[_-][A-Za-z0-9]+)*$", required = true) @FormParam("username") String username,
            @Length(min = 1, max = 255)
            @NotBlank(message = "Le password ne peut être null, vide ou contenir uniquement des caractères blancs")
            @ApiParam(value = "Le mot de passe en clair", required = true) @FormParam("password") String password,
            @Length(min = 1, max = 255)
            @NotBlank(message = "Le fullname ne peut être null, vide ou contenir uniquement des caractères blancs")
            @ApiParam(value = "Le nom complet", required = true) @FormParam("fullname") String fullname,
            @Email @Length(max = 255)
            @ApiParam(value = "L'adresse email") @FormParam("email") String email,
            @Length(max = 255)
            @ApiParam(value = "La biographie") @FormParam("biographie") String biographie,
            @ApiParam(value = "La date de naissance au format ISO8601: YYYY-MM-DD") @FormParam("dateNaissance") String dateNaissance,
            @URL @Length(max = 255)
            @ApiParam(value = "Le lien vers une photo") @FormParam("photo") String photo
    ) {

        Pattern regex = Pattern.compile(this.regexString);
        Matcher matcher = regex.matcher(username);
        if (!matcher.matches()) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(new UtilisateurMalformeErreur("L'username doit matcher cette regex: " + this.regexString))
                    .build();
        }

        try {
            // Process the user password for storage
            String salt = SecurityUtils.generateSalt(this.SALT_BYTE_LENGTH).get();
            String hash = SecurityUtils.hashPassword(password, salt).get();

            LocalDateTime dateInscription = LocalDateTime.now(ZoneId.of("Europe/Paris"));

            Utilisateur newUser = new Utilisateur(username, hash, salt);
            newUser.setFullname(fullname);
            newUser.setBiographie(biographie);
            newUser.setEmail(email);
            newUser.setPhoto(photo);
            newUser.setDateInscription(dateInscription);
            newUser.setDateModification(dateInscription);
            // Parse the birth date
            if (dateNaissance != null) {
                LocalDate parsedDateNaissance = LocalDate.parse(dateNaissance);
                newUser.setDateNaissance(parsedDateNaissance);
            }

            Optional<Utilisateur> optionalUtilisateur = QueryUtils.trouverUtilisateur(newUser.getUsername(), em);
            if (optionalUtilisateur.isPresent()) {
                return Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity(new UsernameIndisponibleErreur("Cet username est déjà utilisé"))
                        .build();
            } else {
                em.persist(newUser);
                return Response.status(Response.Status.CREATED).entity(newUser).build();
            }
        } catch (DateTimeParseException e) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(new UtilisateurMalformeErreur("La date de naissance n'est pas valide, suivre le format YYYY-MM-DD"))
                    .build();
        }
    }

    @PATCH
    @Path("/{username}")
    @Consumes({"application/x-www-form-urlencoded"})
    @Produces({"application/json"})
    @ApiOperation(value = "Modifier un utilisateur",
            consumes = "application/x-www-form-urlencoded",
            notes = "Permet de modifier son mot de passe ou son nom complet. Il faut donc être authentifié.",
            authorizations = {@Authorization(value = "basicAuth")}
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Succès de la modification de l'utilisateur"),
            @ApiResponse(code = 400, message = "Le format de l'authentification dans le header est invalide"),
            @ApiResponse(code = 401, message = "L'authentification a échoué, mot de passe invalide"),
            @ApiResponse(code = 404, message = "Utilisateur introuvable")
    })
    public Response modifier(
            @Length(max = 255) @ApiParam(value = "Le nouveau mot de passe en clair") @FormParam("password") String password,
            @Length(max = 255) @ApiParam(value = "Le nouveau nom complet") @FormParam("fullname") String fullname,
            @Length(max = 512) @ApiParam(value = "La nouvelle biographie") @FormParam("biographie") String biographie,
            @ApiParam(value = "La nouvelle date de naissance au format ISO8601: YYYY-MM-DD") @FormParam("dateNaissance") String dateNaissance,
            @Length(max = 255) @URL @ApiParam(value = "Le lien vers la nouvelle photo") @FormParam("photo") String photo,
            @Length(max = 255) @Email @ApiParam(value = "Le nouvel email") @FormParam("email") String email,
            @ApiParam(value = "Le format est \"Basic <username:password in base64>\"") @HeaderParam("authorization") String authorization,
            @ApiParam(value = "Le username de l'utilisateur à modifier") @NotBlank @PathParam("username") String username
    ) {
        Optional<Response> authorizationErrors = SecurityUtils.handleAuthorization(authorization, username, em);
        if (authorizationErrors.isPresent()) {
            return authorizationErrors.get();
        } else {
            Optional<Utilisateur> userToModifyOption = QueryUtils.trouverUtilisateur(username, em);
            // We can get directly because if authentication was successfull, user exists
            Utilisateur userToModify = userToModifyOption.get();
            boolean modification = false;
            if (password != null) {
                String hash = SecurityUtils.hashPassword(password, userToModify.getSalt()).get();
                userToModify.setHash(hash);
                modification = true;
            }
            if (fullname != null) {
                userToModify.setFullname(fullname);
                modification = true;
            }
            if (biographie != null) {
                userToModify.setBiographie(biographie);
                modification = true;
            }
            if (photo != null) {
                userToModify.setPhoto(photo);
                modification = true;
            }
            if (email != null) {
                userToModify.setEmail(email);
                modification = true;
            }
            if (dateNaissance != null) {
                try {
                    LocalDate parsedDateNaissance = LocalDate.parse(dateNaissance);
                    userToModify.setDateNaissance(parsedDateNaissance);
                    modification = true;
                } catch (DateTimeParseException e) {
                    return Response
                            .status(Response.Status.BAD_REQUEST)
                            .entity(new UtilisateurMalformeErreur("La date de naissance n'est pas valide, suivre le format YYYY-MM-DD"))
                            .build();
                } finally {
                    if (modification) {
                        userToModify.setDateModification(LocalDateTime.now(ZoneId.of("Europe/Paris")));
                    }
                }
            }

            if (modification) {
                userToModify.setDateModification(LocalDateTime.now(ZoneId.of("Europe/Paris")));
            }
            return Response.ok(userToModify).build();
        }
    }


    @DELETE
    @Path("/{username}")
    @Produces({"application/json"})
    @ApiOperation(value = "Supprimer un utilisateur",
            notes = "Permet de supprimer son compte utilisateur. Il faut donc être authentifié.",
            authorizations = {@Authorization(value = "basicAuth")}
    )
    @ApiResponses({
            @ApiResponse(code = 204, message = "Succès de la suppression de l'utilisateur"),
            @ApiResponse(code = 400, message = "Le format de l'authentification dans le header est invalide"),
            @ApiResponse(code = 401, message = "L'authentification a échoué, mot de passe invalide"),
            @ApiResponse(code = 404, message = "Utilisateur introuvable")
    })
    public Response supprimer(
            @ApiParam(value = "Le format est \"Basic <username:password in base64>\"") @HeaderParam("authorization") String authorization,
            @ApiParam(value = "Le username de l'utilisateur à supprimer") @NotBlank @PathParam("username") String username
    ) {
        Optional<Response> authorizationErrors = SecurityUtils.handleAuthorization(authorization, username, em);
        if (authorizationErrors.isPresent()) {
            return authorizationErrors.get();
        } else {
            Optional<Utilisateur> userToRemove = QueryUtils.trouverUtilisateur(username, em);
            // We can get directly because if authentication was successfull, user exists
            userToRemove.get().remove(em);
            return Response.status(Response.Status.NO_CONTENT).build();
        }
    }

    @GET
    @Path("/{username}")
    @Produces({"application/json"})
    @ApiOperation(value = "Récupérer les informations sur un utilisateur")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Succès de la récupération des données utilisateur"),
            @ApiResponse(code = 404, message = "Utilisateur introuvable")
    })
    public Response recuperer(
            @ApiParam(value = "Le username de l'utilisateur dont on veut récupérer les informations") @NotBlank() @PathParam("username") String username) {
        Optional<Utilisateur> optionalUtilisateur = QueryUtils.trouverUtilisateur(username, em);
        if (optionalUtilisateur.isPresent()) {
            Utilisateur userToGet = optionalUtilisateur.get();
            return Response.ok(userToGet).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @GET
    @Path("/")
    @Produces({"application/json"})
    @ApiOperation(value = "Récupérer la liste des utilisateurs")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Succès de la récupération de la liste des utilisateurs"),
    })
    public Response lister() {
        TypedQuery<Utilisateur> req = em.createQuery("select u from Utilisateur u", Utilisateur.class);
        List<Utilisateur> utilisateurs = req.getResultList();
        List<UtilisateurCompact> utilisateurCompacts = new ArrayList<>();
        utilisateurs.forEach(u -> {
            utilisateurCompacts.add(new UtilisateurCompact(u));
        });
        return Response.ok(utilisateurCompacts).build();
    }

    @POST
    @Path("/{username}/favoris/{rid}")
    @Consumes({"application/x-www-form-urlencoded"})
    @Produces({"application/json"})
    @ApiOperation(value = "Ajouter une recette aux favoris de l'utilisateur", authorizations = {
            @Authorization(value = "basicAuth")
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "Succès de l'ajout du favoris"),
            @ApiResponse(code = 400, message = "Requête invalide"),
            @ApiResponse(code = 401, message = "L'authentification a échoué, mot de passe invalide"),
            @ApiResponse(code = 404, message = "Recette ou utilisateur introuvable")
    })
    public Response ajouterFavori(
            @HeaderParam("authorization") String authorization,
            @ApiParam(value = "Username de l'utilisateur") @NotBlank @PathParam("username") String username,
            @ApiParam(value = "Identifiant de la recette") @PathParam("rid") int rid
    ) {
        Optional<Response> authorizationErrors = SecurityUtils.handleAuthorization(authorization, username, em);
        if (authorizationErrors.isPresent()) {
            return authorizationErrors.get();
        } else {
            Optional<Utilisateur> userToAddFavoriteTo = QueryUtils.trouverUtilisateur(username, em);
            // We can get directly because if authentication was successfull, user exists
            Utilisateur utilisateur = userToAddFavoriteTo.get();

            Recette recette = em.find(Recette.class, rid);
            if (recette != null) {
                // Try to find if this user already fav this recipe
                TypedQuery<Favori> req = em.createQuery("select f from Favori f where f.recette = :recette and f.proprietaire = :proprietaire", Favori.class);
                req.setParameter("recette", recette);
                req.setParameter("proprietaire", utilisateur);
                List<Favori> favoris = req.getResultList();

                if (favoris.size() == 0) {
                    Favori favori = new Favori(utilisateur, recette);
                    em.persist(favori);
                    return Response.ok(favori).build();
                }

                return Response.ok(favoris.get(0)).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        }
    }

    @DELETE
    @Path("/{username}/favoris/{fid}")
    @Produces({"application/json"})
    @ApiOperation(value = "Supprimer un favori", authorizations = {
            @Authorization(value = "basicAuth")
    })
    @ApiResponses({
            @ApiResponse(code = 204, message = "Succès de la suppression du favori"),
            @ApiResponse(code = 400, message = "Requête invalide"),
            @ApiResponse(code = 401, message = "L'authentification a échoué, mot de passe invalide"),
            @ApiResponse(code = 404, message = "Recette, utilisateur ou favori introuvable (bonne chance)")
    })
    public Response supprimerFavori(
            @HeaderParam("authorization") String authorization,
            @ApiParam(value = "Username de l'utilisateur") @PathParam("username") String username,
            @ApiParam(value = "Identifiant du favori") @PathParam("fid") int fid
    ) {
        Optional<Response> authorizationErrors = SecurityUtils.handleAuthorization(authorization, username, em);
        if (authorizationErrors.isPresent()) {
            return authorizationErrors.get();
        } else {
            Optional<Utilisateur> userToDeleteFavoriteFrom = QueryUtils.trouverUtilisateur(username, em);
            // We can get directly because if authentication was successfull, user exists
            Utilisateur utilisateur = userToDeleteFavoriteFrom.get();

            Favori favori = em.find(Favori.class, fid);
            if (favori != null) {
                if (favori.getProprietaire().getUsername() == utilisateur.getUsername()) {
                    em.remove(favori);
                    return Response.status(Response.Status.NO_CONTENT).build();
                } else {
                    return Response.status(Response.Status.UNAUTHORIZED).build();
                }
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        }
    }
}
