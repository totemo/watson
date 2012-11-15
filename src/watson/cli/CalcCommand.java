package watson.cli;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;

import net.minecraft.src.ICommandSender;
import net.minecraft.src.SyntaxErrorException;

// --------------------------------------------------------------------------
/**
 * A simple calculator with +, -, *, / and parentheses.
 * 
 * TODO: StreamTokenizer turns out to be more trouble than it's worth
 * (surprise!). It greedily groups '-' with a number into a negative number
 * literal, rather than treating it as negation or subtraction. So ditch it.
 * Also doesn't understand numbers in scientific notation. Useless.
 */
public class CalcCommand extends WatsonCommandBase
{
  // --------------------------------------------------------------------------
  /**
   * Main program for interactive testing.
   */
  public static void main(String[] args)
    throws IOException
  {
    String commandLine = concat(args);
    StreamTokenizer tokenizer = makeTokenizer(commandLine);
    dump(tokenizer);
    StreamTokenizer tokenizer2 = makeTokenizer(commandLine);
    System.out.printf("%g\n", calculation(tokenizer2));
  }

  // --------------------------------------------------------------------------
  /**
   * @see net.minecraft.src.ICommand#getCommandName()
   */
  @Override
  public String getCommandName()
  {
    return "calc";
  }

  // --------------------------------------------------------------------------
  /**
   * @see net.minecraft.src.ICommand#processCommand(net.minecraft.src.ICommandSender,
   *      java.lang.String[])
   */
  @Override
  public void processCommand(ICommandSender sender, String[] args)
  {
    if (args.length == 0)
    {
      help(sender);
      return;
    }
    else if (args.length == 1 && args[0].equals("help"))
    {
      help(sender);
      return;
    }
    else
    {
      String commandLine = concat(args);
      StreamTokenizer tokenizer = makeTokenizer(commandLine);
      try
      {
        // Light blue.
        localOutput(sender,
          String.format("%s = %g", commandLine, calculation(tokenizer)));
      }
      catch (IOException ex)
      {
        throw new SyntaxErrorException("commands.generic.syntax", new Object[0]);
      }
    }
  } // processCommand

  // --------------------------------------------------------------------------
  /**
   * Show a help message.
   */
  private void help(ICommandSender sender)
  {
    localOutput(sender, "Usage:");
    localOutput(sender, "  /calc help");
    localOutput(sender,
      "  /calc <expression>  -  Compute an arithmetic expression.");
    localOutput(sender, "Documentation: http://github.com/totemo/watson");
  }

  // --------------------------------------------------------------------------
  /**
   * Return the concatenation of the parsed/split command line arguments.
   * 
   * @return the concatenation of the parsed/split command line arguments.
   */
  private static String concat(String[] args)
  {
    StringBuilder commandLine = new StringBuilder();
    for (int i = 0; i < args.length; ++i)
    {
      commandLine.append(args[i]);
      if (i < args.length - 1)
      {
        commandLine.append(' ');
      }
    }
    return commandLine.toString();
  } // concat

  // --------------------------------------------------------------------------
  /**
   * Create a StreamTokenizer that tokenises the specified command line
   * arguments.
   * 
   * @param args command line arguments string.
   * @return a new StreamTokenizer that tokenises the args.
   */
  private static StreamTokenizer makeTokenizer(String args)
  {

    // Currently not supporting variables, but define words for future use.
    StreamTokenizer tokenizer = new StreamTokenizer(new StringReader(args));
    tokenizer.slashSlashComments(false);
    tokenizer.slashStarComments(false);
    tokenizer.wordChars('A', 'Z');
    tokenizer.wordChars('a', 'z');
    tokenizer.wordChars('_', '_');
    // These need to be defined as ordinary or they will be parsed as comment
    // introducers. >.<
    tokenizer.ordinaryChar('*');
    tokenizer.ordinaryChar('/');
    return tokenizer;
  } // makeTokenizer

  // --------------------------------------------------------------------------
  /**
   * ENBF:
   * 
   * <pre>
   * calculation ::= expr EOF
   * </pre>
   */
  private static double calculation(StreamTokenizer tokenizer)
    throws IOException
  {
    double result = expr(tokenizer);
    if (tokenizer.nextToken() != StreamTokenizer.TT_EOF)
    {
      throw new IOException();
    }
    return result;
  } // calculation

  // --------------------------------------------------------------------------
  /**
   * EBNF:
   * 
   * <pre>
   * expr ::= term ( ('+'|'-') term )*
   * </pre>
   */
  private static double expr(StreamTokenizer tokenizer)
    throws IOException
  {
    double result = term(tokenizer);
    for (;;)
    {
      int token = tokenizer.nextToken();
      if (token == '+')
      {
        result += term(tokenizer);
      }
      else if (token == '-')
      {
        result -= term(tokenizer);
      }
      else
      {
        break;
      }
    } // for
    tokenizer.pushBack();
    return result;
  } // expr

  // --------------------------------------------------------------------------
  /**
   * EBNF:
   * 
   * <pre>
   * term ::= factor ( ('*'|'/') factor )*
   * </pre>
   */
  private static double term(StreamTokenizer tokenizer)
    throws IOException
  {
    double result = factor(tokenizer);
    for (;;)
    {
      int token = tokenizer.nextToken();
      if (token == '*')
      {
        result *= factor(tokenizer);
      }
      else if (token == '/')
      {
        result /= factor(tokenizer);
      }
      else
      {
        break;
      }
    } // for
    tokenizer.pushBack();
    return result;
  } // term

  // --------------------------------------------------------------------------
  /**
   * EBNF:
   * 
   * <pre>
   * factor ::= number | '(' expr ')' | '-' factor
   * </pre>
   * 
   * Variables, when implemented, are also factors.
   */
  private static double factor(StreamTokenizer tokenizer)
    throws IOException
  {
    int token = tokenizer.nextToken();
    if (token == StreamTokenizer.TT_NUMBER)
    {
      return tokenizer.nval;
    }
    else if (token == '(')
    {
      double result = expr(tokenizer);
      if (tokenizer.nextToken() != ')')
      {
        throw new IOException();
      }
      return result;
    }
    else if (token == '-')
    {
      return -factor(tokenizer);
    }
    else
    {
      throw new IOException();
    }
  } // factor

  // --------------------------------------------------------------------------
  /**
   * Dump the stream of tokens from the tokenizer to standard output for
   * debugging purposes.
   * 
   * @param tokenizer the StreamTokenizer.
   */
  private static void dump(StreamTokenizer tokenizer)
    throws IOException
  {
    int token;
    do
    {
      token = tokenizer.nextToken();
      switch (token)
      {
        case StreamTokenizer.TT_NUMBER:
          System.out.println("NUMBER " + tokenizer.nval);
          break;
        case StreamTokenizer.TT_WORD:
          System.out.println("WORD " + tokenizer.sval);
          break;
        case StreamTokenizer.TT_EOF:
          System.out.println("EOF");
          break;
        default:
          System.out.println((char) token);
          break;
      }
    } while (token != StreamTokenizer.TT_EOF);
  } // dump
} // class CalcCommand