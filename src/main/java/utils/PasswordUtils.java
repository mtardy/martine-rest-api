package utils;

import Erreurs.InvalidAuthorizationException;
import models.Utilisateur;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.persistence.EntityManager;
import javax.ws.rs.NotFoundException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;

public class PasswordUtils {
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
