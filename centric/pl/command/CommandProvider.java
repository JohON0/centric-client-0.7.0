package centric.pl.command;

public interface CommandProvider {
    Command command(String alias);
}
