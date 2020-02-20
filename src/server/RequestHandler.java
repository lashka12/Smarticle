package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import util.Constants;
import util.E_Direction;



public class RequestHandler extends Thread {

	private Socket socket;

	private Car entity;

	public RequestHandler(Socket socket) {
		this.socket = socket;
	}

	
	public void setSocket(Socket socket) {
		this.socket = socket;
	}
	
	
	public Car getEntity() {
		return entity;
	}


	public void setEntity(Car entity) {
		this.entity = entity;
	}


	/**
	 * For each message you get - call to relevant method
	 * When you get FIN message - terminate the loop 
	 */
	@Override
	public void run() {
	
		boolean finMsg = false;

        
  
		try {
			
		    DataInputStream is = new DataInputStream(socket.getInputStream());
            DataOutputStream os = new DataOutputStream(socket.getOutputStream()); 
			
			while (!finMsg) {
				
			String[] client_Msg = is.readUTF().split(" ");
				
			switch (client_Msg[1]) {
			
				case "REG":
					handleRegister(client_Msg, os);
					break;
					
				case "RDY":
					handleReady(client_Msg, os);
					break;
					
				case "MOV":
					handleMove(client_Msg, os);
					break;
					
					 
				case "FIN":
					socket.close();
					finMsg=true;
					break;
			       
			
				}
		
		    
			}
				
		}catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * registration answer to send:
	 * 			ACK/NACK REG
	 * @param msg
	 * @param os
	 */
	private void handleRegister(String[] msg, DataOutputStream os) throws IOException {
		
		
		
		Car car = null;
		
	    	RoadPart fronCell = Road.getInstance().road[Integer.parseInt(msg[3].substring(msg[3].indexOf(":")+1, msg[3].indexOf(",")))][Integer.parseInt(msg[3].substring(msg[3].indexOf(",")+1))];
	    	RoadPart backCell= Road.getInstance().road[Integer.parseInt(msg[4].substring(msg[4].indexOf(":")+1, msg[4].indexOf(",")))][Integer.parseInt(msg[4].substring(msg[4].indexOf(",")+1))];
	    	
		    if(msg[2].equals("CAR")) {
		    	car = new Car(Integer.parseInt(msg[0]),fronCell, backCell);
		    	
		    }
		    
		    
		    if (msg[2].equals("TRUCK")) {
		  
		    	RoadPart middleCell = Road.getInstance().road[Integer.parseInt(msg[4].substring(msg[4].indexOf(":")+1, msg[4].indexOf(",")))][Integer.parseInt(msg[4].substring(msg[4].indexOf(",")+1))];
		    
		    	car = new Truck(Integer.parseInt(msg[0]), fronCell, middleCell, backCell);
		    
			}
		    
		
		
		if(car == null) 
			
			os.writeUTF("NACK"+" "+"REG");
		
		
		else {
			
			
			os.writeUTF("ACK"+" "+"REG");
			setEntity(car);
	    	Road.getInstance().addCar(car);
	    	
		}  
	}
	
	/**
	 * ready answer to send:
	 * 			ACK/NACK RDY CDR:currDirection PDR:possibleDirections
	 * @param msg
	 * @param os
	 */
	private void handleReady(String[] msg, DataOutputStream os) throws IOException {
		

		if(msg[0] != null) {
			
			E_Direction newDirection =  RoadPart.getDirectionBetweenTwoParts(entity.getFrontCell(), entity.getBackCell());
		    os.writeUTF("ACK"+" "+"RDY"+" "+"CDR:"+newDirection.toString()+" "+"PDR:"+getPossibleDirections());
		    
		}
		
		else 
			os.writeUTF("NACK"+" "+"RDY");
		
	}

	/**
	 * move answer to send:
	 * 			ACK/NACK MOV YES
	 * 			ACK/NACK MOV NO PDR:possibleDirections
	 * @param msg
	 * @param os
	 */
	private void handleMove(String[] msg, DataOutputStream os) throws IOException {

		
	    E_Direction direction = E_Direction.valueOf(msg[2].substring(msg[2].indexOf(":")+1));
		
	    if(direction == null) 
		   os.writeUTF("NACK"+" "+"MOV"+" "+"NO");    
	    
	    else {
	    	
		    entity.moveCar(direction);
		    
	     	if(entity.isFinished())os.writeUTF("ACK"+" "+"MOV"+" "+"NO");
	      	
		    else  os.writeUTF("ACK"+" "+"MOV"+" "+"YES"+" "+"PDR:"+getPossibleDirections());
		    
	        }
	    
		
	}
	
	/**
	 * Get all possible directions of entity front cell, and return them as String, separated by ','
	 * @return
	 */
	
	private String getPossibleDirections() {
		
		String directionsToReturn="";
		
		if(entity.getFrontCell() != null) {
			
		HashSet<E_Direction> possibleDirections = entity.getFrontCell().getPossibleDirections();
		int size=possibleDirections.size();
		
		E_Direction[] directions = new E_Direction[size];
		directions = possibleDirections.toArray(directions);
		
		if(size > 1) directionsToReturn = directions[0].toString()+","+directions[1].toString();   
	
		else
             directionsToReturn = directions[0].toString();
	
		}
		
		
		return directionsToReturn;  // the directons text to string
		
	}
	
	
	

}
