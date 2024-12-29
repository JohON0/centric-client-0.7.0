package centric.pl.command.impl;

import centric.pl.command.Logger;
import centric.pl.johon0.utils.client.IMinecraft;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class MinecraftLogger implements Logger, IMinecraft {
    @Override
    public void log(String message) {
        print(message);
    }
}
