package martine.utils;

import martine.erreurs.InvalidAuthorizationException;
import martine.models.Utilisateur;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.ws.rs.NotFoundException;
import java.util.Optional;

public class QueryUtils {
    public static Optional<Utilisateur> trouverUtilisateur(String username, EntityManager em) {
        TypedQuery<Utilisateur> req = em.createQuery("SELECT u FROM Utilisateur u WHERE username = :username", Utilisateur.class);
        req.setParameter("username", username);
        try {
            Utilisateur u = req.getSingleResult();
            return Optional.of(u);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public static Optional<Utilisateur> trouverUtilisateurAuthorization(String authorization, EntityManager em) throws InvalidAuthorizationException {
        String ID = SecurityUtils.decodeAuthorization(authorization);
        String[] splittedID = ID.split(":");
        String username = splittedID[0];
        return QueryUtils.trouverUtilisateur(username, em);
    }
}
