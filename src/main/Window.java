package main;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;

import org.apache.commons.lang3.tuple.Triple;

import network.SimpleProxyServer;

public class Window extends JFrame{
	
	private static final long serialVersionUID = 1L;

	int id = 0;

	JLabel statusLabel=null;
	JLabel mainLabel=null;
//	static HashSet<Triple<Point,Integer,Integer>> posSet = new HashSet<>();
	static Dimension screenSize=Toolkit.getDefaultToolkit().getScreenSize();
	private Triple<Point,Integer,Integer> initialTriple;
	private InternalTranscoder transcoderObj=null;
	static private LinkedHashMap<Integer,Window> windowsMap = new LinkedHashMap<>();
	
	static double currentX=400;
	static double currentY=10;
	
	static int LOCATION_OFFSET_X;
	static int LOCATION_OFFSET_Y;
	
	static boolean blocked = false;
	
	
	public void startMethod(String title,int x, int y, int id) {
		this.id = id;
		this.setLayout(new BorderLayout());
		this.setSize(x, y);
		
	
		JPanel mainPanel = new JPanel();
		JScrollPane scrPane = null;
		this.add(mainPanel, BorderLayout.NORTH);		
		mainPanel.setPreferredSize(new Dimension(this.getWidth(), this.getHeight()-60));
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));
		if(title.contains("REST")) {
			mainLabel = new JLabel("");
			scrPane = new JScrollPane(mainLabel);
			scrPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			scrPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
			scrPane.setAutoscrolls(true);
			
			scrPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
				public void adjustmentValueChanged(AdjustmentEvent e) {
				e.getAdjustable().setValue(e.getAdjustable().getMaximum());
				}});
			
			mainLabel.setHorizontalAlignment(SwingConstants.CENTER);
			mainPanel.add(scrPane);
		}else {
			mainLabel = new JLabel("");
			mainLabel.setHorizontalAlignment(SwingConstants.CENTER);
			mainPanel.add(mainLabel);
		}
		
		// create the status bar panel and shove it down the bottom of the frame
		JPanel statusPanel = new JPanel();
		statusPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
		this.add(statusPanel, BorderLayout.SOUTH);
		statusPanel.setMaximumSize(new Dimension(this.getWidth(), 18));
		statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));
		statusLabel = new JLabel("status");
		statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
		statusLabel.setMaximumSize(new Dimension(this.getWidth(), 16));
		statusPanel.add(statusLabel);
	
		this.setTitle(title);
		
        if(System.getProperty("os.name").contains("indows")) {
		
			if(transcoderObj==null)
				this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			else
				this.addWindowListener(exitListener);
			
//			Point location = this.getLocation();
//			posSet.add(this.initialTriple=Triple.of(location, this.getWidth(), this.getHeight()));
			
        }
		windowsMap.put(id,this);
		if(id>25) {
			currentX=400;
			currentY=10;			
			LOCATION_OFFSET_X=0;
			LOCATION_OFFSET_Y=0;
			place();
		}
	}
	
	static private void place() {
			if(blocked)
				return;
			blocked=true;
			for(Map.Entry<Integer, Window> w:windowsMap.entrySet()) {
				LOCATION_OFFSET_X=w.getValue().getSize().width;
				
				w.getValue().setLocation((int)Math.round(currentX), (int)Math.round(currentY+ LOCATION_OFFSET_Y));			
				w.getValue().setVisible(true);
				
				LOCATION_OFFSET_Y=w.getValue().getSize().height;
				
				currentX = w.getValue().getLocation().getX();
				currentY = w.getValue().getLocation().getY();
				
				if(currentY+1.75*LOCATION_OFFSET_Y >= screenSize.height*0.95) {
					System.err.println(currentY+1.75*LOCATION_OFFSET_Y+" "+ screenSize.height*0.95);
					currentY=10;
					currentX=currentX+LOCATION_OFFSET_X;
					LOCATION_OFFSET_Y = 0;
				}
			}
			blocked=false;
	}
	
	public void writeTitle(String title) {
		this.setTitle(title);
	}
	
	public String getMyStatus() {
		if(this.statusLabel!=null)
			return this.statusLabel.getText();
		else return "";
	}
	
	public void writeSomethingToStatus(String writeString){
		if(statusLabel!=null)
			statusLabel.setText(writeString);
	}
	
	public void writeSomethingToMainWindow(String writeString){
		if(this.mainLabel!=null)
			this.mainLabel.setText(writeString);
	}
	
	public String getMyMainWindowText(){
		if(mainLabel!=null)
			return mainLabel.getText();
		return "";
	}
	
	
	public void closeWindow(boolean stopTranscoder){		
//    	currentX = getLocation().getX();
//		currentY = getLocation().getY()-LOCATION_OFFSET_Y;	
		windowsMap.remove(this.id);
		
		setVisible(false);
//			posSet.remove(this.initialTriple);
		for(Map.Entry<String, SimpleProxyServer> es:transcoderObj.getProxies().entrySet()) {
			es.getValue().stopMe();}
		if(transcoderObj!=null && stopTranscoder)
			transcoderObj.removeClient("requestStop", "requestStop");
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		dispose();

	}
	
	
	public void setTranscoder(InternalTranscoder it) {
		this.transcoderObj=it;
	}
	
	WindowAdapter exitListener = new WindowAdapter() {

	    @Override
	    public void windowClosing(WindowEvent e) {		
	    	closeWindow(true);
	    }
	};
	
	
}
