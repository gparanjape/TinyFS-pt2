package UnitTests;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.chunkserver.ChunkServer;
import com.client.Client;

/**
 * A utility used by the UnitTests
 * @author Shahram Ghandeharizaden
 *
 */

public class TestReadAndWrite {
	
	//public static ChunkServer cs = new ChunkServer();
	public static Client client = new Client(5963, "localhost");
	int CHUNK_SIZE = 4096;
	
	/**
	 * Create and write chunk(s) of a physical file.
	 * The default full chunk size is 4K. Note that the last chunk of the file may not have the size 4K.
	 * The sequence of chunk handles are returned, which are stored in a static map.
	 */
	public String[] createFile(File f) {
		try {
			RandomAccessFile raf = new RandomAccessFile(f.getAbsolutePath(), "rw");
			raf.seek(0);
			long size = f.length();
			int num = (int)Math.ceil((double)size / CHUNK_SIZE);
			String[] ChunkHandles = new String[num];
			String handle = null;
			byte[] chunkArr = new byte[CHUNK_SIZE];
			for(int i = 0; i < num; i++){
				handle = client.initializeChunk();
				ChunkHandles[i] = handle;
				raf.read(chunkArr, 0, CHUNK_SIZE);
				boolean isWritten = client.putChunk(handle, chunkArr, 0);
				if(isWritten == false){
					throw new IOException("Cannot write a chunk to the chunk server!");
				}
			}
			raf.close();
			client.close();
			return ChunkHandles;
		} catch (IOException ie){
			return null;
		}
	}

}
