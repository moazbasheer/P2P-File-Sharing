import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

//AddFile 0 , RequestFile 1 ,

public class Client {
	public static int peerNumber; // / index of the client in Server
	static Scanner scan = new Scanner(System.in);

	public static void main(String[] args) {

		SetHashFunction();
		LoadCurrentUsersData();
		UpdateUserFile();
		LoadFromTrackerFile();

		while (true) {
			System.out.println("Choose the option :");
			System.out.println("1-Sign up");
			System.out.println("2-Sign in");
			int choice = scan.nextInt();
			scan.nextLine(); // to Remove '\n'
			while (choice < 1 || choice > 2) {
				System.out.println("Enter valid choice");
				choice = scan.nextInt();
				scan.nextLine();
			}
			if (choice == 1) {
				Signup();
			} else if (choice == 2) {
				// ///--------------Log In--------
				peerNumber = -1; // Number is not chosen yet (in logout mode)

				while (peerNumber == -1
						|| Tracker.peers.get(peerNumber).LogedIn == 1) {
					if (peerNumber != -1
							&& Tracker.peers.get(peerNumber).LogedIn == 1) {
						System.out.println("This is online try again");
					}
					peerNumber = Login();
					if (peerNumber == -1) {
						System.out.println("It is not found");
					}
				}

				Tracker.peers.get(peerNumber).LogedIn = 1; // Loged In
															// successfully
				UpdateUserFile();

				while (true) {
					choice = GetChoice();
					ExecuteChoice(choice, peerNumber);
					if (Tracker.peers.get(peerNumber).LogedIn == 0)
						break;
				}
			}
		}
	}

	public static void Signup() {
		while (true) {
			System.out.println("Enter username");
			String name = scan.nextLine();
			System.out.println("Enter password");
			String password = scan.nextLine();
			if (!IsUserFound(name)) {
				Tracker.peers.add(new Peer(name, password, 0));
				break;
			}
		}
	}

	public static boolean IsUserFound(String name) {
		for (int i = 0; i < Tracker.peers.size(); i++) {
			if (Tracker.peers.get(i).name.equals(name)) {
				return true;
			}
		}
		return false;
	}

	public static int GetChoice() {
		System.out.println("1-Request a file");
		System.out.println("2-Add File");
		System.out.println("3-Logout");
		int ch = scan.nextInt();
		scan.nextLine();
		while (ch < 1 || ch > 3) {
			ch = scan.nextInt();
			scan.nextLine();
		}

		return ch;
	}

	public static void ExecuteChoice(int choice, int peerNumber) {
		if (choice == 1) {
			RequestFile(peerNumber);
		} else if (choice == 2) {
			AddFile();
		} else if (choice == 3) {
			Tracker.peers.get(peerNumber).LogedIn = 0;
			UpdateUserFile();
		}
	}

	public static void AddFile() {
		try {
			Socket clientSocket = new Socket("localhost", 100);

			DataInputStream input = new DataInputStream(
					clientSocket.getInputStream());
			DataOutputStream output = new DataOutputStream(
					clientSocket.getOutputStream());

			System.out.println("Connecting to server");
			System.out.println("Enter Filename");

			String request = scan.nextLine();
			output.writeUTF(Tracker.peers.get(peerNumber).name);
			output.writeUTF(request);
			output.writeInt(0); // / request number
			String reply = input.readUTF();
			System.out.println("Server : " + reply);
			clientSocket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void RequestFile(int peerNumber) {
		try {
			//open a socket
			Socket clientSocket = new Socket("localhost", 100);

			DataInputStream input = new DataInputStream(
					clientSocket.getInputStream());
			DataOutputStream output = new DataOutputStream(
					clientSocket.getOutputStream());
			System.out.println("Connecting to server");
			System.out.println("Enter Filename\n");
			String request = scan.nextLine();
			output.writeUTF(Tracker.peers.get(peerNumber).name);
			output.writeUTF(request);
			output.writeInt(1); // / request number
			String reply = input.readUTF();
			System.out.println("Server : " + reply);
			clientSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void SetHashFunction() {
		Tracker.HashOfFile.put("File1", 12);
		Tracker.HashOfFile.put("File2", 13);
		Tracker.HashOfFile.put("File3", 100);
	}

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

	public static void LoadFromTrackerFile() {
		try {
			File file = new File("tracker.txt");
			Scanner load = new Scanner(file);
			while (load.hasNext()) {
				int hash = load.nextInt();
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

	public static void AddInitialData() {
		Tracker.peers.add(new Peer("user1", "123456", 0));
		Tracker.peers.add(new Peer("user2", "123456", 0));
		Tracker.peers.add(new Peer("user3", "123456", 0));
		Tracker.peers.add(new Peer("user4", "123456", 0));
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

	public static void UpadateTrackerFile() {
		try {
			FileWriter file = new FileWriter("tracker.txt");
			for (Map.Entry mapElement : Tracker.UsersOfHash.entrySet()) {
				file.write(mapElement.getKey() + " ");
				ArrayList<String> usernames = (ArrayList<String>) mapElement
						.getValue();
				for (int i = 0; i < usernames.size() - 1; i++)
					file.write(usernames.get(i) + ", ");
				file.write(usernames.get(usernames.size() - 1) + "\n");

			}
			file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static int GetClientNumber(String username, String password) {

		for (int i = 0; i < Tracker.peers.size(); i++) {
			if (Tracker.peers.get(i).name.equals(username)
					&& Tracker.peers.get(i).password.equals(password)) {
				return i;
			}
		}
		return -1;
	}

	public static int Login() {
		System.out.println("-----------Login Page-------------");
		System.out.print("Enter username");
		String name = scan.nextLine();
		System.out.println("Enter Password");
		String password = scan.nextLine();
		System.out.println(name + " " + password);
		return GetClientNumber(name, password);
	}
	/*
	 
	 
	 */
}
