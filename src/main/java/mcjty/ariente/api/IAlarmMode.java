package mcjty.ariente.api;

// Tile entities implementing this interface can cause alarms to go off for the city
public interface IAlarmMode {

    // Return true if this causes high alert alarm for the city
    boolean isHighAlert();
}
