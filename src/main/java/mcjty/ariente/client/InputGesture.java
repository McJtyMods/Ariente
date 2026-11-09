package mcjty.ariente.client;

import net.minecraft.world.InteractionHand;

public final class InputGesture {

    private final boolean attack;
    private final InteractionHand hand;
    private boolean canceled;

    public InputGesture(boolean attack, InteractionHand hand) {
        this.attack = attack;
        this.hand = hand;
    }

    public boolean isAttack() {
        return attack;
    }

    public boolean isUseItem() {
        return !attack;
    }

    public InteractionHand getHand() {
        return hand;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void setSwingHand(boolean swing) {
    }
}
