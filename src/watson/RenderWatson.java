package watson;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.src.ModLoader;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import watson.db.BlockEditSet;

// --------------------------------------------------------------------------
/**
 * A Render implementation that draws all of the 3-D objects known to Watson.
 * 
 * Minecraft maintains a map from Entity classes to the corresponding Render
 * subclass instance to draw entities of that class. Watson creates a single
 * EntityWatson in the world to represent the 3-D wireframe view of LogBlock
 * edits, and associates RenderWatson with it as the Render implementation.
 * RenderWatson draws all of the blocks, direction vectors and whatever other
 * 3-D iconography it may support, not just a single block.
 */
public class RenderWatson extends Render
{
  // --------------------------------------------------------------------------

  public RenderWatson()
  {
  }

  // --------------------------------------------------------------------------
  /**
   * Implements inherited method.
   */
  @Override
  public void doRender(Entity entity, double x, double y, double z,
                       float unknownParameter, float tick)
  {
    renderEntityWatson((EntityWatson) entity, tick);
  }

  // --------------------------------------------------------------------------
  /**
   * Don't waste any time drawing shadow or fire effects.
   */
  @Override
  public void doRenderShadowAndFire(Entity par1Entity, double x, double y,
                                    double z, float pitch, float yaw)
  {
    // Nothing.
  }

  // --------------------------------------------------------------------------

  EntityPlayerSP getPlayer()
  {
    return ModLoader.getMinecraftInstance().thePlayer;
  }

  // --------------------------------------------------------------------------

  public double getPlayerXGuess(float renderTick)
  {
    EntityPlayerSP p = getPlayer();
    return p.prevPosX + (p.posX - p.prevPosX) * renderTick;
  }

  // --------------------------------------------------------------------------

  public double getPlayerYGuess(float renderTick)
  {
    EntityPlayerSP p = getPlayer();
    return p.prevPosY + (p.posY - p.prevPosY) * renderTick;
  }

  // --------------------------------------------------------------------------

  public double getPlayerZGuess(float renderTick)
  {
    EntityPlayerSP p = getPlayer();
    return p.prevPosZ + (p.posZ - p.prevPosZ) * renderTick;
  }

  // --------------------------------------------------------------------------
  /**
   * Render an WatsonEntity instance at the specified x, y, z and time.
   */
  private void renderEntityWatson(@SuppressWarnings("unused") EntityWatson entity,
                                  float tick)
  {
    if (Configuration.instance.isEnabled()
        && Controller.instance.getDisplaySettings().isDisplayed())
    {
      RenderHelper.disableStandardItemLighting();

      GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
      GL11.glEnable(GL11.GL_BLEND);
      GL11.glDisable(GL11.GL_TEXTURE_2D);
      GL11.glDisable(GL11.GL_LIGHTING);
      GL11.glDisable(GL11.GL_FOG);

      GL11.glDepthMask(false);

      GL11.glPushMatrix();
      GL11.glTranslatef((float) -getPlayerXGuess(tick),
        (float) -getPlayerYGuess(tick), (float) -getPlayerZGuess(tick));
      GL11.glColor3f(1.0f, 1.0f, 1.0f); // Necessary?
      GL11.glDepthFunc(GL11.GL_ALWAYS); // Why do chests etc still occlude?

      BlockEditSet edits = Controller.instance.getBlockEditSet();
      edits.drawOutlines();
      edits.drawVectors();

      // Restore normal depth buffer function.
      GL11.glDepthFunc(GL11.GL_LEQUAL);
      GL11.glDepthMask(true);
      GL11.glEnable(GL11.GL_TEXTURE_2D);
      GL11.glDisable(GL11.GL_BLEND);
      GL11.glEnable(GL11.GL_FOG);
      GL11.glEnable(GL11.GL_LIGHTING);

      GL11.glPopMatrix();
      RenderHelper.enableStandardItemLighting();

      edits.drawAnnotations();
      edits.getOreDB().drawDepositLabels();
      // edits.drawHUD();
    } // if Watson is enabled and drawing
  } // renderEntityWatson

  // --------------------------------------------------------------------------
  /**
   * @see net.minecraft.client.renderer.entity.Render#getEntityTexture(net.minecraft.entity.Entity)
   */
  @Override
  protected ResourceLocation getEntityTexture(Entity entity)
  {
    return null;
  }
} // class RenderWatson