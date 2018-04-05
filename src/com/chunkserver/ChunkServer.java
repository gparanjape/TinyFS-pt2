package com.chunkserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.net.*;
//import java.util.Arrays;
import com.client.Client;
import com.interfaces.ChunkServerInterface;

/**
 * implementation of interfaces at the chunkserver side
 * @author Shahram Ghandeharizadeh
 *
 */

public class ChunkServer implements ChunkServerInterface {
	final static String filePath = "C:\\Users\\Gautam\\Desktop\\TinyFS-pt2\\csci485\\";
	public static long counter;
	static ChunkServer cs;// = new ChunkServer();
	static ServerSocket ss;
	
	/**
	 * Initialize the chunk server
	 */
	public ChunkServer(int port){
		File dir = new File(filePath);
		File[] fs = dir.listFiles();

		if(fs.length == 0){
			counter = 0;
		}else{
			long[] cntrs = new long[fs.length];
			for (int j=0; j < cntrs.length; j++)
				cntrs[j] = Long.valueOf( fs[j].getName() ); 
			
			Arrays.sort(cntrs);
			counter = cntrs[cntrs.length - 1];
		}
		try {
			ss = new ServerSocket(port);
			//System.out.println("connecting...");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Each chunk is corresponding to a file.
	 * Return the chunk handle of the last chunk in the file.
	 */
	public String initializeChunk() {
		counter++;
		return String.valueOf(counter);
	}
	
	/**
	 * Write the byte array to the chunk at the offset
	 * The byte array size should be no greater than 4KB
	 */
	public boolean putChunk(String ChunkHandle, byte[] payload, int offset) {
		try {
			//If the file corresponding to ChunkHandle does not exist then create it before writing into it
			RandomAccessFile raf = new RandomAccessFile(filePath + ChunkHandle, "rw");
			raf.seek(offset);
			raf.write(payload, 0, payload.length);
			raf.close();
			return true;
		} catch (IOException ex) {
			ex.printStackTrace();
			return false;
		}
	}
	
		/**
		 * read the chunk at the specific offset
		 */
	public byte[] getChunk(String ChunkHandle, int offset, int NumberOfBytes) {
		try {
			//If the file for the chunk does not exist the return null
			boolean exists = (new File(filePath + ChunkHandle)).exists();
			if (exists == false) return null;
			
			//File for the chunk exists then go ahead and read it
			byte[] data = new byte[NumberOfBytes];
			RandomAccessFile raf = new RandomAccessFile(filePath + ChunkHandle, "rw");
			raf.seek(offset);
			raf.read(data, 0, NumberOfBytes);
			raf.close();
			return data;
		} catch (IOException ex){
			ex.printStackTrace();
			return null;
		}
	}


	public static void ReadAndWrite(){
		DataInputStream dis;
		DataOutputStream dos;
		while(true)
		{
			Socket clientConnect = null;
			try {
				clientConnect = ss.accept();
			}catch(IOException ioe) {
			}
				while(!clientConnect.isClosed()){
				try {
					dis = new DataInputStream(clientConnect.getInputStream());
					dos = new DataOutputStream(clientConnect.getOutputStream());

					int cmd = dis.readInt();
					if(cmd == 102) {//putchunk
						int payloadSize = dis.readInt();
						byte[] payload = null;
						payload = new byte[payloadSize];
						dis.readFully(payload, 0, payloadSize);
						int handleLength = dis.readInt();
						byte[] chunkHandleBytes = new byte[handleLength];
						dis.read(chunkHandleBytes, 0, handleLength);
						String chunkHandle = new String(chunkHandleBytes);
						int offset = dis.readInt();
						boolean pass = cs.putChunk(chunkHandle, payload, offset);
						if(pass == true) {
							dos.flush();
							dos.writeInt(1);
							dos.flush();
						}
						else {
							dos.writeInt(0);
							dos.flush();
						}
					}
					else if(cmd == 103) {//getchunk
						int handleLength = dis.readInt();
						byte[] chunkHandleBytes = new byte[handleLength];
						dis.read(chunkHandleBytes, 0, handleLength);
						String chunkHandle = new String(chunkHandleBytes);
						
						int offset = dis.readInt();
						int numberOfBytes = dis.readInt();
						
						byte[] pl = cs.getChunk(chunkHandle, offset, numberOfBytes);
						int payloadLength = pl.length;
						
						dos.writeInt(payloadLength);
						dos.flush();
						dos.write(pl);
						dos.flush();
					}
					else if(cmd == 101){//initializechunk
						String chunkVal = cs.initializeChunk();
						byte[] chunkValAsByteArray = chunkVal.getBytes();
						int arrayLen = chunkValAsByteArray.length;
						dos.writeInt(arrayLen);
						dos.flush();
						dos.write(chunkValAsByteArray);
						dos.flush();
					}
				}catch (IOException e) {
				break;
			}
		}
	}		
	}
		
	public static void main(String[] args){
		cs = new ChunkServer(5963);
		ReadAndWrite();
	}

}
