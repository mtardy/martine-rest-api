package api;

import Erreurs.InvalidAuthorizationException;
import Erreurs.UsernameIndisponibleErreur;
import Erreurs.UtilisateurMalformeErreur;
import io.swagger.annotations.*;
import io.swagger.jaxrs.PATCH;
import models.Utilisateur;
import models.format.UtilisateurCompact;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.URL;
import utils.PasswordUtils;

import javax.ejb.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton
@Path("/user")
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

        // Process the user password for storage
        String salt = PasswordUtils.generateSalt(this.SALT_BYTE_LENGTH).get();
        String hash = PasswordUtils.hashPassword(password, salt).get();

        Utilisateur newUser = new Utilisateur(username, hash, salt, fullname, biographie, email, photo);

        Utilisateur u = em.find(Utilisateur.class, newUser.getUsername());
        if (u != null) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(new UsernameIndisponibleErreur("Cet username est déjà utilisé"))
                    .build();
        }

        em.persist(newUser);
        return Response.status(Response.Status.CREATED).build();
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
            @ApiResponse(code = 204, message = "Succès de la modification de l'utilisateur"),
            @ApiResponse(code = 400, message = "Le format de l'authentification dans le header est invalide"),
            @ApiResponse(code = 401, message = "L'authentification a échoué, mot de passe invalide"),
            @ApiResponse(code = 404, message = "Utilisateur introuvable")
    })
    public Response modifier(
            @Length(min = 1, max = 255)
            @NotBlank(message = "Le password ne peut être null, vide ou contenir uniquement des caractères blancs")
            @ApiParam(value = "Le nouveau mot de passe en clair") @FormParam("password") String password,
            @Length(min = 1, max = 255)
            @NotBlank(message = "Le fullname ne peut être null, vide ou contenir uniquement des caractères blancs")
            @ApiParam(value = "Le nouveau nom complet") @FormParam("fullname") String fullname,
            @ApiParam(value = "Le format est \"Basic <username:password in base64>\"") @HeaderParam("authorization") String authorization,
            @NotBlank @PathParam("username") String username) {
        try {
            Optional<Utilisateur> u = authentifierUtilisateur(authorization);
            if (u.isPresent()) {
                Utilisateur userToModify = em.find(Utilisateur.class, username);
                if (hasPermissionToManage(u.get(), userToModify)) {
                    if (userToModify != null) {
                        if (password != null) {
                            String hash = PasswordUtils.hashPassword(password, userToModify.getSalt()).get();
                            userToModify.setHash(hash);
                        }
                        if (fullname != null) {
                            userToModify.setFullname(fullname);
                        }
                        return Response.status(Response.Status.NO_CONTENT).build();
                    } else {
                        return Response.status(Response.Status.NOT_FOUND).build();
                    }
                } else {
                    return Response.status(Response.Status.UNAUTHORIZED).build();
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
            @NotBlank @PathParam("username") String username) {
        try {
            Optional<Utilisateur> u = authentifierUtilisateur(authorization);
            if (u.isPresent()) {
                Utilisateur userToRemove = em.find(Utilisateur.class, username);
                if (userToRemove != null) {
                    if (hasPermissionToManage(u.get(), userToRemove)) {
                        em.remove(userToRemove);
                        return Response.status(Response.Status.NO_CONTENT).build();
                    } else {
                        return Response.status(Response.Status.UNAUTHORIZED).build();
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
    @Path("/{username}")
    @Produces({"application/json"})
    @ApiOperation(value = "Récupérer les informations sur un utilisateur",
            notes = "Permet de récupérer les informations de son compte utilisateur. Il faut donc être authentifié.",
            authorizations = {@Authorization(value = "basicAuth")}
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Succès de la récupération des données utilisateur"),
            @ApiResponse(code = 400, message = "Le format de l'authentification dans le header est invalide"),
            @ApiResponse(code = 401, message = "L'authentification a échoué, mot de passe invalide"),
            @ApiResponse(code = 404, message = "Utilisateur introuvable")
    })
    public Response recuperer(
            @ApiParam(value = "Le format est \"Basic <username:password in base64>\"") @HeaderParam("authorization") String authorization,
            @NotBlank() @PathParam("username") String username) {
        try {
            Optional<Utilisateur> u = authentifierUtilisateur(authorization);
            if (u.isPresent()) {
                Utilisateur userToGet = em.find(Utilisateur.class, username);
                if (userToGet != null) {
                    if (hasPermissionToManage(u.get(), userToGet)) {
                        UtilisateurCompact userToSend = new UtilisateurCompact(userToGet);
                        return Response.ok(userToSend).build();
                    } else {
                        return Response.status(Response.Status.UNAUTHORIZED).build();
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

    private boolean hasPermissionToManage(Utilisateur manager, Utilisateur managed) {
        // For now it's really simple, there is no special permission
        return manager.getUsername().equals(managed.getUsername());
    }

    private Optional<Utilisateur> authentifierUtilisateur(String authorization) throws InvalidAuthorizationException, NotFoundException {
        String ID = PasswordUtils.decodeAuthorization(authorization);
        String[] splittedID = ID.split(":");
        String username = splittedID[0];
        String password = splittedID[1];

        Utilisateur u = em.find(Utilisateur.class, username);
        if (u != null) {
            if (PasswordUtils.verifyPassword(password, u.getHash(), u.getSalt())) {
                return Optional.of(u);
            } else {
                return Optional.empty();
            }
        } else {
            throw new NotFoundException();
        }
    }
}
