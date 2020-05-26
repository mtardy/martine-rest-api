package martine.utils;

import martine.erreurs.InvalidAuthorizationException;
import martine.models.Utilisateur;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.persistence.EntityManager;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;

public class SecurityUtils {
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 512;
    private static final String ALGORITHM = "PBKDF2WithHmacSHA512";

    private static final SecureRandom RAND = new SecureRandom();

    public static Optional<String> generateSalt(final int length) {
        if (length < 1) {
            System.err.println("error in generateSalt: length must be > 0");
            return Optional.empty();
        }

        byte[] salt = new byte[length];
        RAND.nextBytes(salt);

        return Optional.of(Base64.getEncoder().encodeToString(salt));
    }

    public static Optional<String> hashPassword(String password, String salt) {
        char[] chars = password.toCharArray();
        byte[] bytes = salt.getBytes();

        PBEKeySpec spec = new PBEKeySpec(chars, bytes, ITERATIONS, KEY_LENGTH);

        Arrays.fill(chars, Character.MIN_VALUE);

        try {
            SecretKeyFactory fac = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] securePassword = fac.generateSecret(spec).getEncoded();
            return Optional.of(Base64.getEncoder().encodeToString(securePassword));

        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            System.err.println("Exception encountered in hashPassword()");
            return Optional.empty();

        } finally {
            spec.clearPassword();
        }
    }

    public static boolean verifyPassword(String password, String key, String salt) {
        Optional<String> optEncrypted = hashPassword(password, salt);
        if (!optEncrypted.isPresent()) return false;
        return optEncrypted.get().equals(key);
    }

    public static String decodeAuthorization(String authorization) throws InvalidAuthorizationException {
        try {
            String IDBase64 = authorization.substring(6);
            byte[] IDByte = Base64.getDecoder().decode(IDBase64);
            String ID = new String(IDByte, StandardCharsets.UTF_8);

            if (!ID.contains(":")) {
                throw new InvalidAuthorizationException("String does not contain a colon");
            } else {
                return ID;
            }
        } catch (Exception e) {
            throw new InvalidAuthorizationException(e);
        }
    }

    public static Optional<Utilisateur> authentifierUtilisateur(String authorization, EntityManager em) throws InvalidAuthorizationException, NotFoundException {
        String ID = SecurityUtils.decodeAuthorization(authorization);
        String[] splittedID = ID.split(":");
        String username = splittedID[0];
        String password = splittedID[1];

        Optional<Utilisateur> optionalUtilisateur = QueryUtils.trouverUtilisateur(username, em);
        if (optionalUtilisateur.isPresent()) {
            Utilisateur u = optionalUtilisateur.get();
            if (SecurityUtils.verifyPassword(password, u.getHash(), u.getSalt())) {
                return Optional.of(u);
            } else {
                return Optional.empty();
            }
        } else {
            throw new NotFoundException();
        }
    }

    // This is bad design and should be handled correctly with a container request filter
    // On top of that it often double the "trouver utilisateur" request, one for authentication, one for the method

    /**
     * Handle the authorization and match username with the username in the authorization string
     * @param authorization the authorization string in the request header
     * @param username      the username of the user to manage
     * @param em            the entity manager
     * @return an option on the response to send in case of error, empty option otherwise
     */
    public static Optional<Response> handleAuthorization(String authorization, String username, EntityManager em) {
        try {
            Optional<Utilisateur> u = SecurityUtils.authentifierUtilisateur(authorization, em);
            if (u.isPresent()) {
                Optional<Utilisateur> optionalUtilisateur = QueryUtils.trouverUtilisateur(username, em);

                if (optionalUtilisateur.isPresent()) {
                    Utilisateur user = optionalUtilisateur.get();
                    if (hasPermissionToManage(u.get(), user)) {
                        return Optional.empty();
                    } else {
                        return Optional.of(Response.status(Response.Status.UNAUTHORIZED).build());
                    }
                } else {
                    return Optional.of(Response.status(Response.Status.NOT_FOUND).build());
                }
            } else {
                return Optional.of(Response.status(Response.Status.UNAUTHORIZED).build());
            }
        } catch (NotFoundException e) {
            return Optional.of(Response.status(Response.Status.UNAUTHORIZED).build());
        } catch (InvalidAuthorizationException e) {
            return Optional.of(Response.status(Response.Status.BAD_REQUEST).build());
        }
    }

    /**
     * Handle the authorization
     * @param authorization the authorization string in the request header
     * @param em            the entity manager
     * @return an option on the response to send in case of error, empty option otherwise
     */
    public static Optional<Response> handleAuthorization(String authorization, EntityManager em) {
        try {
            Optional<Utilisateur> u = SecurityUtils.authentifierUtilisateur(authorization, em);
            if (u.isPresent()) {
                return Optional.empty();
            } else {
                return Optional.of(Response.status(Response.Status.UNAUTHORIZED).build());
            }
        } catch (NotFoundException e) {
            return Optional.of(Response.status(Response.Status.UNAUTHORIZED).build());
        } catch (InvalidAuthorizationException e) {
            return Optional.of(Response.status(Response.Status.BAD_REQUEST).build());
        }
    }

    public static boolean hasPermissionToManage(Utilisateur manager, Utilisateur managed) {
        // For now it's really simple, there is no special permission
        return manager.getUsername().equals(managed.getUsername());
    }
}
