package martine.erreurs;

public class UtilisateurMalformeErreur extends ApiErreur {
    private String details;

    public UtilisateurMalformeErreur(String details) {
        super(UtilisateurMalformeErreur.class.getSimpleName());
        this.details = details;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
