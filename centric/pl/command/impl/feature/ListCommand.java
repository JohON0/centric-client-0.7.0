package centric.pl.command.impl.feature;

import centric.pl.command.Command;
import centric.pl.command.Logger;
import centric.pl.command.MultiNamedCommand;
import centric.pl.command.Parameters;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ListCommand implements Command, MultiNamedCommand {

    final List<Command> commands;
    final Logger logger;

    @Override
    public void execute(Parameters parameters) {
        logger.log(TextFormatting.RED + "advice" + TextFormatting.WHITE + " | " + TextFormatting.GRAY + "�������� ������ ��� ������������ �������");

        for (Command command : commands) {
            if (command == this) {
                continue;
            }
            logger.log(TextFormatting.RED + command.name() + TextFormatting.WHITE + " | " + TextFormatting.GRAY + command.description());
        }
    }

    @Override
    public String name() {
        return "list";
    }

    @Override
    public String description() {
        return "������ ������ ���� ������";
    }

    @Override
    public List<String> aliases() {
        return List.of("", "help");
    }
}
