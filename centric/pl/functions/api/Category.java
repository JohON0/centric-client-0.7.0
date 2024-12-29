package centric.pl.functions.api;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum Category {

    Combat("U", 8,1),
    Movement("V",15,1),
    Player("Y",5,1),
    Render("D",5,1),
    Misc("Z",0,1);

    public final String icon;
    public final int sizex;

    public double anim;
}
