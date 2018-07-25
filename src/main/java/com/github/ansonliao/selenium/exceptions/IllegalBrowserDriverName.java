package com.github.ansonliao.selenium.exceptions;

public class IllegalBrowserDriverName extends Exception {

    public IllegalBrowserDriverName() {
        super();
    }

    public IllegalBrowserDriverName(String message) {
        super(message);
    }

    public IllegalBrowserDriverName(Throwable cause) {
        super(cause);
    }

    public IllegalBrowserDriverName(String message, Throwable cause) {
        super(message, cause);
    }

}
