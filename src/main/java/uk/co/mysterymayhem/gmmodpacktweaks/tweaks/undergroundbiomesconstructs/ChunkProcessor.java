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

/**
 * The heart of the Underground Biomes Constructs tweak. Responsible for noting the requests to replace the blocks in specified chunks with UBC versions, performing the replacements when possible.
 * @author Mysteryem
 */
class ChunkProcessor {

  // Map containing all the ChunkProcessors by World
  private static final HashMap<World, ChunkProcessor> processors = new HashMap<>();

  /**
   * Gets a ChunkProcessor from an internal map, creating a new one if one
   * doesn't exist.
   *
   * @param world the world the ChunkProcessor is for.
   * @return the ChunkProcessor for the specified world.
   */
  static ChunkProcessor get(World world) {
    ChunkProcessor get = processors.get(world);
    if (get == null) {
      get = new ChunkProcessor(world);
      processors.put(world, get);
    }
    return get;
  }

  /**
   * Remove a specified ChunkProcessor, used for cleanup so that we can't have
   * references to World objects floating around
   *
   * @param world the world the ChunkProcessor is for.
   * @return the ChunkProcessor removed, or null if it doesn't exist.
   */
  static ChunkProcessor remove(World world) {
    return processors.remove(world);
  }

  // World reference stored for easy access
  private final World world;

  /**
   * Constructs a new ChunkProcessor. Only one ChunkProcessor should exist per
   * world so this is private. All new instances are created via the static
   * get(World world) method.
   *
   * @param world
   */
  private ChunkProcessor(World world) {
    this.world = world;
  }

  /**
   * Call when a chunk has finished populating and/or decorating. This should
   * usually be called as late as possible.
   *
   * @param fullyGenned
   */
  void postModOreGen(ChunkRef fullyGenned) {

    // Go through all the chunks that need to be notified when this chunk is done generating
    Iterator<ChunkRef> iterator = fullyGenned.notifyOnPostPopulate.iterator();
    while (iterator.hasNext()) {
      ChunkRef toNotify = iterator.next();
      // After notifying a particular chunk that we've been populated, we can forget all about needing to notify that chunk
      iterator.remove();
      // Notify the chunk from our notify list that we've been populated
      notify(toNotify, fullyGenned);
    }

    // We want to see if the surrounding chunks have been populated, if so, we replace the blocks in this chunk
    for (int xOffset = -1; xOffset <= 1; xOffset++) {
      for (int zOffset = -1; zOffset <= 1; zOffset++) {
        if (xOffset == 0 && zOffset == 0) {
          continue;
        }
        // Check if the chunk exists, if it does and the chunk is not decorated or if it doesn't exist, we must wait for it to become decorated
        // The order is important here, as checking if it's decorated will attempt to create the chunk if it doesn't exist, leading to an infinite loop
        if (!world.getChunkProvider().chunkExists(fullyGenned.x + xOffset, fullyGenned.z + zOffset) || !world.getChunkFromChunkCoords(fullyGenned.x + xOffset, fullyGenned.z + zOffset).isTerrainPopulated) {

          // Not yet populated, or possible not even existant
          ChunkRef notYetPopulated = ChunkRef.get(fullyGenned.x + xOffset, fullyGenned.z + zOffset);

          // I'm waiting on this yet to be decorated chunk before I'll replace my blocks
          fullyGenned.waitingOn.add(notYetPopulated);
          // I want this chunk to tell me once it's decorated
          notYetPopulated.notifyOnPostPopulate.add(fullyGenned);

          //DEBUG
          //log("Chunk at (" + fullyGenned.x + ", " + fullyGenned.z + ") is waiting for chunk at (" + (fullyGenned.x + x) + ", " + (fullyGenned.z + z) + ") to populate too");
        }
      }
    }
    // Possibly perform replacement in this chunk, depending on whether this chunk is waiting on any others by the time we reach this step
    replaceBlocksIfNotWaiting(fullyGenned);
  }

  /**
   * Notify a chunk that another has been populated.
   *
   * @param toNotify the chunk to notify
   * @param justBeenPopulated the chunk that has been populated
   */
  private void notify(ChunkRef toNotify, ChunkRef justBeenPopulated) {
    // Notify the chunk by telling it that it's not waiting for us any more
    toNotify.waitingOn.remove(justBeenPopulated);
    // Check if that means it can have its blocks replaced now and do so if it can
    replaceBlocksIfNotWaiting(toNotify);
  }

  /**
   * Replace the blocks in a chunk with their UBC versions if the chunk is not
   * waiting on any other chunks to have their blocks replaced.
   *
   * @param ref
   */
  private void replaceBlocksIfNotWaiting(ChunkRef ref) {
    // If all the surrounding chunks have been populated
    if (ref.waitingOn.isEmpty()) {
      //log("Replacing blocks for chunk at (" + ref.x + ", " + ref.z + ")");
      // We are free to replace the block in this chunk now
      replaceBlocks(ref);
      // This reference is no longer going to be used, so remove it from the map
      ChunkRef.remove(ref);
      //ref.notifyOnPostDecorate should be empty
    }
  }

  /**
   * Replace the blocks in a chunk with their UBC versions.
   *
   * @param chunkRef
   */
  private void replaceBlocks(ChunkRef chunkRef) {
    replaceBlocks(chunkRef.x, chunkRef.z);
  }

  /**
   * Replace the blocks in a chunk with their UBC versions. Largely copied, with
   * changes, from Underground Biomes Constructs.
   *
   * @param chunkX
   * @param chunkZ
   */
  private void replaceBlocks(int chunkX, int chunkZ) {

    // Get world coordinates for (0,0) within the specified chunk
    int par_x = chunkX * 16;
    int par_z = chunkZ * 16;

    // Sneaky way to get the DimensionManager
    DimensionManager dimManager = (DimensionManager) UBAPIHook.ubAPIHook.ubSetProviderRegistry;
    // and the WorldGenManager for this world
    WorldGenManager worldGen = dimManager.worldGenManager(0);

    // The maximum y value for block replacement
    int generationHeight = 255; //UndergroundBiomes.generateHeight();

    // Load UBC biome data
    BiomeGenUndergroundBase[] undergroundBiomesForGeneration = new BiomeGenUndergroundBase[256];
    undergroundBiomesForGeneration = worldGen.loadUndergroundBlockGeneratorData(undergroundBiomesForGeneration, par_x, par_z, 16, 16);

    // Iterate through the blocks in this chunk
    for (int x = par_x; x < par_x + 16; x++) {
      for (int z = par_z; z < par_z + 16; z++) {

        // Get the current UBC biome
        BiomeGenUndergroundBase currentBiome = undergroundBiomesForGeneration[(x - par_x) + (z - par_z) * 16];
        // Get the variation?
        int variation = (int) (currentBiome.strataNoise.noise(x / 55.533, z / 55.533, 3, 1, 0.5) * 10 - 5);
        // Get default stone for this column, not sure when this gets used in the UBC side of things
        UBStoneCodes defaultColumnStone = currentBiome.fillerBlockCodes;

        // Continue the process of iterating through all blocks in the chunk
        for (int y = 1; y < generationHeight; y++) {
          // Get the block here
          Block currentBlock = world.getBlock(x, y, z);

          // Get the method that will be used to decide the replacement block
          // While a big long list of if statements or a big switch statement on currentBlock.'something that returns a unique String' could be used, I used this as an oportunity to learn more about Java 8's lambda expressions. I have not checked how performance compares between the options.
          IBlockChanger blockChanger = UndergroundBiomesConstructs.blockReplaceMethodLookup.get(currentBlock);

          // If there is a IBlockChanger for the found block
          if (blockChanger != null) {
            // Get the UBC strata (this is the type of stone) for this block's replacement.
            UBStoneCodes strata = currentBiome.getStrataBlockAtLayer(y + variation);

            if (strata.name == NamedVanillaBlock.stone || strata.name == NamedVanillaBlock.sand || strata.name == NamedVanillaBlock.sandstone) {
              // We don't need to change vanilla blocks into UBC versions if we're trying to turn them into vanilla blocks.
              // Also, conversions for vanilla strata are not in my maps, which would cause the NPEs specified below.
              continue;
            }

            // Change the block according to the retrieved IBlockChanger
            // Note that if the blockChanger cannot handle the specified strata, an NPE will get thrown here
            blockChanger.changeBlock(strata, world, x, y, z);
          } // If there is no IBlockChanger for the found block
          else {
            // Get the metadata of the found block
            int metadata = world.getBlockMetadata(x, y, z);

            // If the UBC OreUBifier should replace the found block
            if (UndergroundBiomesConstructs.oreUBifier.replaces(currentBlock, metadata)) {
              // Get the strata for the replacement
              UBStoneCodes baseStrata = currentBiome.getStrataBlockAtLayer(y + variation);
              // Get the replacement block(state)
              BlockState replacement = UndergroundBiomesConstructs.oreUBifier.replacement(currentBlock, metadata, baseStrata, defaultColumnStone);
              // Set the replacement block in the world, replacing the found block
              world.setBlock(x, y, z, replacement.block, replacement.metadata, 2);
            }
          }
        }
      }
    }
  }

}
