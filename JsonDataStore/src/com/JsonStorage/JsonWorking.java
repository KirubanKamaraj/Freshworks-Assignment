package com.JsonStorage;
import java.util.Scanner;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.File;

public class JsonWorking {
	final String loc; // for storage path 
	final String TimeToLiveLoc; // for time-to-live path

	JSONObject jsonObj; // storage json object
	JSONObject timeLive; // time-to-live json object
	JSONParser parser;
	
	final Scanner sc = new Scanner(System.in);
	
	JsonWorking(String loc, String TimeToLiveLoc){
		this.loc = loc;
		this.TimeToLiveLoc = TimeToLiveLoc;
		try {
			parser = new JSONParser();
			Object obj = parser.parse(new FileReader(loc));
			this.jsonObj = (JSONObject) obj; // storage json obj
			this.timeLive = (JSONObject) parser.parse(new FileReader(TimeToLiveLoc)); // time-to-live json obj
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public synchronized void working() {
		outer : while (true) { // the loop is always running the exit option select
			System.out.println("Select the Operation: \n");
			System.out.println("Create: 1\nRead: 2\nDelete: 3\nExit: 4\n");
			int select = sc.nextInt();	 
			switch(select) {
				case 1:
					create(); // create a key-value pair
					break;
				case 2:
					read(); // read a value by using a key
					break;
				case 3:
					System.out.println("Key: ");
					String key = sc.next(); // delete a key
					delete(key);
					break;
				case 4:
					update(); // update the changes into the file 
					sc.close();
					break outer;
				default:
					System.out.println("Please enter the valid Operation\n");
			}
		}
	}
	
	private synchronized void create() {
		if(fileSizeInGB(new File(TimeToLiveLoc)) >= 1) { // if the file size is more than 1 GB then we can't new values
			System.out.println("Your storage is full");
			return;
		}
		System.out.println("Give input as a 1. file or 2. Object");
		int type = sc.nextInt();
		switch(type) {
			case 1:
				fileInput();
				break;
			case 2:
				objectInput();
				break;
			default:
				System.out.println("Please select a correct option..");
				break;
		}
		update();
	}
	
	private synchronized void fileInput() { // give the json file as a value
		System.out.println("Key: ");
		String key = sc.next();
		System.out.println("Time-To-Live: \n");
		long start = System.currentTimeMillis();
		String seconds = sc.next();
		
		if(!seconds.equals("NA")) {
			timeLive.put(key, (start)/1000+Long.parseLong(seconds));
		}
		else timeLive.put(key, "NA");

		System.out.println("Give the file path: ");
		String path = sc.next(); // get the path of json file
		JSONObject jobjt = new JSONObject();
		JSONParser par = new JSONParser();
		try {
			File fi = new File(path);
			if(fileSizeInKb(fi) <= 16) {
				JSONObject jo = (JSONObject) par.parse(new FileReader(path));
				jobjt.put(key, jo.toString());			
				jsonObj.put(key, jo); // add the json file into the storage
			}
			else {
				System.out.println("File size is high");
			}
		}
		catch(Exception e) {
			System.out.println("File not found");
			e.printStackTrace();
		}
	}
	
	private synchronized double fileSizeInKb(File file) { // get the file size in kb
		return (double) file.length()/1024;
	}
	
	private synchronized double fileSizeInGB(File file) { // get the file size in gb
		return (double) file.length()/(1024*1024*1024);
	}
	
	private synchronized void objectInput() { // give the object as a value
		System.out.println("Enter the Number of Keys want to insert: ");
		int numberOfKeys = sc.nextInt(); // get the number of key for give the input

		for(int i=1; i<=numberOfKeys; i++) { 
			System.out.println("Key: \n");
			String key = sc.next();

			if(jsonObj.containsKey(key)) {
				System.out.println("This Key is already exists\n");
				continue;
			}
			
			System.out.println("Time-To-Live (in seconds): \n"); // get the time to live in seconds
			long start = System.currentTimeMillis();
			String seconds = sc.next();
			
			if(!seconds.equals("NA")) {
				System.out.println("Insert "+(start)/1000+Long.parseLong(seconds));
				timeLive.put(key, (start)/1000+Long.parseLong(seconds));
			}
			else timeLive.put(key, "NA");
			
			System.out.println("Number of Properties: "); 
			int numOfPro = sc.nextInt(); // this is for value properties
			if(numOfPro == 0) {
				System.out.println("Value : ");
				String val = sc.next();
				jsonObj.put(key, val);
			}
			else {
				JSONObject value = new JSONObject();
				for(int j=1; j<=numOfPro; j++) {
					System.out.println("Key : ");
					String insideKey = sc.next();
					System.out.println("Value : ");
					String insideValue = sc.next();
					value.put(insideKey, insideValue);
				}
				jsonObj.put(key, value.toString()); // add the objects into the main jsonObjects
			}
		}
		
	}
	
	private synchronized void read() { // read the value by using the key
 		try {
			System.out.println("Key: ");
			String k = sc.next(); // get the key
			parser = new JSONParser();
			Object obj = parser.parse(new FileReader(loc));
			jsonObj = (JSONObject) obj;

			if(!jsonObj.containsKey(k)) { // if the key is not there
				System.out.println("Key Not Found ");
				return;
			}
			else {
				String t = String.valueOf(timeLive.get(k));
				long cur = System.currentTimeMillis();
				
				if(t.equals("NA")) System.out.println(jsonObj.get(k));
				else if(cur/1000 < Long.parseLong(t)) System.out.println(jsonObj.get(k)); // if the key living time is greater than the current time then show the value
				else {// otherwise
					delete(k);
					System.out.println("Key Not Found ");
				}
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private synchronized void delete(String key) { // delete a key
		if(!timeLive.containsKey(key)) {
			System.out.println("Key Not Found\n");
			return;
		}
		String t = String.valueOf(timeLive.get(key));
		long cur = System.currentTimeMillis();
		if(t.equals("NA")) {
			timeLive.remove(key);
			jsonObj.remove(key);		
			System.out.println("Key Deleted..\n");
		}
		else if(cur/1000 > Long.parseLong(t)) {
			timeLive.remove(key);
			jsonObj.remove(key);
			System.out.println("Key Not Found..\n");
			return;
		}
		else {
			timeLive.remove(key);
			jsonObj.remove(key);
			System.out.println("Key Deleted..\n");
		}		
		update();
	}
	
	private synchronized void update() { // update every changes in the file
		try {
			FileWriter fw = new FileWriter(loc);
			fw.write(jsonObj.toString());
			fw.close();
			FileWriter fw1 = new FileWriter(TimeToLiveLoc);
			fw1.write(timeLive.toString());
			fw1.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}
