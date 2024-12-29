package centric.pl.command;

import centric.pl.command.impl.AdviceCommand;

public interface AdviceCommandFactory {
    AdviceCommand adviceCommand(CommandProvider commandProvider);
}
