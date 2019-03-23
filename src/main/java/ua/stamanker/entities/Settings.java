package ua.stamanker.entities;

public class Settings {

    public String botUsername;
    public String botToken;

    public Settings validate() {
        if(botUsername==null || botToken == null) {
            throw new IllegalStateException("Error: settings are not defined");
        }
        return this;
    }

}
