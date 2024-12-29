package centric.pl.events.impl;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class InventoryCloseEvent extends CancelEvent {

    public int windowId;

}
