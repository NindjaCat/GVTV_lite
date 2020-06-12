package main;

import java.util.HashMap;

import org.apache.commons.lang3.tuple.Triple;

import channels.ChannelItem;

public class TranscoderBuilder implements BuilderInterface{

	private ChannelItem channel;
	private String serverIp;
	private Byte quality;
	private String protocol;
	private String multicastIp;
	private Byte resolution;
		

	@Override
	public InternalTranscoder getProduct() {		 
		InternalTranscoder myProduct=new InternalTranscoder(this.channel); 
		if(this.multicastIp!=null)
			myProduct.setMulticastIp(this.multicastIp);
		else
			myProduct.setMulticastIp("239.0.0.0");		
		myProduct.setServerIp(this.serverIp);
		myProduct.setQuality(this.quality);
		myProduct.setProtocol(this.protocol);
		myProduct.setResolution(this.resolution);
	    Sngltn.addInternalTranscoderToMap(Triple.of(this.protocol, this.channel, this.quality+(this.resolution*10)), myProduct);
	    this.reset();
	    return myProduct;
	 }

	@Override
	public void reset() {
		
		this.channel=null;
		this.serverIp=null;
		this.quality=null;
		this.protocol=null;
		this.multicastIp=null;
		
	}

	public TranscoderBuilder setQuality(final Byte quality) {
		this.quality = quality;
		return this;
	}
	
	public String getProtocol() {
		return protocol;
	}

	public TranscoderBuilder setProtocol(final String p) {
		this.protocol = p;
		return this;
	}
		
	public ChannelItem getChannel() {
		return channel;
	}

	public TranscoderBuilder setChannel(final ChannelItem channel) {
		this.channel = channel;
		return this;
	}

	public String getServerIp() {
		return serverIp;
	}

	public TranscoderBuilder setServerIp(final String serverIp) {
		this.serverIp = serverIp;
		return this;
	}

	public Byte getQuality() {
		return quality;
	}


	public String getMulticastIp() {
		return multicastIp;
	}

	public void setMulticastIp(final String multicastIp) {
		this.multicastIp = multicastIp;
	}
	
	public Byte getResolution() {
		return resolution;
	}

	public TranscoderBuilder setResolution(Byte resolution) {
		this.resolution = resolution;
		return this;
	}

}

