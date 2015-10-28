/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.mysterymayhem.gmmodpacktweaks.tweaks.undergroundbiomesconstructs;

import java.util.ArrayList;
import java.util.HashMap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import uk.co.mysterymayhem.gmmodpacktweaks.util.Log;

/**
 *
 * @author Thomas
 */
public class OphanedChunkRefData extends WorldSavedData {
  
  public static final String UNIQUE_IDENTIFIER = "m_orphaned_chunkref_data";
  
  private static final String COORDINATES_IDENTIFIER = "c";
  // Only very rarely should there be waiting lists as this indicates notifying an unloaded chunk
  private static final String WAITING_LIST_IDENTIFIER = "w";
  private static final String NOTIFY_LIST_IDENTIFIER = "n";
  private static final String MAIN_LIST_IDENTIFIER = "l";
  private static final byte NBTTagCompound_ID = 10;
  private ArrayList<ChunkRef> referencesToNonExistantChunks = new ArrayList<>();

  public OphanedChunkRefData() {
    super(UNIQUE_IDENTIFIER);
  }

  public OphanedChunkRefData(String ignored) {
    this();
  }

  @Override
  public void readFromNBT(NBTTagCompound nbt) {
    Log.log("Reading NBT from WorldSavedData");
    NBTTagList tagList = nbt.getTagList(MAIN_LIST_IDENTIFIER, NBTTagCompound_ID);
    if (tagList == null) {
      return;
    }
    Log.log("Found nbt: " + tagList);
    int tagCount = tagList.tagCount();
    ArrayList<ChunkRef> refs = new ArrayList<>(tagCount);
    
    for (int i = 0; i < tagCount; i++) {
      NBTTagCompound compoundTag = tagList.getCompoundTagAt(i);
      // MUST exist, no need for null check
      int[] intArray = compoundTag.getIntArray(COORDINATES_IDENTIFIER);
      refs.add(ChunkRef.get(intArray[0], intArray[1]));
    }
    
    for (int i = 0; i < tagCount; i++) {
      NBTTagCompound compoundTag = tagList.getCompoundTagAt(i);
      int[] waitingRefIDs = compoundTag.getIntArray(WAITING_LIST_IDENTIFIER);
      if (waitingRefIDs != null) {
        for (int refID : waitingRefIDs) {
          refs.get(i).waitingOn.add(refs.get(refID).toIntPair());
        }
      }
      
      int[] notifyRefIDs = compoundTag.getIntArray(NOTIFY_LIST_IDENTIFIER);
      if (notifyRefIDs != null) {
        for (int refID : notifyRefIDs) {
          refs.get(i).notifyOnPostPopulate.add(refs.get(refID).toIntPair());
        }
      }
    }
    Log.log("Read NBT from WorldSavedData");
  }

  @Override
  public void writeToNBT(NBTTagCompound nbt) {
    Log.log("Writing to WorldSavedData NBT");
    NBTTagList mainList = new NBTTagList();
    
    // Set up reverse lookup
    HashMap<ChunkRef, Integer> reverseLookup = new HashMap<>();
    for (int index = 0; index < referencesToNonExistantChunks.size(); index++) {
      reverseLookup.put(referencesToNonExistantChunks.get(index), index);
    }
    ArrayList<ChunkRef> existantChunks = new ArrayList<>();
    
    for (ChunkRef ref : referencesToNonExistantChunks) {
      NBTTagCompound mainTag = new NBTTagCompound();
      
      mainTag.setIntArray(COORDINATES_IDENTIFIER, new int[]{ref.x, ref.z});
      
      int[] waitingListRef = new int[ref.waitingOn.size()];
      int index = 0;
      for (ChunkCoordIntPair otherRefPair : ref.waitingOn) {
        ChunkRef otherRef = ChunkRef.get(otherRefPair);
        // Should never throw an NPE as a non-existant chunk won't have attempted to have its blocks replaced
        // so won't know what chunks it's waiting on
        waitingListRef[index++] = reverseLookup.get(otherRef);
      }
      
      int[] notifyListRef = new int[ref.notifyOnPostPopulate.size()];
      index = 0;
      for (ChunkCoordIntPair otherRefPair : ref.notifyOnPostPopulate) {
        ChunkRef otherRef = ChunkRef.get(otherRefPair);
        Integer get = reverseLookup.get(otherRef);
        if (get == null) {
          // Non-existant chunk intends to notify an existant chunk
          // Need to do somethign about this
          // On load, chunks combine their notify lists with what's know about them
          // and remove from their loaded data the contents of their waitingOnList (and replace their blocks if this is now empty)
          // Create 'fake' ChunkRef with only x and z pos data?
          // Use actual existant ChunkRef, but onyl store x and z pos data?
          
          // Size will be index of newly added element
          get = reverseLookup.size();
          // Add it to the map
          reverseLookup.put(otherRef, get);
          // Add the reference to the existant chunk to its own list that will have its data appended later
          existantChunks.add(otherRef);
        }
        notifyListRef[index++] = reverseLookup.get(otherRef);
      }
      
      if (waitingListRef.length != 0) {
        mainTag.setIntArray(WAITING_LIST_IDENTIFIER, waitingListRef);
      }
      if (notifyListRef.length != 0) {
        mainTag.setIntArray(NOTIFY_LIST_IDENTIFIER, notifyListRef);
      }
      
      mainList.appendTag(mainTag);
    }
    
    // Add all the lately acquired references to existant chunks
    for (ChunkRef existantChunk : existantChunks) {
      NBTTagCompound mainTag = new NBTTagCompound();
      mainTag.setIntArray(COORDINATES_IDENTIFIER, new int[]{existantChunk.x, existantChunk.z});
      mainList.appendTag(mainTag);
    }

    nbt.setTag(MAIN_LIST_IDENTIFIER, mainList);
    Log.log("Wrote to WorldSavedData NBT: " + mainList);
  }
  
  public void setData(ArrayList<ChunkRef> referencesToNonExistantChunks) {
    Log.log("Setting data for OrphanedChunkRefData");
    this.referencesToNonExistantChunks = referencesToNonExistantChunks;
    this.setDirty(true);
    Log.log("Set data for OrphanedChunkRefData");
  }
  
  public void saveToNBT(World world) {
    Log.log("Saving NBT data to world");
    world.perWorldStorage.setData(UNIQUE_IDENTIFIER, null);
  }
  
  // Not needed?
  private void loadFromNBT(World world) {
    
  }
  
}
