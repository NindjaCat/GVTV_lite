package main;

import java.net.ServerSocket;
import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import channels.ChannelItem;
import channels.ChannelReader;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.log.LogLevel;
import uk.co.caprica.vlcj.log.NativeLog;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

public class Sngltn {

	private static Sngltn instance=null;

	private boolean badConfig=false;
	private final HashMap<Integer, ServerSocket> socMap= new HashMap<>();
	private final HashMap<String, Integer> ipToSocMap= new HashMap<>();
	private static final MediaPlayerFactory mediaPlayerFactory=new MediaPlayerFactory();
	
	private final static HashMap<String,InternalTranscoder> currentTranscoder = new HashMap<>();
	private static HashMap<Integer,ChannelReader> portToChannelReaderMap;
	private static HashMap<Integer,ChannelReader> idToChannelReaderMap;

	private static HashMap<String,String> lastCommand = new HashMap<>();
	private static Window wd;
	
	private static final HashSet<EmbeddedMediaPlayer> reusePlayer = new HashSet<>();
	private static final HashSet<EmbeddedMediaPlayer> garbagePlayer = new HashSet<>();
	
	private static AtomicBoolean blockedAB = new AtomicBoolean(false);
	private static Long lastCommandTS=LocalTime.now().getLong(ChronoField.MILLI_OF_DAY);

	
	private static Map<Pair<Integer,String>,Dispatcher> tvMap=Collections.synchronizedMap(new HashMap<Pair<Integer,String>,Dispatcher>());
	private static HashMap<Triple<String, ChannelItem,Integer>, InternalTranscoder> internalTranscoderMap = new HashMap<Triple<String, ChannelItem,Integer>, InternalTranscoder>();
	
	private ConcurrentSkipListMap<Integer, LinkedTransferQueue<String>> commandsLake = new ConcurrentSkipListMap<Integer, LinkedTransferQueue<String>>();	//server-id, index, command
	
	private static NativeLog logging=null;
	
	public static Sngltn getInstance() {
		if(instance==null) {
			logging=mediaPlayerFactory.application().newLog();
			logging.setLevel(LogLevel.ERROR);						
			instance=new Sngltn();
			wd = new Window();
	        wd.startMethod("REST commands",350,200,-1);
			}
		return instance;
	}
		
	private Sngltn() {	
//		sourcePort=-1;
	}
	
	public static Integer getFreeInternalPort() {
		int port=20000;
		
		if(internalTranscoderMap==null || internalTranscoderMap.isEmpty()) {
			return port;
		}
		
		outer: 
			while(true) {
				port=20000 + (int) Math.floor(Math.random()*10000);
				synchronized(internalTranscoderMap) {
					for(Map.Entry<Triple<String, ChannelItem,Integer>, InternalTranscoder> it:internalTranscoderMap.entrySet()) {
						if(port == it.getValue().getChosenInternalStreamPort()) {
							continue outer;
						}
					}
				}
				break;
			}
		
		return port;
	}

	public int getReusePlayerSize() {
		return reusePlayer.size();
	}
	
	public EmbeddedMediaPlayer getReusePlayer() {
		if(reusePlayer.isEmpty())
			return null;
		EmbeddedMediaPlayer temp = (EmbeddedMediaPlayer)reusePlayer.toArray()[0];
		if(temp!=null)
			reusePlayer.remove(temp);
		writeToStatus();
		return temp;
	}		

	public void addReusePlayer(EmbeddedMediaPlayer reusePlayer) {
		this.reusePlayer.add(reusePlayer);
		writeToStatus();
	}

	public void addGarbagePlayer(EmbeddedMediaPlayer reusePlayer) {		
		this.garbagePlayer.add(reusePlayer);
		writeToStatus();
	}
	
	public HashMap<Integer, ServerSocket> getSocMap() {
		return socMap;
	}

	public HashMap<String, Integer> getIpToSocMap() {
		return ipToSocMap;
	}

	public boolean isBadConfig() {
		return badConfig;
	}

	public void setBadConfig(boolean badConfig) {
		this.badConfig = badConfig;
	}
	
	public static HashMap<String,String> getLastCommand() {
		return lastCommand;
	}

	public static void addLastCommand(String id,String command) {
		Sngltn.lastCommand.put(id,command);
	}
	
	public static void writeToStatus() {
		wd.repaint();
	}
	
	public static void writeToStatusMain(String text) {
		wd.writeSomethingToMainWindow(text);
		wd.repaint();
	}	

	public MediaPlayerFactory getMediaPlayerFactory() {
		return mediaPlayerFactory;
	}
	
	public static HashMap<Triple<String,ChannelItem,Integer>, InternalTranscoder> getInternalTranscoderMap() {
		return internalTranscoderMap;
	}

	
	public static void addInternalTranscoderToMap(Triple<String,ChannelItem,Integer> trip, InternalTranscoder itc) {
		internalTranscoderMap.put(trip, itc);
	}
	
	public static int getInternalTranscoderMapSize() {
		return internalTranscoderMap.size();
	}
	

	public void addPortToChannelReader(Integer portCommand, ChannelReader cr) {
		if(portToChannelReaderMap == null)
			portToChannelReaderMap = new HashMap<>();
		portToChannelReaderMap.put(portCommand, cr);
	}
	
	public void addIdToChannelReader(Integer id, ChannelReader cr) {
		if(idToChannelReaderMap == null)
			idToChannelReaderMap = new HashMap<>();
		idToChannelReaderMap.put(id, cr);
	}
	
	public ChannelReader getPortToChannelReader(Integer portCommand) {
		if(portToChannelReaderMap == null)
			portToChannelReaderMap = new HashMap<>();
		return portToChannelReaderMap.get(portCommand);
	}
	
	public ChannelReader getIdToChannelReader(Integer id) {
		if(idToChannelReaderMap == null)
			idToChannelReaderMap = new HashMap<>();
		return idToChannelReaderMap.get(id);
	}

	
	public String getStringSim(Integer id, String cha) {
		final ChannelReader temp = getIdToChannelReader(id);
		return temp.getChannels().get(StringSim.similarity(cha, temp.getChannels())).getName();
	}
	
	public Dispatcher tvMapGet(Pair<Integer, String> pair) {
		return tvMap.get(pair);
	}

	public void tvMapAdd(Pair<Integer, String> pair, Dispatcher dispacher) {
		tvMap.put(pair, dispacher);
	}	
	
	public boolean isCurrentlyBlocked() {
	    synchronized (blockedAB) {
	    	return blockedAB.get();
	    }
	}
	
	public void setCurrentlyBlocked() {
	    	lastCommandTS=LocalTime.now().getLong(ChronoField.MILLI_OF_DAY);
	    	blockedAB.set(true);
	}
	
	public void setCurrentlyNotBlocked() {
	    synchronized (blockedAB) {
	    	blockedAB.set(false);
	    }
	}
	
	public Dispatcher tvMapGetDispacherById(Integer id) {
		Iterator<Pair<Integer, String>> itr = tvMap.keySet().iterator();
		synchronized (tvMap) 
        {
            for(Pair<Integer, String> pair : tvMap.keySet()) {
            	if(pair.getLeft().equals(id))
            		return tvMap.get(pair);
            }
        }
		return null; 
	}
	
	public void clearCommandsForIdFromQueue(final Integer id) {
		commandsLake.get(id).clear();
	}
	
	public String getCommandsForIdFromQueue(final Integer id) {
		final LinkedTransferQueue<String> temp = commandsLake.get(id);
		String firstCommandInQueue=null;
		if(temp!=null) {
			if(temp.isEmpty())
				return null;
			if(temp.size() == 1) {
				firstCommandInQueue = temp.poll();
				return firstCommandInQueue;
			}
			if(temp.size()>1) {
				firstCommandInQueue = "#" + temp.poll();
				return firstCommandInQueue;
				}
		} else {
			return firstCommandInQueue;
		}
		return firstCommandInQueue;	
	}
	
	public void addCommandsToQueue(final Integer id, final String command) {
		if(this.commandsLake.get(id)==null) {
			this.commandsLake.put(id, new LinkedTransferQueue<String>());
			this.commandsLake.get(id).add(command);
		} else if(this.commandsLake.get(id).size()==0){
			this.commandsLake.get(id).add(command);
		} else {
			this.commandsLake.get(id).add(command);
		}
	}


	public enum OperationMode {
	    tcp(1), udp(2);
		
	    private int value;

	    private OperationMode(int value) {
	        this.value = value;
	    }

	    public int getValue() {
	        return value;
	    }
	}
	
	
}
