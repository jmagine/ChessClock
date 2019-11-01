package jmagine.chessclock;

/**
 * Created by JasonMa on 2/24/2017.
 */

public class Constants {
  //millis factors
  final static long HOUR = 3600000;
  final static long MINUTE = 60000;
  final static long SECOND = 1000;

  //comp_default types
  final static int FISCHER = 0;
  final static int DELAY = 1;
  final static int BRONSTEIN = 2;

  enum Comp {
    FISCHER, DELAY, BRONSTEIN;
  }

  enum Mode {
    INIT, PLAY, PAUSE;
  }
}
