package watson.analysis;

import java.util.regex.Pattern;

// ----------------------------------------------------------------------------
/**
 * Regular expressions describing chat messages output by LogBlock.
 */
public interface LogBlockPatterns
{
  public static final Pattern LB_POSITION             = Pattern.compile("^Block changes at (-?\\d+):(-?\\d+):(-?\\d+) in .+:$");

  // Optional bit at the end deals with signs.
  public static final Pattern LB_EDIT                 = Pattern.compile("^(\\d{2})-(\\d{2}) (\\d{2}):(\\d{2}):(\\d{2}) (\\w+) (created|destroyed) ((?: |\\w)+)( \\[.*\\] \\[.*\\] \\[.*\\] \\[.*\\])?$");

  public static final Pattern LB_EDIT_REPLACED        = Pattern.compile("^(\\d{2})-(\\d{2}) (\\d{2}):(\\d{2}):(\\d{2}) (\\w+) replaced ((?: |\\w)+) with ((?: |\\w)+)$");

  public static final Pattern LB_COORD                = Pattern.compile("^\\((\\d+)\\) (\\d{2})-(\\d{2}) (\\d{2}):(\\d{2}):(\\d{2}) (\\w+) (created|destroyed) ([a-zA-Z ]+)(?: \\[(.*)\\] \\[(.*)\\] \\[(.*)\\] \\[(.*)\\])? at (-?\\d+):(\\d+):(-?\\d+)$");

  public static final Pattern LB_COORD_KILLS          = Pattern.compile("^\\((\\d+)\\) (\\d{2})-(\\d{2}) (\\d{2}):(\\d{2}):(\\d{2}) (\\w+) (killed) ([a-zA-Z ]+) at (-?\\d+):(\\d+):(-?\\d+) with (.*)$");

  public static final Pattern LB_COORD_REPLACED       = Pattern.compile("^\\((\\d+)\\) (\\d{2})-(\\d{2}) (\\d{2}):(\\d{2}):(\\d{2}) (\\w+) replaced ([a-zA-Z ]+) with ([a-zA-Z ]+) at (-?\\d+):(\\d+):(-?\\d+)$");

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