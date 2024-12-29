package centric.pl.command;

import centric.pl.command.impl.DispatchResult;

public interface CommandDispatcher {
    DispatchResult dispatch(String command);
}
