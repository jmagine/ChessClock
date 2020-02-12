package jmagine.chessclock;

/**
 * Created by JasonMa on 2/24/2017.
 */

public class Const {
  //millis factors
  final static long HOUR = 3600000;
  final static long MINUTE = 60000;
  final static long SECOND = 1000;

  //conversion factors
  final static int MIN_PER_HOUR = 60;
  final static int SEC_PER_MIN = 60;

  //comp_default types
  final static int FISCHER = 0;
  final static int DELAY = 1;
  final static int BRONSTEIN = 2;

  //clock modes
  final static int SUDDEN_DEATH = 0;
  final static int BYO_YOMI = 1;
  final static int HOURGLASS = 2;

  final static int TICK_INTERVAL = 50;
}
