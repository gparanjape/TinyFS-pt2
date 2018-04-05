package com.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import com.chunkserver.ChunkServer;
import com.interfaces.ClientInterface;

/**
 * implementation of interfaces at the client side
 * @author Shahram Ghandeharizadeh
 *
 */
public class Client implements ClientInterface {
	//public static ChunkServer cs = new ChunkServer();
	static Socket clientSocket;
	public DataOutputStream dos;
	public DataInputStream dis;
	
	/**
	 * Initialize the client
	 */
	public Client(int port, String hostname){
	//public Client(int port, InetAddress address) {
		try {
			clientSocket = new Socket(hostname, port);
			//clientSocket = new Socket(address, port);
			dos = new DataOutputStream(clientSocket.getOutputStream());
			dis = new DataInputStream(clientSocket.getInputStream());
		} catch (UnknownHostException e) {
			//e.printStackTrace();
		} catch (IOException e) {
			//e.printStackTrace();
		}
	}
	
	/**
	 * Create a chunk at the chunk server from the client side.
	 */
	public String initializeChunk() {
		String chunkHandle = "";
		try {
			dos.writeInt(101);
			dos.flush();
			int payloadLength = dis.readInt();
			byte[] chunkHandleAsByteArray = new byte[payloadLength];
			dis.read(chunkHandleAsByteArray, 0, payloadLength);
			chunkHandle = new String(chunkHandleAsByteArray);
		} catch (IOException e) {
			//e.printStackTrace();
		}
		return chunkHandle;
	}
	
	/**
	 * Write a chunk at the chunk server from the client side.
	 */
	public boolean putChunk(String ChunkHandle, byte[] payload, int offset) {
		if(offset + payload.length > ChunkServer.ChunkSize){
			//System.out.println("The chunk write should be within the range of the file, invalide chunk write!");
			return false;
		}
		try {			
			dos.writeInt(102);//putchunk command
			dos.flush();
			
			int payloadSize = payload.length;
			dos.writeInt(payloadSize);
			dos.flush();
			
			dos.write(payload);
			dos.flush();
			
			//handle size
			int chunkHandleSize = ChunkHandle.length();
			dos.writeInt(chunkHandleSize);
			dos.flush();
			
			//handle
			byte[] chunkHandleAsBytes = ChunkHandle.getBytes();
			dos.write(chunkHandleAsBytes, 0, chunkHandleSize);
			dos.flush();
			
			//offset
			dos.writeInt(offset);
			dos.flush();
			
			int passed = dis.readInt();
			if(passed == 1) {
				return true;
			}
			else {
				return false;
			}
			
		} catch (IOException e) {
			//e.printStackTrace();
			return false;
		}
		
	}
	
	/**
	 * Read a chunk at the chunk server from the client side.
	 */
	public byte[] getChunk(String ChunkHandle, int offset, int NumberOfBytes) {
		if(NumberOfBytes + offset > ChunkServer.ChunkSize){
			System.out.println("The chunk read should be within the range of the file, invalide chunk read!");
			return null;
		}
		try {
			dos.writeInt(103);//getchunk command
			dos.flush();
			
			int chunkHandleLength = ChunkHandle.length();
			dos.writeInt(chunkHandleLength);
			dos.flush();
			
			byte[] chunkHandleAsBytes = ChunkHandle.getBytes();
			dos.write(chunkHandleAsBytes, 0, chunkHandleLength);
			dos.flush();
			
			dos.writeInt(offset);
			dos.flush();
			
			dos.writeInt(NumberOfBytes);
			dos.flush();
			
			int payloadLength = dis.readInt();
			byte[] payload = new byte[payloadLength];
			dis.readFully(payload, 0, payloadLength);
			
			return payload;
			
		} catch (IOException e) {
			//e.printStackTrace();
		}
		return null;
	}

	
public void close() {
	try {
		if(clientSocket != null)
			clientSocket.close();
	} catch (IOException e) {
		//e.printStackTrace();
	}
}

}
