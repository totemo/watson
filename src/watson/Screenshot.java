package watson;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;

import net.minecraft.event.ClickEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

// ----------------------------------------------------------------------------
/**
 * Methods to save screenshots.
 */
public class Screenshot
{
  // --------------------------------------------------------------------------
  /**
   * Save a screenshot.
   * 
   * @param file the file to write.
   * @param width the screen width.
   * @param height the screen height.
   */
  public static IChatComponent save(File file, int width, int height)
  {
    try
    {
      file.getParentFile().mkdirs();

      ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);
      GL11.glReadBuffer(GL11.GL_FRONT);
      // GL11.glReadBuffer() unexpectedly sets an error state (invalid enum).
      GL11.glGetError();
      GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

      BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
      for (int x = 0; x < width; x++)
      {
        for (int y = 0; y < height; y++)
        {
          int i = (x + width * y) * 4;
          int r = buffer.get(i) & 0xFF;
          int g = buffer.get(i + 1) & 0xFF;
          int b = buffer.get(i + 2) & 0xFF;
          image.setRGB(x, (height - 1) - y, (0xFF << 24) | (r << 16) | (g << 8) | b);
        }
      }

      ImageIO.write(image, "png", file);
      ChatComponentText text = new ChatComponentText(file.getName());
      text.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, file.getAbsolutePath()));
      text.getChatStyle().setUnderlined(Boolean.valueOf(true));
      return new ChatComponentTranslation("screenshot.success", new Object[]{text});
    }
    catch (Exception ex)
    {
      return new ChatComponentTranslation("screenshot.failure", new Object[]{ex.getMessage()});
    }
  }

  // --------------------------------------------------------------------------
  /**
   * Return a unique PNG filename for the next screenshot.
   * 
   * @param dir the directory to contain the new file.
   * @param player the name of the player who did the most recently selected
   *          edit, or null if none is selected.
   * @param now the current time, on which the screenshot filename is based.
   * @return a unique PNG filename for the next screenshot.
   */
  public static File getUniqueFilename(File dir, String player, Date now)
  {
    String baseName = _DATE_FORMAT.format(now);

    int count = 1;
    String playerSuffix = (player == null) ? "" : "-" + player;
    while (true)
    {
      File result = new File(dir, baseName + playerSuffix
                                  + (count == 1 ? "" : "-" + count)
                                  + ".png");
      if (!result.exists())
      {
        return result;
      }
      ++count;
    }
  } // getScreenshotFile

  // --------------------------------------------------------------------------
  /**
   * Used to format dates for making screenshot filenames.
   */
  private static final DateFormat _DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
} // class Screenshot