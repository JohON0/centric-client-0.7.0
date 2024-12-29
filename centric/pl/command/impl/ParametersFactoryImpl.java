package centric.pl.command.impl;

import centric.pl.command.Parameters;
import centric.pl.command.ParametersFactory;

public class ParametersFactoryImpl implements ParametersFactory {

    @Override
    public Parameters createParameters(String message, String delimiter) {
        return new ParametersImpl(message.split(delimiter));
    }
}
