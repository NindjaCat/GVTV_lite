package channels;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXB;

public class ChannelReader {
	
	private HashMap<Integer,ChannelItem> channels;
	
	public HashMap<Integer, ChannelItem> getChannels() {
		return channels;
	}
	
	public void setChannels(HashMap<Integer, ChannelItem> channels) {
		this.channels = channels;
	}


	private List<ChannelTable.Channel> channelsXML;
	boolean rtsp,http;
	byte type;
	String ipSatIpServer;
	
	public ChannelReader(String operationMode,String ipSatIpServer, byte type) {
		if(operationMode.contains("udp")) {
			channels=new HashMap<>();rtsp=true;http=false;this.type=type;this.ipSatIpServer=ipSatIpServer;}
		else {
			channels=new HashMap<>();rtsp=false;http=true;this.type=type;this.ipSatIpServer=ipSatIpServer;}
	}
	
	
	
	public void readChannelsDVBS() {
		Path path = Paths.get("dlna_channelList.xml");
		if(path==null) {
			try {
				Thread.sleep(999);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		try(InputStream fis=(new FileInputStream(path.toAbsolutePath().toFile()))){
	        ChannelTable ct = JAXB.unmarshal( fis, ChannelTable.class );
	    	channelsXML=ct.getChannel();
	    	for(ChannelTable.Channel c:channelsXML) {

		    		if(rtsp && type==2) { 
		    			channels.put((int)c.getNumber(),new ChannelItem(c.getNumber(),c.getName(),"rtsp://"+this.ipSatIpServer.replace(":80", ":554")+"/?src="+c.getSrc()+"&freq="+c.getFreq()+"&pol="+c.getPol()+"&ro=0.35&msys="+c.getTuneType().toLowerCase().replace("-","")+"&mtype="+c.getModulation().toLowerCase()+"&plts="+(c.getTuneType().contains("2")?"on":"off")+"&sr="+c.getSr()+(c.getFec()==null?"&fec=56":("&fec="+c.getFec()))+"&pids="+c.getPids()));}
		    		else if(http && type==2){
		    			channels.put((int)c.getNumber(),new ChannelItem(c.getNumber(),c.getName(),"http://"+this.ipSatIpServer+"/dlna/?type="+c.tuneType+"&src="+c.getSrc()+"&freq="+c.getFreq()+"&sr="+c.getSr()+"&pol="+c.getPol()+"&pids="+c.getPids()+",17,18"));}
	    			else if(rtsp && type==1) {		    				
		    			channels.put((int)c.getNumber(),new ChannelItem(c.getNumber(),c.getName(),"rtsp://"+this.ipSatIpServer.replace(":80", ":554")+"/?src="+c.getSrc()+"&freq="+c.getFreq()+"&pol="+c.getPol()+"&ro=0.35&msys="+c.getTuneType().toLowerCase().replace("-","")+"&mtype="+c.getModulation().toLowerCase()+"&plts="+(c.getTuneType().contains("2")?"on":"off")+"&sr="+c.getSr()+(c.getFec()==null?"&fec=56":("&fec="+c.getFec()))+"&pids="+c.getPids()));}
	    			else if(http && type==1) {
	    				channels.put((int)c.getNumber(),new ChannelItem(c.getNumber(),c.getName(),"http://"+this.ipSatIpServer+"/dlna/?type=DVB-S-AUTO&src="+c.getSrc()+"&freq="+c.getFreq()+"&sr="+c.getSr()+"&pol="+c.getPol()+"&mtype="+c.getModulation().toLowerCase()+"&pids="+c.getPids()+",17,18"));}
	    			else if(rtsp && type==3) {
		    			channels.put((int)c.getNumber(),new ChannelItem(c.getNumber(),c.getName(),"rtsp://"+this.ipSatIpServer.replace(":80", ":554")+"/?src="+c.getSrc()+"&freq="+c.getFreq()+"&pol="+c.getPol()+"&ro=0.35&msys="+c.getTuneType().toLowerCase().replace("-","")+"&mtype="+c.getModulation().toLowerCase()+"&plts="+(c.getTuneType().contains("2")?"on":"off")+"&sr="+c.getSr()+(c.getFec()==null?"&fec=56":("&fec="+c.getFec()))+"&pids="+c.getPids()));}		    		
		    		else if(http && type==3){
	    				channels.put((int)c.getNumber(),new ChannelItem(c.getNumber(),c.getName(),"http://"+this.ipSatIpServer+"/dlna/?type="+c.tuneType+"&src="+c.getSrc()+"&freq="+c.getFreq()+"&sr="+c.getSr()+"&pol="+c.getPol()+"&pids="+c.getPids()+",17,18"));}	    		
	    	}
	    } catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void readChannelsDVBC() {
		Path pathSD = Paths.get("tvsd.m3u");
		Path pathHD = Paths.get("tvhd.m3u");
		
		int i=1;
		
		try(BufferedReader br1 = new BufferedReader(new FileReader(pathSD.toAbsolutePath().toFile()))){

			String line=null;
			String name=null;
			while((line=br1.readLine())!=null) {
				if(line.startsWith("#EXTINF"))
					name=line.substring(line.indexOf(",")+1,line.length());
				if(line.startsWith("rtsp")) {
					channels.put(i, new ChannelItem(i,name,line.replaceAll("rtsp:\\/\\/\\d*\\.\\d*\\.\\d*\\.\\d*", "rtsp://"+this.ipSatIpServer.replaceAll(":\\d*", ""))));
					i++;
				}
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try(BufferedReader br2 = new BufferedReader(new FileReader(pathHD.toAbsolutePath().toFile()))){

			String line=null;
			String name=null;
			while((line=br2.readLine())!=null) {				
				if(line.startsWith("#EXTINF"))
					name=line.substring(line.indexOf(",")+1,line.length());
				if(line.startsWith("rtsp")) {
					channels.put(i, new ChannelItem(i,name,line.replaceAll("rtsp:\\/\\/\\d*\\.\\d*\\.\\d*\\.\\d*", "rtsp://"+this.ipSatIpServer.replaceAll(":\\d*", ""))));
					i++;
				}
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
