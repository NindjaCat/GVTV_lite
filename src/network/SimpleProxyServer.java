package network;

import java.io.*;
import java.net.*;
import java.util.HashSet;

public class SimpleProxyServer implements Runnable{
	
	private boolean stopMe=false;
	private String host;
	private int remoteport;
	private int localport;
	private static HashSet<SimpleProxyServer> ssps = new HashSet<>();

	private ServerSocket ss=null;
	
	private Socket client = null;
	
	public synchronized static HashSet<SimpleProxyServer> getSsps() {
		return ssps;
	}

  /**
   * runs a single-threaded proxy server on
   * the specified local port. It never returns.
   */
  public void stopMe() {
	  System.err.println("Socket is closed programmatically");
	  this.stopMe=true;
	  try {
		  if(client!=null) {
			  client.close();
		  }
		  if(ss!=null) {
			  ss.close();
		  }
		  getSsps().remove(this);
		  
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
  }	

  public SimpleProxyServer(String host, int remoteport, int localport) {
	  this.host=host;
	  this.remoteport=remoteport;
	  this.localport=localport; //presented port
	  getSsps().add(this);
  }
  
  
  
@Override
public String toString() {
	return "SimpleProxyServer [stopMe=" + stopMe + ", host=" + host + ", remoteport=" + remoteport + ", localport="
			+ localport + "]";
}  

@Override
public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((host == null) ? 0 : host.hashCode());
	result = prime * result + localport;
	result = prime * result + remoteport;
	result = prime * result + (stopMe ? 1231 : 1237);
	return result;
}

@Override
public boolean equals(Object obj) {
	if (this == obj)
		return true;
	if (obj == null)
		return false;
	if (getClass() != obj.getClass())
		return false;
	SimpleProxyServer other = (SimpleProxyServer) obj;
	if (host == null) {
		if (other.host != null)
			return false;
	} else if (!host.equals(other.host))
		return false;
	if (localport != other.localport)
		return false;
	if (remoteport != other.remoteport)
		return false;
	if (stopMe != other.stopMe)
		return false;
	return true;
}

public void runServer()
      throws IOException {
    // Create a ServerSocket to listen for connections with
    

    final byte[] request = new byte[1024];
    byte[] reply = new byte[4096];

    wouter: while (this.stopMe==false) {
    	synchronized (this){
	    	if((ss==null||ss.isClosed())&&this.stopMe==false) {
	    		for(SimpleProxyServer s:getSsps()) {
	    			if(s.getLocalport()==this.localport&&s!=this&&s.stopMe==false) {
	    				System.err.println("Found a conflicting and running proxy. wtf?");
	    				s.stopMe();
	    				try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
	    				continue wouter;
	    				}
	    		}
	    		ss = new ServerSocket(localport);
	    	}
    	}
      Socket server = null;
      try {
        // Wait for a connection on the local port
    	System.err.println("Wait for a connection on the local port");
        client = ss.accept();
        if(stopMe) {
        	getSsps().remove(this);
        	return;
        }
  	  	System.err.println("Wait for a connection on the local port - got it!");
  	  	System.err.println("client: "+client.getRemoteSocketAddress());
        final InputStream streamFromClient = client.getInputStream();
        final OutputStream streamToClient = client.getOutputStream();

        // Make a connection to the real server.
        // If we cannot connect to the server, send an error to the
        // client, disconnect, and continue waiting for connections.
        try {
          server = new Socket(host, remoteport);
        } catch (IOException e) {
          PrintWriter out = new PrintWriter(streamToClient);
          out.print("Proxy server cannot connect to " + host + ":" + remoteport + ":\n" + e + "\n");
          out.flush();
          client.close();
          continue;
        }

        // Get server streams.
        final InputStream streamFromServer = server.getInputStream();
        final OutputStream streamToServer = server.getOutputStream();

        // a thread to read the client's requests and pass them
        // to the server. A separate thread for asynchronous.
        Thread t = new Thread() {
          public void run() {
            int bytesRead;
            int counter=0;
            try {
              while (stopMe==false && (bytesRead = streamFromClient.read(request)) != -1) {
            	if(counter<1) {
	                streamToServer.write(request, 0, bytesRead);
	                streamToServer.flush();
	                counter++;
	                System.err.println("."+counter);
              	}
              }
            } catch (IOException e) {
            }

            // the client closed the connection to us, so close our
            // connection to the server.
            try {
              streamToServer.close();
            } catch (IOException e) {
            }
          }
        };

        // Start the client-to-server request thread running
        if(stopMe==false&&!server.isClosed()) //2. new
        	t.start();
        else {
        	try {// new
	        	client.close();
	        	server.close();
        	} catch(Exception ex) 
        		{	;}
        	continue;
        }

        // Read the server's responses
        // and pass them back to the client.
        int bytesRead;
        try {
          while (this.stopMe==false && (bytesRead = streamFromServer.read(reply)) != -1) {
            streamToClient.write(reply, 0, bytesRead);
            streamToClient.flush();
          }
          streamToClient.close();
          streamFromServer.close();
          streamFromClient.close();
          server.close();
        } catch (IOException e) {        		
        }

        // The server closed its connection to us, so we close our
        // connection to our client.
        streamToClient.close();
        streamFromServer.close();
        streamFromClient.close();
        server.close();
      } catch (IOException e) {
        System.err.println("outer: "+e);
        continue;
      } finally {
        try {
          if (server != null)
            server.close();
          if (client != null)
            client.close();
          ss.close();
        } catch (IOException e) {
        }
      }
      
    }
    System.err.println("SimpleProxyServer says goodbye!");
    getSsps().remove(this);
    ss.close();
  }


	public String getHost() {
		return host;
	}

	public int getRemoteport() {
		return remoteport;
	}

	public int getLocalport() {
		return localport;
	}
	
	public boolean isShouldBeStopped() {
		return this.stopMe;
	}

@Override
public void run() {
	// TODO Auto-generated method stub
	try {
		runServer();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}

}