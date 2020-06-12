package main;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author multimedia
 */
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author multimedia
 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import sun.misc.Signal;


public class WriteToConsole implements Runnable{
    LinkedHashMap<Integer,String> responses;
    int numRespones;
    List<String> commands ;
    ProcessBuilder builder;
    Process process;
    String consoleEncoding;
    
    OutputStream stdin;
    InputStream stderr;
    InputStream stdout;
    
    BufferedReader reader;
    BufferedWriter writer;
    
    AtomicBoolean quitNow=new AtomicBoolean(false);
    
    Thread outThread;
    Thread errThread;
    
    WriteToConsole(boolean isMacOS)
    {
        commands = new ArrayList<>(); 
        /**
        if(!isMacOS){
            commands.add("C:\\Windows\\System32\\cmd.exe"); commands.add("/K");
            commands.add("start \"JAVA cmd exec (Fenstername)\"");}
        **/
        builder = new ProcessBuilder(commands);
        builder.redirectErrorStream(true);
        builder.environment().put("LANG", "de_DE.UTF-8");
        //consoleEncoding = "UTF-8"; 
//        if (System.getProperty("os.name").contains("Windows")) 
//        	consoleEncoding = "CP850";
        
    }    
    
    public synchronized void sendLine(String line){
        commands.add(line);
    }

    public synchronized void execute(){
        try {
            process = builder.start();            
            commands.clear();

            outThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                	String line;
                	while (quitNow.get()==false)
					{
						try {
							System.out.println(reader.readLine());
							if(reader.readLine()==null){																								
								quitForWin();
							}
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}  	
                	potentialCrash();                	
                } catch (Exception e) {
                }                
            });
            
            errThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                	String line;
                	while ((line = reader.readLine()) != null && quitNow.get()==false) {
                	      System.err.println(line); //use the output here
                	    }
                } catch (Exception e) {
                }
              });
            
            outThread.setDaemon(true);
            errThread.setDaemon(true);
            outThread.start();
            errThread.start();
            
        } catch (IOException ex) {
            Logger.getLogger(WriteToConsole.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public synchronized  String getResponse(){
        StringBuilder s=new StringBuilder();
        for(String sr:this.responses.values())
            s.append(sr);
        return s.toString();
    }
    
    private void potentialCrash(){
    	System.err.println("potential Crash");
    	quitForWin();
    }
    
    public synchronized void quitForWin(){
    	//commands.add("C:\\Windows\\System32\\cmd.exe"); commands.add("/K");
    	//commands.add("taskkill /IM vlc.exe");
    	//builder = new ProcessBuilder(commands);
    	//execute();
    	quitNow.set(true);
    	System.out.println("trying to kill process - pc");
    	try {
			Thread.sleep(25);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	builder=null;
    	if(process!=null) {
    		process.destroy();
    		if(process.isAlive())
                process.destroyForcibly();
//	    	try {
//	    		if(process!=null)
//	    			process.waitFor();
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
    	}
//    	quitNow.set(false);
    	if(outThread!=null) {
    		if(outThread.isAlive()) {
    			quitNow.set(true);
    		}
    	}
    	
//    	try {
//			process.getOutputStream().write("NULL".getBytes());
//			process.getOutputStream().flush();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
    }
    
    public synchronized void quitForMac(){
        System.out.println("trying to kill process - mac");
        builder=null;        
        if(process!=null)
            process.destroyForcibly();        
    }
    
    public boolean isShouldBeClosed() {
    	return quitNow.get();
    }

	@Override
	public void run() {
		this.execute();
	}
}
