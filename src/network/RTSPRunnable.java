package network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class RTSPRunnable implements Runnable {
	
	
		private SATIPClient sic;
	    private boolean doStop = false;

	    public void setSATIPClient(SATIPClient sic) {
	    	this.sic=sic;
	    }
	    
	    public synchronized void doStop() {
	    	if(this.doStop==false) {
		        this.doStop = true;
		        try {
					sic.Teardown();
				} catch (SocketException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }
	    }

	    private synchronized boolean keepRunning() {
	        return this.doStop == false;
	    }

	    @Override
	    public void run() {
	        while(keepRunning()) {
	        		try {
						sic.Option();
					} catch (SocketException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	        		try {
						Thread.sleep(20000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}				
	        }
	        try {
				Thread.currentThread().join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	}
