package centric.pl.command.impl;

import lombok.Value;

@Value
public class CommandException extends RuntimeException {
    String message;
}
