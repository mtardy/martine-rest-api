package Erreurs;

public class UsernameIndisponibleErreur extends ApiErreur {
    private String details;

    public UsernameIndisponibleErreur(String details) {
        super(UsernameIndisponibleErreur.class.getSimpleName());
        this.details = details;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
