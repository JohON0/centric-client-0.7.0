package centric.pl.functions.settings;

import java.util.function.Supplier;

public interface ISetting {
    Setting<?> setVisible(Supplier<Boolean> bool);
}