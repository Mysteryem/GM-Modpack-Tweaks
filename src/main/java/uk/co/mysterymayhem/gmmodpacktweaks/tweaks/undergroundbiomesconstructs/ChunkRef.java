package uk.co.mysterymayhem.gmmodpacktweaks.tweaks.undergroundbiomesconstructs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import uk.co.mysterymayhem.gmmodpacktweaks.util.Log;

/**
 * Allows access to and stores chunks by referencing their x and y coordinates.
 * Used heavily by custom UBC block replacement.
 *
 * @author Mysteryem
 */
public class ChunkRef {

  // Internal map for storing ChunkRefs
  private static final HashMap<Integer, HashMap<Integer, ChunkRef>> allRefs = new HashMap<>();
  
  private static final HashSet<ChunkRef> currentlyGenerating = new HashSet<>();

  /**
   * Get a ChunkRef, creating a new one if it doesn't yet exist.
   *
   * @param chunkX
   * @param chunkZ
   * @return the ChunkRef for these coordinates.
   */
  public static ChunkRef get(int chunkX, int chunkZ) {
    HashMap<Integer, ChunkRef> get = allRefs.get(chunkX);
    if (get == null) {
      get = new HashMap<>();
      allRefs.put(chunkX, get);
    }
    ChunkRef ref = get.get(chunkZ);
    if (ref == null) {
      ref = new ChunkRef(chunkX, chunkZ);
      get.put(chunkZ, ref);
    }
    return ref;
  }

  /**
   * Get a ChunkRef from a Minecraft Chunk, creating a new one if it doesn't
   * exist yet.
   *
   * @param chunk
   * @return the ChunkRef for this Chunk.
   */
  public static ChunkRef get(Chunk chunk) {
    return get(chunk.xPosition, chunk.zPosition);
  }

  /**
   * Remove the specified ChunkRef from the internal mapping.
   *
   * @param chunkX
   * @param chunkZ
   * @return the ChunkRef that was removed, or null if there wasn't one to be
   * removed.
   */
  public static ChunkRef remove(int chunkX, int chunkZ) {
    HashMap<Integer, ChunkRef> get = allRefs.get(chunkX);
    if (get == null) {
      // No ChunkRefs exist for this x value
      return null;
    }
    return get.remove(chunkZ);
  }

  /**
   * Remove the ChunkRef for the specified Chunk from the internal mapping.
   *
   * @param chunk
   * @return the ChunkRef that was removed, or null if there wasn't one to be
   * removed.
   */
  public static ChunkRef remove(Chunk chunk) {
    return remove(chunk.xPosition, chunk.zPosition);
  }

  /**
   * Remove the specified ChunkRef from the internal mapping.
   *
   * @param ref
   * @return the ChunkRef that was removed, or null if it wasn't in the internal
   * mapping.
   */
  public static ChunkRef remove(ChunkRef ref) {
    return remove(ref.x, ref.z);
  }

  //NOTE: This would be a good place to check for any erroneous ChunkRefs
  /**
   * Get every ChunkRef stored in the internal mapping.
   *
   * @return an ArrayList of every ChunkRef.
   */
  public static ArrayList<ChunkRef> getAll() {
    ArrayList<ChunkRef> valid = new ArrayList<>();
    allRefs.values().stream().forEach((firstLayer) -> {
      firstLayer.values().stream().forEach(valid::add);
    });
    return valid;
  }

  /**
   * Replace the contents of the internal mapping wit the contents of a
   * Collection of ChunkRefs. This had been when Object based IO was used, which
   * failed due to the nesting of lists causing a stack overflow.
   *
   * @param collection
   */
  public static void setAll(Collection<ChunkRef> collection) {
    // Clean up
    removeAll();

    // Add the new values
    for (ChunkRef ref : collection) {
      HashMap<Integer, ChunkRef> get = allRefs.get(ref.x);
      if (get == null) {
        get = new HashMap<>();
        allRefs.put(ref.x, get);
      }
      get.put(ref.z, ref);
    }
  }

  /**
   * Remove all ChunkRefs from the internal mapping and clear each ChunkRef's
   * notify and waiting lists to remove any lingering references to one another
   * to help with garbage collection.
   */
  public static void removeAll() {
    for (HashMap<Integer, ChunkRef> inner : allRefs.values()) {
      for (ChunkRef ref : inner.values()) {
        ref.notifyOnPostPopulate.clear();
        ref.waitingOn.clear();
      }
      inner.clear();
    }
    allRefs.clear();
  }

  public final int x;
  public final int z;
  final HashSet<ChunkRef> waitingOn = new HashSet<>();
  final HashSet<ChunkRef> notifyOnPostPopulate = new HashSet<>();

  private ChunkRef(int chunkX, int chunkZ) {
    this.x = chunkX;
    this.z = chunkZ;
  }

  private ChunkRef(PopulateChunkEvent.Post event) {
    this(event.chunkX, event.chunkZ);
  }

  private ChunkRef(Chunk chunk) {
    this(chunk.xPosition, chunk.zPosition);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ChunkRef) {
      ChunkRef other = (ChunkRef) obj;
      return this.x == other.x && this.z == other.z;
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 79 * hash + this.x;
    hash = 79 * hash + this.z;
    return hash;
  }

  /**
   * Returns a String representation of the ChunkRef formatted as "(chunkX, chunkZ)".
   * @return String representation of this ChunkRef.
   */
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    return builder.append("(").append(this.x).append(", ").append(this.z).append(")").toString();
  }

  /**
   * Similar to ChunkRef#toString but also contains information about the two internal lists.
   * @return full String representation of this ChunkRef.
   */
  public String toFullString() {
    StringBuilder builder = new StringBuilder();
    builder.append(this).append("waiting on: ");
    for (ChunkRef ref : this.waitingOn) {
      builder.append(ref).append(", ");
    }
    builder.append("to notify: ");
    for (ChunkRef ref : this.notifyOnPostPopulate) {
      builder.append(ref).append(", ");
    }
    return builder.toString();
  }

  /**
   * Check for any cyclic dependencies between ChunkRefs.
   * This may take a while as it checks every single ChunkRef, then looks through every ChunkRef it's waiting on and then checks if any of those are waiting on it.
   */
  public static boolean cyclicCheck() {
    boolean thereIsAnError = false;
    for (ChunkRef ref : getAll()) {
      for (ChunkRef ref2 : ref.waitingOn) {
        for (ChunkRef ref3 : ref2.waitingOn) {
          if (ref == ref3) {
            thereIsAnError = true;
            Log.error(ref.toString() + " and " + ref2.toString() + " are waiting for each other before their blocks will be replaced");
          }
        }
      }
    }
    return thereIsAnError;
  }
  
  public static boolean validateAll() {
    boolean valid = true;
    for (ChunkRef ref : ChunkRef.getAll()) {
      boolean validateWaiting = ChunkRef.validateWaiting(ref);
      boolean validateNotify = ChunkRef.validateNotify(ref);
      if (!validateWaiting || !validateNotify) {
        valid = false;
      }
    }
    return valid;
  }
  
  public static boolean validateWaiting(ChunkRef ref) {
    boolean valid = true;
    for (ChunkRef waitingOn : ref.waitingOn) {
      if (!waitingOn.notifyOnPostPopulate.contains(ref)) {
        valid = false;
        Log.error("!!!!! " + ref + " is waiting on " + waitingOn + ", but " + waitingOn + " isn't going to notify " + ref + "!");
      }
    }
    return valid;
  }
  
  public static boolean validateNotify(ChunkRef ref) {
    boolean valid = true;
    for (ChunkRef toNotify : ref.notifyOnPostPopulate) {
      if (!toNotify.waitingOn.contains(ref)) {
        valid = false;
        Log.error(ref + " is going to notify " + toNotify + ", but " + toNotify + " isn't waiting on " + ref + "!");
      }
    }
    return valid;
  }
  
  public static boolean validateEmpty(ChunkRef ref) {
    if (ref.notifyOnPostPopulate.isEmpty() && ref.waitingOn.isEmpty()) {
      Log.error(ref + " isn't waiting on any other chunk, nor is it going to notify any others!");
      return false;
    }
    return true;
  }
  
  public void setCurrentlyPopulating() {
    currentlyGenerating.add(this);
  }
  
  public void setFinishedPopulating() {
    currentlyGenerating.remove(this);
  }
  
  public static boolean isCurrentlyPopulating(int chunkX, int chunkZ) {
    return chunkRefExists(chunkX, chunkZ) && currentlyGenerating.contains(new ChunkRef(chunkX, chunkZ));
  }
  
  public static boolean chunkRefExists(int chunkX, int chunkZ) {
    HashMap<Integer, ChunkRef> get = allRefs.get(chunkX);
    if (get == null) {
      return false;
    }
    return get.get(chunkZ) != null;
  }

}
