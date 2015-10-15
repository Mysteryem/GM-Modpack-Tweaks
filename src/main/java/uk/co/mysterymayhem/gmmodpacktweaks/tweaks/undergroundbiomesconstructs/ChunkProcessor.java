/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.mysterymayhem.gmmodpacktweaks.tweaks.undergroundbiomesconstructs;

import Zeno410Utils.BlockState;
import exterminatorJeff.undergroundBiomes.api.BiomeGenUndergroundBase;
import exterminatorJeff.undergroundBiomes.api.NamedVanillaBlock;
import exterminatorJeff.undergroundBiomes.api.UBAPIHook;
import exterminatorJeff.undergroundBiomes.api.UBStoneCodes;
import exterminatorJeff.undergroundBiomes.common.DimensionManager;
import exterminatorJeff.undergroundBiomes.common.WorldGenManager;
import java.util.HashMap;
import java.util.Iterator;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import uk.co.mysterymayhem.gmmodpacktweaks.util.Misc;

/**
 *
 * @author Thomas
 */
class ChunkProcessor {

  private static final HashMap<World, ChunkProcessor> processors = new HashMap<>();

  static ChunkProcessor get(World world, UndergroundBiomesConstructs ubcTweak) {
    ChunkProcessor get = processors.get(world);
    if (get == null) {
      get = new ChunkProcessor(world, ubcTweak);
      processors.put(world, get);
    }
    return get;
  }
  private final World world;
  private final UndergroundBiomesConstructs ubcTweak;

  private ChunkProcessor(World world, UndergroundBiomesConstructs ubcTweak) {
    this.world = world;
    this.ubcTweak = ubcTweak;
  }

  // To be called whenever a chunk is finished populating
  void postPopulate(ChunkRef populated) {
    Iterator<ChunkRef> iterator = populated.notifyOnPostPopulate.iterator();
      while (iterator.hasNext()) {
        ChunkRef toNotify = iterator.next();
        // After notifying a particular chunk that we've been populated, we can forget all about needing to notify that chunk
        iterator.remove();
        notify(toNotify, populated);
      }
//    for (ChunkRef toNotify : decorated.notifyOnPostDecorate) {
//      // Tell everyone who's interested that I've been populated
//      notify(toNotify, decorated);
//    }
    // We want to see if the surrounding chunks have been populated, if so, we replace the blocks in this chunk
    for (int x = -1; x <= 1; x++) {
      for (int z = -1; z <= 1; z++) {
        if (x == 0 && z == 0) {
          continue;
        }
        // Check if the chunk exists, if it does and the chunk is not decorated or if it doesn't exist, we must wait for it to become decorated
        // The order is important here, as checking if it's decorated will attempt to create the chunk if it doesn't exist, leading to an infinite loop
        if (!world.getChunkProvider().chunkExists(populated.x + x, populated.z + z) || !world.getChunkFromChunkCoords(populated.x + x, populated.z + z).isTerrainPopulated) {
          
          // Not yet populated, or possible not even existant
          ChunkRef notYetPopulated = ChunkRef.get(populated.x + x, populated.z + z);
          // I'm waiting on this yet to be decorated chunk before I'll replace my blocks
          populated.waitingOn.add(notYetPopulated);
          // I want this chunk to tell me once it's decorated
          notYetPopulated.notifyOnPostPopulate.add(populated);
        }
//        Chunk chunk = world.getChunkFromChunkCoords(decorated.x + x, decorated.z + z);
//        if (!chunk.isTerrainPopulated) {
//          //
//          ChunkRef notYetPopulated = ChunkRef.get(chunk);
//          // I'm waiting on this yet to be decorated chunk before I'll replace my blocks
//          decorated.waitingOn.add(notYetPopulated);
//          // I want this chunk to tell me once it's decorated
//          notYetPopulated.notifyOnPostDecorate.add(decorated);
//        }
      }
    }
    replaceBlocksIfNotWaiting(populated);
  }

  private void notify(ChunkRef toNotify, ChunkRef justBeenPopulated) {
    //!!! Moved into the loop to prevent ConcurrentModificationExceptions
    // Clear out the chunks we need to notify when we've finished decorating
    //justBeenDecorated.notifyOnPostDecorate.remove(toNotify);
    // Notify the chunk by telling it that it's not waiting for us any more
    toNotify.waitingOn.remove(justBeenPopulated);
    // Check if that means
    replaceBlocksIfNotWaiting(toNotify);
  }

  private void replaceBlocksIfNotWaiting(ChunkRef ref) {
    // If all the surrounding chunks have been populated
    if (ref.waitingOn.isEmpty()) {
      // We are free to replace the block in this chunk now
      replaceBlocks(ref);
      // This reference is no longer going to be used, so remove it from the map
      ChunkRef.remove(ref);
      //ref.notifyOnPostDecorate should be empty
    }
    //
    /*
     for (ChunkRef toNotify : ref.notifyChunks) {
     toNotify.waitingOn.remove(ref);
     check(toNotify);
     }
     */
  }

  private void replaceBlocks(ChunkRef ref) {
    int par_x = ref.x * 16;
    int par_z = ref.z * 16;
    DimensionManager dimManager = (DimensionManager) UBAPIHook.ubAPIHook.ubSetProviderRegistry;
    WorldGenManager worldGen = dimManager.worldGenManager(0);
    //UBStoneCodes fillerCodes = worldGen.getUndergroundBiomeGenAt(event.chunkX, event.chunkZ).fillerBlockCodes;
    int generationHeight = 255; //UndergroundBiomes.generateHeight();
    BiomeGenUndergroundBase[] undergroundBiomesForGeneration = new BiomeGenUndergroundBase[256];
    undergroundBiomesForGeneration = worldGen.loadUndergroundBlockGeneratorData(undergroundBiomesForGeneration, par_x, par_z, 16, 16);
    for (int x = par_x; x < par_x + 16; x++) {
      for (int z = par_z; z < par_z + 16; z++) {
        BiomeGenUndergroundBase currentBiome = undergroundBiomesForGeneration[(x - par_x) + (z - par_z) * 16];
        int variation = (int) (currentBiome.strataNoise.noise(x / 55.533, z / 55.533, 3, 1, 0.5) * 10 - 5);
        UBStoneCodes defaultColumnStone = currentBiome.fillerBlockCodes;
        for (int y = 1; y < generationHeight; y++) {
          Block currentBlock = world.getBlock(x, y, z);
          IBlockChanger blockChanger = UndergroundBiomesConstructs.blockReplaceMethodLookup.get(currentBlock);
          int metadata;
          if (blockChanger != null) {
            UBStoneCodes strata = currentBiome.getStrataBlockAtLayer(y + variation);
            if (strata.name == NamedVanillaBlock.stone || strata.name == NamedVanillaBlock.sand || strata.name == NamedVanillaBlock.sandstone) {
              // Conversions for vanilla stone are not in my maps, which would cause NPEs, really though, we don't need to change anything if this is the case
              continue;
            }
            try {
              blockChanger.changeBlock(strata, world, x, y, z);
            } catch (NullPointerException npe) {
              Misc.log("NPE! Strata is " + strata.name.internal() + ", Block is " + currentBlock.getUnlocalizedName());
              throw npe;
            }
          } else if (UndergroundBiomesConstructs.oreUBifier.replaces(currentBlock, metadata = world.getBlockMetadata(x, y, z))) {
            UBStoneCodes baseStrata = currentBiome.getStrataBlockAtLayer(y + variation);
            BlockState replacement = UndergroundBiomesConstructs.oreUBifier.replacement(currentBlock, metadata, baseStrata, defaultColumnStone);
            world.setBlock(x, y, z, replacement.block, replacement.metadata, 2);
          }
          //          if (NamedVanillaBlock.stone.matches(currentBlock)) {
          //            UBStoneCodes strata = currentBiome.getStrataBlockAtLayer(y + variation);
          //            currentWorld.setBlock(x, y, z, strata.block, strata.metadata, 2);
          //          }
          //          // skip if air;
          //          else if (Block.isEqualTo(Blocks.air, currentBlock)) {
          //          }
          //          else if (Block.isEqualTo(Blocks.water, currentBlock)) {
          //          }
          //          else if (Block.isEqualTo(currentBlock, UndergroundBiomes.igneousStone)) {
          //          }
          //          else if (Block.isEqualTo(currentBlock, UndergroundBiomes.metamorphicStone)) {
          //          }
          //          else if (Block.isEqualTo(Blocks.cobblestone, currentBlock)) {
          //            //
          //          }
          //                    // only replace cobble if in config
          //          // currently off: unacceptable results
          //                    /*
          //           if (!(UndergroundBiomes.replaceCobblestone)) continue;
          //           if (currentBlockID == Block.cobblestone.blockID) {
          //           BlockCodes strataCobble =
          //           currentBiome.fillerBlockCodes.onDrop;
          //           currentWorld.setBlock(x, y, z, strataCobble.blockID, strataCobble.metadata, 0x02);
          //           }*/
        }
      }
    }
  }

}
