package network;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import main.Window;

public class PunchClient implements Runnable{

	private String clientIp;
	private int streamPort;

	PunchClient(String clientIp, int streamPort){
		this.clientIp=clientIp;
		this.streamPort=streamPort;
	}
	
    public void goPunch() throws Exception {
    	
    //Window.getInstance().writeSomethingToStatus("punching...");
    // prepare Socket
    DatagramSocket clientSocket = new DatagramSocket();

    // prepare Data
    byte[] sendData = "Hello".getBytes();

    // send Data to Server with fix IP (X.X.X.X)
    // Client1 uses port 7070, Client2 uses port 7071
    DatagramPacket sendPacket = new DatagramPacket(sendData,
            sendData.length, InetAddress.getByName(clientIp), streamPort);

    // output converted Data for check
    System.out.println("IP: " + clientIp + " PORT: " + streamPort);

    // send 5000 Messages for testing
    for (int i = 0; i < 500; i++) {

        // send Message to other client
        sendData = ("Datapacket(" + i + ")").getBytes();
        sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(clientIp), streamPort);
        clientSocket.send(sendPacket);
        
    }

    // close connection
    clientSocket.close();
    }

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
}
