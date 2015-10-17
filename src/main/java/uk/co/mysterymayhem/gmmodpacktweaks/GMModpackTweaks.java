package uk.co.mysterymayhem.gmmodpacktweaks;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import java.util.HashSet;
import uk.co.mysterymayhem.gmmodpacktweaks.tweaks.FossilsAndArcheology;
import uk.co.mysterymayhem.gmmodpacktweaks.tweaks.GalacticraftMorePlanets;
import uk.co.mysterymayhem.gmmodpacktweaks.tweaks.JaffasAndMore;
import uk.co.mysterymayhem.gmmodpacktweaks.tweaks.RFTools;
import uk.co.mysterymayhem.gmmodpacktweaks.tweaks.Tweak;
import uk.co.mysterymayhem.gmmodpacktweaks.tweaks.UBCOres;
import uk.co.mysterymayhem.gmmodpacktweaks.tweaks.undergroundbiomesconstructs.UndergroundBiomesConstructs;


@Mod(modid = GMModpackTweaks.MODID, version = GMModpackTweaks.VERSION, dependencies = GMModpackTweaks.DEPENDENCIES, name = GMModpackTweaks.MODNAME)
public class GMModpackTweaks
{
    public static final String MODID = "gmpacktweaks";
    public static final String VERSION = "1.5";
    public static final String MODNAME ="GM Modpack Tweaks";
    
    private static final HashSet<Tweak> tweaks = new HashSet<>();
//    static {
//      tweaks.add(new GalacticraftMorePlanets());
//      tweaks.add(new JaffasAndMore());
//      tweaks.add(new RFTools());
//      tweaks.add(new FossilsAndArcheology());
//      tweaks.add(new UBCOres());
//      tweaks.add(new UndergroundBiomesConstructs());
//    }

    public static final String DEPENDENCIES = 
               "after:GalacticraftCore"
            + ";after:GalacticraftMars;"
            + ";after:MorePlanet"
            + ";before:aobd"
            + ";before:ubcores"
            + ";after:Jaffas-Technic"
            + ";after:rftools"
            + ";before:RotaryCraft"
            + ";before:UndergroundBiomes"
            + ";after:fossil"
            + ";required-after:CoFHCore";

    
    
    
    //private static final String dependencies = GalacticraftMorePlanets.dependencies.JaffasAndMore.dependencies;
    
//    @EventHandler
//    public void init(FMLInitializationEvent event)
//    {
//		// some example code
//        //System.out.println("DIRT BLOCK >> "+Blocks.dirt.getUnlocalizedName());
//    }
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
      tweaks.add(new GalacticraftMorePlanets());
      tweaks.add(new JaffasAndMore());
      tweaks.add(new RFTools());
      tweaks.add(new FossilsAndArcheology());
      tweaks.add(new UBCOres());
      tweaks.add(new UndergroundBiomesConstructs());
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event) {
      tweaks.stream().forEach(t -> t.init(event));
    }
    
    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
      tweaks.stream().forEach(t -> t.postInit(event));
    }
    
    
    
    
    
    
}
