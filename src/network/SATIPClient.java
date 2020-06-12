package network;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.IOException;


/*

 * 

 * SATIP-Translator accepts an RTSP request from a VLC-Player and translates and

 * send the RTSP request to a SATIP-Server

    Copyright (C) 2014  Andreas Schultes



    This program is free software: you can redistribute it and/or modify

    it under the terms of the GNU General Public License as published by

    the Free Software Foundation, either version 3 of the License, or

    (at your option) any later version.



    This program is distributed in the hope that it will be useful,

    but WITHOUT ANY WARRANTY; without even the implied warranty of

    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the

    GNU General Public License for more details.



    You should have received a copy of the GNU General Public License

    along with this program.  If not, see <http://www.gnu.org/licenses/>.

 */
public class SATIPClient {
	
	private static final Logger LOGGER = Logger.getLogger(SATIPClient.class.getName());
	private String mHost;
//	private Sender mSender;
	private Socket mSocket;
	private int mCseq;
	
	private String mSession;
	private String mServerPort;
	private String mClientPort;
	private String mDestination;
	private String rtpinfo;
	private int mStreamID;
	private int mTimeout;
	private File file;
	
	private final static Pattern status= Pattern.compile("RTSP/\\d.\\d +(\\d+) +(\\w+)");
	private final static Pattern session= Pattern.compile("Session: *(\\p{XDigit}+) *;timeout=(\\d+)");
	private final static Pattern cseq= Pattern.compile("CSeq: *(\\d+)");
	private final static Pattern transport= Pattern.compile("Transport:RTP/AVP.*;unicast;destination=(.*);client_port=(.*)");
	private final static Pattern streamID= Pattern.compile("com.ses.streamID: *(\\d+)");
	private final static Pattern RTPInfo= Pattern.compile("RTP-Info:(.*)");
	
	public String GetRTPInfo()
	{
		return rtpinfo;
	}
	public String GetSession()
	{
		return mSession;
	}
	public int GetSteamID()
	{
		return mStreamID;
	}
	public String GetClientPort()
	{
		return mClientPort;
	}
	public String GetServerPort()
	{
		return mServerPort;
	}
	public String GetDestination()
	{
		return mDestination;
	}
	
	public int GetTimeout()
	{
		return mTimeout;
	}
	public InetAddress GetLocalAddress() 
	{
		if(mSocket==null)
		{
			try {
				mSocket=new Socket(mHost,554);
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return mSocket.getLocalAddress();
	}
	
	public SATIPClient(InetAddress address)
	{
		mCseq=0;
		mHost=address.getHostAddress();
		
	}
	public void Setup(String query,String clientPort) throws SocketException, IOException
	{
		Setup(query,null,clientPort);
	}
	
	public void Setup(String query,String destination, String clientPort) throws IOException,SocketException
	{
		if(mSocket==null)
		{
			mSocket=new Socket(mHost,554);
			System.out.println("DEBUG Socket null");
		}
		//mHost=host;
		//mSender=sender;
		mSocket.getOutputStream();
		BufferedOutputStream output= new BufferedOutputStream(mSocket.getOutputStream());
		String request=new String();
		request="SETUP rtsp://"+mHost+":554/"+query+" RTSP/1.0\r\n";
		request+="CSeq: "+mCseq+"\r\n";
		if(destination!=null)
			request+="Transport:RTP/AVP/UDP;multicast;destination="+destination+";port="+clientPort+";ttl=5\r\n";
		else
			request+="Transport:RTP/AVP/UDP;unicast;client_port="+clientPort+"\r\n";
		//request+="Connection:close\r\n";
		request+="\r\n\r\n";
		output.write(request.getBytes(StandardCharsets.UTF_8));
		output.flush();
		//output.write();
		LOGGER.info(request);
	
		BufferedReader input =new BufferedReader(new InputStreamReader(mSocket.getInputStream()));

		String line;
		Matcher match;
		if((line=input.readLine())!=null)
		{
			match=status.matcher(line);
			if(match.find())
				if(Integer.parseInt(match.group(1))!=200)return;
		}
		else throw new SocketException("Connection Lost");
		while((line=input.readLine())!=null&& line.length()>3)
		{
			match=session.matcher(line);
			if(match.find())
			{
				mSession=match.group(1);
				mTimeout=Integer.parseInt(match.group(2));				
			}
			match=streamID.matcher(line);
			if(match.find())
			{
				mStreamID=Integer.parseInt(match.group(1));
			}
			match=cseq.matcher(line);
			if(match.find())
			{			
			mCseq=Integer.parseInt(match.group(1));
			}
			match=transport.matcher(line);
			if(match.find())
			{
				mClientPort=match.group(2);
				mDestination=match.group(1);
			}
			System.out.println(line);
		}
		if(line==null) throw new SocketException("Connection Lost");
		//System.out.println("CSeq:"+mCseq+" Session:"+mSession+" Timeout:"+mTimeout+" StreamID:"+mStreamID);
	}
	
	public void Play(String pids) throws IOException,SocketException
	{
		mCseq++;
		LOGGER.info("DEBUG PLAY "+mCseq);

		if(mSocket==null)
			mSocket=new Socket(mHost,554);
		if(!mSocket.isConnected())
			mSocket.connect(new InetSocketAddress(mHost,554));
		mSocket.getOutputStream();
		BufferedOutputStream output= new BufferedOutputStream(mSocket.getOutputStream());
		String request=new String();
		request="PLAY rtsp://"+mHost+":554/stream="+mStreamID+pids+" RTSP/1.0\r\n";
		request+="CSeq: "+mCseq+"\r\n";
		request+="Session: "+mSession+ "\r\n";
		request+="\r\n";
		output.write(request.getBytes(StandardCharsets.UTF_8));
		output.flush();
		LOGGER.info(request);
		
		BufferedReader input =new BufferedReader(new InputStreamReader(mSocket.getInputStream()));

		String line;
		Matcher match;
		if((line=input.readLine())!=null)
		{
			LOGGER.info(line);
			match=status.matcher(line);
			if(match.find())
				if(Integer.parseInt(match.group(1))!=200)return;
		}
		else throw new SocketException("Connection Lost");	
		
		while((line=input.readLine())!=null&& line.length()>3)
		{
			System.out.println(line);
			match=RTPInfo.matcher(line);
			if(match.find())
			{
				rtpinfo=match.group(1);
			}
			LOGGER.info(line);
		}
		if(line==null) throw new SocketException("Connection Lost");

		
	}
	
	public void Option() throws IOException,SocketException
	{
		mCseq++;
		
		LOGGER.info("DEBUG OPTIONS "+mCseq);
		if(mSocket==null)
		{
			mSocket=new Socket(mHost,554);
			LOGGER.info("Socket null");
		}
		if(!mSocket.isConnected())
		{
			mSocket.connect(new InetSocketAddress(mHost,554));
			LOGGER.info("Socket Reconnect");
		}
		mSocket.getOutputStream();
		BufferedOutputStream output= new BufferedOutputStream(mSocket.getOutputStream());
		String request=new String();
		request="OPTIONS rtsp://"+mHost+"/ RTSP/1.0\r\n";
		request+="CSeq:"+mCseq+"\r\n";
		if(mSession != null)
			request+="Session:"+mSession+ "\r\n";
		request+="\r\n\r\n";
		output.write(request.getBytes(StandardCharsets.UTF_8));
		output.flush();
		LOGGER.info(request);
		
		BufferedReader input =new BufferedReader(new InputStreamReader(mSocket.getInputStream()));

		String line;
		Matcher match;
		if((line=input.readLine())!=null)
		{
			System.out.println(line);
			match=status.matcher(line);
			if(match.find())
				if(Integer.parseInt(match.group(1))!=200)return;
			LOGGER.info(line);
		}
		//else throw new SocketException("Connection Lost");	
		while((line=input.readLine())!=null&& line.length()>3)
		{
			LOGGER.info(line);
		}
		//if(line==null) throw new SocketException("Connection Lost");

	}
	public void Teardown() throws IOException,SocketException
	{
		LOGGER.info("DEBUG TEARDOWN "+mCseq);
		mCseq++;
		
		if(mSocket==null)
			mSocket=new Socket(mHost,554);
		if(!mSocket.isConnected())
			mSocket.connect(new InetSocketAddress(mHost,554));
		mSocket.getOutputStream();
		BufferedOutputStream output= new BufferedOutputStream(mSocket.getOutputStream());
		String request=new String();
		request="TEARDOWN rtsp://"+mHost+"/stream="+mStreamID+" RTSP/1.0\r\n";
		request+="CSeq: "+mCseq+"\r\n";
		request+="Session: "+mSession+ "\r\n";
		request+="\r\n\r\n";
		output.write(request.getBytes(StandardCharsets.UTF_8));
		output.flush();
		LOGGER.info(request);
		
		BufferedReader input =new BufferedReader(new InputStreamReader(mSocket.getInputStream()));

		String line;
		Matcher match;
		if((line=input.readLine())!=null)
		{
			LOGGER.info(line);
			match=status.matcher(line);
			if(match.find())
				if(Integer.parseInt(match.group(1))!=200)return;
		}
		else throw new SocketException("Connection Lost");	
		while((line=input.readLine())!=null&& line.length()>3)
		{
			LOGGER.info(line);
		}
		if(line==null) throw new SocketException("Connection Lost");

	}
	public void Describe(String streamId,boolean doit) throws IOException,SocketException
	{
		mCseq++;
		
		LOGGER.info("DEBUG DESCRIBE"+mCseq+" "+streamId);
		if(mSocket==null)
			mSocket=new Socket(mHost,554);
		if(!mSocket.isConnected())
			mSocket.connect(new InetSocketAddress(mHost,554));
		mSocket.getOutputStream();
		BufferedOutputStream output= new BufferedOutputStream(mSocket.getOutputStream());
		String request=new String();
		request="DESCRIBE rtsp://"+mHost+"/ RTSP/1.0\r\n";
		request+="CSeq: "+mCseq+"\r\n";
		//request+="Session: "+mSession+ "\r\n";
		request+="\r\n\r\n";
		output.write(request.getBytes(StandardCharsets.UTF_8));
		output.flush();
		LOGGER.info(request);
		
		String v=null,o=null,s=null,t=null,m=null,c=null,a1=null,a2=null,a3=null;
		
		BufferedReader input =new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
		
		file = new File("C:\\Users\\Server\\eclipse-workspace\\TV-ServerUDP\\live.sdp");
		boolean isValid=false;
		FileWriter fw = new FileWriter(file, true);
		BufferedWriter br = new BufferedWriter(fw);

		String line;
		Matcher match;
		if((line=input.readLine())!=null)
		{
			System.out.println(line+" streamId:"+streamId);
			match=status.matcher(line);
			if(match.find())
				if(Integer.parseInt(match.group(1))!=200)return;
			LOGGER.info(line);
		}
		else {
			br.close();
			fw.close();
			throw new SocketException("Connection Lost");	}
		while((line=input.readLine())!=null)
		{
			System.out.println(line);
//			LOGGER.info(line);		
						

			if(line.startsWith("v")) {
				v=line;continue;
				}
			if(line.startsWith("o")) {
				o=line;continue;
				}
			if(line.startsWith("s")) {
				s=line;continue;
				}
			if(line.startsWith("t")) {
				t=line;continue;
				}
			if(line.startsWith("m")) {
				m=line;continue;
				}
			if(line.startsWith("c")) {
				c=line;continue;
				}
			if(line.startsWith("a")&&a1==null) {
				a1=line;continue;
				}
			if(line.startsWith("a")&&a2==null) {
				a2=line;continue;
				}
			if(line.startsWith("a")&&a3==null) {
				a3=line;
				System.err.println("HERE!!!!!!!!!!!!!!!!!");
				if(a1.contains(streamId)&&!a2.contains("pids")||doit&&a1.contains(streamId)) {
					System.err.println("YEAH!!!!!!!!!!!!!!!!!");
					br.write(v+"\r\n");
					br.write(o+"\r\n");
					br.write(s+"\r\n");
					br.write(t+"\r\n");
					br.write(m+"\r\n");
					br.write(c+"\r\n");
					br.write(a1+"\r\n");
					br.write(a2+"\r\n");
					br.write(a3+"\r\n");
					br.close();
					fw.close();
					isValid=true;
					return;
				}
				else
				{
					m=null;c=null;a1=null;a2=null;a3=null;
					continue;
				}
			}
		}		
					
//		while((line=input.readLine())!=null&&line.length()>3)
//		{
//			LOGGER.info(line);
//		}
	/*	while((line=input.readLine())!=null&&line.length()>3)
		{
			System.out.println(line);
		}*/
		if(line==null) { 
			if(isValid) {
				br.close();
				fw.close();	
				}
			throw new SocketException("Connection Lost");
		}

	}
	protected void finalize()
	{
		try {
			mSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}