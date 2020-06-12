package main;
/**
 *
 * @author myname
 */
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.SequenceInputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.xml.bind.JAXB;


import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
//
//import com.sun.jna.Native;
//import com.sun.jna.NativeLibrary;

import channels.ChannelReader;
import channels.ChannelItem;
import network.RTSPRunnable;
import network.SATIPClient;
import network.SimpleProxyServer;
//import uk.co.caprica.vlcj.binding.LibVlc;
//import uk.co.caprica.vlcj.binding.RuntimeUtil;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.media.callback.nonseekable.NonSeekableCallbackMedia;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.videosurface.VideoSurface;



public class Dispatcher {

    private WriteToConsole p=null;
    private WriteToConsole currentP=null;
    
    private SimpleProxyServer sps;
    private String protocol="tcp";
    
    public Date clientAlive=new Date();
    int commandFromClient=1;
    int lastCommand=1;
    private InternalTranscoder lastTranscoder=null;
    
    private TranscoderBuilder tcb;
    
    MediaPlayerReadyEventHandler mpEvents;

	boolean localClient=false;
    Byte quality=new Byte((byte)0);
    Byte resolution=new Byte((byte)0);

    public int portString=79;
    public int portStream=80;
    public int getPortStream() {
		return portStream;
	}


	public int portCommand=8080;
    int remotePort=-1;

    Window wd;
    public Window getWd() {
		return wd;
	}


	SATIPClient sic=null;
    int rtspPort=5000;
    Thread t;
    AtomicBoolean streaming=new AtomicBoolean(false);
    String clientIp=null;

	int clientPort=-1;
    String serverIp;
    

    ChannelReader cr;
    Canvas canvas;
    private boolean isPlaying;
    private MediaPlayerFactory factory;
    MediaPlayer mediaPlayer;
    VideoSurface videoSurface;
    
    RTSPRunnable r=null;
    
    InternalTranscoder myTranscoder=null;
    
	private Integer id;
    

    
	Dispatcher(final String ip,final Triple<String,String,String> conf,final ChannelReader cr, Integer id,String protocol){
		
    	
    	System.err.println(ip+" "+conf.getLeft()+" "+conf.getMiddle()+" "+conf.getRight());
    	
    	this.cr=cr;
    	try {
    		this.id=id;
    		this.protocol=protocol;
	    	portCommand=Integer.valueOf((conf.getMiddle().subSequence(0, conf.getMiddle().indexOf(","))).toString().trim());
	    	portString=portCommand;
	    	portStream=Integer.valueOf((conf.getMiddle().subSequence(conf.getMiddle().indexOf(",")+1, conf.getMiddle().length())).toString().trim());
	    	this.serverIp=ip.substring(0,ip.indexOf(":"));
    	}catch(Exception e) {
    		System.err.println("The configuration for server "+ip+" looks bad... Exiting.");
    		Sngltn.getInstance().setBadConfig(true);
    		return;
    	}
    	
    	//String vlcPath = "C:\\Program Files\\VideoLAN\\VLC";  ???

        //NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), vlcPath);
        //Native.load(RuntimeUtil.getLibVlcLibraryName(), uk.co.caprica.vlcj.binding.LibVlc.class);
    	factory = Sngltn.getInstance().getMediaPlayerFactory();

        mediaPlayer = factory.mediaPlayers().newMediaPlayer();
        mediaPlayer.events().addMediaPlayerEventListener(mpEvents=new MediaPlayerReadyEventHandler());

        wd=new Window();
         
        wd.startMethod("Server, "+conf.getRight()+", "+portCommand + " " + id,350,100,id);
        wd.writeSomethingToStatus("ports: "+conf.getMiddle());
    }

public void receiveString() {
	try (final ServerSocket stringSocket= new ServerSocket(portString)){		
        System.out.println("Connection established - string");
        
		Socket ssc =stringSocket.accept();
		wd.writeSomethingToStatus("Server string-port "+stringSocket.getLocalPort()+" listening.");
		InputStream obj=ssc.getInputStream();
        DataInputStream inSsc= new DataInputStream(obj);
        String read=inSsc.readUTF();
        switch(read)
        {
            case "3000": quality=(byte)0;return; 
            case "3001": quality=(byte)1;return; 
            case "3002": quality=(byte)2;return;
            case "3003": quality=(byte)3;return; 
            case "3004": quality=(byte)4;return; 
            case "3005": quality=(byte)5;return; 
            case "3006": quality=(byte)6;return;
            case "++Quality": quality=(byte)0;return; 
            case "+Quality": quality=(byte)1;return; 
            case "-Quality": quality=(byte)2;return;
            case "--Quality": quality=(byte)3;return; 
            case "---Quality": quality=(byte)4;return; 
            case "audio": quality=(byte)5;return; 
        }
        System.err.println("READING STRING:"+read);
        if(read.equals("stop") || read.equals("9999"))
        {	
        	commandFromClient=9999;
        	return;
    	} 
        	else if(read.contains("alive") || read.contains("10000") || read.contains("20000"))
    	{
    		commandFromClient=10000;
        	clientAlive=new Date();
        	return;
    	}
        else {
        	commandFromClient=StringSim.similarity(read,cr.getChannels());
			System.err.println("setting channel by string "+commandFromClient+" "+read);
		}        
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}	
}

private boolean checkQuality(final String read, boolean legacy) {
	int qualityBefore=quality;
	boolean wasQualityCommand=false;
	switch(read)
    {
        case "3000": quality=(byte)0;wasQualityCommand=true;break; 
        case "3001": quality=(byte)1;wasQualityCommand=true;break; 
        case "3002": quality=(byte)2;wasQualityCommand=true;break;
        case "3003": quality=(byte)3;wasQualityCommand=true;break; 
        case "3004": quality=(byte)4;wasQualityCommand=true;break; 
        case "3005": quality=(byte)5;wasQualityCommand=true;break; 
        case "3006": quality=(byte)6;wasQualityCommand=true;break;
        case "++Quality": quality=(byte)0;wasQualityCommand=true;break; 
        case "+Quality": quality=(byte)1;wasQualityCommand=true;break; 
        case "-Quality": quality=(byte)2;wasQualityCommand=true;break;
        case "--Quality": quality=(byte)3;wasQualityCommand=true;break; 
        case "---Quality": quality=(byte)4;wasQualityCommand=true;break; 
        case "audio": quality=(byte)5;wasQualityCommand=true;break; 
    }
		

	return wasQualityCommand;
}

public void evaluateChannelString(final String read,boolean legacy) {
    
	if(checkQuality(read, legacy))
		return;
    
    System.err.println("READING STRING:"+read +"legacy ="+legacy);
    if(read.contains("stop") || read.contains("9999"))
    {	
    	commandFromClient=9999;
    	if(!legacy)
			try {
				setChannel(commandFromClient,this.protocol);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	return;
	} 
    	else if(read.contains("alive") || read.contains("10000") || read.contains("20000"))
	{
		commandFromClient=10000;
    	clientAlive=new Date();
    	if(!legacy)
			try {
				setChannel(commandFromClient,this.protocol);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	return;
	}
    else {
    	commandFromClient=StringSim.similarity(read,cr.getChannels());
		System.err.println("setting channel by string "+commandFromClient+" "+read);
		if(!legacy)
			try {
				setChannel(commandFromClient,this.protocol);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return;
	}
    	
}

public boolean setChannel(int channelID, String proto) throws IOException{
    System.out.println("Setting some channel.");
    if(proto!=null)
		this.protocol=proto;
    if(channelID==10000||channelID==20000){
        clientAlive=new Date();
        System.out.println("Client has just sent its regards.");
        return true;}
    
    if(channelID!=9999)
        clientAlive=new Date();
    
    if(channelID==3000||channelID==3001||channelID==3002||channelID==3003||channelID==3004||channelID==3005||channelID==3006)
    {
    	channelID=lastCommand;
    }
    if(false) {
    	System.err.println("stopping player");
    	mediaPlayer.controls().stop();
    	mediaPlayer.release();
    	try {
			Thread.sleep(250);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
//    if(p!=null)
//        q=currentP;
//    else
//    	p=currentP;
    if(channelID==9999)
    {
    	System.err.println("TeeWe, received 9999");
    	if(mediaPlayer.media().isValid())
    		mediaPlayer.controls().stop();
    	if(p!=null)
    		p.quitForWin();
    	if(currentP!=null)
    		currentP.quitForWin();
    	if(myTranscoder!=null) {
    		myTranscoder.removeClient(""+portStream,String.valueOf(id));}

    	wd.writeSomethingToMainWindow("Stop received.");
    	System.err.println("client stop requested");
    	return true;
    }

            System.out.println("Setting Channel "+channelID);

            final ChannelItem chan=(ChannelItem)cr.getChannels().get(channelID);
            


            System.err.println("current QUALITY: " +quality);
            if(channelID!=9999&&channelID!=10000&&channelID!=20000){            	
            	
                if(chan.getLink().contains("rtsp")){
                	if(currentP!=null)
                		currentP.quitForWin();
                	currentP= new WriteToConsole(false);
                	currentP.sendLine("C:\\Progra~1\\VideoLAN\\VLC\\vlc.exe");
                    //p.sendLine((((triaxChannelItem)channels[channelID]).toString()));   
                	currentP.sendLine(((ChannelItem)cr.getChannels().get(channelID)).toString());
                    //p.sendLine("--qt-start-minimized");
                	currentP.sendLine("--intf=dummy");
                	currentP.sendLine("-vv");
                    switch(quality){
                        case 0: currentP.sendLine("--sout=#transcode{vcodec=h264,preset=veryfast,deinterlace=yadif2x,venc=x264{keyint=200,vbv-maxrate=8000,vbv-bufsize=1400,level=4.2,ratetol=1.1},acodec=aac,ab=128,channels=2,samplerate=48000}:http{dst=:"+portStream+"/go.ts} --sout-deinterlace-mode=yadif2x --audio-track=0 --network-caching=1000 --sout-mux-caching=15000 --avcodec-threads=1 --qt-start-minimized :no-sout-all :sout-keep");break;
                        case 1: currentP.sendLine("--sout=#transcode{vcodec=h264,preset=veryfast,deinterlace=yadif2x,venc=x264{keyint=200,vbv-maxrate=5000,vbv-bufsize=1400,level=4.2,ratetol=1.1},acodec=aac,ab=128,channels=2,samplerate=48000}:http{dst=:"+portStream+"/go.ts} --sout-deinterlace-mode=yadif2x --audio-track=0 --network-caching=1000 --sout-mux-caching=15000 --avcodec-threads=1 --qt-start-minimized :no-sout-all :sout-keep");break;
                        case 2: currentP.sendLine("--sout=#transcode{vcodec=h264,preset=fast,    deinterlace=yadif2x,venc=x264{keyint=200,vbv-maxrate=3000,vbv-bufsize=900,level=5.1,ratetol=1.2}, acodec=aac,ab=128,channels=2,samplerate=48000}:http{dst=:"+portStream+"/go.ts} --sout-deinterlace-mode=yadif2x --audio-track=0 --network-caching=1000 --sout-mux-caching=15000 --avcodec-threads=1 --qt-start-minimized :no-sout-all :sout-keep");break;
                        case 3: currentP.sendLine("--sout=#transcode{vcodec=h264,preset=medium,  deinterlace=yadif2x,venc=x264{keyint=200,vbv-maxrate=500,vbv-bufsize=500,level=5.1,ratetol=1.5},  acodec=aac,ab=128,channels=2,samplerate=48000}:http{dst=:"+portStream+"/go.ts} --sout-deinterlace-mode=yadif2x --audio-track=0 --network-caching=1000 --sout-mux-caching=15000 --avcodec-threads=1 --qt-start-minimized :no-sout-all :sout-keep");break;
                        case 4: currentP.sendLine("--sout=#transcode{vcodec=h264,preset=medium,  deinterlace=yadif2x,venc=x264{keyint=100,vbv-maxrate=250,vbv-bufsize=200,level=5.1,ratetol=1.5},  acodec=aac,ab=128,channels=2,samplerate=48000}:http{dst=:"+portStream+"/go.ts} --sout-deinterlace-mode=yadif2x --audio-track=0 --network-caching=1000 --sout-mux-caching=15000 --avcodec-threads=1 --qt-start-minimized :no-sout-all :sout-keep");break;
                        case 5: currentP.sendLine("--sout=#transcode{vcodec=none,																												   acodec=aac,ab=128,channels=2,samplerate=48000}:http{dst=:"+portStream+"/go.ts} --network-caching=1000 --sout-mux-caching=15000 --avcodec-threads=1 --qt-start-minimized :no-sout-all :sout-keep");break;
                    }
                    currentP.execute();                  
                    System.err.println("Streaming at port "+portStream);
                    
                    wd.writeSomethingToMainWindow("watching "+((ChannelItem)cr.getChannels().get(channelID)).getName() +" @"+quality +" prt:"+portStream);

                } else {
                	
                
	                if(myTranscoder!=null && myTranscoder.getMpea()!=null && myTranscoder.getMpea().isPlaying())
	                	myTranscoder.removeClient(""+portStream,String.valueOf(id));
	                
	                final Triple<String, ChannelItem, Integer> temp = Triple.of(protocol, chan,quality+(resolution*10));
	                final InternalTranscoder tempT = Sngltn.getInstance().getInternalTranscoderMap().get(temp);
	                
	                if(tempT!=null&&!tempT.isGarbage()) {
	                	myTranscoder = tempT;
	                	System.err.println("TeeWe: found matching transcoder!!! " + myTranscoder.getChannel().getName());
	                	
	                }else {
	                	if(tempT!=null && tempT.isGarbage()) {
	                		System.err.println("MATCHING TRANSCODER IS STALE! New instance. isGarbage=" + tempT.isGarbage()+ ", " + tempT.getChannel().getName());
	                		tempT.removeClient(""+portStream,String.valueOf(id)); //recent add
	                	}
	                		
	                	tcb=new TranscoderBuilder();
	                	myTranscoder = tcb.setProtocol(protocol).setChannel(chan).setQuality(quality).setServerIp(serverIp).setResolution(resolution).getProduct();
	                	System.err.println("BUILDING NEW TRANSCODER! New instance. isGarbage=" + myTranscoder.isGarbage() + ", " + chan.getName());
	                }


	            myTranscoder.startTranscoder(""+portStream, String.valueOf(id));

	            sps=new SimpleProxyServer("127.0.0.1", myTranscoder.getChosenInternalStreamPort(), portStream);
            	myTranscoder.addProxy(""+portStream,sps);
            	Thread spst=new Thread(sps);
            	spst.start();

                System.err.println("About to start transcoder-------------------");
                if(myTranscoder.getMpea().isPlaying())
              	System.err.println("Transcoder already running------------------");

                myTranscoder.startTranscoder(""+portStream, String.valueOf(id));
                System.err.println("Transcoder started or joined ------------------");

              wd.writeSomethingToMainWindow("watching "+chan.getName() +" @"+quality +" prt:"+myTranscoder.getChosenInternalStreamPort());
                
                
            }
        }
            

        if(channelID!=3000&&channelID!=3001&&channelID!=3002&&channelID!=3003&&channelID!=3004&&channelID!=3005&&channelID!=3006)
        {
        	lastCommand=channelID;
        }
    return true;
}

public void setRemotePort(int port) {
	this.remotePort=port;
}

public InternalTranscoder getLastTranscoder() {
	return lastTranscoder;
}

public Byte getQuality() {
	return quality;
}

public String getClientIp() {
	return clientIp;
}

public void setClientIp(String clientIp) {
	this.clientIp=clientIp;	
}


public Integer getId() {
	return id;
}
    
    
public WriteToConsole getP() {
	return p;
}

final class MediaPlayerReadyEventHandler extends MediaPlayerEventAdapter {

    /**
     * Flag if the event has fired since the media was last started or not.
     */
    private boolean playing=false;

    public boolean isPlaying() {
		return playing;
	}

	@Override
    public void playing(MediaPlayer mediaPlayer) {
    	System.err.println("Transcoder PLAYING!!!!!");
    	super.playing(mediaPlayer);
    	playing=true;
    }

    @Override
    public void stopped(MediaPlayer mediaPlayer) {
    	System.err.println("Transcoder STOPPED!!!!!");
        playing=false;
    }

    @Override
    public void finished(MediaPlayer mediaPlayer) {
    	System.err.println("Transcoder FINISHED!!!!!");
        playing = false;
    }

}
} 
