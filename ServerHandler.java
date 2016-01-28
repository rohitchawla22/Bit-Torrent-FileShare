/**
 * Created by rohit ka pc on 7-Nov-15.
 */
import java.net.*;
import java.io.*;
import java.util.*;
import java.lang.*;

abstract class ThreadStartForSERVER extends Thread {

   
    public abstract void run();

}
class ServerHandler extends ThreadStartForSERVER {
	protected Socket socket;
    protected HashMap<Integer, byte[]> split_piece;
    public void storePiecesOfFile(HashMap<Integer, byte[]> pieces_of_File)
    {
        this.split_piece = pieces_of_File;
    }
	//Storing the user input file to a variable for multiThreading 
protected String userFile;
    public void input_File_Name(String userFile)
    {
        this.userFile = userFile;
    }

	protected ObjectOutputStream objectOutputStream;
	protected ObjectInputStream objectInputStream;
    public void socketPort(Socket socket) {
        this.socket = socket;
		System.out.println("++++++++++++++++++");
        
        System.out.println("PEER WITH NAME =>" + this.getName() + "IS MAKING CONNECTION FOR FILE TRANSFER FROM =>" + socket.getPort());
        try {
            objectOutputStream = new ObjectOutputStream(this.socket.getOutputStream());
            objectInputStream = new ObjectInputStream(this.socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	//send the calls received from multiple peers for connection
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

    protected int Client_Number = -1;

    private int genPeerId() {
        for(int i = 0; i < 100; i++) {
            if (Server.StoreConfigPeer.containsKey(i) && (!Server.neighbourPeerInfo.containsKey(i))) {
                Server.neighbourPeerInfo.put(i, (Server.StoreConfigPeer.get(i)).get(0));
                return i;
            }
        }
        return -1;
    }
public void sendResponseToClients()
{
	while (true) {
            try
			{
                System.out.println("Waiting for Clients to Contact");
				System.out.println("=================================================");
                Object quantity;
                while(true) 
				{
                    try
					{
                        quantity = this.objectInputStream.readObject();
                        assert(quantity instanceof String);
                        break;
                    } catch (Exception ignored)
						{

						}
                }
                String check_msg = (String) quantity;
                System.out.println("Server getting response of " + check_msg + " from Peer=> " + Client_Number);
                int p = -1;
                
					if(check_msg.equals("DISPLAY"))
                    {
						
                        // Send chunk list
                        ArrayList<Integer> arrayList = new ArrayList<Integer>(this.split_piece.size());
                        for(int i = 0; i < this.split_piece.size(); i++)
                        {
                            if(this.split_piece.containsKey(i))
                            {
                                arrayList.add(i);
                            }
                        }
                        transfer(arrayList);
                        
					}
                    if(check_msg.equals("NAME"))
					{
							System.out.println("Send the file name of the file owner to the client");
                
                       transfer((Object) this.userFile);// Sending the File name to the Client 
                        
					}
						if(check_msg.equals("ENQUIRE"))
						{
						// Store the number is Pieces numbers
                        p = this.objectInputStream.readInt();
                        // Send the received piece
                        transfer(p);
                        transfer(this.split_piece.get(p));
                        }
                    if(check_msg.equals("SETUP"))
                    {
						// Save the first number as peer's port number 
                        // Return a peer id for client
                        int peer = this.genPeerId();
                        int port = Server.neighbourPeerInfo.get(peer);
                        transfer(peer);
                        transfer(port);
                    }
						if(check_msg.equals("EXIT"))
						{
					    // Exiting Connection with Clients
                        objectOutputStream.close();
                        objectInputStream.close();
                        Server.neighbourPeerInfo.remove(Client_Number);
                        return;
						
						}
						if(check_msg.equals("NEIGHBOURING_PEER"))
						{
					    // Sending the Clients information about neighbouring peers stored in a HASHMAP
                        //System.out.print("The following is the NEIGHBOURING_PEER INFORMATION");
                        int clientPeerId = objectInputStream.readInt();
                        for(int _peer: Server.neighbourPeerInfo.keySet()) {
                            System.out.print("Server now has information of Peers. Try to connect ");
							//System.out.print(_peer + " ");
                        }
                        System.out.println();
                        System.out.println("send response about peers to clients ");
						System.out.println();
                        transfer(Server.neighbourPeerInfo);
                        transfer((Object)(Server.StoreConfigPeer.get(clientPeerId)).get(1));
                        transfer((Object)(Server.StoreConfigPeer.get(clientPeerId)).get(2));
                   
					
						}
						if(check_msg.equals("PIECES"))
						{
							System.out.println("Numbering the pieces and storing it in a buffer");
						
                        p = this.objectInputStream.readInt();
                        // Save PIECES
                        byte[] piece = (byte[]) this.objectInputStream.readObject();
                        	
						}
                    
                
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
                System.out.println("Peer" + this.getName() + " FULL FILE TRANSFERED. CLOSING CONNECTION");
                Server.neighbourPeerInfo.remove(Client_Number);
                return;
            }
        }
}
    @Override
    public void run() {
        sendResponseToClients();
    }
}

