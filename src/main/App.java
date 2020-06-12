package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import channels.ChannelReader;
import channels.ObjectFactory;
import channels.Settings;
import channels.Settings.Servers;
import network.DLNAScanner;
import network.HTTPCommandReceiver;
import network.Orchestrator;

public class App {
	
	
	private static HashMap<String,String> confMapFirstRun =new HashMap<>();
	
	private static HttpServer server=null;
	private static ResourceConfig resourceConfig = new ResourceConfig();
	
	public static Settings settings=null;
	public static Thread orchThread=null;

	
	
	public static void main(String [] args) {
		
		Sngltn.getInstance();
		
		File file = new File("config_test.xml");
		DLNAScanner dlnas=null;
		
		try (FileReader fileReader=new FileReader(file)){
			if(fileReader.read()==-1) {					
				dlnas=new DLNAScanner(confMapFirstRun);
				System.err.println("Scan completed: "+dlnas.scan());
			}
		} catch (FileNotFoundException e1) {
			dlnas=new DLNAScanner(confMapFirstRun);
			System.err.println("Scan completed: "+dlnas.scan());
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 


		if(file!=null && file.canRead()) {
			System.err.println("reading config file");
			
			JAXBContext jaxbContext;
			try {
				jaxbContext = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName());			
				Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
	//			unmarshaller.setSchema( schema );
				settings=(Settings)unmarshaller.unmarshal( file );
			} catch (JAXBException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			if(settings!=null) {
				for(Settings.Servers.Server s:settings.getServers().getServer()) {
					String id=String.valueOf(s.getId()), ip=s.getIp(), model=s.getModel(), ports=s.getPorts(), protocol=s.getProtocol(), command=s.getCommand();
					System.out.println(id+" "+ip+" "+model+" "+ports);
				}
			}		
		}


		
		Path path=null;
		path = Paths.get("config.xml");
		if(!Files.exists(path)|| !file.canRead())
			{
				try(PrintWriter printWriter = new PrintWriter(file)) {
				    
				
			    printWriter.print("<?xml version=\"1.0\"?>\r\n");
			    printWriter.print("<Settings>\r\n");
			    printWriter.print(" <Servers>\r\n");
			    
			    int k=0;
			    for(Map.Entry<String, String> entry:confMapFirstRun.entrySet())
			    {
			    	
			    	printWriter.print("	 <Server><!-- you can define as many servers as you like -->\r\n");
			    	printWriter.print("		 <id>"+(k++)+"</id>\r\n");
			    	printWriter.print("		 <ip>"+entry.getKey()+"</ip>\r\n");
				    printWriter.print("		 <model>"+entry.getValue()+"</model>\r\n");
				    printWriter.print("		 <ports><!--choose two ports (comma separated) and configure your router--></ports>\r\n");
				    printWriter.print("		 <protocol>tcp</protocol>\r\n");
				    printWriter.print("		 <command>string_id</command>\r\n");
			    	printWriter.print("	 </Server>\r\n");
			    }
			    printWriter.print(" </Servers>\r\n");
			    printWriter.print("</Settings>\r\n");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		
		if(settings==null) {
			System.err.println("No valid configuration. Ignore on first run!");
			return;
		}

		
		System.err.println("Configuration may be valid.");
		HashMap<Pair<Byte,String>,Dispatcher> tvMap= new HashMap<>();
		
        Orchestrator donaldRomain = new Orchestrator();
        orchThread = new Thread(donaldRomain);
        orchThread.setName("donaldRomain");
        orchThread.setPriority(Thread.MAX_PRIORITY);
        orchThread.start();
		
		for(Settings.Servers.Server entry:settings.getServers().getServer()) {
			
			ChannelReader cr=null;
			if(entry.getModel().contains("TSS400") && entry.getModel().contains("MKII")) {
				cr=new ChannelReader(entry.getProtocol()+"_"+entry.getCommand(),entry.getIp(),(byte)2);
				cr.readChannelsDVBS();}
			else if(entry.getModel().contains("TSS400")) {
				cr=new ChannelReader(entry.getProtocol()+"_"+entry.getCommand(),entry.getIp(),(byte)1);
				cr.readChannelsDVBS();}
			else if(entry.getModel().contains("FRITZ")) {
				cr=new ChannelReader(entry.getProtocol()+"_"+entry.getCommand(),entry.getIp(),(byte)4);
				cr.readChannelsDVBC();}
			else {
				cr=new ChannelReader(entry.getProtocol()+"_"+entry.getCommand(),entry.getIp(),(byte)3);				
				cr.readChannelsDVBS();}
			
			////////////////////////////////////////////////////////////////////
			Pair<Integer, String> currentServer=Pair.of((int)entry.getId(),entry.getIp());
			////////////////////////////////////////////////////////////////////
			
			Sngltn.getInstance().tvMapAdd(currentServer,new Dispatcher(entry.getIp(),Triple.of(entry.getModel(),entry.getPorts(),entry.getProtocol()+"_"+entry.getCommand()),cr,currentServer.getLeft(),entry.getProtocol()));
			if(Sngltn.getInstance().isBadConfig())
				return;

			Integer portCommand=Integer.valueOf((entry.getPorts().subSequence(0, entry.getPorts().indexOf(","))).toString());
			Sngltn.getInstance().addPortToChannelReader(portCommand,cr);
			Sngltn.getInstance().addIdToChannelReader(currentServer.getLeft(),cr);
			
			Thread aliveThread=new Thread(new Runnable(){
	            @Override
	            public void run() {
	                while(true)
	                {
	                    try {
	                        Thread.sleep(100000);
	                        int interval=1600000;
	                        if(Sngltn.getInstance().tvMapGet(currentServer).clientAlive.getTime()-((new Date()).getTime())<-interval) { //-900000
	                        	//tvMap.get(currentServer).stopAll();
	                        	Sngltn.getInstance().tvMapGet(currentServer).evaluateChannelString("stop", false);//setChannel(9999) => clientAlive=new Date();	                        	
		                        System.out.print("timeout triggered " + (Sngltn.getInstance().tvMapGet(currentServer).clientAlive.getTime()-((new Date()).getTime())+", "));
		                        Sngltn.getInstance().tvMapGet(currentServer).clientAlive=new Date();
		                        Sngltn.getInstance().tvMapGet(currentServer).wd.writeSomethingToMainWindow("Client timeout. Terminating: " + (Sngltn.getInstance().tvMapGet(currentServer).clientAlive.getTime()-((new Date()).getTime())));
	                    	}
	                    } catch (InterruptedException ie) {
	                        System.err.println("Alive thread status error.");
	                    }
	                }
	            }
	        });
			
		        if(Sngltn.getInstance().isBadConfig()) {
					System.err.println("Configuration not valid.");
		        	return;}
		        aliveThread.setPriority(Thread.MIN_PRIORITY);
		        aliveThread.setName("alive " + entry.getId());
		        aliveThread.start();
		}
		
        Thread serverThread=null;
        serverThread=new Thread(new Runnable(){
        	
        	@Override
            public void run() {
		        ResourceConfig resourceConfig = new ResourceConfig();
//		        resourceConfig.register(PicturesImpl.class);
		        resourceConfig.register(HTTPCommandReceiver.class);
		        //HttpHandler handler = RuntimeDelegate.getInstance().createEndpoint(resourceConfig, HttpHandler.class);		        


		        HttpServer server=null;
				try {
					server = GrizzlyHttpServerFactory.createHttpServer(new URI("http://0.0.0.0:80/"),resourceConfig, true);
					
				} catch (URISyntaxException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
		
		        try {

		            server.start();
		            System.out.println("Press any key to stop the server...");
		        } catch (Exception e) {
		            System.err.println(e);
		        }
        	}
        });
        serverThread.setPriority(Thread.NORM_PRIORITY);
        serverThread.start();

	}
}
