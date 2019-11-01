package jmagine.chessclock;

import jmagine.chessclock.Constants.Comp;
import jmagine.chessclock.Constants.Mode;

//Handles logic for clock state
public class Clock {
    int turn = -1;
    Mode mode;

    Comp comp_type;
    int[] time_default;
    int[] comp_default;
    int[] time_curr;
    int[] comp_curr;

    public Clock(int[] time_default, int[] comp_default, Comp comp_type) {
        this.time_default = time_default;
        this.comp_default = comp_default;
        this.time_curr = time_default;
        this.comp_curr = comp_default;
        this.comp_type = comp_type;
    }


}
