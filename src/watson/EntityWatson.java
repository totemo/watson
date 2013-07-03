package watson;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.src.ModLoader;
import net.minecraft.util.Vec3;
import watson.debug.Log;

// --------------------------------------------------------------------------
/**
 * An Entity that used to trigger rendering of all of the edits currently under
 * question.
 * 
 * NOTES:
 * <ul>
 * <li>Other methods that I may want to override: getBrightness(),
 * getBrightnessForRender().</li>
 * 
 * </ul>
 */
public class EntityWatson extends Entity
{
  /**
   * Constructor.
   * 
   * @param mc the Minecraft instance.
   */
  public EntityWatson(Minecraft mc)
  {
    super(mc.theWorld);
    Log.debug("EntityWatson constructed.");
    // Mainly because WECUI does it...
    ignoreFrustumCheck = true;
  }

  // --------------------------------------------------------------------------
  /**
   * Implements inherited method.
   */
  @Override
  protected void entityInit()
  {

  }

  // --------------------------------------------------------------------------
  /**
   * Return true so that the Watson entity is drawn at any distance from the
   * player.
   */
  public boolean isInRangeToRenderVec3D(Vec3 par1Vec3)
  {
    return true;
  }

  // --------------------------------------------------------------------------
  /**
   * Gets called every tick from main Entity class
   */
  @Override
  public void onUpdate()
  {
    // moveToPlayerPosition();
  }

  // --------------------------------------------------------------------------
  /**
   * Setting the brightness of this entity to 255 ensures that it is always
   * drawn at full brightness.
   * 
   * An alternative approach would have been to call
   * Tesselator.setBrightness(255) after calling Tesselator.startDrawing().
   */
  public int getBrightnessForRender(float par1)
  {
    return 0xf000f0;
  }

  // --------------------------------------------------------------------------
  /**
   * Always fully bright. Not sure if actually necessary.
   */
  @Override
  public float getBrightness(float f)
  {
    return 1f;
  }

  // --------------------------------------------------------------------------
  /**
   * Just because I'm curious when/if this will get called for this entity...
   */
  @Override
  public void setDead()
  {
    super.setDead();
    System.out.println("setDead() called");
  }

  // --------------------------------------------------------------------------
  /**
   * Implements inherited method.
   */
  @Override
  protected void readEntityFromNBT(NBTTagCompound var1)
  {
    // Do nothing. Not loaded.
  }

  // --------------------------------------------------------------------------
  /**
   * Implements inherited method.
   */
  @Override
  protected void writeEntityToNBT(NBTTagCompound var1)
  {
    // Do nothing. Not saved.
  }

  // --------------------------------------------------------------------------
  /**
   * Set the position of this entity to coincide with that of the current
   * player.
   */
  private void moveToPlayerPosition()
  {
    // In order for an Entity to be visible (i.e. the Watson display), it must
    // be within range of the player. So we simply move that entity to coincide
    // with the player, constantly.
    EntityPlayerSP p = ModLoader.getMinecraftInstance().thePlayer;
    setPosition(p.posX, p.posY, p.posZ);
  }
} // class WatsonEntity