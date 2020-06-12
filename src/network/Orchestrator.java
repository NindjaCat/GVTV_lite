package network;

import channels.Settings;
import main.App;
import main.Sngltn;

public class Orchestrator implements Runnable {

	public Orchestrator() {
		
	}
	
	@Override
	public void run() {
		while(true) {
			for(Settings.Servers.Server entry:App.settings.getServers().getServer()) {
				int tempId = (int)entry.getId();
				String tempCommand=Sngltn.getInstance().getCommandsForIdFromQueue(tempId);
				if(tempCommand!=null) {
					int abort=0;
					Sngltn.getInstance().tvMapGetDispacherById(tempId).evaluateChannelString(tempCommand.replace("#", ""), false);
					while(tempCommand.startsWith("#") && abort<5) {
						Sngltn.getInstance().tvMapGetDispacherById(tempId).evaluateChannelString(tempCommand.replace("#", ""), false);
						tempCommand=Sngltn.getInstance().getCommandsForIdFromQueue(tempId);
						abort++;
						if(tempCommand==null)
							break;
					}
					Sngltn.getInstance().clearCommandsForIdFromQueue(tempId);					
				}
			}
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {				
				System.err.println("Wake up Neo!");
			}	
		}
	}

}
