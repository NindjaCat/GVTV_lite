package main;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.tuple.Triple;


import channels.ChannelItem;
import channels.ChannelReader;
import network.RTSPRunnable;
import network.SimpleProxyServer;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.media.callback.nonseekable.NonSeekableCallbackMedia;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.videosurface.VideoSurface;


public class InternalTranscoder {//extends Application {

	private ChannelItem channel;
	private String serverIp;
	private String multicastIp;
	private Byte quality=0;	
	private Byte resolution=0;
	
	private String protocol;
	
	private boolean getSuccess=true;
	private boolean error=false;
	
	private Integer chosenInternalStreamPort;
	
    boolean shouldBePlaying=false;
    private ChannelReader cr;
	private Canvas canvas;
    private static MediaPlayerFactory factory;
    private EmbeddedMediaPlayer mediaPlayer;
    private VideoSurface videoSurface;
    
    private final ConcurrentHashMap<String,Boolean> clientsMap = new ConcurrentHashMap<>(); //Boolean not used
    private final ConcurrentHashMap<String,SimpleProxyServer> sps=new ConcurrentHashMap<>();
    private boolean garbage=false;
    

	private RTSPRunnable r=null;
	private MediaPlayerReadyEventHandler mpea;

	private AtomicInteger cdCounter = new AtomicInteger(200000);
	
	private Window wd;
    
   
	
	public InternalTranscoder(final ChannelItem channel) {
		
		this.channel=channel;

		if(factory==null)
			factory = Sngltn.getInstance().getMediaPlayerFactory();
		mediaPlayer=Sngltn.getInstance().getReusePlayer();		
		if(mediaPlayer==null) {
			mediaPlayer = factory.mediaPlayers().newEmbeddedMediaPlayer();}
		error=false;
        mediaPlayer.events().addMediaPlayerEventListener(mpea=new MediaPlayerReadyEventHandler(this));
        canvas = new Canvas();
        canvas.setSize(350, 195);
        canvas.setBackground(Color.black);      
        

        videoSurface = factory.videoSurfaces().newVideoSurface(canvas);
        mediaPlayer.videoSurface().set(videoSurface);
        
        wd=new Window();
        wd.setTranscoder(this);
        wd.getContentPane().add(canvas, BorderLayout.CENTER);
        wd.startMethod("Transcoder new",350,250,this.chosenInternalStreamPort=Sngltn.getFreeInternalPort());
        wd.writeSomethingToStatus(""+this.getChosenInternalStreamPort());
        
	}
	
	
	public synchronized boolean startTranscoder(final String port, String id) throws IOException {		
		        
		
		if(this.garbage || this.error) {
			System.err.println("1.TRANCODER IS GARBAGE/ERROR.");
			this.stopTranscoderNowForcfully(true);
			this.garbage=true;
			return false;
		}
		clientsMap.put(port,false);

		String statusTemp = wd.getMyStatus();
		if(!statusTemp.contains(id + " "))
			wd.writeSomethingToStatus(statusTemp + " " + id + ",");
		wd.repaint();
		System.err.println("CHANNEL REQUEST by id "+id+".");
		wd.setTitle(this.channel.getName()+" q"+this.quality +","+resolution+ "," + this.protocol);

		System.err.println("mpea.isPlaying()=" + mpea.isPlaying() +" "+ this.toString() + " & " + port);
		if(mpea!=null && mpea.isPlaying()) {

			return !this.garbage;
		}else if(shouldBePlaying) {
			this.removeClient(port, id);
			return false;
		}
		

        	
            if(Sngltn.getInternalTranscoderMapSize()>3)
            	this.resolution=new Byte((byte)1);
        	
            String resolutionString="";
            if(this.resolution==((byte)1)&&channel.getName().toLowerCase().contains("hd"))
            	resolutionString="width=828,";
        	String deinterlace1="--deinterlace=1";
            String deinterlace2="--deinterlace-mode=yadif";
            String preset="faster";
            
                switch(Sngltn.getInternalTranscoderMapSize()){
                case 0:
                	preset="faster";
                    break;
                case 1:
                	preset="faster";
                    break;
                case 2:
                	preset="faster";
                    break;
                case 3:
                	preset="veryfast";
                    break;
                case 4:
                	preset="superfast";
                    break;
                default:
                	preset="ultrafast";
                    break;
                }
            
            if(this.channel.getName().toLowerCase().contains("hd")) {
            	deinterlace1="--deinterlace=0";
            	deinterlace2="--deinterlace-mode=disabled";
            }
            System.err.println(resolutionString);

            String extension="ts";
            
            String[] options= {""};
            
            
            String statusTemp2 = wd.getMyStatus();
    			wd.writeSomethingToStatus(preset.substring(0, 1) + ":" + this.quality + " " + statusTemp2);
            
            if(this.protocol.equals("tcp")) {
            switch(this.quality){ //tune=film,
            
            	case 0: options = new String[]{":sout=#transcode{soverlay,sfilter=marq{marquee='1',opacity=120,size=16,timeout=30000},vcodec=h264,"+resolutionString+"venc=x264{preset="+preset+",profile=baseline,keyint=100,vbv-maxrate=3250,vbv-bufsize=1625,level=4,ratetol=10.0},acodec=aac,ab=128,channels=2,samplerate=48000,intra-refresh=true}			:duplicate{dst=http{mux=ts,dst=:"+chosenInternalStreamPort+"/go."+extension+"},dst=display}","--network-caching=50","--sout-mux-caching=25","--live-caching=35","--sout-x264-lookahead=25",deinterlace1,deinterlace2,"--avcodec-threads=1",":no-sout-keep",":no-sout-all"};break; //"--sout-x264-tune=zerolatency" "--sout-x264-tune=film"
            	case 1: options = new String[]{":sout=#transcode{soverlay,sfilter=marq{marquee='2',opacity=120,size=16,timeout=30000},vcodec=h264,"+resolutionString+"venc=x264{preset="+preset+",profile=baseline,keyint=100,vbv-maxrate=3000,vbv-bufsize=1500,level=4,ratetol=10.0},acodec=aac,ab=128,channels=2,samplerate=48000,intra-refresh=true}			:duplicate{dst=http{mux=ts,dst=:"+chosenInternalStreamPort+"/go.ts},dst=display}","--network-caching=50","--sout-mux-caching=25","--live-caching=35","--sout-x264-lookahead=25",deinterlace1,deinterlace2,"--avcodec-threads=1",":no-sout-keep",":no-sout-all"};break; //"--sout-x264-tune=film"
            	case 2: options = new String[]{":sout=#transcode{soverlay,sfilter=marq{marquee='3',opacity=120,size=16,timeout=30000},vcodec=h264,"+resolutionString+"venc=x264{preset="+preset+",						keyint=100,vbv-maxrate=2700,vbv-bufsize=1000,level=4,ratetol=10.0},acodec=aac,ab=128,channels=2,samplerate=48000,intra-refresh=true}			:duplicate{dst=http{mux=ts,dst=:"+chosenInternalStreamPort+"/go.ts},dst=display}","--network-caching=50","--sout-mux-caching=25","--live-caching=35","--sout-x264-lookahead=25",deinterlace1,deinterlace2,"--sout-x264-tune=film","--avcodec-threads=1",":no-sout-keep",":no-sout-all"};break;
            	case 3: options = new String[]{":sout=#transcode{soverlay,sfilter=marq{marquee='4',opacity=120,size=16,timeout=30000},vcodec=h264,"+resolutionString+"venc=x264{preset="+preset+",						keyint=100,vbv-maxrate=500,vbv-bufsize=500,level=4,ratetol=10.0},acodec=aac,ab=128,channels=2,samplerate=48000,intra-refresh=true}			:duplicate{dst=http{mux=ts,dst=:"+chosenInternalStreamPort+"/go.ts},dst=display}","--network-caching=50","--sout-mux-caching=25","--live-caching=35","--sout-x264-lookahead=25",deinterlace1,deinterlace2,"--sout-x264-tune=film","--avcodec-threads=1",":no-sout-keep",":no-sout-all"};break;
            	case 4: options = new String[]{":sout=#transcode{soverlay,sfilter=marq{marquee='5',opacity=120,size=16,timeout=30000},vcodec=h264,width=288,venc=x264{preset=fast,										keyint=100,vbv-maxrate=175,vbv-bufsize=175,level=3.0,ratetol=10.0},acodec=aac,ab=128,channels=1,samplerate=48000,intra-refresh=true}			:duplicate{dst=http{mux=ts,dst=:"+chosenInternalStreamPort+"/go.ts},dst=display}","--network-caching=50","--sout-mux-caching=25","--live-caching=35","--sout-x264-lookahead=25",":no-sout-keep",":no-sout-all"};break;
            	case 5: options = new String[]{":sout=#transcode{vcodec=none,acodec=mp4a,ab=128,channels=2,samplerate=48000}:duplicate{dst=http{dst=:"+chosenInternalStreamPort+"/go.ts},dst=display}","--network-caching=30","--sout-mux-caching=0","--live-caching=0",":sout-keep",":no-sout-all"};break;
            	}
            }else if(this.protocol.equals("udp")){
                switch(this.quality){
            	case 0: options = new String[]{":sout=#transcode{soverlay,sfilter=marq{marquee='R1',opacity=120,size=16,timeout=30000},vcodec=h264,venc=x264{preset=ultrafast,tune=film,profile=high,keyint=150,vbv-maxrate=5500,vbv-bufsize=2500,level=4,ratetol=1.1},acodec=mpga,ab=128,channels=2,samplerate=48000,deinterlace,intra-refresh=true}		:duplicate{dst=rtp{mux=ts,dst="+multicastIp+",port="+(chosenInternalStreamPort+0)+",sdp=sap,ttl=100},dst=display}","--sout-rtp-sap","--sout-rtp-rtcp-mux","--sout-rtp-ttl=100","--network-caching=50","--sout-mux-caching=50","--sout-rtp-caching=100","--live-caching=100","--sout-x264-lookahead=25","--no-deblock","--sout-deinterlace-mode=yadif","--sout-x264-tune=film","--avcodec-threads=0",":sout-keep",":no-sout-all"};break; // file:///C:\\Temp\\sdp_"+chosenInternalStreamPort+".sdp ,,,"--sout-rtp-rtcp-mux","--rtcp-port="+(chosenInternalStreamPort+1) , +",port-video="+(chosenInternalStreamPort+0) "--sout-x264-tune=zerolatency"
            	case 1: options = new String[]{":sout=#transcode{soverlay,sfilter=marq{marquee='R2',opacity=120,size=16,timeout=30000},vcodec=h264,venc=x264{preset=ultrafast,tune=film,profile=high,keyint=150,vbv-maxrate=4500,vbv-bufsize=1750,level=4,ratetol=1.1},acodec=mpga,ab=128,channels=2,samplerate=48000,deinterlace,intra-refresh=true}		:duplicate{dst=rtp{mux=ts,dst="+multicastIp+",port="+(chosenInternalStreamPort+0)+",sdp=sap,ttl=100},dst=display}","--sout-rtp-sap","--sout-rtp-rtcp-mux","--sout-rtp-ttl=100","--network-caching=50","--sout-mux-caching=50","--sout-rtp-caching=100","--live-caching=100","--sout-x264-lookahead=25","--no-deblock","--sout-deinterlace-mode=yadif","--sout-x264-tune=film","--avcodec-threads=0",":sout-keep",":no-sout-all"};break; // file:///C:\\Temp\\sdp_"+chosenInternalStreamPort+".sdp ,,,"--sout-rtp-rtcp-mux","--rtcp-port="+(chosenInternalStreamPort+1) , +",port-video="+(chosenInternalStreamPort+0) "--sout-x264-tune=zerolatency"
            	case 2: options = new String[]{":sout=#transcode{soverlay,sfilter=marq{marquee='R3',opacity=120,size=16,timeout=30000},vcodec=h264,venc=x264{preset=veryfast,tune=film,profile=high,keyint=150,vbv-maxrate=3000,vbv-bufsize=1000,level=4,ratetol=1.1},acodec=mpga,ab=128,channels=2,samplerate=48000,deinterlace,intra-refresh=true}		:duplicate{dst=rtp{mux=ts,dst="+multicastIp+",port="+(chosenInternalStreamPort+0)+",sdp=sap,ttl=100},dst=display}","--sout-rtp-sap","--sout-rtp-rtcp-mux","--sout-rtp-ttl=100","--network-caching=50","--sout-mux-caching=50","--sout-rtp-caching=100","--live-caching=100","--sout-x264-lookahead=25","--no-deblock","--sout-deinterlace-mode=yadif","--sout-x264-tune=film","--avcodec-threads=0",":sout-keep",":no-sout-all"};break; // file:///C:\\Temp\\sdp_"+chosenInternalStreamPort+".sdp ,,,"--sout-rtp-rtcp-mux","--rtcp-port="+(chosenInternalStreamPort+1) , +",port-video="+(chosenInternalStreamPort+0) "--sout-x264-tune=zerolatency"
            	case 3: options = new String[]{":sout=#transcode{soverlay,sfilter=marq{marquee='R4',opacity=120,size=16,timeout=30000},vcodec=h264,venc=x264{preset=veryfast,tune=film,profile=high,keyint=150,vbv-maxrate=500,vbv-bufsize=500,level=4,ratetol=1.1},acodec=mpga,ab=128,channels=2,samplerate=48000,deinterlace,intra-refresh=true}			:duplicate{dst=rtp{mux=ts,dst="+multicastIp+",port="+(chosenInternalStreamPort+0)+",sdp=sap,ttl=100},dst=display}","--sout-rtp-sap","--sout-rtp-rtcp-mux","--sout-rtp-ttl=100","--network-caching=50","--sout-mux-caching=50","--sout-rtp-caching=100","--live-caching=100","--sout-x264-lookahead=25","--no-deblock","--sout-deinterlace-mode=yadif","--sout-x264-tune=film","--avcodec-threads=0",":sout-keep",":no-sout-all"};break; // file:///C:\\Temp\\sdp_"+chosenInternalStreamPort+".sdp ,,,"--sout-rtp-rtcp-mux","--rtcp-port="+(chosenInternalStreamPort+1) , +",port-video="+(chosenInternalStreamPort+0) "--sout-x264-tune=zerolatency"
            	case 4: options = new String[]{":sout=#transcode{soverlay,sfilter=marq{marquee='R5',opacity=120,size=16,timeout=30000},vcodec=h264,venc=x264{preset=veryfast,tune=film,profile=high,keyint=150,vbv-maxrate=250,vbv-bufsize=250,level=4,ratetol=1.1},acodec=mpga,ab=128,channels=2,samplerate=48000,deinterlace,intra-refresh=true}			:duplicate{dst=rtp{mux=ts,dst="+multicastIp+",port="+(chosenInternalStreamPort+0)+",sdp=sap,ttl=100},dst=display}","--sout-rtp-sap","--sout-rtp-rtcp-mux","--sout-rtp-ttl=100","--network-caching=50","--sout-mux-caching=50","--sout-rtp-caching=100","--live-caching=100","--sout-x264-lookahead=25","--no-deblock","--sout-deinterlace-mode=yadif","--sout-x264-tune=film","--avcodec-threads=0",":sout-keep",":no-sout-all"};break; // file:///C:\\Temp\\sdp_"+chosenInternalStreamPort+".sdp ,,,"--sout-rtp-rtcp-mux","--rtcp-port="+(chosenInternalStreamPort+1) , +",port-video="+(chosenInternalStreamPort+0) "--sout-x264-tune=zerolatency"
            	case 5: options = new String[]{":sout=#transcode{vcodec=none,acodec=mp4a,ab=128,channels=2,samplerate=48000}:duplicate{dst=rtp{mux=ts,dst="+multicastIp+",port="+(chosenInternalStreamPort+0)+",sdp=sap,ttl=100},dst=display}","--network-caching=30","--sout-mux-caching=0","--live-caching=0",":sout-keep",":no-sout-all"};break;
                }
            }else if(this.protocol.equals("htm")) {
            	if(this.quality!=5)
            				options = new String[]{":sout=#transcode{soverlay,sfilter=marq{marquee='3',opacity=120,size=16,timeout=60000},vcodec=h264,width=576,venc=x264{preset=fast,						keyint=150,vbv-maxrate=2750,vbv-bufsize=1000,level=3.0,ratetol=1.05},acodec=aac,ab=128,channels=1,samplerate=48000,intra-refresh=true}		:duplicate{dst=http{mux=ts,dst=:"+chosenInternalStreamPort+"/go.ts},dst=display}","--network-caching=25","--sout-mux-caching=25","--live-caching=25","--sout-x264-lookahead=25","--sout-deinterlace-mode=yadif","--sout-x264-tune=film","--avcodec-threads=0",":no-sout-keep",":no-sout-all"};
            	else
            				options = new String[]{":sout=#transcode{soverlay,sfilter=marq{marquee='5',opacity=120,size=16,timeout=60000},vcodec=h264,width=288,venc=x264{preset=slow,						keyint=200,vbv-maxrate=100,vbv-bufsize=500,level=2.0,ratetol=1.05},acodec=aac,ab=128,channels=1,samplerate=48000,intra-refresh=true}		:duplicate{dst=http{mux=ts,dst=:"+chosenInternalStreamPort+"/go.ts},dst=display}","--network-caching=25","--sout-mux-caching=25","--live-caching=25","--sout-x264-lookahead=25","--sout-deinterlace-mode=yadif","--sout-x264-tune=film","--avcodec-threads=0",":no-sout-keep",":no-sout-all"};

            }else if(this.protocol.equals("tcp-dup")){
            	options = new String[]{":sout=#http{mux=ts,dst=:"+chosenInternalStreamPort+"/go.ts}","--network-caching=25","--live-caching=25","--avcodec-threads=0",":no-sout-keep",":no-sout-all"}; //"--sout-x264-tune=zerolatency"
            }else if(this.protocol.equals("udp-dup")){
            	options = new String[]{":sout=#transcode{acodec=mpga,ab=128,channels=2,samplerate=48000,deinterlace,intra-refresh=true}:duplicate{dst=rtp{mux=ts,dst="+multicastIp+",port="+(chosenInternalStreamPort+0)+",sdp=sap,ttl=100},dst=display}","--sout-rtp-sap","--sout-rtp-rtcp-mux","--sout-rtp-ttl=100","--network-caching=50","--sout-mux-caching=50","--sout-rtp-caching=100","--live-caching=100","--sout-x264-lookahead=25","--no-deblock","--sout-deinterlace-mode=yadif","--sout-x264-tune=film","--avcodec-threads=0",":sout-keep",":no-sout-all"}; //"--sout-x264-tune=zerolatency"
            }
            
            if(mediaPlayer!=null && getSuccess &&clientsMap.size()>0 && !this.protocol.equals("tcp-dup"))
            	mediaPlayer.media().start(this.channel.toString(), options);
            else {
            	System.out.println("mediaPlayer playing? No, no, no...");
            	garbage=true;
            	}
            System.out.println("mediaPlayer playing? "+mpea.isPlaying() +" "+this);

            try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}            

            if(clientsMap.size()>0)
            	shouldBePlaying=true;
            else {
            	shouldBePlaying=false;
            	stopTranscoderNowForcfully(true);
            	return false;
            }
            garbage = !mpea.isPlaying();
            return mpea.isPlaying();
    }
	
	
	public void removeClient(final String port, String id) {
		boolean stopNow = id.equals("requestStop");
		System.err.println("removeClient started" + port + " " + channel.getName());
		clientsMap.remove(port);
		String temp=null;
		for(Map.Entry<String,SimpleProxyServer> sp:sps.entrySet()) {
			if(port.contains(String.valueOf(sp.getValue().getLocalport())) || stopNow) {
				//System.err.println("if(port.contains(String.valueOf(sp.getLocalport()))) { "+sp.getLocalport());
				sp.getValue().stopMe();
				temp=sp.getKey();
			}
		}
		if(stopNow)
				sps.clear();
		else if (temp!=null)
				sps.remove(temp);
		
		wd.writeSomethingToStatus(wd.statusLabel.getText().replace(id+",", ""));
		if(clientsMap.size()==0)
			stopTranscoderNowForcfully(false);
		System.err.println("removeClient finished" + port + " " + channel.getName());
	}
	
	private void stopTranscoderNowForcfully(boolean hasAlreadyStopped) {
		System.err.println("stopTranscoderNowForcfully called "+this.channel + " cd: " + cdCounter + " " +this);
		garbage=true;
		clientsMap.clear();
		for(Map.Entry<String,SimpleProxyServer> sp:sps.entrySet()) {
			if(!sp.getValue().isShouldBeStopped())
				sp.getValue().stopMe();
		}
		sps.clear();
		try {
			Thread.sleep(75);
		} catch (InterruptedException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		System.err.println(new Date().toString()+"stopTranscoderNowForcfully/ after sps.clear() hasAlreadyStopped="+hasAlreadyStopped+" " + channel.getName());
		if(!hasAlreadyStopped && mediaPlayer!=null) {
			if(mpea.isPlaying())
				mediaPlayer.controls().stop();
		}
		if(mediaPlayer!=null && mpea!=null)
			mediaPlayer.events().removeMediaPlayerEventListener(mpea);
		try {
			Thread.sleep(50);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if(error)
			Sngltn.getInstance().addGarbagePlayer(mediaPlayer);
		else
			Sngltn.getInstance().addReusePlayer(mediaPlayer);
		wd.closeWindow(false);
		mpea=null;
		mediaPlayer=null;
		System.err.println(this + " should be completely removed");
		Sngltn.getInstance().getInternalTranscoderMap().entrySet().removeIf(e -> e.getValue()==this);

	}

	
	
    public boolean isGarbage() {
		return this.garbage;
	}
	
	public void addProxy(String port, SimpleProxyServer sps) {		
		this.sps.put(port,sps);
	}

	public ConcurrentHashMap<String,SimpleProxyServer> getProxies() {		
		return this.sps;
	}	
	
	public void setServerIp(final String ip) {
		this.serverIp=ip;
	}
	
	public void setQuality(final byte q) {
		this.quality=q;
	}
	
	public void setProtocol(final String p) {
		this.protocol=p;
	}
	
	public void setMulticastIp(final String p) {
		this.multicastIp=p;
	}
	
	public String getMulticastIp() {
		return this.multicastIp;
	}
	
	public Byte getResolution() {
		return this.resolution;
	}

	public void setResolution(Byte resolution) {
		this.resolution = resolution;
	}
		
	public ChannelItem getChannel() {
		return this.channel;
	}

	public String getProtocol() {
		return this.protocol;
	}
	
	public Byte getQuality() {
		return this.quality;
	}
	
    public Integer getChosenInternalStreamPort() {
		return this.chosenInternalStreamPort;
	}
    
	public MediaPlayerReadyEventHandler getMpea() {
		return this.mpea;
	}

	
	final class MediaPlayerReadyEventHandler extends MediaPlayerEventAdapter {

	    /**
	     * Flag if the event has fired since the media was last started or not.
	     */
	    private boolean playing=false;
	    private InternalTranscoder itr;
	    
	    public MediaPlayerReadyEventHandler(InternalTranscoder itr) {
	    	this.itr=itr;
	    }

	    public boolean isPlaying() {
			return playing;
		}

		@Override
	    public void playing(MediaPlayer mediaPlayer) {
	    	System.err.println("Transcoder PLAYING!!!!!");
	    	super.playing(mediaPlayer);
	    	playing=true;
	    	garbage=false;
	    }

	    @Override
	    public void stopped(MediaPlayer mediaPlayer) {
	    	System.err.println("Transcoder STOPPED!!!!!");
	    	super.stopped(mediaPlayer);
	        playing=false;
	    	garbage=true;
//	        itr.stopTranscoderNowForcfully(true);
	    }

	    @Override
	    public void finished(MediaPlayer mediaPlayer) {
	    	System.err.println("Transcoder FINISHED!!!!!");
	    	super.finished(mediaPlayer);
	        playing = false;
	    	garbage=true;
//	        itr.stopTranscoderNowForcfully(true);
	    }
	    
	    @Override
	    public void error(MediaPlayer mediaPlayer) {
	        //mediaPlayer.status().state().equals(uk.co.caprica.vlcj.player.base.State.ERROR);
	    	super.error(mediaPlayer);
	    	playing = false;
	    	garbage=true;
	    	System.err.println("Transcoder ERROR!!!!!");
	    	System.err.println("Transcoder ERROR!!!!!");
//	    	mediaPlayer.controls().stop();
//	    	itr.stopTranscoderNowForcfully(true);
	    	error=true;
	    }
	    
	    @Override
	    public void corked(MediaPlayer mediaPlayer, boolean corked) {
	    	super.corked(mediaPlayer, corked);
	    	playing=false;
	    	garbage=true;
	    	System.err.println("Transcoder CORKED!!!!!");
	    	System.err.println("Transcoder CORKED!!!!!");
//	    	itr.stopTranscoderNowForcfully(true);
	    	error=true;
	    }

	}
	
}

