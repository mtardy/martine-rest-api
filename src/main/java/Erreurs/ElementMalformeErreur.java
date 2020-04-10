package Erreurs;

public class ElementMalformeErreur extends ApiErreur {
    private int elementIndex;
    private String details;

    public ElementMalformeErreur(int elementNumber, String details) {
        super(ElementMalformeErreur.class.getSimpleName());
        this.elementIndex = elementNumber;
        this.details = details;
    }

    public int getElementIndex() {
        return elementIndex;
    }

    public void setElementIndex(int elementIndex) {
        this.elementIndex = elementIndex;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
