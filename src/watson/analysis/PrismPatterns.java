package watson.analysis;

import java.util.regex.Pattern;

// --------------------------------------------------------------------------
/**
 * Regular expressions describing chat messages output by Prism.
 */
public interface PrismPatterns
{
  // Prism query result patterns; old form (also has auto grouping enabled):
  // + totemo placed birchlog x3 4m ago (a:place)
  // -- 2192 - 3/25/13 6:37pm - world @ -5.0 64.0 246.0
  // - totemo broke leaves x3 5m ago (a:break)
  // -- 2178 - 3/25/13 6:37pm - world @ 2.0 65.0 238.0
  //
  // Prism query result patterns; new form (numbers each edit in square
  // brackets):
  // + [1] totemo placed diamondblock 1h25m ago (a:place)
  // -- 24 - 5/14/13 9:27:20pm - world @ 9.0 63.0 -6.0
  // + [2] totemo placed diamondblock 1h25m ago (a:place)
  // -- 23 - 5/14/13 9:27:12pm - world @ 10.0 63.0 -5.0

  // Note: This pattern doesn't handle multi-word actions after the initial
  // player name, e.g. " - totemo picked up dirt ...".
  // The sequence "emerald ore x10" is simply parsed as (\\w | )+ and the "x10"
  // part split out in code.
  // " - totemo broke leaves x3 5m ago (a:break)"
  // " + [2] totemo placed diamondblock 1h25m ago (a:place)"
  //
  // More recent versions include block ID and data value after the block type's
  // name:
  // " - totemo broke ironore 15:0 2m ago (a:break)"
  // " -- 14 - 14/01/29 6:7:53am - world @ -15.0 12.0 103.0"
  public static final Pattern PLACE_BREAK            = Pattern.compile("^ [+-](?:\\s+\\[\\d+\\])? (\\w+) (?:placed|broke|poured|hung) ((?:\\w| )+) (?:(\\d+):(\\d+) )?(\\w+ ago|just now) \\(a:(\\w+)\\).*$");

  // Date is M/D/YY.
  // -- 2178 - 3/25/13 6:37pm - world @ 2.0 65.0 238.0
  public static final Pattern DATE_TIME_WORLD_COORDS = Pattern.compile("^ -- \\d+ - (\\d+)/(\\d+)/(\\d+) (\\d+):(\\d+):(\\d+)([ap]m) - .+ @ (-?\\d+).0 (\\d+).0 (-?\\d+).0\\s*$");

  // Prism // Showing 1 results. Page 1 of 1
  // Someone may one day fix the 's' on "results" for singular results.
  public static final Pattern LOOKUP_HEADER          = Pattern.compile("^Prism // Showing (\\d+) result(s)?. Page (\\d+) of (\\d+).*$");

  // Prism // Using defaults: t:3d
  public static final Pattern LOOKUP_DEFAULTS        = Pattern.compile("^Prism // Using defaults: .*$");

  // Prism // --- Inspecting jack o lantern at 5 67 282 ---
  public static final Pattern INSPECTOR_HEADER       = Pattern.compile("^Prism // --- Inspecting (?:\\w| )+ at (-?\\d+) (\\d+) (-?\\d+) ---$");

} // class PrismPatterns