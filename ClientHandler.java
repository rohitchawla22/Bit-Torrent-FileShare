/**
 * Created by rohit ka pc on 7-Nov-15.
 */
import java.net.*;
import java.io.*;
import java.util.*;
import java.lang.*;

class ClientHandler extends ClientThreadStart 
{


	// get the client name
    public String c_name = this.getName();
	//hashmap to store splitted pieces
    protected HashMap<Integer, byte[]> split_piece;
	
    public void storePiecesOfFile(HashMap<Integer, byte[]> pieces_of_File)
    {
        this.split_piece = pieces_of_File;
    }

    protected ObjectOutputStream objectOutputStream;
    protected ObjectInputStream objectInputStream;
	//exchanging calls for sending and receiving pieces from neighbouring peers
	public void CheckForMSG()
	{
		while (true) 
		{
            try 
			{
				System.out.println("------------------------------");
                System.out.println("PEER " + this.c_name + "IS WAITING FOR NEIGHBOURING PEERS TO CONNECT");
                Object quantity = this.objectInputStream.readObject();
                assert (quantity instanceof String);
                String check_msg = (String) quantity;
                System.out.println("PEER " + this.c_name + "IS GETTING RESPONSE (" + check_msg + ")");
                int log = -1;
               
					if(check_msg.equals("EXIT"))
					{
						
                        // EXIT THE CONNECTION
                        objectOutputStream.close();
                        objectInputStream.close();
                        return;
					}
						
						if(check_msg.equals("DISPLAY"))
						{
						// Send the peer information 
                        ArrayList<Integer> arrayList = new ArrayList<Integer>(this.split_piece.size());
                        for (Integer key : this.split_piece.keySet()) {
                            arrayList.add(key);
                        }
                        transfer(arrayList);
                        }
						if(check_msg.equals("ENQUIRE"))
						{
						// store the first number as piece 1 and so on
                        log = this.objectInputStream.readInt();
                        //pieces will now be sent 
                        transfer(log);
                        transfer(this.split_piece.get(log));
                        }
                   
                 if(check_msg.equals("PIECES"))
				 {
					
                        // Read first INTEGER as PIECE number
                        log = this.objectInputStream.readInt();
                        // STORE THE RECEIVING PIECES
                        byte[] piece = (byte[]) this.objectInputStream.readObject();
                        if (!this.split_piece.containsKey(log)) {
                            split_piece.put(log, piece);
                            System.out.println("Received piece #" + log);
                            store_Split_Pieces(log, piece);
                        }
                        
				 }
						if(check_msg.equals("GETFROMCLIENT"))
						{
							
                        log = this.objectInputStream.readInt();
                        if (this.split_piece.containsKey(log)) {
                            transfer(1);
                        } else {
                            transfer(0);
                        }
					
						}
            }
             catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
                System.out.println("PEER" + this.getName() + " FILE TRANSFERED. CLOSING CONNECTION.");
				System.out.println("------------------------------");
                return;
            }
        }
	}
	 protected Socket socket;
    public void socketPort(Socket socket) {
        this.socket = socket;
		System.out.println("------------------------------");
        System.out.println("PEER WITH NAME =>" + c_name + "IS MAKING CONNECTION FOR FILE TRANSFER FROM =>" + socket.getPort());
		System.out.println("------------------------------");
        try {
            objectOutputStream = new ObjectOutputStream(this.socket.getOutputStream());
            objectInputStream = new ObjectInputStream(this.socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	//pushing the received call to output stream 
    public void transfer(Object sendMSG) throws IOException {
        objectOutputStream.writeObject(sendMSG);
        objectOutputStream.flush();
        objectOutputStream.reset();
    }

    public void transfer(int sendMSG) throws IOException {
        objectOutputStream.writeInt(sendMSG);
        objectOutputStream.flush();
        objectOutputStream.reset();
    }
    protected int pid;
	//get the peer id from Client Class and store in PID 
    public void get_Pid(int id) {
        this.pid = id;
    }

    @Override
    public void run() {
        CheckForMSG();
    }
	//Store the Splitted file in buffer and write it 
    private void store_Split_Pieces(int pieceNUM, byte[] piece) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream("Peer" + this.pid + "Folder" + pieceNUM, false);
            fileOutputStream.write(piece);
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
abstract class ClientThreadStart extends Thread {
 public abstract void run();
}