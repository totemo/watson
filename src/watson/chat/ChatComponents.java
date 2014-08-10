package watson.chat;

import java.util.ArrayList;
import java.util.HashMap;

import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

// ----------------------------------------------------------------------------
/**
 * utility methods for dealing with IChatComponent.
 */
public class ChatComponents
{
  // --------------------------------------------------------------------------
  /**
   * Return true of the chat has associated events (click, hover).
   * 
   * The current Watson highlighting implementation only supports old style
   * formatting codes and so can only highlight IChatComponents that don't have
   * associated events.
   * 
   * @return true of the chat has associated events (click, hover).
   */
  public static boolean hasEvents(IChatComponent chat)
  {
    return chat.getChatStyle().getChatClickEvent() != null ||
           chat.getChatStyle().getChatHoverEvent() != null;
  }

  // --------------------------------------------------------------------------
  /**
   * Return an array containing the specified IChatComponent and all its
   * siblings.
   * 
   * @return an array containing the specified IChatComponent and all its
   *         siblings.
   */
  @SuppressWarnings("unchecked")
  public static ArrayList<IChatComponent> getComponents(IChatComponent chat)
  {
    ArrayList<IChatComponent> components = new ArrayList<IChatComponent>();
    for (Object o : chat)
    {
      IChatComponent component = (IChatComponent) o;
      IChatComponent copy = new ChatComponentText(component.getUnformattedTextForChat());
      copy.setChatStyle(component.getChatStyle().createDeepCopy());
      components.add(copy);
    }
    return components;
  }

  // --------------------------------------------------------------------------
  /**
   * Return an array containing all of the components in the input array and all
   * of their siblings, in their natural order.
   * 
   * @param components an array of IChatComponents.
   * @return an array containing all of the components in the input array and
   *         all of their siblings, in their natural order.
   */
  public static ArrayList<IChatComponent> flatten(ArrayList<IChatComponent> components)
  {
    ArrayList<IChatComponent> result = new ArrayList<IChatComponent>();
    for (IChatComponent component : components)
    {
      result.addAll(getComponents(component));
    }
    return result;
  }

  // --------------------------------------------------------------------------
  /**
   * Convert an array of IChatComponents into a single IChatComponent with all
   * the individual components as siblings.
   * 
   * @param component the array of components to be added.
   * @return an IChatComponent containing copies of all of the components in the
   *         array.
   */
  public static IChatComponent toChatComponent(ArrayList<IChatComponent> components)
  {
    ArrayList<IChatComponent> all = flatten(components);
    if (components.size() == 0)
    {
      return new ChatComponentText("");
    }
    else
    {
      IChatComponent result = all.get(0);
      for (int i = 1; i < all.size(); ++i)
      {
        IChatComponent component = all.get(i);

        if (component.getUnformattedTextForChat().length() != 0 || !component.getChatStyle().isEmpty())
        {
          result.appendSibling(component);
        }
      }
      return result;
    }
  } // toChatComponent

  // --------------------------------------------------------------------------
  /**
   * Map a formatting character to the corresponding Minecraft
   * EnumChatFormatting.
   * 
   * @param code the formatting code.
   * @param the corresponding enum value.
   */
  public static EnumChatFormatting getEnumChatFormatting(char code)
  {
    return _formatCharToEnum.get(code);
  }

  // --------------------------------------------------------------------------
  /**
   * Dump information about the IChatComponent to standard output.
   * 
   * @patam component the component.
   */
  public static void dump(IChatComponent component)
  {
    System.out.println("Formatted: " + component.getFormattedText());
    dump(flatten(getComponents(component)));
  }

  // --------------------------------------------------------------------------
  /**
   * Dump information about the IChatComponent to standard output.
   * 
   * @patam component the component.
   */
  public static void dump(ArrayList<IChatComponent> components)
  {
    System.out.println("Dump: " + toChatComponent(components).getFormattedText());
    for (int i = 0; i < components.size(); ++i)
    {
      IChatComponent c = components.get(i);
      System.out.println(i + ": " + hasEvents(c) + ": \"" + c.getFormattedText() + "\" "
                         + c.getUnformattedTextForChat().length() + " "
                         + c.getChatStyle().isEmpty() + " " + c.getChatStyle().toString());
    }
  }

  // --------------------------------------------------------------------------
  /**
   * Map formatting character to the corresponding Minecraft EnumChatFormatting.
   */
  private static HashMap<Character, EnumChatFormatting> _formatCharToEnum = new HashMap<Character, EnumChatFormatting>();

  static
  {
    for (EnumChatFormatting format : EnumChatFormatting.values())
    {
      _formatCharToEnum.put(format.getFormattingCode(), format);
    }
  }
} // class ChatComponents