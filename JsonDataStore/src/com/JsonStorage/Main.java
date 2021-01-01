package com.JsonStorage;

// I externally add json-simple-1.1.1.jar file for parsing the json files
// I run this code only Ubuntu 20.04 os version

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.io.File;
import java.io.FileReader;
import java.util.Scanner;
import java.io.FileWriter;
import org.json.simple.JSONArray;

public class Main {
	public static void main(String[] args) {
		final Scanner sc = new Scanner(System.in);
		String currentDirectory = System.getProperty("user.dir"); // get the current user directory
	    File start = new File(currentDirectory+"/start.txt");
	    boolean result = false;
	    try {
	    	result = start.createNewFile();
	    	if(result) { // if the file is newly created then work this code (first time installatoin)
	    		System.out.println("Give the path: \n");
	    		System.out.println("Type no for use this current path..");
	        	String installPath = sc.next(); // get the path for install the storage file
	        	
	        	FileWriter filewrite = new FileWriter(currentDirectory+"/start.txt");
	        	
	        	if(installPath.equals("no")) { // if the path is no then use the current user directory
	        		filewrite.write(currentDirectory);
	        	}
	        	else filewrite.write(installPath); // otherwise use the given path
	        	
	        	filewrite.close();
	        	
	        	File file = new File(currentDirectory+"/file.json");
	    	    file.createNewFile(); // create a file.json file for store the user details
	    	    
	    	    FileWriter flw = new FileWriter(currentDirectory+"/file.json");
	    	    flw.write("{}");
	    	    flw.close();
	    	    
	    	    System.out.println("User Name: ");
	    		String name = sc.next(); // get the username 
	    		System.out.println("Password: ");
	    		String pass = sc.next(); // get the password
	    		signUp(name, pass); // signup the user
	    	}
	    	else { // if the start.txt file is already there then work this below code
	    		System.out.println("1. LogIn\n2. SignUp\n");
	    		int select = sc.nextInt(); // select the option for login or signUp
	    		String name, pass;
	    		if(select == 1) {
	    			do {
	    	    		System.out.println("User Name: ");
	    	    		name = sc.next();
	    	    		if(!itHas(name)) {
	    	    			System.out.println("User Not Found\n");
	    	    			continue;
	    	    		}
	    	    		System.out.println("Password: ");
	    	    		pass = sc.next();
	    	    		if(logIn(name, pass)) break; // if the given credentials is enough for login then logged in
	    			}while(!itHas(name)); // this loop is running until the user give the correct username
	    		}
	    		else if(select == 2) {
	    			do {
	    	    		System.out.println("User Name: ");
	    	    		name = sc.next();
	    	    		if(itHas(name)) {
	    	    			System.out.println("Already exsist\n");
	    	    			continue;
	    	    		}
	    	    		System.out.println("Password: ");
	    	    		pass = sc.next();	
	    	    		if(signUp(name, pass)) break;// if the given credentials is enough for login then logged in
	    			}while(itHas(name));// this loop is running until the user give the new username
	    		}
	    		else System.out.println("Please select a correct Option.\n");
	    	}
	    }
	    catch(Exception e) {
	    	e.printStackTrace();
	    }
	    sc.close();
	}
	
	private synchronized static void fileCreating(String path) { // this function is used for create a file
		File f = new File(path);
		try {
			f.createNewFile();
			FileWriter fwj = new FileWriter(path);
			fwj.write("{}");
			fwj.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
		
	private synchronized static boolean signUp(String name, String pass) {
		String currentDirectory = System.getProperty("user.dir");    	
    	JSONParser Jpar = new JSONParser();
    	
    	try {
        	JSONObject Jobj = (JSONObject) Jpar.parse(new FileReader(currentDirectory+"/file.json")); // parse the file.json file
        	JSONArray array = new JSONArray();
        	String fileName = String.valueOf(System.currentTimeMillis()); // get the current time in milliseconds for create a unique file name

    		File fLoc = new File(currentDirectory+"/start.txt");
    		Scanner fr = new Scanner(fLoc); // get the installation path
    		String path = fr.nextLine();
        	fr.close();
        	
    		FileWriter filewrite = new FileWriter(currentDirectory+"/file.json");    	
        	array.add(pass);
        	array.add(fileName);
        	Jobj.put(name, array);
        	
        	filewrite.write(Jobj.toJSONString());	// write user details into file.json	    	    	
        	filewrite.close();
 
        	fileCreating(path+"/"+fileName+".json"); // create a new file for storing the key-value pair data
    		fileCreating(path+"/"+fileName+"time-to-live.json"); // create a new file for key time-to-live
			JsonWorking jsonwork = new JsonWorking(path+"/"+fileName+".json", path+"/"+fileName+"time-to-live.json");
			jsonwork.working();
			// create a jsonWorking object and run working method
			return true;
    	}
	    catch(Exception e) {
	    	e.printStackTrace();
	    }
    	return false;
	}
	
	private synchronized static boolean logIn(String name, String pass) {
		try {
			String currentDirectory = System.getProperty("user.dir");
		    JSONParser parser = new JSONParser();
		    JSONObject obj = (JSONObject) parser.parse(new FileReader(currentDirectory+"/file.json"));
		    if(obj.containsKey(name)) { // check the given username is there or not in the file.json
		    	JSONArray arr = (JSONArray)obj.get(name);
		    	String getPass = arr.get(0).toString();
		    	
		    	if(getPass.equals(pass)) { // if the given password is equal to the original password then logged in
		    		System.out.println("Logged in\n");
		    		File fLoc = new File(currentDirectory+"/start.txt");
		    		Scanner fr = new Scanner(fLoc);
		    		String path = fr.nextLine();
		        	fr.close();
		    		path += "/"+arr.get(1).toString();
					JsonWorking jsonwork = new JsonWorking(path+".json", path+"time-to-live.json");	
					jsonwork.working();
					return true;
		    	}
		    	else {
		    		System.out.println("Incorrect Password..\n");
		    		return false;
		    	}
		    }
		    else {
		    	System.out.println("User NOT found..\n");
		    	return false;
		    }
		}
		catch(Exception e) {
			e.printStackTrace();
			System.out.println("Can not LogIn..\n");
		}
		return false;
	}
	
	private synchronized static boolean itHas(String name) {
		try { // check the given username is there or not in the file.json
			String currentDirectory = System.getProperty("user.dir");
		    JSONParser parser = new JSONParser();
		    JSONObject obj = (JSONObject) parser.parse(new FileReader(currentDirectory+"/file.json"));
		    if(obj.containsKey(name)) return true;
		}
		catch(Exception e) {
			e.printStackTrace();
		}		
		return false;
	}
}
