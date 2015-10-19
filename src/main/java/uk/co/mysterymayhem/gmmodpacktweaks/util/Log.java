/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.mysterymayhem.gmmodpacktweaks.util;

import cpw.mods.fml.common.FMLLog;
import org.apache.logging.log4j.Level;
import static uk.co.mysterymayhem.gmmodpacktweaks.GMModpackTweaks.MODID;

/**
 *
 * @author Thomas
 */
public class Log {

  public static void log(Object o) {
    FMLLog.log(MODID, Level.INFO, String.valueOf(o));
  }

  public static void error(Object o) {
    FMLLog.log(MODID, Level.ERROR, "Error: " + String.valueOf(o));
  }
}
