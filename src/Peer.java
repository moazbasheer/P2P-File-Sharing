

public class Peer {
	public String name;
	public String password;
	public int LogedIn;
	Peer(){
		name="";
		password="";
		LogedIn=0;
	}
	Peer(String n,String p,int log){
		name=n;
		password=p;
		LogedIn=log;
	}
}
//public static ArrayList<Peer> peers;