/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.mysterymayhem.gmmodpacktweaks.tweaks.undergroundbiomesconstructs;

import Zeno410Utils.BlockState;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import exterminatorJeff.undergroundBiomes.api.BiomeGenUndergroundBase;
import exterminatorJeff.undergroundBiomes.api.NamedBlock;
import exterminatorJeff.undergroundBiomes.api.NamedVanillaBlock;
import exterminatorJeff.undergroundBiomes.api.UBAPIHook;
import exterminatorJeff.undergroundBiomes.api.UBIDs;
import exterminatorJeff.undergroundBiomes.api.UBStoneCodes;
import exterminatorJeff.undergroundBiomes.common.DimensionManager;
import exterminatorJeff.undergroundBiomes.worldGen.OreUBifier;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.event.terraingen.PopulateChunkEvent.Post;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import exterminatorJeff.undergroundBiomes.common.UndergroundBiomes;
import exterminatorJeff.undergroundBiomes.common.WorldGenManager;
import exterminatorJeff.undergroundBiomes.constructs.entity.UndergroundBiomesTileEntity;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.MinecraftForge;
import uk.co.mysterymayhem.gmmodpacktweaks.tweaks.Tweak;
//import net.minecraftforge.event.terraingen.
import uk.co.mysterymayhem.gmmodpacktweaks.util.OreDict;
import static uk.co.mysterymayhem.gmmodpacktweaks.util.Misc.log;

/**
 *
 * @author Thomas
 */
public class UndergroundBiomesConstructs extends Tweak {

  //private int processors = Runtime.getRuntime().availableProcessors();
  static OreUBifier oreUBifier;
//  private final HashMap<Integer, Function<UBStoneCodes, BlockCodes>> blockIDToMethod = new HashMap<>();
  final static HashMap<Block, IBlockChanger> blockReplaceMethodLookup = new HashMap<>();

  private static final HashMap<NamedBlock, NamedBlock> slabMap = new HashMap<>();
  private static final HashMap<NamedBlock, NamedBlock> slabFullMap = new HashMap<>();
  private static final HashMap<NamedBlock, NamedBlock> brickMap = new HashMap<>();
  private static final HashMap<NamedBlock, NamedBlock> cobbleMap = new HashMap<>();
  private static final HashMap<NamedBlock, Block> monsterEggMap = new HashMap<>();

  private static final HashMap<NamedBlock, Integer> nbtIndexMap = new HashMap<>();

  static {
    brickMap.put(UBIDs.igneousStoneName, UBIDs.igneousStoneBrickName);
    brickMap.put(UBIDs.metamorphicStoneName, UBIDs.metamorphicStoneBrickName);
    brickMap.put(UBIDs.sedimentaryStoneName, UBIDs.sedimentaryStoneName);
//    brickMap.put(NamedVanillaBlock.stone, NamedVanillaBlock.stoneBrick);

    cobbleMap.put(UBIDs.igneousStoneName, UBIDs.igneousCobblestoneName);
    cobbleMap.put(UBIDs.metamorphicStoneName, UBIDs.metamorphicCobblestoneName);
    cobbleMap.put(UBIDs.sedimentaryStoneName, UBIDs.sedimentaryStoneName);
//    cobbleMap.put(NamedVanillaBlock.stone, NamedVanillaBlock.cobblestone);

    slabMap.put(UBIDs.igneousStoneName, UBIDs.igneousStoneSlabName.half);
    slabMap.put(UBIDs.igneousCobblestoneName, UBIDs.igneousCobblestoneSlabName.half);
    slabMap.put(UBIDs.igneousStoneBrickName, UBIDs.igneousBrickSlabName.half);
    slabMap.put(UBIDs.metamorphicStoneName, UBIDs.metamorphicStoneSlabName.half);
    slabMap.put(UBIDs.metamorphicCobblestoneName, UBIDs.metamorphicCobblestoneSlabName.half);
    slabMap.put(UBIDs.metamorphicStoneBrickName, UBIDs.metamorphicBrickSlabName.half);
    slabMap.put(UBIDs.sedimentaryStoneName, UBIDs.sedimentaryStoneSlabName.half);
//    slabMap.put(NamedVanillaBlock.cobblestone, NamedVanillaBlock.stoneSingleSlab);
//    slabMap.put(NamedVanillaBlock.stone, NamedVanillaBlock.stoneSingleSlab);
//    slabMap.put(NamedVanillaBlock.stone, NamedVanillaBlock.stoneSingleSlab);

    slabFullMap.put(UBIDs.igneousStoneName, UBIDs.igneousStoneSlabName.full);
    slabFullMap.put(UBIDs.igneousCobblestoneName, UBIDs.igneousCobblestoneSlabName.full);
    slabFullMap.put(UBIDs.igneousStoneBrickName, UBIDs.igneousBrickSlabName.full);
    slabFullMap.put(UBIDs.metamorphicStoneName, UBIDs.metamorphicStoneSlabName.full);
    slabFullMap.put(UBIDs.metamorphicCobblestoneName, UBIDs.metamorphicCobblestoneSlabName.full);
    slabFullMap.put(UBIDs.metamorphicStoneBrickName, UBIDs.metamorphicBrickSlabName.full);
    slabFullMap.put(UBIDs.sedimentaryStoneName, UBIDs.sedimentaryStoneSlabName.full);

    // Marks the starting nbt IDs for the correspondign tile entities, simply add the metadata of the UB stone to the found value
    nbtIndexMap.put(UBIDs.metamorphicStoneName, 0);
    nbtIndexMap.put(UBIDs.metamorphicCobblestoneName, 8);
    nbtIndexMap.put(UBIDs.metamorphicStoneBrickName, 16);
    nbtIndexMap.put(UBIDs.igneousStoneName, 24);
    nbtIndexMap.put(UBIDs.igneousCobblestoneName, 32);
    nbtIndexMap.put(UBIDs.igneousStoneBrickName, 40);
    nbtIndexMap.put(UBIDs.sedimentaryStoneName, 48);
  }

  public UndergroundBiomesConstructs() {
    this.MODID = "UndergroundBiomes";

  }

  @Override
  public void postInit(FMLPostInitializationEvent event) {
    try {
      if (!this.isModLoaded()) {
        return;
      }
      MinecraftForge.EVENT_BUS.register(this);
      String[] dictNames = new String[]{"Redstone", "Coal", "Diamond", "Lapis", "Emerald", "Gold", "Iron"};
      String[] blockPrefixes = new String[]{"igneous_ore", "metamorphic_ore", "sedimentary_ore"};
      int maxMeta = 7;

      for (String dictName : dictNames) {
        for (String blockPrefix : blockPrefixes) {
          for (int meta = 0; meta <= maxMeta; meta++) {
            OreDict.register(OreDict.DictType.ORE, dictName, MODID, blockPrefix + dictName, meta);
          }
        }
      }
      // Grab ourselves the oreUBifier isntance from UndergroundBiomes
      Field field = UndergroundBiomes.class.getDeclaredField("oreUBifier");
      field.setAccessible(true);
      oreUBifier = (OreUBifier) field.get(UndergroundBiomes.instance());
      // As the config option for replacing ores is off (should be off...), sneakily force it on in the OreUBifier, hopefully, anything else usign the value from the config will consider it to be off
      field = OreUBifier.class.getDeclaredField("replacementActive");
      field.setAccessible(true);
      field.setBoolean(oreUBifier, true);

//      blockIDToMethod.put(Block.getIdFromBlock(GameRegistry.findBlock("minecraft", "stone")), (UBStoneCodes t) -> {
//        return t;
//      });
//      blockIDToMethod.put(Block.getIdFromBlock(GameRegistry.findBlock("minecraft", "stonebrick")), (UBStoneCodes s) -> s.brickVersionEquivalent());
//      blockIDToMethod.put(Block.getIdFromBlock(GameRegistry.findBlock("minecraft", "stone_slab")), (UBStoneCodes s) -> s.slabVersionEquivalent());
      blockReplaceMethodLookup.put(Blocks.stone, (UBStoneCodes strata, World world, int x, int y, int z) -> {
        world.setBlock(x, y, z, strata.block, strata.metadata, 2);
      });

      blockReplaceMethodLookup.put(Blocks.cobblestone, (UBStoneCodes strata, World world, int x, int y, int z) -> {
        world.setBlock(x, y, z, cobbleMap.get(strata.name).block, strata.metadata, 2);
      });

      blockReplaceMethodLookup.put(Blocks.cobblestone_wall, (UBStoneCodes strata, World world, int x, int y, int z) -> {
        int index = nbtIndexMap.get(cobbleMap.get(strata.name));

        world.setBlock(x, y, z, UBIDs.UBWallsName.block, 0, 2);

        UndergroundBiomesTileEntity tEnt = new UndergroundBiomesTileEntity();
        tEnt.setWorldObj(world);
        tEnt.setMasterIndex(index + strata.metadata);
        world.addTileEntity(tEnt);
        world.setTileEntity(x, y, z, tEnt);
      });

      blockReplaceMethodLookup.put(Blocks.stone_brick_stairs, (UBStoneCodes strata, World world, int x, int y, int z) -> {
        int index = nbtIndexMap.get(brickMap.get(strata.name));
        int metaData = world.getBlockMetadata(x, y, z);

        world.setBlock(x, y, z, UBIDs.UBStairsName.block, metaData, 2);

        UndergroundBiomesTileEntity tEnt = new UndergroundBiomesTileEntity();
        tEnt.setWorldObj(world);
        tEnt.setMasterIndex(index + strata.metadata);
        world.addTileEntity(tEnt);
        world.setTileEntity(x, y, z, tEnt);
      });

      blockReplaceMethodLookup.put(Blocks.stone_button, (UBStoneCodes strata, World world, int x, int y, int z) -> {
        int index = nbtIndexMap.get(strata.name);
        int metaData = world.getBlockMetadata(x, y, z);

        world.setBlock(x, y, z, UBIDs.UBButtonName.block, metaData, 2);

        UndergroundBiomesTileEntity tEnt = new UndergroundBiomesTileEntity();
        tEnt.setWorldObj(world);
        tEnt.setMasterIndex(index + strata.metadata);
        world.addTileEntity(tEnt);
        world.setTileEntity(x, y, z, tEnt);
      });

      //Not yet implemented
      //blockReplaceMethodLookup.put(Blocks.stone_pressure_plate, null);
      blockReplaceMethodLookup.put(Blocks.stone_slab, (UBStoneCodes strata, World world, int x, int y, int z) -> {
        //int
        switch (world.getBlockMetadata(x, y, z)) {
          // Smooth stone
          case 0:
            world.setBlock(x, y, z, slabMap.get(strata.name).block(), strata.metadata, 2);
            break;
          // Cobblestone
          case 3:
            world.setBlock(x, y, z, slabMap.get(cobbleMap.get(strata.name)).block(), strata.metadata, 2);
            break;
          // Stone Brick
          case 5:
            world.setBlock(x, y, z, slabMap.get(brickMap.get(strata.name)).block(), strata.metadata, 2);
            break;
          // Top Smooth Stone
          case 8:
            world.setBlock(x, y, z, slabMap.get(strata.name).block(), strata.metadata + 8, 2);
            break;
          // Top Cobble
          case 11:
            world.setBlock(x, y, z, slabMap.get(cobbleMap.get(strata.name)).block(), strata.metadata + 8, 2);
            break;
          // Top Stone Bricks
          case 13:
            world.setBlock(x, y, z, slabMap.get(brickMap.get(strata.name)).block(), strata.metadata + 8, 2);
            break;
          default:
        }
        //world.setBlock(x, y, z, strata.slabVersionEquivalent().block, strata.slabVersionEquivalent().metadata, 2);
      });

      // Double stone slabs
      blockReplaceMethodLookup.put(Blocks.double_stone_slab, (UBStoneCodes strata, World world, int x, int y, int z) -> {
        switch (world.getBlockMetadata(x, y, z)) {
          // Smooth stone
          case 0:
            world.setBlock(x, y, z, slabFullMap.get(strata.name).block(), strata.metadata, 2);
            break;
          // Cobblestone
          case 3:
            world.setBlock(x, y, z, slabFullMap.get(cobbleMap.get(strata.name)).block(), strata.metadata, 2);
            break;
          // Stone Brick
          case 5:
            world.setBlock(x, y, z, slabFullMap.get(brickMap.get(strata.name)).block(), strata.metadata, 2);
            break;
          default:
        }
      });

      // Cobblestone stairs
      blockReplaceMethodLookup.put(Blocks.stone_stairs, (UBStoneCodes strata, World world, int x, int y, int z) -> {
        NamedBlock cobble = cobbleMap.get(strata.name);
//        if (cobble == null) {
//          log("Unknown cobblestone stairs strata " + strata.name.internal());
//          return;
//        }
        int index = nbtIndexMap.get(cobble);
        int metaData = world.getBlockMetadata(x, y, z);

        world.setBlock(x, y, z, UBIDs.UBStairsName.block, metaData, 2);

        UndergroundBiomesTileEntity tEnt = new UndergroundBiomesTileEntity();
        tEnt.setWorldObj(world);
        tEnt.setMasterIndex(index + strata.metadata);
        world.addTileEntity(tEnt);
        world.setTileEntity(x, y, z, tEnt);
      });

      // Stone bricks
      blockReplaceMethodLookup.put(Blocks.stonebrick, (UBStoneCodes strata, World world, int x, int y, int z) -> {
        world.setBlock(x, y, z, brickMap.get(strata.name).block, strata.metadata, 2);
      });

      blockReplaceMethodLookup.put(Blocks.monster_egg, (UBStoneCodes strata, World world, int x, int y, int z) -> {
        Block get = monsterEggMap.get(strata.name);
//        if (get != null) {
        world.setBlock(x, y, z, get, strata.metadata, 2);
//        }
//        else {
//          uk.co.mysterymayhem.gmmodpacktweaks.util.Misc.log("Unknown monsterEgg strata " + strata.name.internal());
//        }
      });
//    Block findBlock = GameRegistry.findBlock(MODID, "sedimentary_oreCoal");
//    if (findBlock != null){
//      findBlock.setBlockName("UBCOreCoalSedimentary");
//    }
      monsterEggMap.put(UBIDs.igneousStoneName, GameRegistry.findBlock(MODID, "igneous_monsterStoneEgg"));
      monsterEggMap.put(UBIDs.metamorphicStoneName, GameRegistry.findBlock(MODID, "metamorphic_monsterStoneEgg"));
      monsterEggMap.put(UBIDs.sedimentaryStoneName, GameRegistry.findBlock(MODID, "sedimentary_monsterStoneEgg"));

    } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
      uk.co.mysterymayhem.gmmodpacktweaks.util.Misc.log("Failed to get UBC OreUBifier");
    }

  }

  /*// Lowest so we're last if possible
   @SubscribeEvent(priority = EventPriority.LOWEST)
   public void postPopulate(Post event) {
   // We're only messing with the overworld
   if (event.world.provider.dimensionId != 0) {
   return;
   }
    
   DimensionManager dimManager = (DimensionManager)UBAPIHook.ubAPIHook.ubSetProviderRegistry;
   WorldGenManager worldGen = dimManager.worldGenManager(0);
   // Event is not cancellable
   int chunkX = event.chunkX;
   int chunkZ = event.chunkZ;
    
   //TODO Check if this actually works
   final BiomeGenUndergroundBase[] undergroundBiomesForGeneration 
   = worldGen.loadUndergroundBlockGeneratorData(new BiomeGenUndergroundBase[256], chunkX, chunkZ, 16, 16);
    
                
   World world = event.world;
   Chunk chunkFromChunkCoords = world.getChunkFromChunkCoords(chunkX, chunkZ);
   ExtendedBlockStorage[] blockStorageArray = chunkFromChunkCoords.getBlockStorageArray();
   Arrays.stream(blockStorageArray).parallel().filter(t -> t != null).forEach(t -> {
   for (int xPos = 0; xPos < 16; xPos++) {
   for (int zPos = 0; zPos < 16; zPos++) {
   BiomeGenUndergroundBase currentBiome = undergroundBiomesForGeneration[(xPos) + (zPos) * 16];
   int variation = (int) (currentBiome.strataNoise.noise(chunkX/55.533, chunkZ/55.533, 3, 1, 0.5) * 10 - 5);
   UBStoneCodes defaultColumnStone = currentBiome.fillerBlockCodes;
   for (int yPos = 0; yPos < 16; yPos++) {
   int blockID = getBlockID(t, xPos, zPos, yPos);
   OreUBifier.BlockReplacer blockReplacer = this.oreUBifier.blockReplacer(blockID);
   if (blockReplacer != null) {
   int metadata = t.getExtBlockMetadata(xPos, zPos, yPos);
   OreUBifier.BlockStateReplacer blockStateReplacer = blockReplacer.replacer(metadata);
   if (blockStateReplacer != null) {
   UBStoneCodes baseStrata = currentBiome.getStrataBlockAtLayer(yPos +variation);
   BlockState replacement = blockStateReplacer.replacement(baseStrata,defaultColumnStone);
   if (replacement != null) {
   int inLevelY = yPos & 15;
   t.func_150818_a(chunkX, inLevelY, chunkZ, replacement.block);
   t.setExtBlockMetadata(chunkX, inLevelY, chunkZ, replacement.metadata);
   }
   }
   }
   else if (blockID == Blocks.cobblestone) {
   UBStoneCodes baseStrata = currentBiome.getStrataBlockAtLayer(yPos +variation);
   baseStrata.
   }
   }
   }
   }
   });
   //int storageArraysPerThread = blockStorageArray.length / processors;
   //Block blockByExtId = blockStorageArray[0].getBlockByExtId(0, 0, 0);
   }*/
  @SubscribeEvent(priority = EventPriority.LOWEST)
  public void interactWithBlock(PlayerInteractEvent event) {
    try {
      if (event.action.equals(PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) && Items.stick.equals(event.entityPlayer.inventory.getCurrentItem().getItem())) {
        int metadata = event.world.getBlockMetadata(event.x, event.y, event.z);
        Block block = event.world.getBlock(event.x, event.y, event.z);
        event.entityPlayer.addChatComponentMessage(new ChatComponentText("Block is " + block.getUnlocalizedName() + (metadata == 0 ? "" : ":" + metadata)));
        if (oreUBifier.replaces(block, metadata)) {
          event.entityPlayer.addChatComponentMessage(new ChatComponentText("OreUBifier says it's to be replaced"));
          Chunk chunk = event.world.getChunkFromBlockCoords(event.x, event.z);
          int par_x = chunk.xPosition * 16;
          int par_z = chunk.zPosition * 16;
          int x = event.x;
          int z = event.z;

          DimensionManager dimManager = (DimensionManager) UBAPIHook.ubAPIHook.ubSetProviderRegistry;
          WorldGenManager worldGen = dimManager.worldGenManager(0);
          BiomeGenUndergroundBase[] undergroundBiomesForGeneration = new BiomeGenUndergroundBase[256];
          undergroundBiomesForGeneration = worldGen.loadUndergroundBlockGeneratorData(undergroundBiomesForGeneration, par_x, par_z, 16, 16);
          BiomeGenUndergroundBase currentBiome = undergroundBiomesForGeneration[(x - par_x) + (z - par_z) * 16];
          int variation = (int) (currentBiome.strataNoise.noise(x / 55.533, z / 55.533, 3, 1, 0.5) * 10 - 5);
          UBStoneCodes defaultColumnStone = currentBiome.fillerBlockCodes;
          UBStoneCodes baseStrata = currentBiome.getStrataBlockAtLayer(event.y + variation);
          BlockState replacement = oreUBifier.replacement(block, metadata, baseStrata, defaultColumnStone);
          event.entityPlayer.addChatComponentMessage(new ChatComponentText("Replacement is " + replacement.block.getUnlocalizedName() + (replacement.metadata == 0 ? "" : ":" + replacement.metadata)));
        } else {
          event.entityPlayer.addChatComponentMessage(new ChatComponentText("OreUBifier says it isn't replaced"));
        }
      }
    } catch (NullPointerException npe) {
      //nope
    }
  }

  @SubscribeEvent(priority = EventPriority.LOWEST)
  public void postPopulate(Post event) {
    if (event.world.provider.dimensionId == 0) {
      ChunkProcessor cProcessor = ChunkProcessor.get(event.world, this);
      cProcessor.postPopulate(ChunkRef.get(event));
    }
  }

//  private int getBlockID(ExtendedBlockStorage eBS, int x, int y, int z) {
//    int l = eBS.getBlockLSBArray()[y << 8 | z << 4 | x] & 255;
//
//    if (eBS.getBlockMSBArray() != null) {
//      l |= eBS.getBlockMSBArray().get(x, y, z) << 8;
//    }
//    return l;
//  }
//  private class BlockScanner implements Runnable{
//    private BlockScanner (ExtendedBlockStorage... storage) {
//      
//    }
//
//    @Override
//    public void run() {
//      throw new UnsupportedOperationException("Not supported yet.");
//    }
//  }
//  private ArrayList<Pair<ExtendedBlockStorage,Integer>> getNonNullSections(Chunk chunk) {
//    ExtendedBlockStorage[] blockStorageArray = chunk.getBlockStorageArray();
//    ArrayList<Pair<ExtendedBlockStorage,Integer>> list = new ArrayList<>();
//    for (int i = 0; i < blockStorageArray.length; i++) {
//      if(blockStorageArray[i] != null) {
//        //list.add()
//      }
//    }
//  }
}
