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
			System.out.println("You have connected");
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Create a chunk at the chunk server from the client side.
	 */
	public String initializeChunk() {
		String chunkHandle = "";
		try {
			System.out.println("init 0");
			dos.writeInt(101);
			dos.flush();
			System.out.println("init 1");
			int payloadLength = dis.readInt();
			System.out.println("init 2");
			byte[] chunkHandleAsByteArray = new byte[payloadLength];
			dis.read(chunkHandleAsByteArray, 0, payloadLength);
			System.out.println("init 3");
			chunkHandle = new String(chunkHandleAsByteArray);
		} catch (IOException e) {
			System.out.println("io exception in init chunk");
			e.printStackTrace();
		}
		System.out.println("ChunkHandle: " + chunkHandle);
		return chunkHandle;
	}
	
	/**
	 * Write a chunk at the chunk server from the client side.
	 */
	public boolean putChunk(String ChunkHandle, byte[] payload, int offset) {
		if(offset + payload.length > ChunkServer.ChunkSize){
			System.out.println("The chunk write should be within the range of the file, invalide chunk write!");
			return false;
		}
		try {			
			dos.writeInt(102);//putchunk command
			System.out.println("put: wrote cmd = 102");
			dos.flush();
			
			int payloadSize = payload.length;
			System.out.println("payloadSize: "+ payloadSize);
			dos.writeInt(payloadSize);
			System.out.println("put: wrote payloadSize");
			dos.flush();
			
			dos.write(payload);
			System.out.println("put: wrote payload");
			dos.flush();
			
			//handle size
			int chunkHandleSize = ChunkHandle.length();
			dos.writeInt(chunkHandleSize);
			System.out.println("put: wrote chunkhandlesize");
			dos.flush();
			
			//handle
			byte[] chunkHandleAsBytes = ChunkHandle.getBytes();
			dos.write(chunkHandleAsBytes, 0, chunkHandleSize);
			System.out.println("put: wrote chunkhandle");
			dos.flush();
			
			//offset
			dos.writeInt(offset);
			System.out.println("put: wrote offset");
			dos.flush();
			
			int passed = dis.readInt();
			System.out.println("put: read passed");
			//System.out.println("put 7");
			//int passed = 1;
			if(passed == 1) {
				return true;
			}
			else {
				return false;
			}
			
		} catch (IOException e) {
			System.out.println("io exception in putchunk");
			e.printStackTrace();
			return false;
		}
		
		//return cs.putChunk(ChunkHandle, payload, offset);
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
			System.out.println("get: wrote cmd = 103");
			dos.flush();
			
			int chunkHandleLength = ChunkHandle.length();
			System.out.println("get: wrote handle length");
			dos.writeInt(chunkHandleLength);
			dos.flush();
			
			byte[] chunkHandleAsBytes = ChunkHandle.getBytes();
			dos.write(chunkHandleAsBytes, 0, chunkHandleLength);
			System.out.println("get: wrote chunk handle");
			dos.flush();
			
			dos.writeInt(offset);
			System.out.println("get: wrote offset");
			dos.flush();
			
			dos.writeInt(NumberOfBytes);
			System.out.println("get: wrote numberofbytes");
			dos.flush();
			
			int payloadLength = dis.readInt();
			byte[] payload = new byte[payloadLength];
			dis.readFully(payload, 0, payloadLength);
			System.out.println("get: wrote read payload");
			
			return payload;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		byte[] tmp = null;
		return tmp;
		//return cs.getChunk(ChunkHandle, offset, NumberOfBytes);
	}

	
public void close() {
	try {
		if(clientSocket != null)
			clientSocket.close();
	} catch (IOException e) {
		System.out.println("Ran into an ioexception in client close" );
		e.printStackTrace();
	}
}

}
