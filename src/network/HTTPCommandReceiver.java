package network;

import java.io.DataOutputStream;
import java.io.File;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.tuple.Pair;

import main.Sngltn;
import main.App;
import main.Dispatcher;

@Path("/")
public class HTTPCommandReceiver {

  	@GET
    @Path("/HTTPCommandWithId/string/{id}/{channel}")
  	@Produces("text/html")
    public static Response getStreamIdProvided(@PathParam(value = "channel") String channel,@PathParam(value = "id") String id,@Context org.glassfish.grizzly.http.server.Request request) {

  		if (Sngltn.getInstance().isCurrentlyBlocked()) {
  			Sngltn.getInstance().setCurrentlyNotBlocked();
  			return Response.status(Status.TOO_MANY_REQUESTS).build();
	  	}
			
			
  		id=id.replaceAll("[^A-Za-z0-9\\%+-\\.]","");
  		channel=channel.replaceAll("[^A-Za-z0-9\\%+-\\.]","");	  		
  		channel=channel.replace("%20", " ");
  		System.err.println("HTML Channel Request/id,ch. " + id + " "+channel);
  		
  		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM hh:mm:ss");
  		String strDate= formatter.format(new Date());
  		
  		
  		Integer intId=1;
  		try{	  
  			intId = Integer.valueOf(id);
  			
  		}catch(Exception e) {
  			return Response.status(Status.BAD_REQUEST).build();
  		}
  		if(channel==null || id==null) {
  			return Response.status(Status.BAD_REQUEST).build();
  		}
  		
			
  			Dispatcher temp=Sngltn.getInstance().tvMapGetDispacherById(intId);
  			if(temp!=null) {
				if(!channel.contains("test")) {
					Sngltn.getInstance().addCommandsToQueue(intId, channel);
					App.orchThread.interrupt();
					}
  		  		Sngltn.writeToStatusMain(strDate + " ," + " id: " + intId + " " + channel);
  				ResponseBuilder response = Response.ok(String.valueOf(temp.getPortStream()),"text/html");
	  			Sngltn.getInstance().setCurrentlyNotBlocked();
  	    		return response.build();
  			}
  		
  		Sngltn.writeToStatusMain("E#");
			//Sngltn.getInstance().setCurrentlyNotBlocked();
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
  	}

}
