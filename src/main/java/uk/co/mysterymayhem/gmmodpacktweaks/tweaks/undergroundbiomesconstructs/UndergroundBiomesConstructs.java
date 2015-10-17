package uk.co.mysterymayhem.gmmodpacktweaks.tweaks.undergroundbiomesconstructs;

import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import exterminatorJeff.undergroundBiomes.api.NamedBlock;
import exterminatorJeff.undergroundBiomes.api.UBIDs;
import exterminatorJeff.undergroundBiomes.api.UBStoneCodes;
import exterminatorJeff.undergroundBiomes.worldGen.OreUBifier;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import exterminatorJeff.undergroundBiomes.common.UndergroundBiomes;
import exterminatorJeff.undergroundBiomes.constructs.entity.UndergroundBiomesTileEntity;
import java.lang.reflect.Field;
import java.util.HashMap;
import net.minecraft.init.Blocks;
import net.minecraftforge.common.MinecraftForge;
import uk.co.mysterymayhem.gmmodpacktweaks.tweaks.Tweak;
import uk.co.mysterymayhem.gmmodpacktweaks.util.OreDict;
import static uk.co.mysterymayhem.gmmodpacktweaks.util.Misc.log;
import cofh.asmhooks.event.ModPopulateChunkEvent;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.relauncher.ReflectionHelper;
import exterminatorJeff.undergroundBiomes.common.block.BlockOverlay;
import exterminatorJeff.undergroundBiomes.common.block.BlockUBHidden;
import exterminatorJeff.undergroundBiomes.common.item.ItemUBHiddenBlock;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import net.minecraft.block.BlockSilverfish;
import net.minecraft.entity.monster.EntitySilverfish;
import net.minecraft.util.MathHelper;
import org.apache.commons.lang3.tuple.ImmutablePair;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.util.Facing;
import net.minecraftforge.event.entity.living.LivingEvent;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Mod tweaks for Underground Biomes Constructs.
 *
 * @author Mysteryem
 */
public class UndergroundBiomesConstructs extends Tweak {

  // Stores the UBC OreUBifier, obtained using reflection during postInit.
  static OreUBifier oreUBifier;

  // Map used to lookup the replacement method
  final static HashMap<Block, IBlockChanger> blockReplaceMethodLookup = new HashMap<>();

  // Output file name for storing ChunkRefs to chunks that either are waiting on some of their surrounding chunks to be populated before they can be populated themselves, or are not yet generated/populated (and therefore not waiting on any other chunks), but should notify some of their surrounding chunks when they do finish population.
  private static final String SAVE_NAME = "gmpacktweaks_chunksaves.txt";

  // Maps for taking a particular type of block and getting a different block back, based on which map is accessed.
  // The maps do not contain all possible entries, i.e., you can go Stone->Cobble->Slab, but not Stone->Slab->Cobble.
  private static final HashMap<NamedBlock, NamedBlock> slabMap = new HashMap<>();
  private static final HashMap<NamedBlock, NamedBlock> slabFullMap = new HashMap<>();
  private static final HashMap<NamedBlock, NamedBlock> brickMap = new HashMap<>();
  private static final HashMap<NamedBlock, NamedBlock> cobbleMap = new HashMap<>();
  private static final HashMap<NamedBlock, Block> monsterEggMap = new HashMap<>();

  // Needs blocks registered by this tweak
  private static final HashMap<NamedBlock, Block> brickMonsterEggMap = new HashMap<>();
  private static final HashMap<NamedBlock, Block> cobbleMonsterEggMap = new HashMap<>();

  // Needs WTFCaveBiomes
  private static final HashMap<NamedBlock, Block> mossyCobbleMap = new HashMap<>();

  // UBC stairs, buttons and stone walls have a unique index stored in their tile entity's nbt data that determines the stone type, including whether it is cobblestone or brick.
  private static final HashMap<NamedBlock, Integer> nbtIndexMap = new HashMap<>();

  private static final HashMap<Block, Function<Integer, ImmutablePair<Block, Integer>>> monsterEggBlocks = new HashMap<>();
  private static final HashMap<Block, Function<Integer, ImmutablePair<Block, Integer>>> silverfishHideLookup = new HashMap<>();

  private static Field silverFishAllyCoolDownField;

  // Statically add all the mappings for UBC
  static {
    brickMap.put(UBIDs.igneousStoneName, UBIDs.igneousStoneBrickName);
    brickMap.put(UBIDs.metamorphicStoneName, UBIDs.metamorphicStoneBrickName);
    brickMap.put(UBIDs.sedimentaryStoneName, UBIDs.sedimentaryStoneName);
    //brickMap.put(NamedVanillaBlock.stone, NamedVanillaBlock.stoneBrick);

    cobbleMap.put(UBIDs.igneousStoneName, UBIDs.igneousCobblestoneName);
    cobbleMap.put(UBIDs.metamorphicStoneName, UBIDs.metamorphicCobblestoneName);
    cobbleMap.put(UBIDs.sedimentaryStoneName, UBIDs.sedimentaryStoneName);
    //cobbleMap.put(NamedVanillaBlock.stone, NamedVanillaBlock.cobblestone);

    slabMap.put(UBIDs.igneousStoneName, UBIDs.igneousStoneSlabName.half);
    slabMap.put(UBIDs.igneousCobblestoneName, UBIDs.igneousCobblestoneSlabName.half);
    slabMap.put(UBIDs.igneousStoneBrickName, UBIDs.igneousBrickSlabName.half);
    slabMap.put(UBIDs.metamorphicStoneName, UBIDs.metamorphicStoneSlabName.half);
    slabMap.put(UBIDs.metamorphicCobblestoneName, UBIDs.metamorphicCobblestoneSlabName.half);
    slabMap.put(UBIDs.metamorphicStoneBrickName, UBIDs.metamorphicBrickSlabName.half);
    slabMap.put(UBIDs.sedimentaryStoneName, UBIDs.sedimentaryStoneSlabName.half);
    //slabMap.put(NamedVanillaBlock.cobblestone, NamedVanillaBlock.stoneSingleSlab);
    //slabMap.put(NamedVanillaBlock.stone, NamedVanillaBlock.stoneSingleSlab);
    //slabMap.put(NamedVanillaBlock.stone, NamedVanillaBlock.stoneSingleSlab);

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

  /**
   * Create a UBC tweaks object. You should never need more than one of these.
   */
  public UndergroundBiomesConstructs() {
    this.MODID = "UndergroundBiomes";
  }

  /**
   * To be called from the main mod class during its postInit event.
   *
   * @param event
   */
  @Override
  public void postInit(FMLPostInitializationEvent event) {
    try {
      // If UBC isn't loaded, completely skip the event
      if (!this.isModLoaded()) {
        return;
      }

      // Register this tweak's events (onWorldLoad, onWorldUnload and postTerrainGenerate)
      MinecraftForge.EVENT_BUS.register(this);

      // Start by adding some UBC blocks to the ore dictionary so that the Mekanism Digital Miner will pick them up
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

      silverFishAllyCoolDownField = ReflectionHelper.findField(EntitySilverfish.class, "allySummonCooldown", "field_70843_d");
      silverFishAllyCoolDownField.setAccessible(true);

      // Grab ourselves the oreUBifier instance from UndergroundBiomes
      Field field = UndergroundBiomes.class.getDeclaredField("oreUBifier");
      field.setAccessible(true);
      oreUBifier = (OreUBifier) field.get(UndergroundBiomes.instance());

      // As the config option for replacing ores is off (it should be off), sneakily force it on in just the OreUBifier, hopefully, anything else using the value from the config will still consider it to be off. This is needed otherwise the OreUBifier will never say that any blocks should be replaced.
      field = OreUBifier.class.getDeclaredField("replacementActive");
      field.setAccessible(true);
      field.setBoolean(oreUBifier, true);

      // Register monster_egg blocks for igneous and metamorphic bricks
      BlockUBHidden ubOre = new BlockUBHidden(UndergroundBiomes.igneousStoneBrick, Blocks.monster_egg);
      NamedBlock namer = new NamedBlock("igneousBrick" + "_" + Blocks.monster_egg.getUnlocalizedName().substring(5));
      BlockOverlay.logger.log(Level.INFO, "block {0} no metadata ", Blocks.monster_egg);
      Block igneousBrickMonsterEgg = GameRegistry.registerBlock(ubOre, ItemUBHiddenBlock.class, namer.internal());

      ubOre = new BlockUBHidden(UndergroundBiomes.metamorphicStoneBrick, Blocks.monster_egg);
      namer = new NamedBlock("metamorphicBrick" + "_" + Blocks.monster_egg.getUnlocalizedName().substring(5));
      BlockOverlay.logger.log(Level.INFO, "block {0} no metadata ", Blocks.monster_egg);
      Block metamorphicBrickMonsterEgg = GameRegistry.registerBlock(ubOre, ItemUBHiddenBlock.class, namer.internal());

      ubOre = new BlockUBHidden(UndergroundBiomes.igneousCobblestone, Blocks.monster_egg);
      namer = new NamedBlock("igneousCobble" + "_" + Blocks.monster_egg.getUnlocalizedName().substring(5));
      BlockOverlay.logger.log(Level.INFO, "block {0} no metadata ", Blocks.monster_egg);
      Block igneousCobbleMonsterEgg = GameRegistry.registerBlock(ubOre, ItemUBHiddenBlock.class, namer.internal());

      ubOre = new BlockUBHidden(UndergroundBiomes.metamorphicCobblestone, Blocks.monster_egg);
      namer = new NamedBlock("metamorphicCobble" + "_" + Blocks.monster_egg.getUnlocalizedName().substring(5));
      BlockOverlay.logger.log(Level.INFO, "block {0} no metadata ", Blocks.monster_egg);
      Block metamorphicCobbleMonsterEgg = GameRegistry.registerBlock(ubOre, ItemUBHiddenBlock.class, namer.internal());

      // Add method for replacing smooth stone
      blockReplaceMethodLookup.put(Blocks.stone, (UBStoneCodes strata, World world, int x, int y, int z) -> {
        world.setBlock(x, y, z, strata.block, strata.metadata, 2);
      });

      // Add method for replacing cobblestone
      blockReplaceMethodLookup.put(Blocks.cobblestone, (UBStoneCodes strata, World world, int x, int y, int z) -> {
        world.setBlock(x, y, z, cobbleMap.get(strata.name).block, strata.metadata, 2);
      });

      // Add method for replacing cobblestone walls
      blockReplaceMethodLookup.put(Blocks.cobblestone_wall, (UBStoneCodes strata, World world, int x, int y, int z) -> {

        // Index is stored in the tile entity to determine the stone type of the wall
        int index = nbtIndexMap.get(cobbleMap.get(strata.name));

        world.setBlock(x, y, z, UBIDs.UBWallsName.block, 0, 2);

        // Set up the tile entity
        UndergroundBiomesTileEntity tEnt = new UndergroundBiomesTileEntity();
        tEnt.setWorldObj(world);
        tEnt.setMasterIndex(index + strata.metadata);
        world.addTileEntity(tEnt);
        world.setTileEntity(x, y, z, tEnt);
      });

      // Add method for replacing stone brick stairs
      blockReplaceMethodLookup.put(Blocks.stone_brick_stairs, (UBStoneCodes strata, World world, int x, int y, int z) -> {

        // Index is stored in the tile entity to determine the stone type of the stairs
        int index = nbtIndexMap.get(brickMap.get(strata.name));

        // Metadata determines the orientation of the stairs
        int metaData = world.getBlockMetadata(x, y, z);

        world.setBlock(x, y, z, UBIDs.UBStairsName.block, metaData, 2);

        // Set up the tile entity
        UndergroundBiomesTileEntity tEnt = new UndergroundBiomesTileEntity();
        tEnt.setWorldObj(world);
        tEnt.setMasterIndex(index + strata.metadata);
        world.addTileEntity(tEnt);
        world.setTileEntity(x, y, z, tEnt);
      });

      // Add method for replacing stone buttons
      blockReplaceMethodLookup.put(Blocks.stone_button, (UBStoneCodes strata, World world, int x, int y, int z) -> {

        // Index is stored in the tile entity to determine the stone type of the button
        int index = nbtIndexMap.get(strata.name);

        // Metadata determines whether the button is powered
        int metaData = world.getBlockMetadata(x, y, z);

        world.setBlock(x, y, z, UBIDs.UBButtonName.block, metaData, 2);

        // Set up the tile entity
        UndergroundBiomesTileEntity tEnt = new UndergroundBiomesTileEntity();
        tEnt.setWorldObj(world);
        tEnt.setMasterIndex(index + strata.metadata);
        world.addTileEntity(tEnt);
        world.setTileEntity(x, y, z, tEnt);
      });

      // Add method for replacing stone slabs (smooth, cobble and bricks)
      blockReplaceMethodLookup.put(Blocks.stone_slab, (UBStoneCodes strata, World world, int x, int y, int z) -> {
        //int
        switch (world.getBlockMetadata(x, y, z)) {
          // Bottom Smooth stone
          case 0:
            world.setBlock(x, y, z, slabMap.get(strata.name).block(), strata.metadata, 2);
            break;
          // Bottom Cobblestone
          case 3:
            world.setBlock(x, y, z, slabMap.get(cobbleMap.get(strata.name)).block(), strata.metadata, 2);
            break;
          // Bottom Stone Brick
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

      // Add method for replacing double stone slabs (smooth, cobble and bricks)
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

      // Add method for replacing cobblestone stairs
      blockReplaceMethodLookup.put(Blocks.stone_stairs, (UBStoneCodes strata, World world, int x, int y, int z) -> {
        // Index is stored in the tile entity to determine the stone type of the stairs
        int index = nbtIndexMap.get(cobbleMap.get(strata.name));

        // Metadata determines orientation of the stairs
        int metaData = world.getBlockMetadata(x, y, z);

        world.setBlock(x, y, z, UBIDs.UBStairsName.block, metaData, 2);

        // Set up the tile entity
        UndergroundBiomesTileEntity tEnt = new UndergroundBiomesTileEntity();
        tEnt.setWorldObj(world);
        tEnt.setMasterIndex(index + strata.metadata);
        world.addTileEntity(tEnt);
        world.setTileEntity(x, y, z, tEnt);
      });

      // Add method for replacing stone bricks
      blockReplaceMethodLookup.put(Blocks.stonebrick, (UBStoneCodes strata, World world, int x, int y, int z) -> {
        world.setBlock(x, y, z, brickMap.get(strata.name).block, strata.metadata, 2);
      });

      blockReplaceMethodLookup.put(Blocks.monster_egg, (UBStoneCodes strata, World world, int x, int y, int z) -> {
        int metadata = world.getBlockMetadata(x, y, z);
        Block get;
        switch (metadata) {
          case 0:
            get = monsterEggMap.get(strata.name);
            break;
          case 1:
            get = cobbleMonsterEggMap.get(strata.name);
            break;
          //case 2:
          default:
            get = brickMonsterEggMap.get(strata.name);
        }

        world.setBlock(x, y, z, get, strata.metadata, 2);

      });

      monsterEggMap.put(UBIDs.igneousStoneName, Objects.requireNonNull(GameRegistry.findBlock(MODID, "igneous_monsterStoneEgg")));
      monsterEggMap.put(UBIDs.metamorphicStoneName, Objects.requireNonNull(GameRegistry.findBlock(MODID, "metamorphic_monsterStoneEgg")));
      monsterEggMap.put(UBIDs.sedimentaryStoneName, Objects.requireNonNull(GameRegistry.findBlock(MODID, "sedimentary_monsterStoneEgg")));
      brickMonsterEggMap.put(UBIDs.igneousStoneName, igneousBrickMonsterEgg);
      brickMonsterEggMap.put(UBIDs.metamorphicStoneName, metamorphicBrickMonsterEgg);
      brickMonsterEggMap.put(UBIDs.sedimentaryStoneName, Objects.requireNonNull(GameRegistry.findBlock(MODID, "sedimentary_monsterStoneEgg")));
      cobbleMonsterEggMap.put(UBIDs.igneousStoneName, igneousCobbleMonsterEgg);
      cobbleMonsterEggMap.put(UBIDs.metamorphicStoneName, metamorphicCobbleMonsterEgg);
      cobbleMonsterEggMap.put(UBIDs.sedimentaryStoneName, Objects.requireNonNull(GameRegistry.findBlock(MODID, "sedimentary_monsterStoneEgg")));

      monsterEggBlocks.put(Blocks.monster_egg, m -> BlockSilverfish.func_150197_b(m));
      monsterEggBlocks.put(Objects.requireNonNull(GameRegistry.findBlock(MODID, "igneous_monsterStoneEgg")), m -> new ImmutablePair<>(UBIDs.igneousStoneName.block(), m));
      monsterEggBlocks.put(igneousCobbleMonsterEgg, m -> new ImmutablePair<>(UBIDs.igneousCobblestoneName.block(), m));
      monsterEggBlocks.put(igneousBrickMonsterEgg, m -> new ImmutablePair<>(UBIDs.igneousStoneBrickName.block(), m));
      monsterEggBlocks.put(Objects.requireNonNull(GameRegistry.findBlock(MODID, "metamorphic_monsterStoneEgg")), m -> new ImmutablePair<>(UBIDs.metamorphicStoneName.block(), m));
      monsterEggBlocks.put(metamorphicCobbleMonsterEgg, m -> new ImmutablePair<>(UBIDs.metamorphicCobblestoneName.block(), m));
      monsterEggBlocks.put(metamorphicBrickMonsterEgg, m -> new ImmutablePair<>(UBIDs.metamorphicStoneBrickName.block(), m));
      monsterEggBlocks.put(Objects.requireNonNull(GameRegistry.findBlock(MODID, "sedimentary_monsterStoneEgg")), m -> new ImmutablePair<>(UBIDs.sedimentaryStoneName.block(), m));

//      Function<Integer, ImmutablePair<Block, Integer>
      ImmutablePair<Block, Integer> smoothstoneMonsterEgg = new ImmutablePair<>(Blocks.monster_egg, 0);
      silverfishHideLookup.put(Blocks.stone, m -> smoothstoneMonsterEgg);
      ImmutablePair<Block, Integer> cobblestoneMonsterEgg = new ImmutablePair<>(Blocks.monster_egg, 1);
      silverfishHideLookup.put(Blocks.cobblestone, m -> cobblestoneMonsterEgg);
      silverfishHideLookup.put(Blocks.stonebrick, m -> new ImmutablePair<>(Blocks.monster_egg, m + 2));
      silverfishHideLookup.put(UBIDs.igneousStoneName.block(), m -> new ImmutablePair<>(monsterEggMap.get(UBIDs.igneousStoneName), m));
      silverfishHideLookup.put(UBIDs.igneousCobblestoneName.block(), m -> new ImmutablePair<>(cobbleMonsterEggMap.get(UBIDs.igneousStoneName), m));
      silverfishHideLookup.put(UBIDs.igneousStoneBrickName.block(), m -> new ImmutablePair<>(brickMonsterEggMap.get(UBIDs.igneousStoneName), m));
      silverfishHideLookup.put(UBIDs.metamorphicStoneName.block(), m -> new ImmutablePair<>(monsterEggMap.get(UBIDs.metamorphicStoneName), m));
      silverfishHideLookup.put(UBIDs.metamorphicCobblestoneName.block(), m -> new ImmutablePair<>(cobbleMonsterEggMap.get(UBIDs.metamorphicStoneName), m));
      silverfishHideLookup.put(UBIDs.metamorphicStoneBrickName.block(), m -> new ImmutablePair<>(brickMonsterEggMap.get(UBIDs.metamorphicStoneName), m));
      silverfishHideLookup.put(UBIDs.sedimentaryStoneName.block(), m -> new ImmutablePair<>(monsterEggMap.get(UBIDs.sedimentaryStoneName), m));

      if (Loader.isModLoaded("CaveBiomes")) {
        blockReplaceMethodLookup.put(Blocks.mossy_cobblestone, (UBStoneCodes strata, World world, int x, int y, int z) -> {
          world.setBlock(x, y, z, mossyCobbleMap.get(strata.name), strata.metadata, 2);
        });
        mossyCobbleMap.put(UBIDs.igneousStoneName, Objects.requireNonNull(GameRegistry.findBlock("CaveBiomes", "mossy_igneous_cobblestone")));
        mossyCobbleMap.put(UBIDs.metamorphicStoneName, Objects.requireNonNull(GameRegistry.findBlock("CaveBiomes", "mossy_metamorphic_cobblestone")));
        mossyCobbleMap.put(UBIDs.sedimentaryStoneName, Objects.requireNonNull(GameRegistry.findBlock("CaveBiomes", "mossy_sedimentary_stone")));
      }

    } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
      uk.co.mysterymayhem.gmmodpacktweaks.util.Misc.log("Failed to get UBC OreUBifier");
    }

  }

  @SubscribeEvent(priority = EventPriority.LOWEST)
  public void postTerrainGenerate(ModPopulateChunkEvent.Post event) {
    if (!event.world.isRemote && event.world.provider.dimensionId == 0) {
      ChunkProcessor cProcessor = ChunkProcessor.get(event.world);
      //cProcessor.replaceBlocks(ChunkRef.get(event.chunkX, event.chunkZ));
      cProcessor.postModOreGen(ChunkRef.get(event.chunkX, event.chunkZ));
    }
  }

  private static final String DATUM_DELIM = ",";
  private static final String SECTION_DELIM = "\n";
  private static final String LIST_ENTRY_DELIM = ";";

  @SubscribeEvent(priority = EventPriority.NORMAL)
  public void onWorldUnload(WorldEvent.Unload event) {
    if (!event.world.isRemote && event.world.provider.dimensionId == 0) {
      log("Beginning write to file attempt");
      ArrayList<ChunkRef> allRefsList = ChunkRef.getAll();
      //DEBUG
      //ChunkRef.cyclicCheck();
      try (PrintWriter output = new PrintWriter(new BufferedWriter(new FileWriter(event.world.getSaveHandler().getWorldDirectory().getAbsolutePath() + File.separator + SAVE_NAME)));) {

        StringBuilder stringOutput = new StringBuilder();
        HashMap<ChunkRef, Integer> reverseLookup = new HashMap<>();

        // Write non-list information for each ChunkRef
        for (ListIterator<ChunkRef> iterator = allRefsList.listIterator(); iterator.hasNext();) {
          ChunkRef next = iterator.next();

          // Add to reverse lookup
          reverseLookup.put(next, iterator.previousIndex());

          // Nothing appended when allGenFinished is false as that should be the majority of the time
          stringOutput.append(next.x).append(DATUM_DELIM).append(next.z);//.append(DATUM_DELIM).append(next.allGenFinished ? ALL_GEN_FINISHED : "");

          if (iterator.hasNext()) {
            stringOutput.append(LIST_ENTRY_DELIM);
          }
        }

        // New line for next section
        stringOutput.append(SECTION_DELIM);

        // notifyOnPostPopulate section
        for (ListIterator<ChunkRef> iterator = allRefsList.listIterator(); iterator.hasNext();) {
          ChunkRef next = iterator.next();

          for (Iterator<ChunkRef> postPopulateIterator = next.notifyOnPostPopulate.iterator(); postPopulateIterator.hasNext();) {
            ChunkRef postPopNext = postPopulateIterator.next();
            stringOutput.append(reverseLookup.get(postPopNext));

            if (postPopulateIterator.hasNext()) {
              stringOutput.append(DATUM_DELIM);
            }
          }

          if (iterator.hasNext()) {
            stringOutput.append(LIST_ENTRY_DELIM);
          }
        }

        // New line for next section
        stringOutput.append(SECTION_DELIM);

        // waitingOn section
        for (ListIterator<ChunkRef> iterator = allRefsList.listIterator(); iterator.hasNext();) {
          ChunkRef next = iterator.next();

          for (Iterator<ChunkRef> waitingOnIterator = next.waitingOn.iterator(); waitingOnIterator.hasNext();) {
            ChunkRef waitingOnNext = waitingOnIterator.next();
            stringOutput.append(reverseLookup.get(waitingOnNext));

            if (waitingOnIterator.hasNext()) {
              stringOutput.append(DATUM_DELIM);
            }
          }

          if (iterator.hasNext()) {
            stringOutput.append(LIST_ENTRY_DELIM);
          }
        }

        output.write(stringOutput.toString());
        log("Wrote UBC data to file");
      } catch (IOException /*| NullPointerException*/ ex) {
        log("Failed to save UBC file:\n" + String.valueOf(ex));
      }
    }
  }

  @SubscribeEvent(priority = EventPriority.NORMAL)
  public void onWorldLoad(WorldEvent.Load event) {
    if (!event.world.isRemote && event.world.provider.dimensionId == 0) {
      log("Beginning load from file attempt");
      try (BufferedReader input = new BufferedReader(new FileReader(event.world.getSaveHandler().getWorldDirectory().getAbsolutePath() + File.separator + SAVE_NAME));) {
        StringBuilder wholeFile = new StringBuilder();
        input.lines().forEach(t -> {
          wholeFile.append(t);
          wholeFile.append("\n");
        });
        String[] sections = wholeFile.toString().split(Pattern.quote(SECTION_DELIM));
        if (sections.length != 3) {
          log("UBC data could not be read, does the file exist?");
          return;
        }

        // Split the first section into individual ChunkRefs
        String[] singlets = sections[0].split(Pattern.quote(LIST_ENTRY_DELIM));

        // NOTE: Could use HashMap<String, ChunkRef>, then we don't need to parse at all, we could even store the references to ChunkRefs using letters or even control characters
        ArrayList<ChunkRef> refList = new ArrayList<>(singlets.length);
        for (String refAsString : singlets) {
          String[] data = refAsString.split(Pattern.quote(DATUM_DELIM));
          ChunkRef ref = ChunkRef.get(Integer.parseInt(data[0]), Integer.parseInt(data[1]));

          refList.add(ref);
        }

        // Split the second section into individual lists of references to chunks.
        String[] notifyList = sections[1].split(Pattern.quote(LIST_ENTRY_DELIM));

        // For each list, split this into ChunkRef references, the index is the reference to the ChunkRef to which the data belongs
        for (int i = 0; i < notifyList.length; i++) {
          // If we have LIST_ENTRY_DELIM followed directly by LIST_ENTRY_DELIM, then we'll have an empty string, splitting using DATUM_DELIM will give that empty string again
          if (notifyList[i].length() == 0) {
            continue;
          }

          ChunkRef owningChunkRef = refList.get(i);
          String[] referencesToChunkRefsAsStrings = notifyList[i].split(Pattern.quote(DATUM_DELIM));
          for (String referenceToChunkRefAsString : referencesToChunkRefsAsStrings) {
            owningChunkRef.notifyOnPostPopulate.add(refList.get(Integer.parseInt(referenceToChunkRefAsString)));
          }
        }

        // Split the second section into individual lists of references to chunks.
        String[] waitingList = sections[2].split(Pattern.quote(LIST_ENTRY_DELIM));

        // For each list, split this into ChunkRef references, the index is the reference to the ChunkRef to which the data belongs
        for (int i = 0; i < waitingList.length; i++) {
          // If we have LIST_ENTRY_DELIM followed directly by LIST_ENTRY_DELIM, then we'll have an empty string, splitting using DATUM_DELIM will give that empty string again
          if (waitingList[i].length() == 0) {
            continue;
          }

          ChunkRef owningChunkRef = refList.get(i);
          String[] referencesToChunkRefsAsStrings = waitingList[i].split(Pattern.quote(DATUM_DELIM));
          for (String referenceToChunkRefAsString : referencesToChunkRefsAsStrings) {
            owningChunkRef.waitingOn.add(refList.get(Integer.parseInt(referenceToChunkRefAsString)));
          }
        }

        log("Loaded UBC data from file");
      } catch (IOException /*| NullPointerException*/ | ArrayIndexOutOfBoundsException | NumberFormatException ex) {
        // Clean up any partially stored data
        ChunkRef.removeAll();
        log("Failed to read UBC file:\n" + String.valueOf(ex));
      }
    }
  }

  /**
   * Allows silverfish to work with UBC stones, seems a little
   * @param event 
   */
  @SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = false)
  public void onUpdate(LivingEvent.LivingUpdateEvent event) {
    if (event.entityLiving instanceof EntitySilverfish) {
      World world = event.entityLiving.worldObj;
      if (!world.isRemote) {
        try {
          EntitySilverfish silverfish = (EntitySilverfish) event.entityLiving;
          int blockX;
          int blockY;
          int blockZ;
          int xOffset;
          if (silverFishAllyCoolDownField.getInt(silverfish) == 1) {
            blockX = MathHelper.floor_double(silverfish.posX);
            blockY = MathHelper.floor_double(silverfish.posY);
            blockZ = MathHelper.floor_double(silverfish.posZ);
            boolean flag = false;

            for (int yOffset = 0; !flag && yOffset <= 5 && yOffset >= -5; yOffset = yOffset <= 0 ? 1 - yOffset : 0 - yOffset) {
              for (xOffset = 0; !flag && xOffset <= 10 && xOffset >= -10; xOffset = xOffset <= 0 ? 1 - xOffset : 0 - xOffset) {
                for (int zOffset = 0; !flag && zOffset <= 10 && zOffset >= -10; zOffset = zOffset <= 0 ? 1 - zOffset : 0 - zOffset) {
                  Function<Integer, ImmutablePair<Block, Integer>> get = monsterEggBlocks.get(silverfish.worldObj.getBlock(blockX + xOffset, blockY + yOffset, blockZ + zOffset));
                  if (get != null) {
                    if (!silverfish.worldObj.getGameRules().getGameRuleBooleanValue("mobGriefing")) {
                      // Given the monsterEggBlock, replace it with the non monsterEgg version
                      int metadata = silverfish.worldObj.getBlockMetadata(blockX + xOffset, blockY + yOffset, blockZ + zOffset);
                      ImmutablePair<Block, Integer> apply = get.apply(metadata);
                      silverfish.worldObj.setBlock(blockX + xOffset, blockY + yOffset, blockZ + zOffset, apply.getLeft(), apply.getRight(), 3);
                    } else {
                      // Break the monsterEggBlock
                      silverfish.worldObj.func_147480_a(blockX + xOffset, blockY + yOffset, blockZ + zOffset, false);
                    }

                    Blocks.monster_egg.onBlockDestroyedByPlayer(silverfish.worldObj, blockX + xOffset, blockY + yOffset, blockZ + zOffset, 0);
                    //DEBUG
                    //log(silverfish.getEntityId() + "Called ally from block (" + (blockX + xOffset) + ", " + (blockY + yOffset) + ", " + (blockZ + zOffset) + ")");
                    if (ThreadLocalRandom.current().nextBoolean()) {
                      flag = true;
                      break;
                    }
                  }
                }
              }
            }
          }
          if (silverfish.getEntityToAttack() == null && !silverfish.hasPath()) {
            blockX = MathHelper.floor_double(silverfish.posX);
            blockY = MathHelper.floor_double(silverfish.posY + 0.5D);
            blockZ = MathHelper.floor_double(silverfish.posZ);
            int l1 = ThreadLocalRandom.current().nextInt(6);
            Block block = silverfish.worldObj.getBlock(blockX + Facing.offsetsXForSide[l1], blockY + Facing.offsetsYForSide[l1], blockZ + Facing.offsetsZForSide[l1]);

            int metadata = silverfish.worldObj.getBlockMetadata(blockX + Facing.offsetsXForSide[l1], blockY + Facing.offsetsYForSide[l1], blockZ + Facing.offsetsZForSide[l1]);

            Function<Integer, ImmutablePair<Block, Integer>> get = silverfishHideLookup.get(block);
            if (get != null) {
              ImmutablePair<Block, Integer> apply = get.apply(metadata);
              silverfish.worldObj.setBlock(blockX + Facing.offsetsXForSide[l1], blockY + Facing.offsetsYForSide[l1], blockZ + Facing.offsetsZForSide[l1], apply.getLeft(), apply.getRight(), 3);
              //DEBUG
              //log(silverfish.getEntityId() + "Hid away in block (" + (blockX + Facing.offsetsXForSide[l1]) + ", " + (blockY + Facing.offsetsYForSide[l1]) + ", " + (blockZ + Facing.offsetsZForSide[l1]) + ")");
              silverfish.spawnExplosionParticle();
              silverfish.setDead();
            }
          } 
        } catch (IllegalArgumentException | IllegalAccessException ex) {
          throw new RuntimeException(ex);//Logger.getLogger(UndergroundBiomesConstructs.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
    }
  }
}
