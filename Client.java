/**
 * Created by rohit ka pc on 7-Nov-15.
 */
import java.net.*;
import java.io.*;
import java.util.*;
import java.lang.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.*;


public class Client extends StartClient implements Runnable 

{
	//Hasmap to store the value of piece information
  public static HashMap<Integer, byte[]> piece_info = new HashMap<Integer, byte[]>();
	//array list to store the divided file 
    public static ArrayList<Integer> piece_no = new ArrayList<Integer>();
 


    
//final file join 
    public static String joinedFile;

    public static int port = -2;
    public ServerSocket serverSocket;
    static int pid = -1;
    public static HashMap<Integer, Integer> neighbourPeerInfo = new HashMap<Integer, Integer>();

    
	 private static int GetPortNo() 
		 {
        try {
            Scanner scanner = new Scanner(new FileInputStream("config"));
          
            int serverPort = scanner.nextInt();
            port = scanner.nextInt();
            scanner.close();
            return port;

        } catch (FileNotFoundException e) {
            System.out.println("Configuration failed. Will use random port for all peers");

        }
        return port = 5858;
    }
	//if cannot read the port number from config use this port number for file share
public static int sender_portNo;

    public Client(int sender_portNo)
    {
        this.sender_portNo = sender_portNo;
    }


    public static void createNote(ObjectOutputStream objectOutputStream, String objectString ) throws IOException {
        objectOutputStream.writeObject(objectString );
        objectOutputStream.flush();
        objectOutputStream.reset();
    }

    public static void insertPieces(ObjectOutputStream objectOutputStream, byte[] quantity) throws IOException {
        objectOutputStream.writeObject(quantity);
        objectOutputStream.flush();
        objectOutputStream.reset();
    }

  
    public static String c_name;
	//Check if all the pieces are there? Display Join Message
    public boolean piece_check()
	{
		//store the value of pieceno in key and check
        for(int key: piece_no) {
            if(!piece_info.containsKey(key))
            {
                return false;
            }
        }
        //Have All the pieces? Join them
		System.out.println("RECEIVED ALL THE PIECES? JOIN");
				System.out.println("------------------------------");
        try {
            File file = new File(joinedFile);
			//if join file exists. Delete it and create a new one
            if(file.exists())
            {
                file.delete();
            }
			
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            for(int i = 0; i < piece_no.size(); i++) 
			{
				
				//Store the pieces in the buffer with position of each piece
                store_Split_Pieces(i, piece_info.get(piece_no.get(i)));
                fileOutputStream.write(piece_info.get(piece_no.get(i)));
            }
            fileOutputStream.flush();
            fileOutputStream.close();
            System.out.println(c_name + " HAS ALL THE PIECES. JOINING BACK THE PIECES");
					System.out.println("------------------------------");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }
	private void generateFinalFile(int c) {
        if(true) {
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(c_name + "Folder", false);
                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 0; i < Client.piece_no.size(); i++) {
                    int positionofPiece = Client.piece_no.get(i);
                    if (Client.piece_info.containsKey(positionofPiece)) {
                        stringBuilder.append(positionofPiece);
						System.out.println("------------------------------");
                        stringBuilder.append(" ");
                    }
                }
				//get the pieces. Send and Close the Stream
                fileOutputStream.write(stringBuilder.toString().getBytes());
                fileOutputStream.flush();
                fileOutputStream.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
	
    public static void createNote(ObjectOutputStream objectOutputStream, int objectValue) throws IOException 
	{
		System.out.println("Receiving Action from Neighbouring peers");
        objectOutputStream.writeInt(objectValue);
        objectOutputStream.flush();
	}
	private int receiving_port_no = -1;
    private int receiving_peer_no = -1;
    private int sender_port_no = -1;
    private int sender_peer_no = -1;
	
	public void HandleClients() 
	{
		
                    try {
                        System.out.println("Thread Handler handling the Clients for Multiple Connections");
						System.out.println("***********************************************************************");
                        Thread.sleep(10000);
                        System.out.println("Establishing Connection to send pieces");
						System.out.println("------------------------------");
                        Socket sendPortNo = new Socket("localhost", sender_port_no);
                        ObjectOutputStream objectOutputStream_forSending = new ObjectOutputStream(sendPortNo.getOutputStream());
                        System.out.println("Establishing Connection to receive pieces");
                        System.out.println("------------------------------");
						Socket Peer_receiveSocket = new Socket("localhost", receiving_port_no);
                        ObjectOutputStream objectOutputStream_forReceiving = new ObjectOutputStream(Peer_receiveSocket.getOutputStream());
                        ObjectInputStream incoming_objectOutputStream_forReceiving = new ObjectInputStream(Peer_receiveSocket.getInputStream());
                        System.out.println("Yeippee Connected!");
                        while (!piece_check()) 
						{
                            System.out.println("Start receiving pieces from neighbouring peers");
							System.out.println("------------------------------");
							//save the pieces. Write it into buffer
                            createNote(objectOutputStream_forReceiving, "DISPLAY");
                            ArrayList<Integer> storeArrayList = (ArrayList<Integer>) incoming_objectOutputStream_forReceiving.readObject();
                            for (int i =0 ; i < storeArrayList.size(); i++ ) {
                                int getPosition = storeArrayList.get(i);
                                if(Client.piece_info.containsKey(getPosition)) 
								{
                                    System.out.print(getPosition + "=>" + getPosition + "\t");
                                } else 
								{
                                    System.out.print(getPosition + "=> NEW\t");
                                }
                            }
                            System.out.println();
							System.out.println("------------------------------");
                            for (int i = 0; i < Client.piece_no.size(); i++) {
                                int getPosition = Client.piece_no.get(i);
								//check if it contains that piece ?
                                if (Client.piece_info.containsKey(getPosition)) {
                                    continue;
                                }

                                System.out.println("PEER" + Client.c_name + "requesting PEER" + receiving_peer_no + " for Piece number" + getPosition);
                                createNote(objectOutputStream_forReceiving, "GETFROMCLIENT");
                                createNote(objectOutputStream_forReceiving, getPosition);
								//If this loop runs that means peer has that particular piece of file
                                if (incoming_objectOutputStream_forReceiving.readInt() == 1) 
								{ 
                                    createNote(objectOutputStream_forReceiving, "ENQUIRE");
                                    createNote(objectOutputStream_forReceiving, getPosition);
                                    int position = incoming_objectOutputStream_forReceiving.readInt();
									//Store the receiving pieces in a buffer 
                                    byte[] piece = (byte[]) incoming_objectOutputStream_forReceiving.readObject();
									//put it into the HashMap and save it 
                                    Client.piece_info.put(position, piece);
                                    System.out.println("Has received piece No" + piece_no.get(i) + " from neighbouring peer" + receiving_peer_no);
									System.out.println("------------------------------");
                                } else {
                                    System.out.println("Peer" + Client.c_name + " " + receiving_peer_no + " has now received piece no" + getPosition);
                                }
                            }
                            System.out.println("PEER" + Client.c_name + "getting pieces from neighbouring peer");
							System.out.println("------------------------------");
                            System.out.println("send pieces to other neighbouring peers");
							//checking if all the pieces have been received 
                            for (Integer apiece_no : Client.piece_no) 
							{
                                int position_singlePiece = apiece_no;
                                if (!Client.piece_info.containsKey(position_singlePiece)) {
                                    continue;
                                }
                                System.out.print(position_singlePiece + " ");
                                createNote(objectOutputStream_forSending, "PIECES");
                                createNote(objectOutputStream_forSending, position_singlePiece);
                                insertPieces(objectOutputStream_forSending, Client.piece_info.get(position_singlePiece));
                            }
                            System.out.println("------------------------------------------------------");
							
                            System.out.println("PEER" + Client.c_name + " received all pieces. time to join");
							System.out.println("------------------------------");
                            Thread.sleep(1000);
                        }
                    } catch (IOException | ClassNotFoundException | InterruptedException e) {
                        e.printStackTrace();
                    }

            
	}
	public void runRequestForClients()
	{
	try {
		System.out.println("------------------------------------------------------");
            //Start Connecting the server and allowing other peers to connect as well
			
            Socket soc = new Socket("localhost", sender_portNo);
			System.out.println("Connected. ");
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(soc.getOutputStream());
			System.out.println("Neighbouring peers will now connect ");
            System.out.println("------------------------------");
			createNote(objectOutputStream, "SETUP");
            ObjectInputStream objectInputStream = new ObjectInputStream(soc.getInputStream());
			//get the peer id 
            pid = objectInputStream.readInt();
            port = objectInputStream.readInt();
            c_name = "Peer" + (pid-1);
			System.out.println("------------------------------");
            System.out.println(pid);
            // Create a peerDir to save file chunk from server
            File peerFolder = new File(c_name + "Folder");
            if(!peerFolder.exists()) {
                peerFolder.mkdir();
            }
           //Request and receive the list of neighbourPeer
            createNote(objectOutputStream, "DISPLAY");
			System.out.println("------------------------------");
            piece_no = (ArrayList<Integer>) objectInputStream.readObject();
            
			System.out.println("Get limited amount of Chunks from FileOwner");
			System.out.println("------------------------------------------------------");
			//initial pieces sent and received from fileOwner
            int initial_PieceNo = (int)(1.0 * piece_no.size() / 5 * (pid % 5));
            int last_PieceNo = (int)(1.0 * piece_no.size() / 5 * ((pid  % 5) + 1));
			//checking if all the pieces starting from first piece to last piece has been received 
            for(int i = initial_PieceNo; i < last_PieceNo; i++) {
                createNote(objectOutputStream, "ENQUIRE");
                createNote(objectOutputStream, piece_no.get(i));
                int position = objectInputStream.readInt();
				//reading the pieces from buffer and storring it into hashmap 
                byte[] piece = (byte[]) objectInputStream.readObject();
                piece_info.put(position, piece);
                System.out.println("Getting piece number" + piece_no.get(i) + " from File Owner");
				System.out.println("------------------------------");
                store_Split_Pieces(position, piece);
            }
            generateFinalFile(0);
           
            // Getting and joining file and its information  
            createNote(objectOutputStream, "NAME");
            String path_givenFile = (String) objectInputStream.readObject();
			System.out.println("------------------------------");
			System.out.println("Joining the splitted file ");
            String givenName = new File(path_givenFile).getName();
            String type = givenName.substring(givenName.lastIndexOf('.') + 1);
            String firstNameOfFile = givenName.substring(0, givenName.lastIndexOf('.'));
            joinedFile = firstNameOfFile + "FROM_PEER" + (pid-1) + "." + type;
			
            System.out.println("FINAL FILE IS  " + joinedFile);
            System.out.println("------------------------------");
			System.out.println("Now send and Receive FIle from Neighbouring PEERS");
             //Start sending and Receiving File to and from neighbouring peers
            do {
				//send Request to neighbouring peers for transfer
                createNote(objectOutputStream, "NEIGHBOURING_PEER");
				//get the peer id of the peer
                createNote(objectOutputStream, pid);
				//get info from hashmap
                neighbourPeerInfo = (HashMap<Integer, Integer>) objectInputStream.readObject();
				
                System.out.println("Peer" + c_name + "requesting File");
				System.out.println("------------------------------");
				//get peer no;
                receiving_peer_no = (int) objectInputStream.readObject();
                //get the senders peer no. 
				sender_peer_no = (int) objectInputStream.readObject();
                //get the receivers port no.
				receiving_port_no = neighbourPeerInfo.containsKey(receiving_peer_no) ? neighbourPeerInfo.get(receiving_peer_no) : 0;
                //get the senders port no.
				sender_port_no = neighbourPeerInfo.containsKey(sender_peer_no) ? neighbourPeerInfo.get(sender_peer_no) : 0;
                Thread.sleep(1000);
            } 
			while (this.receiving_port_no <= 0 || this.sender_port_no <= 0);
			//getting the port numbers of neighbouring peers that are sending the peers and receiving the peers to a particular peer 
            System.out.println("Peer" + c_name + " sending to " + sender_peer_no + ":" + sender_port_no);
            System.out.println("Peer" + c_name + " Receiving from " + receiving_peer_no + ":" + receiving_port_no);

            (new Thread() {
                @Override
                public void run() 
				{
					HandleClients();
				}
            }).start();

            while(port < 0) {
                Thread.sleep(500);
            }

            serverSocket = new ServerSocket(this.port);

            while (true) {
				//start Thread to handle multiple clients
                ClientHandler client_localSocket = new ClientHandler();
                Socket socket = null;
                try {
                    System.out.println("Client PEER is working on PORT number " + serverSocket.getLocalPort());
					//Funtion calls to get Peer id and store the pieces of file in hashmap 
                    socket = serverSocket.accept();
                    client_localSocket.socketPort(socket);
                    client_localSocket.get_Pid(Client.pid);
                    client_localSocket.storePiecesOfFile(piece_info);
                    client_localSocket.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } catch (IOException  | InterruptedException | ClassNotFoundException e) {
            e.printStackTrace();
        }

}
    public void Start() {
        
runRequestForClients();
    }
	//Split and store the pieces in a buffer 
    private void store_Split_Pieces(int position, byte[] piece) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(c_name + "Folder" + position, false);
            fileOutputStream.write(piece);
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    

    @Override
    public void run() {
        Start();
    }

   

    public static void main(String[] args) {
		
        int _sender_portNo = GetPortNo();
        
        new Client(_sender_portNo).Start();

    }
}

abstract class StartClient 
{
 public abstract void Start();
StartClient() {}
}
