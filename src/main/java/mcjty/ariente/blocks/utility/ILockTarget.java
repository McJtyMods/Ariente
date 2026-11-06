package mcjty.ariente.blocks.utility;

/** Equipment controlled by nearby Locks and Wireless Locks. */
public interface ILockTarget extends ILockable {

    void setLocked(boolean locked);
}
