package net.minecraft.src;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.IntBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import watson.Controller;

public class ScreenShotHelper
{
  private static final DateFormat field_74295_a = new SimpleDateFormat(
                                                  "yyyy-MM-dd_HH.mm.ss");
  private static IntBuffer        field_74293_b;
  private static int[]            field_74294_c;

  /**
   * Takes a screenshot and saves it to the screenshots directory. Returns the
   * filename of the screenshot.
   */
  public static String saveScreenshot(File par0File, int par1, int par2)
  {
    return func_74292_a(par0File, (String) null, par1, par2);
  }

  public static String func_74292_a(File par0File, String par1Str, int par2,
                                    int par3)
  {
    try
    {
      File var4 = new File(par0File, "screenshots");
      var4.mkdir();
      int var5 = par2 * par3;

      if (field_74293_b == null || field_74293_b.capacity() < var5)
      {
        field_74293_b = BufferUtils.createIntBuffer(var5);
        field_74294_c = new int[var5];
      }

      GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 1);
      GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
      field_74293_b.clear();
      GL11.glReadPixels(0, 0, par2, par3, GL12.GL_BGRA,
        GL12.GL_UNSIGNED_INT_8_8_8_8_REV, field_74293_b);
      field_74293_b.get(field_74294_c);
      func_74289_a(field_74294_c, par2, par3);
      BufferedImage var6 = new BufferedImage(par2, par3, 1);
      var6.setRGB(0, 0, par2, par3, field_74294_c, 0, par2);
      File var7;

      if (par1Str == null)
      {
        var7 = func_74290_a(var4);
      }
      else
      {
        var7 = new File(var4, par1Str);
      }

      ImageIO.write(var6, "png", var7);
      return "Saved screenshot as " + var7.getName();
    }
    catch (Exception var8)
    {
      var8.printStackTrace();
      return "Failed to save: " + var8;
    }
  }

  private static File func_74290_a(File par0File)
  {
    String var2 = field_74295_a.format(new Date()).toString();
    int var3 = 1;

    // Append player name if set.
    String player = (String) Controller.instance.getVariables().get("player");
    String playerSuffix = (player != null) ? "-" + player : "";
    while (true)
    {
      // For numbers, change to a minus sign so it is not a valid player name
      // character.
      File var1 = new File(par0File, var2 + playerSuffix
                                     + (var3 == 1 ? "" : "-" + var3) + ".png");

      if (!var1.exists())
      {
        return var1;
      }

      ++var3;
    }
  }

  private static void func_74289_a(int[] par0ArrayOfInteger, int par1, int par2)
  {
    int[] var3 = new int[par1];
    int var4 = par2 / 2;

    for (int var5 = 0; var5 < var4; ++var5)
    {
      System.arraycopy(par0ArrayOfInteger, var5 * par1, var3, 0, par1);
      System.arraycopy(par0ArrayOfInteger, (par2 - 1 - var5) * par1,
        par0ArrayOfInteger, var5 * par1, par1);
      System.arraycopy(var3, 0, par0ArrayOfInteger, (par2 - 1 - var5) * par1,
        par1);
    }
  }
}
