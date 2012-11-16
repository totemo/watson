package net.minecraft.src;

import java.util.ArrayList;
import java.util.List;

import ClientCommands.ChatCallback;
import ClientCommands.ClientCommandManager;

public class mod_ClientCommands extends BaseMod
{
  private static mod_ClientCommands instance             = null;
  private ClientCommandManager      clientCommandManager = new ClientCommandManager();
  // private ClientCommandSender commandSender;
  private List<ChatCallback>        chatCallbacks        = new ArrayList<ChatCallback>();

  public Boolean handleClientCommand(String commandLine)
  {
    /*
     * if (!text.startsWith("/")) return false;
     * 
     * String commandName = text.substring(1).split(" ")[0]; ICommand command =
     * (ICommand)clientCommandManager.getCommands().get(commandName);
     * 
     * if (command != null) { clientCommandManager.executeCommand(new
     * ClientCommandSender(clientCommandManager), text); return true; }
     * 
     * return false;
     */
    return clientCommandManager.handleClientCommand(commandLine);
  }

  public String executeChatCallbacks(String text)
  {
    StringBuilder textBuilder = new StringBuilder(text);
    for (ChatCallback callback : getChatCallbacks())
      if (callback.execute(textBuilder))
        break;

    return textBuilder.toString();
  }

  public mod_ClientCommands()
  {
    instance = this;
  }

  public static mod_ClientCommands getInstance()
  {
    if (instance == null)
      instance = new mod_ClientCommands();

    return instance;
  }

  @Override
  public String getVersion()
  {
    return "v0.9 by slide23, totemo for Minecraft v1.4.4";
  }

  @Override
  public void load()
  {
  }

  public ClientCommandManager getClientCommandManager()
  {
    return clientCommandManager;
  }
  public void addChatCallback(ChatCallback callback)
  {
    chatCallbacks.add(callback);
  }
  public void registerCommand(ICommand par1ICommand)
  {
    clientCommandManager.registerCommand(par1ICommand);
  }
  public List<ChatCallback> getChatCallbacks()
  {
    return chatCallbacks;
  }
}