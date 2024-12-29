package centric.pl.events.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EventRotate extends CancelEvent {
   public float yaw, pitch;
}
