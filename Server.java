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


public class Server extends ServerStart{
	//HashMap to store the piece of piece INFO with the clients that have that piece
    public static HashMap<Integer, byte[]> piece_info = new HashMap<Integer, byte[]>();

      public final int max_pieceSize = 102400; //As given that each file should be of 100kB
	//hash table to store peer information
    public static HashMap<Integer, Integer> neighbourPeerInfo = new HashMap<Integer, Integer>();
	//hash table to store peer information and their neighbour peers pieces
    public static HashMap<Integer, ArrayList<Integer>> StoreConfigPeer = new HashMap<Integer, ArrayList<Integer>>();

	//create a new hashmap to store neighbour peer info
    static{
        if (neighbourPeerInfo == null) 
		{
            neighbourPeerInfo = new HashMap<Integer, Integer>();
        }
    }
 public static int socket_no;//=5858;
 //read the port number and Peer Port number from Config file 
private static void GatherDataFromConfig() {
        try {
            Scanner scanner = new Scanner(new FileInputStream("config"));
            // server line
            int serverPort = scanner.nextInt();
            socket_no = scanner.nextInt();
            while(scanner.hasNext()) {
                int pid = scanner.nextInt();
                ArrayList<Integer> peerInfo = new ArrayList<Integer>();
                peerInfo.add(scanner.nextInt());
                peerInfo.add(scanner.nextInt());
                peerInfo.add(scanner.nextInt());
                StoreConfigPeer.put(pid, peerInfo);
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            socket_no = 5858;
            System.out.println("NOT ABLE TO READ CONFIG FILE. ");
			System.out.println("GOING TO OPTION OF RANDOM ALLOCATION");
        }
    }
	private String path_givenFile;
	
	  protected void splittedFile() {
        try {
            File serverFolder = new File("MainFolder");
            if(!serverFolder.exists()) 
			{
                serverFolder.mkdir();
            }
            // Read everything to memory
            FileInputStream fileInputStream = new FileInputStream(this.path_givenFile); // reading the file 
            byte[] max_size = new byte[this.max_pieceSize]; //max value in which one piece needs to be divided
            int buffer_limit;
            int piece_number = 1;
            while ((buffer_limit = fileInputStream.read(max_size)) != -1) //
			{
                byte[] byte_piece = Arrays.copyOfRange(max_size, 0, buffer_limit);
                piece_info.put(piece_number, byte_piece);
                System.out.println("Piece " + piece_number);
				System.out.println("------------------------------");
				System.out.println("Save splitted file in main Folder");
                FileOutputStream fileOutputStream = new FileOutputStream("MainFolder/" + piece_number, false);
				//writing the splitted pieces in the main folder of file owner 
                fileOutputStream.write(byte_piece);
                fileOutputStream.flush();
                fileOutputStream.close();
                max_size = new byte[this.max_pieceSize];
                piece_number++;
            }
            System.out.println("Total number of pieces in which file is divided is " + (piece_number-1) + " number of Pieces");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
 private ServerSocket serverSocket;
	
	
    public Server(String path_givenFile, int socket_no)
	{
        if (null != path_givenFile && new File(path_givenFile).exists())
		{
            this.path_givenFile = path_givenFile;
        }
        this.socket_no = socket_no;

        try 
		{
            serverSocket = new ServerSocket(this.socket_no);
        } catch (IOException e) 
		{
            e.printStackTrace();
            System.exit(1);
        }
        //diving the file into pieces and storing it
        this.splittedFile();
    }
	
	

  
    @Override
    public void Start() {

        try {
            while(true) {
                System.out.println("waiting for clients to connect to FILE OWNER i.e Main SERVER");
				System.out.println("-------------------------------------------------------------");
                Socket soc = serverSocket.accept();
                ServerHandler serverHandler = new ServerHandler();
                serverHandler.storePiecesOfFile(piece_info);
                serverHandler.input_File_Name(this.path_givenFile);
                serverHandler.socketPort(soc);
                serverHandler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

   

    public static void main(String[] args) {
 
        GatherDataFromConfig();
		//static File Given by me 
        String inputFile = "image1.jpg";
        new Server(inputFile, socket_no).Start();
    }
}
abstract class ServerStart {

    public abstract void Start();
    ServerStart() {}

}