package uk.co.mysterymayhem.gmmodpacktweaks.util;

import cpw.mods.fml.common.FMLLog;
import org.apache.logging.log4j.Level;
import static uk.co.mysterymayhem.gmmodpacktweaks.GMModpackTweaks.MODID;

/**
 *
 * @author Mysteryem
 */
public class Log {
  
  public static final boolean DEBUGGING_ACTIVE = false;

  public static void log(Object o) {
    FMLLog.log(MODID, Level.INFO, String.valueOf(o));
  }

  public static void error(Object o) {
    FMLLog.log(MODID, Level.ERROR, "Error: " + String.valueOf(o));
  }
  
  public static void debug(Object o) {
    if (DEBUGGING_ACTIVE) {
      FMLLog.log(MODID, Level.DEBUG, String.valueOf(o));
    }
  }
}
