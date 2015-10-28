package uk.co.mysterymayhem.gmmodpacktweaks.tweaks.undergroundbiomesconstructs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
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
  
  public static final String WAITING_ON_KEY = "MWaitList";
  public static final String NOTIFY_KEY = "MNoteList";

  // Internal map for storing ChunkRefs
  private static final HashMap<Integer, HashMap<Integer, ChunkRef>> allRefs = new HashMap<>();
  
  private static final HashSet<ChunkRef> currentlyGenerating = new HashSet<>();
  private static final HashSet<ChunkRef> currentlyUnloading = new HashSet<>();

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
  
  public static ChunkRef get(ChunkCoordIntPair pair) {
    return get(pair.chunkXPos, pair.chunkZPos);
  }
  
  public static ChunkRef getIfExists(ChunkCoordIntPair pair) {
    return getIfExists(pair.chunkXPos, pair.chunkZPos);
  }
  
  /**
   * Get a chunk only if it exists
   * @param chunk
   * @return The ChunkRef or null if it doesn't exist
   */
  public static ChunkRef getIfExists(Chunk chunk) {
    return getIfExists(chunk.xPosition, chunk.zPosition);
  }
  /**
   * Get a chunk only if it exists
   * @param chunkX
   * @param chunkZ
   * @return The ChunkRef or null if it doesn't exist
   */
  public static ChunkRef getIfExists(int chunkX, int chunkZ) {
    HashMap<Integer, ChunkRef> get = allRefs.get(chunkX);
    if (get == null) {
      return null;
    }
    else {
      ChunkRef ref = get.get(chunkZ);
      if (ref == null) {
        return null;
      }
      else {
        return ref;
      }
    }
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
  final HashSet<ChunkCoordIntPair> waitingOn = new HashSet<>();
  final HashSet<ChunkCoordIntPair> notifyOnPostPopulate = new HashSet<>();
  //private final ChunkCoordIntPair

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
    for (ChunkCoordIntPair ref : this.waitingOn) {
      builder.append(ref).append(", ");
    }
    builder.append("to notify: ");
    for (ChunkCoordIntPair ref : this.notifyOnPostPopulate) {
      builder.append(ref).append(", ");
    }
    return builder.toString();
  }

  /**
   * Check for any cyclic dependencies between ChunkRefs.
   * This may take a while as it checks every single ChunkRef, then looks through every ChunkRef it's waiting on and then checks if any of those are waiting on it.
   * @return 
   */
  public static boolean cyclicCheck() {
    boolean thereIsAnError = false;
    for (ChunkRef ref : getAll()) {
      for (ChunkCoordIntPair pair2 : ref.waitingOn) {
        ChunkRef ref2 = ChunkRef.getIfExists(pair2);
        if (ref2 == null) {continue;}
        for (ChunkCoordIntPair pair3 : ref2.waitingOn) {
          ChunkRef ref3 = ChunkRef.getIfExists(pair3);
          if (ref == ref3) {
            thereIsAnError = true;
            Log.error(ref.toString() + " and " + ref2 + " are waiting for each other before their blocks will be replaced");
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
    for (ChunkCoordIntPair waitingOnPair : ref.waitingOn) {
      ChunkRef waitingOn = ChunkRef.getIfExists(waitingOnPair);
      if (waitingOn != null && !waitingOn.notifyOnPostPopulate.contains(ref.toIntPair())) {
        valid = false;
        Log.error("!!!!! " + ref + " is waiting on " + waitingOn + ", but " + waitingOn + " isn't going to notify " + ref + "!");
      }
    }
    return valid;
  }
  
  public static boolean validateNotify(ChunkRef ref) {
    boolean valid = true;
    for (ChunkCoordIntPair toNotifyPair : ref.notifyOnPostPopulate) {
      ChunkRef toNotify = ChunkRef.getIfExists(toNotifyPair);
      if (toNotify != null && !toNotify.waitingOn.contains(ref.toIntPair())) {
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
  
  public void saveToChunkData(NBTTagCompound nbt) {
    // If any data exists already, we can safely replace it all as the ChunkRefs contain the most up to date data.
    int[] notifyList = new int[this.notifyOnPostPopulate.size() * 2];
    int index = 0;
    for (ChunkCoordIntPair otherPair : this.notifyOnPostPopulate) {
      notifyList[index++] = otherPair.chunkXPos;
      notifyList[index++] = otherPair.chunkZPos;
    }
    if (notifyList.length != 0) {
      nbt.setIntArray(NOTIFY_KEY, notifyList);
    }
    else {
      nbt.removeTag(NOTIFY_KEY);
    }
    
    int[] waitingOnList = new int[this.waitingOn.size() * 2];
    index = 0;
    for (ChunkCoordIntPair otherPair : this.waitingOn) {
      waitingOnList[index++] = otherPair.chunkXPos;
      waitingOnList[index++] = otherPair.chunkZPos;
    }
    if (waitingOnList.length != 0) {
      nbt.setIntArray(WAITING_ON_KEY, waitingOnList);
    }
    else {
      nbt.removeTag(WAITING_ON_KEY);
    }
       
    ChunkRef.remove(this);
    //DEBUG
    Log.log("Removed " + this + " as data is beign saved to chunk " + nbt);
  }
  
  public void loadFromChunkData(NBTTagCompound nbt, World world) {
    // Important to merge loaded data with any existing data.
    // Another chunk could have loaded and decided it's waiting on this chunk while this chunk wasn't loaded
    int[] notifyList = nbt.getIntArray(NOTIFY_KEY);
    if (notifyList != null) {
      for (int i = 0; i < notifyList.length; i += 2) {
        this.notifyOnPostPopulate.add(ChunkRef.get(notifyList[i], notifyList[i + 1]).toIntPair());
      }
    }
    
    HashSet<ChunkRef> waitingCopy = (HashSet<ChunkRef>)this.waitingOn.clone();
       
    int[] waitingList = nbt.getIntArray(WAITING_ON_KEY);
    if (waitingList != null) {
      for (int i = 0; i < waitingList.length; i += 2) {
        this.waitingOn.add(ChunkRef.get(waitingList[i], waitingList[i + 1]).toIntPair());
      }
    }
    this.waitingOn.removeAll(waitingCopy);
    // Replace on next tick to avoid chunks that haven't fully loaded
    ChunkProcessor.get(world).replaceBlocksIfNotWaitingNextTick(this);
    Log.debug("Loaded " + this + " from nbt " + nbt);
    Log.debug("WaitingOn list is now " + this.waitingOn);
  }
  
  public ChunkCoordIntPair toIntPair() {
    return new ChunkCoordIntPair(x, z);
  }

}
