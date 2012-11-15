package watson.cli;

import java.util.ArrayList;
import java.util.HashMap;

import net.minecraft.src.ICommandSender;
import net.minecraft.src.SyntaxErrorException;
import watson.Annotation;
import watson.BlockEditSet;
import watson.Controller;

// --------------------------------------------------------------------------
/**
 * A command to manipulate annotations in the current {@link BlockEditSet}.
 */
public class AnnoCommand extends WatsonCommandBase
{
  // --------------------------------------------------------------------------
  /**
   * @see net.minecraft.src.ICommand#getCommandName()
   */
  @Override
  public String getCommandName()
  {
    return "anno";
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
    else if (args.length == 1)
    {
      if (args[0].equals("help"))
      {
        help(sender);
        return;
      }
      else if (args[0].equals("list"))
      {
        BlockEditSet edits = Controller.instance.getBlockEditSet();
        ArrayList<Annotation> annotations = edits.getAnnotations();

        localOutput(sender,
          String.format("%d annotation(s)", annotations.size()));
        int index = 1;
        for (Annotation annotation : annotations)
        {
          String line = String.format("(%d) (%d,%d,%d) %s", index,
            annotation.getX(), annotation.getY(), annotation.getZ(),
            annotation.getText());
          localOutput(sender, line);
          ++index;
        }
        return;
      }
      else if (args[0].equals("clear"))
      {
        BlockEditSet edits = Controller.instance.getBlockEditSet();
        ArrayList<Annotation> annotations = edits.getAnnotations();
        localOutput(sender,
          String.format("%d annotation(s) cleared.", annotations.size()));
        annotations.clear();
        return;
      }
    }
    else if (args.length >= 2)
    {
      if (args[0].equals("tp"))
      {
        if (args.length == 2)
        {
          BlockEditSet edits = Controller.instance.getBlockEditSet();
          ArrayList<Annotation> annotations = edits.getAnnotations();

          int index = Integer.parseInt(args[1]) - 1;
          if (index >= 0 && index < annotations.size())
          {
            Annotation annotation = annotations.get(index);
            Controller.instance.serverChat(String.format("/tppos %d %d %d",
              annotation.getX(), annotation.getY(), annotation.getZ()));
          }
          else
          {
            localError(sender, "Annotation index out of range.");
          }
          return;
        }
      }
      else if (args[0].equals("remove"))
      {
        if (args.length == 2)
        {
          BlockEditSet edits = Controller.instance.getBlockEditSet();
          ArrayList<Annotation> annotations = edits.getAnnotations();

          int index = Integer.parseInt(args[1]) - 1;
          if (index >= 0 && index < annotations.size())
          {
            annotations.remove(index);
            localOutput(sender,
              String.format("Removed annotation #%d", (index + 1)));
          }
          else
          {
            localError(sender, "Annotation index out of range.");
          }
          return;
        }
      }
      else if (args[0].equals("add"))
      {
        HashMap<String, Object> vars = Controller.instance.getVariables();
        Integer x = (Integer) vars.get("x");
        Integer y = (Integer) vars.get("y");
        Integer z = (Integer) vars.get("z");
        if (x == null || y == null || z == null)
        {
          localError(sender, "Use the LogBlock tool to set a position.");
        }
        else
        {
          StringBuilder text = new StringBuilder();
          for (int i = 1; i < args.length; ++i)
          {
            text.append(args[i]);
            if (i < args.length - 1)
            {
              text.append(' ');
            }
          }
          BlockEditSet edits = Controller.instance.getBlockEditSet();
          ArrayList<Annotation> annotations = edits.getAnnotations();
          Annotation annotation = new Annotation(x, y, z, text.toString());
          annotations.add(annotation);
          String description = String.format("(%d) (%d,%d,%d) %s",
            annotations.size(), annotation.getX(), annotation.getY(),
            annotation.getZ(), annotation.getText());
          localOutput(sender, description);
        }
        return;
      }
    }

    throw new SyntaxErrorException("commands.generic.syntax", new Object[0]);
  } // processCommand

  // --------------------------------------------------------------------------
  /**
   * Show a help message.
   */
  private void help(ICommandSender sender)
  {
    localOutput(sender, "Usage:");
    localOutput(sender, "  /anno help");
    localOutput(sender, "  /anno clear");
    localOutput(sender, "  /anno list");
    localOutput(sender, "  /anno add <text>");
    localOutput(sender, "  /anno remove <number>");
    localOutput(sender, "  /anno tp <number>");
    localOutput(sender, "Documentation: http://github.com/totemo/watson");
  }

} // class AnnoCommand