package channels;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author multimedia
 */
public class ChannelItem {

	private int id;
	private String name;
	private String link;

    public ChannelItem(int ID, String name, String link) {
        this.id=ID;
        this.name=name;
        this.link=link;    
    }
    
	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getLink() {
		return link;
	}


    @Override
    public String toString() {
        return link;
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (int) id;
        hash = 31 * hash + (name == null ? 0 : name.hashCode());
        return hash;
    }
    
    @Override
    public boolean equals(Object obj) {
    	if (this == obj)
    		return true;
    	if (obj == null)
    		return false;
    	if (getClass() != obj.getClass())
    		return false;
    	ChannelItem other = (ChannelItem) obj;
    	if (name == null) {
    		if (other.name != null)
    			return false;
    	} else if (!name.equals(other.name))
    		return false;
    	
    	
    	
    	return true;
    }
    
}
