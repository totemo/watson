package watson.analysis;

import java.util.regex.Pattern;

// ----------------------------------------------------------------------------
/**
 * Regular expressions describing chat messages output by LogBlock.
 *
 * I would love to specify these patterns with named groups, but I can't until
 * Mojang fixes Minecraft's Java support on the Mac.
 *
 * (It is possible to fix it yourself, but not everybody is technically
 * inclined.)
 * <ol>
 * <li>https://bugs.mojang.com/browse/MCL-1049</li>
 * <li>
 * http://www.cgwerks.com/make-minecraft-work-mac-osx-yosemite-latest-java-8/</li>
 * <li>http://kovuthehusky.com/blog/running-minecraft-on-os-x-using-java-7/</li>
 * </ol>
 */
public interface LogBlockPatterns
{
  public static final Pattern LB_POSITION             = Pattern.compile("^Block changes at (-?\\d+):(-?\\d+):(-?\\d+) in .+:$");
  // Java 1.7: public static final Pattern LB_POSITION =
  // Pattern.compile("^Block changes at (?<x>-?\\d+):(?<y>-?\\d+):(?<z>-?\\d+) in .+:$");

  // Optional bit at the end deals with signs.
  public final Pattern        LB_EDIT                 = Pattern.compile("^((?:\\d{2,4}-)?\\d{2}-\\d{2}) (\\d{2}):(\\d{2}):(\\d{2}) (\\w+) (created|destroyed) ((?: |\\w)+)( \\[.*\\] \\[.*\\] \\[.*\\] \\[.*\\])?$");
  // Java 1.7: public final Pattern LB_EDIT =
  // Pattern.compile("^(?<date>(?:\\d{2,4}-)?\\d{2}-\\d{2}) (?<hour>\\d{2}):(?<min>\\d{2}):(?<sec>\\d{2}) (?<player>\\w+) (?<action>created|destroyed) (?<block>(?: |\\w)+)(?<sign> \\[.*\\] \\[.*\\] \\[.*\\] \\[.*\\])?$");

  public static final Pattern LB_EDIT_REPLACED        = Pattern.compile("^((?:\\d{2,4}-)?\\d{2}-\\d{2}) (\\d{2}):(\\d{2}):(\\d{2}) (\\w+) replaced ((?: |\\w)+) with ((?: |\\w)+)$");
  // Java 1.7: public static final Pattern LB_EDIT_REPLACED =
  // Pattern.compile("^(?<date>(?:\\d{2,4}-)?\\d{2}-\\d{2}) (?<hour>\\d{2}):(?<min>\\d{2}):(?<sec>\\d{2}) (?<player>\\w+) replaced (?<oldblock>(?: |\\w)+) with (?<newblock>(?: |\\w)+)$");

  public static final Pattern LB_COORD                = Pattern.compile("^\\((\\d+)\\) ((?:\\d{2,4}-)?\\d{2}-\\d{2}) (\\d{2}):(\\d{2}):(\\d{2}) (\\w+) (created|destroyed) ([a-zA-Z ]+)(?: \\[(?<sign1>.*)\\] \\[(?<sign2>.*)\\] \\[(?<sign3>.*)\\] \\[(?<sign4>.*)\\])? at (-?\\d+):(\\d+):(-?\\d+)$");
  // Java 1.7: public static final Pattern LB_COORD =
  // Pattern.compile("^\\((?<index>\\d+)\\) (?<date>(?:\\d{2,4}-)?\\d{2}-\\d{2}) (?<hour>\\d{2}):(?<min>\\d{2}):(?<sec>\\d{2}) (?<player>\\w+) (?<action>created|destroyed) (?<block>[a-zA-Z ]+)(?: \\[(?<sign1>.*)\\] \\[(?<sign2>.*)\\] \\[(?<sign3>.*)\\] \\[(?<sign4>.*)\\])? at (?<x>-?\\d+):(?<y>\\d+):(?<z>-?\\d+)$");

  public static final Pattern LB_COORD_KILLS          = Pattern.compile("^\\((\\d+)\\) ((?:\\d{2,4}-)?\\d{2}-\\d{2}) (\\d{2}):(\\d{2}):(\\d{2}) (\\w+) killed ([a-zA-Z ]+) at (-?\\d+):(\\d+):(-?\\d+) with (.*)$");
  // Java 1.7: public static final Pattern LB_COORD_KILLS =
  // Pattern.compile("^\\((?<index>\\d+)\\) (?<date>(?:\\d{2,4}-)?\\d{2}-\\d{2}) (?<hour>\\d{2}):(?<min>\\d{2}):(?<sec>\\d{2}) (?<player>\\w+) killed (?<victim>[a-zA-Z ]+) at (?<x>-?\\d+):(?<y>\\d+):(?<z>-?\\d+) with (?<weapon>.*)$");

  public static final Pattern LB_COORD_REPLACED       = Pattern.compile("^\\((\\d+)\\) ((?:\\d{2,4}-)?\\d{2}-\\d{2}) (\\d{2}):(\\d{2}):(\\d{2}) (\\w+) replaced ([a-zA-Z ]+) with ([a-zA-Z ]+) at (-?\\d+):(\\d+):(-?\\d+)$");
  // Java 1.7: public static final Pattern LB_COORD_REPLACED =
  // Pattern.compile("^\\((?<index>\\d+)\\) (?<date>(?:\\d{2,4}-)?\\d{2}-\\d{2}) (?<hour>\\d{2}):(?<min>\\d{2}):(?<sec>\\d{2}) (?<player>\\w+) replaced (?<oldblock>[a-zA-Z ]+) with (?<newblock>[a-zA-Z ]+) at (?<x>-?\\d+):(?<y>\\d+):(?<z>-?\\d+)$");

  public static final Pattern LB_TP                   = Pattern.compile("^Teleported to (-?\\d+):(\\d+):(-?\\d+)$");

  public static final Pattern LB_PAGE                 = Pattern.compile("^Page (\\d+)/(\\d+)$");

  public static final Pattern LB_HEADER_NO_RESULTS    = Pattern.compile("^No results found\\.$");

  public static final Pattern LB_HEADER_CHANGES       = Pattern.compile("^\\d+ changes? found\\.$");

  public static final Pattern LB_HEADER_BLOCKS        = Pattern.compile("^\\d+ blocks? found\\.$");

  public static final Pattern LB_HEADER_SUM_BLOCKS    = Pattern.compile("^Created - Destroyed - Block$");

  public static final Pattern LB_HEADER_SUM_PLAYERS   = Pattern.compile("^Created - Destroyed - Player$");

  public static final Pattern LB_HEADER_SEARCHING     = Pattern.compile("^Searching Block changes from player \\w+ in the last \\d+ minutes (?:within \\d+ blocks of you )?in .+:$");

  // A specialisation of ID lb.header.block, below, to match the query issued
  // when /w ratio is done.
  public static final Pattern LB_HEADER_RATIO         = Pattern.compile("^Stone and diamond ore changes from player \\w+ between (\\d+) and (\\d+) minutes ago in .+ summed up by blocks:$");

  public static final Pattern LB_HEADER_RATIO_CURRENT = Pattern.compile("^Stone and diamond ore changes from player \\w+ in the last (\\d+) minutes in .+ summed up by blocks:$");

  // A specialisation of ID lb.header.block for the query used to infer the
  // local time on the server.
  public static final Pattern LB_HEADER_TIME_CHECK    = Pattern.compile("Block changes from player watsonservertimecheck between (\\d+) and \\d+ minutes ago in .+:");

  // Negative time: Query is correct. LogBlock header is wrong. Result is right.
  // "§3Block changes from player darkhawk02 more than -81 minutes ago in world:"
  // "§3Diamond ore changes from player totemo in the last 1440 minutes in world:"
  // "§3Stone and diamond ore changes from player freddo between 869 and 787 minutes ago in world summed up by blocks:"
  public static final Pattern LB_HEADER_BLOCK         = Pattern.compile("^(?: |,|\\w)+ (?:destructions|changes) from player \\w+ (?:in the last \\d+ minutes |between \\d+ and \\d+ minutes ago |more than -?\\d+ minutes ago )?(?:within \\d+ blocks of you )?in .+(?: summed up by (players|blocks))?:$");

  public static final Pattern LB_SUM                  = Pattern.compile("^(\\d+)[ ]{6,}(\\d+)[ ]{6,}((?:\\w| )+)$");

} // class LogBlockPatterns