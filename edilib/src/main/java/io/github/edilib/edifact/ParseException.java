package io.github.edilib.edifact;

import java.io.IOException;

public class ParseException extends IOException {

    private Location location;

    public ParseException(String message, Location location) {
        super(message);
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }
}
