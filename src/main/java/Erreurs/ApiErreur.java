package Erreurs;

public class ApiErreur {
    private String type;

    public ApiErreur(String e) {
        type = e;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
