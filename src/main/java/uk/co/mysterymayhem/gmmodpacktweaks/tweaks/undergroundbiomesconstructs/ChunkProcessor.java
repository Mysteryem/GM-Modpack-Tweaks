package uk.co.mysterymayhem.gmmodpacktweaks.tweaks.undergroundbiomesconstructs;

import Zeno410Utils.BlockState;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import exterminatorJeff.undergroundBiomes.api.BiomeGenUndergroundBase;
import exterminatorJeff.undergroundBiomes.api.NamedVanillaBlock;
import exterminatorJeff.undergroundBiomes.api.UBAPIHook;
import exterminatorJeff.undergroundBiomes.api.UBStoneCodes;
import exterminatorJeff.undergroundBiomes.common.DimensionManager;
import exterminatorJeff.undergroundBiomes.common.WorldGenManager;
import java.util.ArrayDeque;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import net.minecraft.block.Block;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.gen.ChunkProviderServer;
import org.apache.commons.lang3.exception.ExceptionUtils;
import uk.co.mysterymayhem.gmmodpacktweaks.util.Log;

/**
 * The heart of the Underground Biomes Constructs tweak. Responsible for noting the requests to replace the blocks in specified chunks with UBC versions, performing the replacements when possible.
 * @author Mysteryem
 */
public class ChunkProcessor {

  // Map containing all the ChunkProcessors by World
  private static final HashMap<World, ChunkProcessor> processors = new HashMap<>();
  private final ArrayDeque<ChunkRef> tryReplaceNextTickList = new ArrayDeque<>();

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
      FMLCommonHandler.instance().bus().register(get);
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
    ChunkProcessor get = processors.remove(world);
    FMLCommonHandler.instance().bus().unregister(get);
    return get;
  }
  
  /**
   * Check if a chunk exists at all. This will first check if the chunk is currently
   * loaded and then check if it exists on the disk in the region file it would be
   * part of.
   * @param world the world we're checking
   * @param chunkX x position of the chunk in chunk coordinates
   * @param chunkZ z position of the chunk in chunk coordinates
   * @return true if the chunk exists
   */
  public static boolean chunkExists(World world, int chunkX, int chunkZ) {
    IChunkProvider provider = world.getChunkProvider();
        
    // Check if chunk is loaded
    boolean chunkExists = chunkIsLoaded(world, chunkX, chunkZ);

    // If not loaded, check if it physically exists in 
    if (!chunkExists && provider instanceof ChunkProviderServer) {
      ChunkProviderServer cps = (ChunkProviderServer)provider;
      IChunkLoader loader = cps.currentChunkLoader;
      if (loader instanceof AnvilChunkLoader) {
        chunkExists = ((AnvilChunkLoader)loader).chunkExists(world, chunkX, chunkZ);
      }
    }
    return chunkExists;
  }
  
  /**
   * Check if a chunk is currently loaded
   * @param world
   * @param chunkX
   * @param chunkZ
   * @return 
   */
  public static boolean chunkIsLoaded(World world, int chunkX, int chunkZ) {
    // World::getChunkProvider will always give a ChunkProviderServer or a ChunkProviderClient[unsure on class' name]
    // as opposed to a ChunkProviderGenerate which always returns true for this method
    return world.getChunkProvider().chunkExists(chunkX, chunkZ);
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

    
    /* Possible to cause a ConcurrentModificationException due to
    	java.util.ConcurrentModificationException
        at java.util.HashMap$HashIterator.nextNode(Unknown Source)
        at java.util.HashMap$KeyIterator.next(Unknown Source)
        at uk.co.mysterymayhem.gmmodpacktweaks.tweaks.undergroundbiomesconstructs.ChunkProcessor.replaceBlocksIfNotWaiting(ChunkProcessor.java:228)
        at uk.co.mysterymayhem.gmmodpacktweaks.tweaks.undergroundbiomesconstructs.ChunkProcessor.onWorldTick(ChunkProcessor.java:351)
    */
    synchronized (fullyGenned.notifyOnPostPopulate) {
      // Go through all the chunks that need to be notified when this chunk is done generating
      Iterator<ChunkCoordIntPair> iterator = fullyGenned.notifyOnPostPopulate.iterator();
      Log.debug("pMOG synchronized " + fullyGenned);
      while (iterator.hasNext()) {
        ChunkCoordIntPair toNotifyPair = iterator.next();
        ChunkRef toNotify = ChunkRef.get(toNotifyPair);
        // After notifying a particular chunk that we've been populated, we can forget all about needing to notify that chunk
        iterator.remove();
        // Notify the chunk from our notify list that we've been populated
        notify(toNotify, fullyGenned);
      }
    }

    // We want to see if the surrounding chunks have been populated, if so, we replace the blocks in this chunk
    for (int xOffset = -1; xOffset <= 1; xOffset++) {
      for (int zOffset = -1; zOffset <= 1; zOffset++) {
        if (xOffset == 0 && zOffset == 0) {
          continue;
        }
        
        int nearbyChunkX = fullyGenned.x + xOffset;
        int nearbyChunkZ = fullyGenned.z + zOffset;
        
        
        // Check if the chunk exists, if it does and the chunk is not decorated or if it doesn't exist, we must wait for it to become decorated
        // The order is important here, as checking if it's decorated will attempt to create the chunk if it doesn't exist, leading to an infinite loop
        if (!chunkExists(world, nearbyChunkX, nearbyChunkZ)
                || !world.getChunkFromChunkCoords(nearbyChunkX, nearbyChunkZ).isTerrainPopulated 
                || ChunkRef.isCurrentlyPopulating(nearbyChunkX, nearbyChunkZ)) {

          // Not yet populated, or possible not even existant
          ChunkRef notYetPopulated = ChunkRef.get(nearbyChunkX, nearbyChunkZ);

          // I'm waiting on this yet to be decorated chunk before I'll replace my blocks
          fullyGenned.waitingOn.add(notYetPopulated.toIntPair());
          // I want this chunk to tell me once it's decorated
          notYetPopulated.notifyOnPostPopulate.add(fullyGenned.toIntPair());

          Log.debug(fullyGenned + " is waiting on " + notYetPopulated);
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
    Log.debug(justBeenPopulated + " is notifying the loaded chunk " + toNotify);
    Log.debug(toNotify + " wait list before: " + toNotify.waitingOn.toString());
    
    if (!toNotify.waitingOn.remove(justBeenPopulated.toIntPair())) {
      toNotify.removeFromWaitingOnOnLoadAdd(justBeenPopulated.toIntPair());
    }
    Log.debug(toNotify + " wait list after: " + toNotify.waitingOn.toString());
    
    replaceBlocksIfNotWaiting(toNotify);
//    // If chunk being notified is loaded
//    if (this.world.getChunkProvider().chunkExists(toNotify.x, toNotify.z)) {
//      
//      Log.debug(justBeenPopulated + " is notifying the loaded chunk " + toNotify);
//      Log.debug(toNotify + " wait list before: " + toNotify.waitingOn.toString());
//      
//      // Notify the chunk by telling it that it's not waiting for us any more
//      toNotify.waitingOn.remove(justBeenPopulated.toIntPair());
//
//      Log.debug(toNotify + " wait list after: " + toNotify.waitingOn.toString());
//
//      // Check if that means it can have its blocks replaced now and do so if it can
//      replaceBlocksIfNotWaiting(toNotify);
//    }
//    // It's not loaded, so the notification needs to happen when it gets loaded
//    // ADD it to the list instead
//    else {
//      Log.debug(justBeenPopulated + " is notifying the UNloaded chunk " + toNotify);
//      Log.debug(toNotify + " wait list before: " + toNotify.waitingOn.toString());
//      toNotify.waitingOn.add(justBeenPopulated.toIntPair());
//      Log.debug(toNotify + " wait list after: " + toNotify.waitingOn.toString());
//      
//      // The chunk's not loaded, so no need to call replaceBlocksIfNotWaiting(toNotify)
//      // It will get called when it loads if all the chunks it was waiting on
//      // generated while it was unloaded
//    }
  }

  /**
   * Replace the blocks in a chunk with their UBC versions if the chunk is not
   * waiting on any other chunks to have their blocks replaced.
   *
   * @param ref
   */
  void replaceBlocksIfNotWaiting(ChunkRef ref) {
    this.tryReplaceNextTickList.remove(ref);
    // If all the surrounding chunks have been populated
    if (ref.waitingOn.isEmpty()) {
      //TODO: Fix the problem in postModOreGen when a chunk can have isTerrainPopulated = false, even when that chunk has been passed to postModOreGen beforehand. isTerrainPopulated sometimes set after postModOreGen? Need to investigate. 
      //TODO: NOPE, isTerrainPopulated is set to true _before_ vanilla population. Mod population then happens after that, with postModOreGen occuring as the last thing during mod population. I really don't know what's causing this, but it is fortunately recoverable. The more important question is whether or not the whole process can produce cycles (chunk a waiting on chunk b, which is waiting on chunk a) or forgotten chunks (chunk c has had its ores populated, is waiting on no chunks, but has not
      Log.error("late entries are in notify list of " + ref.toFullString());
      /* Possible ConcurrentModificationException due to
        java.util.ConcurrentModificationException: Locked
          at uk.co.mysterymayhem.gmmodpacktweaks.tweaks.undergroundbiomesconstructs.LockableSet.checkLock(LockableSet.java:60)
          at uk.co.mysterymayhem.gmmodpacktweaks.tweaks.undergroundbiomesconstructs.LockableSet$LockableSetIterator.remove(LockableSet.java:37)
          at uk.co.mysterymayhem.gmmodpacktweaks.tweaks.undergroundbiomesconstructs.ChunkProcessor.postModOreGen(ChunkProcessor.java:133)
          at uk.co.mysterymayhem.gmmodpacktweaks.tweaks.undergroundbiomesconstructs.UndergroundBiomesConstructs.postTerrainGenerate(UndergroundBiomesConstructs.java:451)
      */
      synchronized (ref.notifyOnPostPopulate) {
        Log.debug("rBINW synchronized " + ref);
        Iterator<ChunkCoordIntPair> iterator = ref.notifyOnPostPopulate.iterator();
        while (iterator.hasNext()) {
          ChunkCoordIntPair brokenNotifyPair = iterator.next();
          ChunkRef brokenNotify = ChunkRef.get(brokenNotifyPair);
          Log.error("Somehow " + brokenNotify + " made it into " + ref + "'s notify list late");
          iterator.remove();
          notify(brokenNotify, ref);
        }
      }
      
      //DEBUG
      //Log.log("Replacing blocks for " + ref);
      
      // We are free to replace the block in this chunk now
      replaceBlocks(ref);
      // This reference is no longer going to be used, so remove it from the map
      ChunkRef.remove(ref);
      
      Log.debug("Removed " + ref + " from tracked references");
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
    //DEBUG
    Log.debug("Replacing blocks for (" + chunkX + ", " + chunkZ + ")");
    ChunkRef ref = ChunkRef.get(chunkX, chunkZ);
    if (ref.replacementComplete) {
      Log.error("Already replaced blocks! Trace: " + ExceptionUtils.getStackTrace(new Throwable()));
      return;
    }
    ref.replacementComplete = true;
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
    Log.debug("Replaced blocks for (" + chunkX + ", " + chunkZ + ")");
  }
  
  @SubscribeEvent
  public void onWorldTick(TickEvent.WorldTickEvent event) {
    if (event.side.isServer()) {
      // Does start vs end matter?
      if (event.phase == TickEvent.Phase.START) {
        Log.debug("Starting delayed replacement for " + this.tryReplaceNextTickList.toString());
        while (!this.tryReplaceNextTickList.isEmpty()) {
          ChunkRef removeFirst = this.tryReplaceNextTickList.removeFirst();
          Log.debug("Delayed replacement if not waiting for " + removeFirst);
          this.replaceBlocksIfNotWaiting(removeFirst);
        }
        Log.debug("Finished delayed replacement");
      }
    }
  }
  
  void replaceBlocksIfNotWaitingNextTick(ChunkRef ref) {
    Log.debug("Delaying replacement if not waiting for " + ref);
    this.tryReplaceNextTickList.addLast(ref);
  }

}
