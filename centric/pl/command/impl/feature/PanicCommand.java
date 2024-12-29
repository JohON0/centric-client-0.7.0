package centric.pl.command.impl.feature;

import centric.pl.Main;
import centric.pl.command.Command;
import centric.pl.command.Logger;
import centric.pl.command.Parameters;
import centric.pl.functions.api.Function;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;


@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PanicCommand implements Command {

    final Logger logger;

    @Override
    public void execute(Parameters parameters) {
        Main.getInstance().getFunctionRegistry().getFunctions().stream().filter(function -> function.isState()).forEach(function -> function.setState(false));
        logger.log("Выключил все модули!");
    }

    @Override
    public String name() {
        return "panic";
    }

    @Override
    public String description() {
        return "Выключает все модули";
    }
}
