package mcjty.ariente.blocks.utility;

import javax.annotation.Nullable;

/** Equipment controlled by nearby Locks and Wireless Locks. */
public interface ILockTarget extends ILockable {

    void setLocked(boolean locked);

    /** A device with its own access key must not be overridden by an unrelated nearby controller. */
    default boolean canBeControlledBy(@Nullable String keyId) {
        return true;
    }
}
