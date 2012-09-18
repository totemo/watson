package watson;

import java.util.Comparator;

// ----------------------------------------------------------------------------
/**
 * A Comparator<> implementation that orders {@link BlockEdit} instances (each
 * representing one LogBlock query result line) in ascending order by time
 * stamp, primarily, and then by edit action (created/destroyed), block ID and
 * finally x, y and z coordinates.
 * 
 * Timestamps on LogBlock results for individual edits have a precision of 1
 * second (at least as they are reported in chat - they may be more precise in
 * the database), meaning that the timestamp does not uniquely identify an edit.
 * So we use other attributes of the edit to disambiguate. It would still be
 * possible for a player to create edits that compare as equal by destroying a
 * block, and then creating the same type and destroying it again within the
 * space of one second, and in that case Watson will collapse the three edits
 * into one. However, it is a very infrequent situation (more frequent in
 * creative mode, admittedly) and the harm done by loss of that information
 * would seem to be minimal.
 * 
 * The result number in a LogBlock result report in chat <i>does</> uniquely
 * identify an edit. However, that number will change if the user reissues the
 * /lb command and the player is still making edits in the time range. If we
 * only use Watson to issue LogBlock queries, then Watson can determine the
 * current server local time and convert relative time query constraints into
 * absolute time bounds, so that the result set will be identical every time the
 * query is reissued. However, I think it is desirable to allow the user to
 * directly control LogBlock and have Watson display those results. (It also
 * avoids the problem of having to separate the LogBlock results that come from
 * Watson queries from those that are the result of /lb commands by the user.)
 * So, I've made the decision to impose this artificial ordering on all
 * BlockEdits.
 */
public class BlockEditComparator implements Comparator<BlockEdit>
{
  // --------------------------------------------------------------------------
  /**
   * Compare two {@link BlockEdit}s.
   * 
   * @param l an edit.
   * @param r another edit.
   * @return +ve if (l > r); -ve if (l < r); 0 if (l == r)
   */
  @Override
  public int compare(BlockEdit l, BlockEdit r)
  {
    if (l.time < r.time)
    {
      return -1;
    }
    else if (l.time > r.time)
    {
      return +1;
    }
    else
    {
      // false < true
      if (!l.creation && r.creation)
      {
        return -1;
      }
      // true > false
      else if (l.creation && !r.creation)
      {
        return +1;
      }
      else
      {
        int dx = l.x - r.x;
        if (dx != 0)
        {
          return dx;
        }
        else
        {
          int dy = l.y - r.y;
          if (dy != 0)
          {
            return dy;
          }
          else
          {
            return (l.z - r.z);
          } // same y
        } // same x
      } // same action
    } // same timestamp
  } // compare
} // class BlockEditComparator
