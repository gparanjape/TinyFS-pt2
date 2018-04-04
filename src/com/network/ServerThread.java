package com.network;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;

import com.chunkserver.ChunkServer;

public class ServerThread extends Thread implements Serializable{
	 
	private static final long serialVersionUID = 4444275984103976325L;
	
	private ObjectInputStream ois;
	private ObjectOutputStream oos;
	private ChunkServer cs;

	public ServerThread(Socket s, ChunkServer cs) {
		try {
			this.cs = cs;
			ois = new ObjectInputStream(s.getInputStream());
			oos = new ObjectOutputStream(s.getOutputStream());
			this.start();
		} catch (IOException ioe) {
			System.out.println("ioe: " + ioe.getMessage());
		}
	}
	
	public void sendMessage(ChunkPacket packet) {
		try {
			oos.writeObject(packet);
			oos.flush();
		} catch (IOException ioe) {
			System.out.println("ioe: " + ioe.getMessage());
		}
	}
	
	public void run() {
		try {
			while(true) {
				ChunkPacket message = (ChunkPacket)ois.readObject();
				//System.out.println("Received message: " + message.getMessage());
				if(message.getName().equals("initializeChunk")) {
					String chunk = cs.initializeChunk();
					message.setChunkHandle(chunk);
					sendMessage(message);
				}
				else if(message.getName().equals("putChunk")){
					boolean success = cs.putChunk(message.getChunkHandle(), message.getPayload(), message.getOffset());
					if(success) {
						message.setName("true");
					}
					else {
						message.setName("false");
					}
					sendMessage(message);
				}
				else if(message.getName().equals("getChunk")) {
					byte[] bytes = cs.getChunk(message.getChunkHandle(), message.getOffset(), message.getNumberOfBytes());
					message.setPayload(bytes);
					sendMessage(message);
				}
			}
		} catch (ClassNotFoundException cnfe) {
			System.out.println("cnfe in run: " + cnfe.getMessage());
		} catch (IOException ioe) {
			System.out.println("ioe in run: " + ioe.getMessage());
		}
	}
}	
	