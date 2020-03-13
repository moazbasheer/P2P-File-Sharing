import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Server {
	public static Scanner scan = new Scanner(System.in);
	public static int GreatestFileID = 100;

	public static void main(String[] args) {
		try {
			
			SetHashFunction();
			LoadCurrentUsersData();
			LoadFromTrackerFile();
			ServerSocket serverSocket = new ServerSocket(100);
			System.out.println("Server is waiting for clients");
			while (true) {
				Socket clientSocket = serverSocket.accept();
				System.out.println("Client [" + clientSocket + "]:"+ "is now connected");
				Thread client = new Connection(clientSocket);
				client.start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void SetHashFunction() {
		Tracker.HashOfFile.put("File1", 12);
		Tracker.HashOfFile.put("File2", 13);
		Tracker.HashOfFile.put("File3", 100);
	}

	public static void LoadFromTrackerFile() { // / Load From Tracker.txt file

		
		try {
			File file = new File("tracker.txt");
			Scanner load = new Scanner(file);
			Tracker.UsersOfHash.clear();
			while (load.hasNext()) {
				int hash = load.nextInt();
				GreatestFileID=Integer.max(hash,GreatestFileID);
				String filename = load.next();
				ArrayList<String> users = new ArrayList<String>();
				while (filename.charAt(filename.length() - 1) == ',') {
					filename = filename.substring(0, filename.length() - 1);
					users.add(filename);
					filename = load.next();
				}
				users.add(filename);
				Tracker.UsersOfHash.put(hash, users);
			}
			load.close();
		} catch (FileNotFoundException e) {
			System.out.println("FileNotFoundException");
		}
		
	}

	public static void UpdateUserFile() {
		try {
			FileWriter file = new FileWriter("users.txt");
			for (int i = 0; i < Tracker.peers.size(); i++) {
				file.write(Tracker.peers.get(i).name + " "
						+ Tracker.peers.get(i).password + " ");
				file.write(Tracker.peers.get(i).LogedIn + "\n");
			}
			file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void UpdateTrackerFile() {
		try {

			FileWriter file = new FileWriter("tracker.txt");
			for (Map.Entry mapElement : Tracker.UsersOfHash.entrySet()) {
				file.write(mapElement.getKey() + " ");
				ArrayList<String> usernames = (ArrayList<String>) mapElement.getValue();
				for (int i = 0; i < usernames.size() - 1; i++)
					file.write(usernames.get(i) + ", ");
				file.write(usernames.get(usernames.size() - 1) + "\n");
			}
			file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/*
	 * int INT;
	 */

	public static void LoadCurrentUsersData() {
		try {
			Tracker.peers.clear();
			File file = new File("users.txt");
			Scanner load = new Scanner(file);
			while (load.hasNext()) {
				String name = load.next();
				String password = load.next();
				int login = load.nextInt();
				Tracker.peers.add(new Peer(name, password, login));
			}
			load.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static int AddFile(String Filename, String USERNAME) {
		if (Tracker.HashOfFile.containsKey(Filename)) {
			return 0;
		}
		Tracker.HashOfFile.put(Filename, ++GreatestFileID);
		ArrayList<String> users = new ArrayList<>();
		users.add(USERNAME);
		Tracker.UsersOfHash.put(GreatestFileID, users);
		UpdateTrackerFile();
		return 1;
	}

	public static int TransferFile(String Filename, String USERNAME) {
		LoadCurrentUsersData();
		LoadFromTrackerFile();
		int hash = 0;

		if (Tracker.HashOfFile.containsKey(Filename)) {
			hash = Tracker.HashOfFile.get(Filename);
		}
		else return 0;
		ArrayList<String> users = Tracker.UsersOfHash.get(hash);
		int Transferred = 0;
		//  check if i have this file
		for(int i=0;i<users.size();i++){
			if(users.get(i).equals(USERNAME))
				return 0;
		}
		for (int i = 0; i < users.size(); i++) {
			int userindex = -1;
			// get its index in tracker
			for (int j = 0; j < Tracker.peers.size(); j++) {
				if (users.get(i).equals(Tracker.peers.get(j).name)) {
					userindex = j;
					break;
				}
			}

			if (Tracker.peers.get(userindex).LogedIn == 1) {
				Tracker.UsersOfHash.get(hash).add(USERNAME);
				UpdateTrackerFile();
				Transferred = 1;
				break;
			}
		}
		return Transferred;
	}

	static class Connection extends Thread {
		private Socket clientSocket;

		Connection(Socket client) {
			clientSocket = client;
		}

		public void run() {
			try {
				DataInputStream input = new DataInputStream(clientSocket.getInputStream());
				DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());
				Scanner scan = new Scanner(System.in);
				String USERNAME = input.readUTF();
				String filename = input.readUTF();
				int requestNumber=input.readInt();  //AddFile 0 , RequestFile 1 ,
				System.out.println("USERNAME " + USERNAME);
				System.out.println("filename " + filename);

				System.out.println("[" + clientSocket + "]: " + filename);
				if (requestNumber == 1) {
					if (TransferFile(filename, USERNAME) == 1)
						output.writeUTF("Done Succesfully!...\nStatus:200 OK\n");
					else
						output.writeUTF("Process Failed!Either the file not found \n"
								+"or the user that has the file is not online"
								+ "...\nStatus:400 ERROR\n");
				} else if (requestNumber == 0) {
					if (AddFile(filename, USERNAME) == 1)
						output.writeUTF("Done Succesfully!...\nStatus:200 OK\n");
					else
						output.writeUTF("Process Failed!Either the file not found \n"
								+"or the user that has the file is not online"
								+ "...\nStatus:400 ERROR\n");
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
