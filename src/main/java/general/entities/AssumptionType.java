package general.entities;

/**
 * Enum representing the two types of an assumption.
 */
public enum AssumptionType {
    INTRODUCE_UNCERTAINTY, RESOLVE_UNCERTAINTY;

    @Override
    public String toString() {
        if(this == INTRODUCE_UNCERTAINTY){
            return "Introduce Uncertainty";
        } else {
            return "Resolve Uncertainty";
        }
    }
}
