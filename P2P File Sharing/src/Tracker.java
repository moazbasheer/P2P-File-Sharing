import java.util.ArrayList;
import java.util.HashMap;


public class Tracker {
	public static ArrayList<Peer> peers=new ArrayList<Peer>();
	public static HashMap<Integer,ArrayList<String>> UsersOfHash=new HashMap<>(); /// Hash-key of file and ArrayList of index of users
	public static HashMap<String,Integer> HashOfFile=new HashMap<>(); /// hash-key and index	
	public static HashMap<String,Integer> PortOfUser=new HashMap<>(); //Port to user-name
	
}