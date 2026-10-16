package cm.kfokam48.ticket.exception;

/**
 * RG-02 : titre déjà utilisé par un autre ticket.
 */
public class TitreDejaUtiliseException extends RuntimeException {

    public TitreDejaUtiliseException(String titre) {
        super("RG-02 : le titre « " + titre + " » est déjà utilisé par un autre ticket. Deux tickets ne peuvent pas avoir le même titre.");
    }
}
