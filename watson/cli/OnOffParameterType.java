package watson.cli;

import java.util.List;

// --------------------------------------------------------------------------
/**
 * The type of {@link Parameter}s whose valid values are "on" and "off".
 */
public class OnOffParameterType extends EnumerationParameterType
{
  public OnOffParameterType()
  {
    super(Boolean.class, "off", "on");
  }

  // --------------------------------------------------------------------------
  /**
   * Implements inherited method.
   */
  @Override
  public Object parse(List<String> args)
    throws CLIParseException
  {
    String arg = (String) super.parse(args);
    return arg.equals("on") ? Boolean.TRUE : Boolean.FALSE;
  }
} // class OnOffParemeterType