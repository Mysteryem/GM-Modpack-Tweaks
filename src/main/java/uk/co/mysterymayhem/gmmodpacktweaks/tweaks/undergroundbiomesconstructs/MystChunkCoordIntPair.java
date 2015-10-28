package uk.co.mysterymayhem.gmmodpacktweaks.tweaks.undergroundbiomesconstructs;

import net.minecraft.world.ChunkCoordIntPair;

/**
 *
 * @author Mysteryem
 */
public class MystChunkCoordIntPair extends ChunkCoordIntPair{

  public MystChunkCoordIntPair(int chunkX, int chunkZ) {
    super(chunkX, chunkZ);
  }

  @Override
  public String toString() {
    return "(" + this.chunkXPos + ", " + this.chunkZPos + ")";
  }
  
  
  
}
