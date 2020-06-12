package network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.jsoup.Jsoup;

import main.Sngltn;

public class DLNAScanner {
	
	HashMap<String,String> confMapFirstRun;
	
	public DLNAScanner(HashMap<String,String> confMapFirstRun){
		this.confMapFirstRun=confMapFirstRun;
	}
	
	public boolean scan() {
		
		boolean keepGoing=true;
		boolean complete=false;        
		final AtomicBoolean timeout = new AtomicBoolean();
		
		try
        {
			timeout.getAndSet(Boolean.FALSE);
            byte[] sendData = new byte[1024];
            //byte[] receiveData = new byte[1024];
            byte[] receiveData;
            String mSearch = "M-SEARCH * HTTP/1.1\r\nHost: 239.255.255.250:1900\r\nMan: \"ssdp:discover\"\r\nMX: 5\r\nST: ssdp:all\r\n\r\n";
            sendData = mSearch.getBytes();

            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("239.255.255.250"), 1900);

            DatagramSocket clientSocket = new DatagramSocket();
            clientSocket.send(sendPacket);

            Thread t = new Thread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					try {
						Thread.sleep(20000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					timeout.getAndSet(Boolean.TRUE);
				}				
            	
            });
            t.start();
            while (keepGoing && !timeout.get())
            {
                receiveData = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                clientSocket.setSoTimeout(17500);
                clientSocket.receive(receivePacket);

                String response = new String(receivePacket.getData());

                if (response == null || response.length() == 0)
                {
                    keepGoing = false;
                }
                else if(response.toLowerCase().contains("satip"))
                {
                    System.out.println(response);
                    String ip=response.subSequence(response.indexOf("LOCATION:")+9,response.indexOf("\r\nSERVER:")).toString();
                    //System.out.println(ip);
                    String html = Jsoup.connect(ip).get().html();
                    String model=html.subSequence(html.indexOf("<modelName>")+11, html.indexOf("</modelName>")).toString().trim();
                    this.confMapFirstRun.put(ip.substring(ip.indexOf("//")+2,ip.lastIndexOf("/")), model);// : ?
                }
                else {
                	System.err.println("no good response:" + response);
                }

            }
            clientSocket.close();
            complete=true;
            return true;
        }
        catch (UnknownHostException ex)
        {
        	Logger.getLogger(DLNAScanner.class.getName()).log(Level.SEVERE, null, ("Unknown Host Exception: " + ex.toString()));
        	complete=false;
            return false;
        }
        catch (SocketException ex)
        {
        	Logger.getLogger(DLNAScanner.class.getName()).log(Level.SEVERE, null, ("Socket Exception: " + ex.toString()));
        	complete=false;
            return false;
        }
        catch (IOException ex)
        {
        	Logger.getLogger(DLNAScanner.class.getName()).log(Level.SEVERE, null, ("IO Exception: " + ex.toString()));
        	complete=false;
            return false;
        }        
	}
}
