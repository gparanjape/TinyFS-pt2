package com.interfaces;

/**
 * Interfaces of the TinyFS Client 
 * @author Shahram Ghandeharizadeh
 *
 */
public interface ClientInterface {
	
	
	
	/**
	 * Return the chunkhandle for a newly created chunk.
	 */
	public String initializeChunk();
	
	/**
	 * Write the byte array payload to the ChunkHandle at the specified offset.
	 */
	public boolean putChunk(String ChunkHandle, byte[] payload, int offset);
	
	/**
	 * Read the specified NumberOfBytes from the target chunk starting at the specified offset.
	 * Return the retrieved number of bytes as a byte array.
	 */
	public byte[] getChunk(String ChunkHandle, int offset, int NumberOfBytes);	
	
}
