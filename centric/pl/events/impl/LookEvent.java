package centric.pl.events.impl;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class LookEvent extends CancelEvent {
    public double yaw,pitch;
}
