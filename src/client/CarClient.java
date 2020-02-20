package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Random;

import javafx.util.Pair;
import server.Road;
import server.Server;
import util.Constants;
import util.E_Direction;

public class CarClient extends Thread {
	
	/**
	 * helper variable
	 */
	private static volatile int carsCounter;
	
	/**
	 * car's id
	 */
	private int id;
	
	/**
	 * car status
	 */
	private boolean isFinished;
	
	/**
	 * pair of front cell coordinates: key = row & value = col
	 */
	private Pair<Integer, Integer> front;
	
	/**
	 * pair of back cell coordinates: key = row & value = col
	 */
	private Pair<Integer, Integer> back;
	
	/**
	 * car's direction
	 */
	private E_Direction direction;

	public CarClient(Pair<Integer, Integer> front, Pair<Integer, Integer> back) {
		this.id = ++carsCounter;
		this.front = front;
		this.back = back;
		isFinished = false;
	}

	protected int getCarId() {
		return this.id;
	}
	
	protected void setDirection(E_Direction direction) {
		this.direction = direction;
	}
	

	public long getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public boolean isFinished() {
		return isFinished;
	}

	public void setFinished(boolean isFinished) {
		this.isFinished = isFinished;
	}

	public Pair<Integer, Integer> getFront() {
		return front;
	}

	public void setFront(Pair<Integer, Integer> front) {
		this.front = front;
	}

	public Pair<Integer, Integer> getBack() {
		return back;
	}

	public void setBack(Pair<Integer, Integer> back) {
		this.back = back;
	}

	public E_Direction getDirection() {
		return direction;
	}
	
	@Override
	public String toString() {
		return "CarClient [id=" + id + ", isFinished=" + isFinished + ", front=" + front + ", back=" + back
				+ ", direction=" + direction + "]";
	}
	
	
	/**
	 * 1. Register this entity at server
	 * 2. Send ready msg and update current direction
	 * 3. While this entity on the road:
	 * 		Move entity
	 * 4. Send finish
	 */
	
	public void run() {

		
		
		try {
				
			
				Socket client_Scoket = new Socket(""+Constants.LOCALHOST, Constants.PORT);
				DataInputStream dis = new DataInputStream(client_Scoket.getInputStream());
				DataOutputStream dos = new DataOutputStream(client_Scoket.getOutputStream());
			
			    sendRegistration(dis, dos); // Register this entity at server
				
			    direction = this.sendReady(dis, dos);  // Send ready msg and update current direction
			   
		        if(direction!=null)
					 this.setDirection(this.direction);
				
		       
		       
				while(!this.isFinished)      // While this entity on the road:
					this.sendMove(dis, dos);   //Move entity
			
				
				
			    dos.writeUTF(this.getId()+"FIN");  // stop the car and update the server with FIN massage
			
			
		
		}
		
		catch (IOException e) {
			e.printStackTrace();
			System.out.println("Client Socket Problem !");
		}
		
	}
	
	/**
	 * registration message to send:
	 * 			ID REG CAR FCL:x,y BCL:x,y
	 * @param is
	 * @param os
	 * @return
	 */
	protected boolean sendRegistration(DataInputStream is, DataOutputStream os) throws IOException{
		
		
		os.writeUTF(this.getCarId()+" "+"REG"+" "+"CAR"+" "+"FCL:"
		        +getFront().getKey()+","+getFront().getValue()+" "+"BCL:"+getBack().getKey()+","+getBack().getValue());
		
		String[] serverMsg = is.readUTF().split(" ");
		
		if(serverMsg[0] == "ACK")return true;
		
		  else 
			   if(serverMsg[0] == "NACK") 
				return false;
			    
		
		return false;

	}
	
	

	/**
	 * ready message to send:
	 * 			ID RDY
	 * @param is
	 * @param os
	 * @return
	 */
	protected E_Direction sendReady(DataInputStream is, DataOutputStream os) throws IOException {
		
		
		os.writeUTF(this.id+" "+"RDY");
		
		String[] serverMsg = is.readUTF().split(" ");
		
		if(serverMsg[0].equals("ACK"))
			
	    return E_Direction.valueOf(serverMsg[2].substring(serverMsg[2].indexOf(":")+1));
						
		
		return null;
		
	}
	
	/**
	 * move message to send:
	 * 			ID MOV DIR:direction
	 * @param is
	 * @param os
	 * @return
	 */
	
	protected E_Direction sendMove(DataInputStream is, DataOutputStream os) throws IOException {
		
		
		os.writeUTF(this.id+" "+"MOV"+" "+"DIR:"+this.direction);
		
		String[] serverMsg = is.readUTF().split(" ");
	
		
		if(serverMsg[0].equals("NACK")) return null;
		
		else {
			
			if(serverMsg[2] == "YES") return this.direction;
			
			else {
				
				if(serverMsg[2] == "NO") {
				
	
			    	if(serverMsg[3].contains(",")) {
			    		
			        	E_Direction first = E_Direction.valueOf(serverMsg[3].substring(serverMsg[3].indexOf(":")+1, serverMsg[3].indexOf(",")));
			        	E_Direction second = E_Direction.valueOf(serverMsg[3].substring(serverMsg[3].indexOf(",")+1));
			        	
			        	Random rand = new Random();
			        	E_Direction random_Of_Two_Directions = ((rand.nextInt(10)+1)>5 ? first : second);
			        	setDirection(random_Of_Two_Directions);
			    		return random_Of_Two_Directions;

			    	}
			    	
			    	else {
			    		
			    		E_Direction direction = E_Direction.valueOf(serverMsg[3].substring(serverMsg[3].indexOf(":")+1));
			    		setDirection(direction);
			    		return direction;
			    	}
				}
			}	
		}
		
		return null;

	}
	
}
