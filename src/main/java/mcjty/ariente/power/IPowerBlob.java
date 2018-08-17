package mcjty.ariente.power;

import mcjty.ariente.cables.CableColor;

/**
 * Implement this interface on tile entities that are supposed to be part of the power blob.
 * Typically these are generators and cables. Power consumers are not included here
 */
public interface IPowerBlob {

    int getCableId();

    void fillCableId(int id);

    CableColor getCableColor();

    boolean canSendPower();
}
