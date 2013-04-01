package watson.db;

import java.util.LinkedHashSet;

import watson.Controller;

// ----------------------------------------------------------------------------
/**
 * Determines which edits are stored (in a {@link BlocKEditSet}) and which are
 * ignored.
 * 
 * Filters are stored as the lower-case version of the player name and checked
 * case-insensitively.
 */
public class Filters
{
  // --------------------------------------------------------------------------
  /**
   * List all of the accepted (stored) edits.
   */
  public void list()
  {
    if (_filters.size() == 0)
    {
      Controller.instance.localOutput("No filters are set. All edits are accepted.");
    }
    else
    {
      Controller.instance.localOutput("Edits by the following players will be accepted:");
      StringBuilder message = new StringBuilder(' ');
      for (String player : _filters)
      {
        message.append(' ');
        message.append(player);
      }
      Controller.instance.localOutput(message.toString());
    }
  } // list

  // --------------------------------------------------------------------------
  /**
   * Remove all filters.
   */
  public void clear()
  {
    Controller.instance.localOutput("All filters cleared.");
    _filters.clear();
  }

  // --------------------------------------------------------------------------
  /**
   * Record that the specified player's edits will be stored.
   * 
   * When a new filter is set, the player variable is also set so that
   * subsequent screenshots are attributed to that player, even when no edits
   * have yet been seen.
   * 
   * @param player the name of the player.
   */
  public void addPlayer(String player)
  {
    player = player.toLowerCase();
    Controller.instance.localOutput("Added a filter to accept edits by "
                                    + player + ".");
    _filters.add(player);
    Controller.instance.getVariables().put("player", player);
  }

  // --------------------------------------------------------------------------
  /**
   * Remove the filter allowing the specified player's edits to be accepted.
   * 
   * @param player the name of the player.
   */
  public void removePlayer(String player)
  {
    player = player.toLowerCase();
    if (_filters.contains(player))
    {
      Controller.instance.localOutput("Removed the filter for " + player + ".");
      _filters.remove(player);
    }
    else
    {
      Controller.instance.localError("There is no filter for " + player + ".");
    }
  } // removePlayer

  // --------------------------------------------------------------------------
  /**
   * Return true if edits by the specified player are accepted.
   * 
   * @return true if edits by the specified player are accepted.
   */
  public boolean isAcceptedPlayer(String player)
  {
    player = player.toLowerCase();
    return _filters.size() == 0 || _filters.contains(player);
  }

  // --------------------------------------------------------------------------
  /**
   * The names of all the players whose edits will be stored when returned from
   * a query. If this set is empty, all edits are stored without filtration.
   */
  protected LinkedHashSet<String> _filters = new LinkedHashSet<String>();
} // class Filters