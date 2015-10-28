/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.mysterymayhem.gmmodpacktweaks.tweaks;

import cpw.mods.fml.common.event.FMLInitializationEvent;
//import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import static uk.co.mysterymayhem.gmmodpacktweaks.util.OreDict.DictType.BLOCK;
import static uk.co.mysterymayhem.gmmodpacktweaks.util.OreDict.DictType.INGOT;
import static uk.co.mysterymayhem.gmmodpacktweaks.util.OreDict.DictType.ORE;
import static uk.co.mysterymayhem.gmmodpacktweaks.util.OreDict.DictType.DUST;
import static uk.co.mysterymayhem.gmmodpacktweaks.util.OreDict.DictType.GEM;
import static uk.co.mysterymayhem.gmmodpacktweaks.util.OreDict.DictType.ITEM;
import static uk.co.mysterymayhem.gmmodpacktweaks.util.OreDict.regMany;
import static uk.co.mysterymayhem.gmmodpacktweaks.util.OreDict.register;
import cpw.mods.fml.common.Loader;
import net.minecraftforge.oredict.OreDictionary;

/**
 *
 * @author Thomas
 */
public class GalacticraftMorePlanets extends Tweak{
  
  public GalacticraftMorePlanets() {
    super(null);
  }

  @Override
  public boolean isModLoaded() {
    return Loader.isModLoaded("GalacticraftCore") && Loader.isModLoaded("GalacticraftMars") && Loader.isModLoaded("MorePlanet");
  }
   
  @Override
  public void init(FMLInitializationEvent event) {
    if (!this.isModLoaded()) return;
      OreDictionary.rebakeMap();
    // Add Galacticraft ores and stuff to ore dictionary
      String MP = "MorePlanet";
      String DIB = "diona_block";
      String DII = "diona_item";
      String POB = "polongnius_block";
      String POI = "polongnius_item";
      String KOB = "koentus_block";
      String KOI = "koentus_item";
      String FRB = "fronos_block";
      String FRI = "fronos_item";
      String FROB = "fronos_ore_block";
      String KAB = "kapteyn-b_block";
      String KAI = "kapteyn-b_item";
      String MEB = "mercury_block";
      String MEI = "mercury_item";
      String VEB = "venus_block";
      String VEI = "venus_item";
      String PHB = "phobos_block";
      String DEB = "deimos_block";
      String NIB = "nibiru_block";
      String NII = "nibiru_item";
      String PLB = "pluto_block";
      String PLI = "pluto_item";
      String SIB = "sirius_block";
      String SII = "sirius-b_item";
      String IOB = "io_block";
      String GC = "GalacticraftCore";
      String GM = "GalacticraftMars";
      
      regMany("Quontonium", MP, new String[]{DIB, DII, DIB}, new int[]{4, 0, 10}, ORE, INGOT, BLOCK);
      regMany("Fronisium", MP, new String[]{DIB, DII, "fronisium_block"}, new int[]{5, 1, 0}, ORE, INGOT, BLOCK);
      regMany("Tin", MP, new String[]{DIB, POB, KOB, FRB, KAB, MEB, VEB, PHB, DEB}, new int[]{6,5,4,5,7,4,6,4,4}, ORE);
      register(BLOCK, "Tin", GC, "tile.gcBlockCore", 10);
      register(INGOT, "Tin", GC, "item.basicItem", 4);
      regMany("Copper", MP, new String[]{DIB, POB, KOB, FRB, KAB, MEB, VEB, PHB, DEB}, new int[]{7,4,5,6,8,5,7,5,5}, ORE);
      register(BLOCK, "Copper", GC, "tile.gcBlockCore", 9);
      register(INGOT, "Copper", GC, "item.basicItem", 3);
      register(ORE, "Silicon", MP, DIB, 8);
      register(ITEM, "Silicon", GC, "item.basicItem", 2);
      regMany("Aluminum", MP, new String[]{DIB, FRB, MEB}, new int[]{9,4,6}, ORE);
      regMany("Aluminium", MP, new String[]{DIB, FRB, MEB}, new int[]{9,4,6}, ORE);
      register(BLOCK, "Aluminum", GC, "tile.gcBlockCore", 11);
      register(BLOCK, "Aluminium", GC, "tile.gcBlockCore", 11);
      register(INGOT, "Aluminum", GC, "item.basicItem", 5);
      regMany("Palladium", MP, new String[]{POB, POI, POI, POB}, new int[]{7,3,5,12}, ORE, DUST, INGOT, BLOCK);
      regMany("Flonium", MP, new String[]{POB, POI}, new int[]{8,0}, ORE, GEM);
      regMany("PurpleCrystal", MP, new String[]{POB, POI, POB}, new int[]{9,1,11}, ORE, GEM, BLOCK);
      regMany("IchoriusGem", MP, new String[]{NIB, "power_crystal", NIB}, new int[]{4,0,9}, ORE, GEM, BLOCK);
      regMany("Norium", MP, new String[]{NIB, NII, NIB}, new int[]{5,1,10}, ORE, INGOT, BLOCK);
      regMany("Diamond", MP, new String[]{NIB, SIB}, new int[]{6,5}, ORE);
      regMany("Coal", MP, new String[]{NIB, FRB, VEB}, new int[]{7,3,8}, ORE);
      regMany("RedGem", MP, new String[]{NIB, NII, NIB}, new int[]{8,0,11}, ORE, GEM, BLOCK);
      regMany("WhiteCrystal", MP, new String[]{KOB, KOI, KOB}, new int[]{6,0,9}, ORE, GEM, BLOCK);
      regMany("EMPCrystal", MP, new String[]{KOB, KOI, KOB}, new int[]{7,1,10}, ORE, GEM, BLOCK);
      regMany("BacterialFossil", MP, new String[]{KOB, KOI}, new int[]{8,2}, ORE, ITEM);
      regMany("Iron", MP, new String[]{POB, FRB, MEB, VEB, PLB, PHB, DEB}, new int[]{6,2,7,9,6,6,6}, ORE);
      register(DUST, "Iron", GM, "item.itemBasicAsteroids", 4);
      register(ORE, "Lapis", MP, FRB, 7);
      regMany("MineralCrystal", MP, new String[]{FRB, FRI}, new int[]{8,0}, ORE, GEM);
      regMany("BlackDiamond", MP, new String[]{FRB, FRI, FROB}, new int[]{9,2,1}, ORE, GEM, BLOCK);
      regMany("Iridium", MP, new String[]{FRB, FRI, FROB}, new int[]{10,3,0}, ORE, INGOT, BLOCK);
      register(ORE, "GrapeJelly", MP, "fronos_ore", 0);
      register(ORE, "RaspberryJelly", MP, "fronos_ore", 1);
      register(ORE, "StrawberryJelly", MP, "fronos_ore", 2);
      register(ORE, "BerryJelly", MP, "fronos_ore", 3);
      register(ORE, "LimeJelly", MP, "fronos_ore", 4);
      register(ORE, "OrangeJelly", MP, "fronos_ore", 5);
      register(ORE, "GreenJelly", MP, "fronos_ore", 6);
      register(ORE, "LemonJelly", MP, "fronos_ore", 7);
      regMany("NameriumCrystal", MP, new String[]{KAB, "namerium_crystal", KAB}, new int[]{4,0,11}, ORE, GEM, BLOCK);
      regMany("FrozenIron", MP, new String[]{KAB, PLB, KAI, KAB}, new int[]{5,5,0,12}, ORE, ORE, INGOT, BLOCK);
      regMany("LightUranium", MP, new String[]{KAB, KAI, KAB}, new int[]{6,1,13}, ORE, GEM, BLOCK);
      regMany("Redstone", MP, new String[]{KAB, "venus_redstone_ore"}, new int[]{9,0}, ORE);
      regMany("MetalMeteoricIron", MP, new String[]{MEB, MEI, MEI, MEB}, new int[]{8,3,1,10}, ORE, INGOT, DUST, BLOCK);
      regMany("HeavySulfur", MP, new String[]{SIB, VEB, IOB, SII, SII, SIB}, new int[]{4,4,4,2,3,8}, ORE, ORE, ORE, DUST, INGOT, BLOCK);
      regMany("MeteoricIron", GC, new String[]{"tile.gcBlockCore", "item.meteoricIronRaw", "item.meteoricIronIngot"}, new int[]{12,0,0}, BLOCK, DUST, INGOT);
      register(ORE, "MeteoricIron", MP, PLB, 4);
      regMany("Lead", MP, new String[]{VEB, VEI, VEB}, new int[]{5,0,11}, ORE, INGOT, BLOCK);
      register(ORE, "Gold", MP, VEB, 10);
      regMany("Xeonium", MP, new String[]{PLB, PLI, "xeonium_dust"}, new int[]{7,0,0}, ORE, GEM, DUST);
      regMany("Desh", GM, new String[]{"tile.mars", "item.null", "item.null", "tile.mars"}, new int[]{2,0,2,8}, ORE, DUST, INGOT, BLOCK);
      regMany("Desh", MP, new String[]{PHB, DEB}, new int[]{7,7}, ORE);
      regMany("Titanium", GM, new String[]{"tile.asteroidsBlock", "item.itemBasicAsteroids"}, new int[]{4,4}, ORE, DUST);
      regMany("Metallic", MP, new String[]{"metallic_rock", MEI, MEI}, new int[]{0,0,2}, ORE, DUST, INGOT);
      regMany("PolongniusMeteoricIron", MP, new String[]{POB, POI, POI}, new int[]{10,4,2}, BLOCK, INGOT, DUST);
      regMany("KoentusMeteoricIron", MP, new String[]{KOB, KOI, KOI}, new int[]{15,4,3}, BLOCK, INGOT, DUST);
  }

}
