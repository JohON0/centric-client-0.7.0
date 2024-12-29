package centric.pl.command.impl;

import centric.pl.command.Logger;

public class ConsoleLogger implements Logger {
    @Override
    public void log(String message) {
        System.out.println("message = " + message);
    }
}
