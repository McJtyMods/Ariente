package mcjty.ariente.power;

public class PowerAmount {

    private long amount = 0;
    private long lastAmount = 0;
    private int lastUsedTick = -1;

    private long prevTotalAdded = 0;
    private long prevTotalConsumed = 0;
    private long totalAdded = 0;
    private long totalConsumed = 0;

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
        totalAdded += amount;
    }

    public void addAmount(long da) {
        this.amount += da;
        totalAdded += da;
        assert amount >= 0;
    }

    public void removeAmount(long da) {
        this.amount -= da;
        totalConsumed += da;
    }

    public long getLastAmount() {
        return lastAmount;
    }

    public long getPrevTotalAdded() {
        return prevTotalAdded;
    }

    public long getPrevTotalConsumed() {
        return prevTotalConsumed;
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
        prevTotalAdded = totalAdded;
        prevTotalConsumed = totalConsumed;
        totalAdded = 0;
        totalConsumed = 0;
    }
}
