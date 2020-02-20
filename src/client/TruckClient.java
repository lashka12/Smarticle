package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.LocalTime;
import java.util.Arrays;

import javafx.util.Pair;

public class TruckClient extends CarClient {

	/**
	 * pair of front cell coordinates: key = row & value = col
	 */
	private Pair<Integer, Integer> front;
	/**
	 * pair of middle cell coordinates: key = row & value = col
	 */
	private Pair<Integer, Integer> middle;
	/**
	 * pair of back cell coordinates: key = row & value = col
	 */
	private Pair<Integer, Integer> back;
	
	public TruckClient(Pair<Integer, Integer> front, Pair<Integer, Integer> middle, Pair<Integer, Integer> back) {
		super(front, back);
		this.front = front;
		this.middle = middle;
		this.back = back;
	}
	
	/**
	 * registration message:
	 * 			ID REG TRUCK FCL:x,y MCL:x,y BCL:x,y
	 */
	@Override
	protected boolean sendRegistration(DataInputStream is, DataOutputStream os) throws IOException{
		
		os.writeUTF(this.getCarId()+" "+"REG"+" "+"TRUCK"+" "+"FCL:"+this.getFront().getKey()+","+
	       this.getFront().getValue()+" "+"MCL:"+this.getMiddle().getKey()+","+this.getMiddle().getValue()+
	             " "+"BCL:"+this.getBack().getKey()+","+getBack().getValue());
		
		if( is.readUTF().split(" ")[0] == "NACK")
			return false;
		
		
		return true;

	}

	public Pair<Integer, Integer> getFront() {
		return front;
	}

	public void setFront(Pair<Integer, Integer> front) {
		this.front = front;
	}

	public Pair<Integer, Integer> getMiddle() {
		return middle;
	}

	public void setMiddle(Pair<Integer, Integer> middle) {
		this.middle = middle;
	}

	public Pair<Integer, Integer> getBack() {
		return back;
	}

	public void setBack(Pair<Integer, Integer> back) {
		this.back = back;
	}
	
}
