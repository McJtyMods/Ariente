package mcjty.ariente.power;

public class PowerBlob {

    private long amount = 0;
    private long lastAmount = 0;
    private int lastUsedTick = -1;

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public void addAmount(long da) {
        this.amount += da;
        assert amount >= 0;
    }

    public long getLastAmount() {
        return lastAmount;
    }

    public void setLastAmount(long lastAmount) {
        this.lastAmount = lastAmount;
    }

    public void addLastAmount(long da) {
        this.lastAmount += da;
        assert lastAmount >= 0;
    }

    public int getLastUsedTick() {
        return lastUsedTick;
    }

    public void setLastUsedTick(int lastUsedTick) {
        this.lastUsedTick = lastUsedTick;
    }
}
