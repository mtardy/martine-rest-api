package martine.api;

import martine.models.Utilisateur;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ejb.Local;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.ws.rs.core.Response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.Assert.*;

@RunWith(Arquillian.class)
public class UtilisateursAPITest {
    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class)
                .addPackages(true,"martine.api", "martine.erreurs", "martine.models", "martine.utils")
                .addAsResource("META-INF/persistence.xml")
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    private UtilisateursAPI utilisateursAPI;

    @PersistenceContext
    private EntityManager em;

    @Test
    public void ajouter() {
        // Create a test user
        Utilisateur testUser = new Utilisateur();
        testUser.setUsername("thiery");
        testUser.setFullname("Thiery Michel");
        testUser.setEmail("thiery@michel.com");
        testUser.setBiographie("I am born in...");
        testUser.setDateNaissance(LocalDate.parse("1970-01-01"));
        testUser.setPhoto("http://photo.com/photo.jpg");

        Response response = utilisateursAPI.ajouter(
                testUser.getUsername(),
                "password",
                testUser.getFullname(),
                testUser.getEmail(),
                testUser.getBiographie(),
                testUser.getDateNaissance().toString(),
                testUser.getPhoto());

        // First verify the response
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());

        // Then verify that the user was correctly added to the DB
        TypedQuery<Utilisateur> req = em.createQuery("SELECT u FROM Utilisateur u WHERE username = :username", Utilisateur.class);
        req.setParameter("username", "thiery");
        Utilisateur userFromDb = req.getSingleResult();
        assertEquals(testUser.getUsername(), userFromDb.getUsername());
        assertEquals(testUser.getFullname(), userFromDb.getFullname());
        assertEquals(testUser.getEmail(), userFromDb.getEmail());
        assertEquals(testUser.getBiographie(), userFromDb.getBiographie());
        assertEquals(testUser.getDateNaissance(), userFromDb.getDateNaissance());
        assertEquals(testUser.getPhoto(), userFromDb.getPhoto());

        assertEquals(LocalDate.now(), userFromDb.getDateInscription().toLocalDate());
        assertEquals(LocalDate.now(), userFromDb.getDateModification().toLocalDate());
    }

    @Test
    public void modifier() {
    }

    @Test
    public void supprimer() {
    }

    @Test
    public void recuperer() {
    }

    @Test
    public void lister() {
    }
}
