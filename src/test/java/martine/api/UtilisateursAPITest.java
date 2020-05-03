package martine.api;

import martine.models.Utilisateur;
import martine.models.format.UtilisateurCompact;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ejb.Local;
import javax.ejb.TransactionManagement;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.UserTransaction;
import javax.ws.rs.core.Response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;

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

    @Inject
    UserTransaction utx;

    @PersistenceContext
    private EntityManager em;

    @Before
    public void flushUtilisateurTable() throws Exception {
        utx.begin();
        em.joinTransaction();
        em.createQuery("DELETE FROM Utilisateur").executeUpdate();
        utx.commit();
    }

    private Utilisateur createTestUser() {
        Utilisateur testUser = new Utilisateur();
        testUser.setUsername("thiery");
        testUser.setFullname("Thiery Michel");
        testUser.setEmail("thiery@michel.com");
        testUser.setBiographie("I am born in...");
        testUser.setDateNaissance(LocalDate.parse("1970-01-01"));
        testUser.setPhoto("http://photo.com/photo.jpg");
        return testUser;
    }

    private String getTestUserPassword() {
        return "michel";
    }

    private String getAuthorization(String username, String password) {
        String concat = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(concat.getBytes());
    }

    private Utilisateur addTestUser() {
        Utilisateur testUser = createTestUser();

        // Add the user
        Response response = utilisateursAPI.ajouter(
                testUser.getUsername(),
                getTestUserPassword(),
                testUser.getFullname(),
                testUser.getEmail(),
                testUser.getBiographie(),
                testUser.getDateNaissance().toString(),
                testUser.getPhoto()
        );

        // Verify that it was executed
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());

        // Verify it's in the DB
        TypedQuery<Utilisateur> req = em.createQuery("SELECT u FROM Utilisateur u WHERE username = :username", Utilisateur.class);
        req.setParameter("username", testUser.getUsername());
        Utilisateur userFromDb = req.getSingleResult();
        assertEquals(testUser.getUsername(), userFromDb.getUsername());

        return testUser;
    }

    @Test
    public void ajouter() {
        Utilisateur testUser = createTestUser();

        Response response = utilisateursAPI.ajouter(
                testUser.getUsername(),
                getTestUserPassword(),
                testUser.getFullname(),
                testUser.getEmail(),
                testUser.getBiographie(),
                testUser.getDateNaissance().toString(),
                testUser.getPhoto()
        );

        // First verify the response
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        Utilisateur userFromResponse = (Utilisateur) response.getEntity();
        assertEquals(testUser.getUsername(), userFromResponse.getUsername());
        assertEquals(testUser.getFullname(), userFromResponse.getFullname());
        assertEquals(testUser.getEmail(), userFromResponse.getEmail());
        assertEquals(testUser.getBiographie(), userFromResponse.getBiographie());
        assertEquals(testUser.getDateNaissance(), userFromResponse.getDateNaissance());
        assertEquals(testUser.getPhoto(), userFromResponse.getPhoto());

        // Then verify that the user was correctly added to the DB
        TypedQuery<Utilisateur> req = em.createQuery("SELECT u FROM Utilisateur u WHERE username = :username", Utilisateur.class);
        req.setParameter("username", testUser.getUsername());
        Utilisateur userFromDb = req.getSingleResult();
        assertEquals(testUser.getUsername(), userFromDb.getUsername());
        assertEquals(testUser.getFullname(), userFromDb.getFullname());
        assertEquals(testUser.getEmail(), userFromDb.getEmail());
        assertEquals(testUser.getBiographie(), userFromDb.getBiographie());
        assertEquals(testUser.getDateNaissance(), userFromDb.getDateNaissance());
        assertEquals(testUser.getPhoto(), userFromDb.getPhoto());
    }

    @Test
    public void modifier() {
        Utilisateur testUser = addTestUser();

        Response response = utilisateursAPI.modifier(
                null,
                "Thiery Macaron",
                "I Was a kid...",
                "1971-01-01",
                "http://photo.com/photo.png",
                "michel@michou.mich",
                getAuthorization(testUser.getUsername(), getTestUserPassword()),
                testUser.getUsername()
        );

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        TypedQuery<Utilisateur> req = em.createQuery("SELECT u FROM Utilisateur u WHERE username = :username", Utilisateur.class);
        req.setParameter("username", testUser.getUsername());
        Utilisateur userFromDb = req.getSingleResult();
        assertEquals("Thiery Macaron", userFromDb.getFullname());
        assertEquals("michel@michou.mich", userFromDb.getEmail());
        assertEquals("I Was a kid...", userFromDb.getBiographie());
        assertEquals(LocalDate.parse("1971-01-01"), userFromDb.getDateNaissance());
        assertEquals("http://photo.com/photo.png", userFromDb.getPhoto());
    }

    @Test
    public void supprimer() {
        Utilisateur testUser = addTestUser();

        // Delete the user
        Response response1 = utilisateursAPI.supprimer(getAuthorization(testUser.getUsername(), getTestUserPassword()), testUser.getUsername());
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response1.getStatus());

        // Verify it's absent from the DB
        TypedQuery<Utilisateur> req1 = em.createQuery("SELECT u FROM Utilisateur u WHERE username = :username", Utilisateur.class);
        req1.setParameter("username", testUser.getUsername());
        assertEquals(new ArrayList<Utilisateur>(), req1.getResultList());
    }

    @Test
    public void recuperer() {
        Utilisateur testUser = addTestUser();

        Response response1 = utilisateursAPI.recuperer(testUser.getUsername());

        assertEquals(Response.Status.OK.getStatusCode(), response1.getStatus());

        Utilisateur userFromGet = (Utilisateur) response1.getEntity();
        assertEquals(testUser.getUsername(), userFromGet.getUsername());
        assertEquals(testUser.getFullname(), userFromGet.getFullname());
        assertEquals(testUser.getEmail(), userFromGet.getEmail());
        assertEquals(testUser.getBiographie(), userFromGet.getBiographie());
        assertEquals(testUser.getDateNaissance(), userFromGet.getDateNaissance());
        assertEquals(testUser.getPhoto(), userFromGet.getPhoto());
    }

    @Test
    public void lister() {
        Response response = utilisateursAPI.lister();
        assertEquals(new ArrayList<Utilisateur>(), response.getEntity());

        Utilisateur testUser = addTestUser();
        Response response1 = utilisateursAPI.lister();
        Collection<UtilisateurCompact> utilisateurCompacts = (Collection) response1.getEntity();
        assertEquals(1, utilisateurCompacts.size());
    }
}
