package centric.pl.command;

import java.util.List;

public interface MultiNamedCommand {
    List<String> aliases();
}
