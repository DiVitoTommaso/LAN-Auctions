package components;

import java.net.InetAddress;

import java.net.UnknownHostException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import server.IP;

public class ObjectsManager {

	private static ObjectsManager instance = new ObjectsManager();

	private List<AuctionObject> objects = Collections.<AuctionObject>synchronizedList(new LinkedList<AuctionObject>());

	public static ObjectsManager getInstance() {
		return instance;
	}

	private ObjectsManager() {

	}

	public LinkedList<AuctionObject> getByCategoria(String categoria) {
		LinkedList<AuctionObject> trovati = new LinkedList<AuctionObject>();

		if (categoria == null)
			return trovati;

		for (AuctionObject o : objects)
			if (o.getCategory().equalsIgnoreCase(categoria))
				trovati.add(o);

		return trovati;
	}

	public LinkedList<AuctionObject> genericSearch(String string) {
		LinkedList<AuctionObject> trovati = new LinkedList<AuctionObject>();

		if (string == null)
			return trovati;

		for (AuctionObject o : objects)
			if (o.getTitle().toLowerCase().contains(string.toLowerCase().replaceAll("\\s+", " ")))
				trovati.add(o);
			else if (o.getCategory().toLowerCase().contains(string.toLowerCase().replaceAll("\\s+", " ")))
				trovati.add(o);

		return trovati;
	}

	public synchronized void newAuction(AuctionObject o) {
		try {
			objects.add(o);
			o.setGroup(InetAddress.getByName(IP.nextAvailableIP()));
			AuctionManager.createNewListener(o).startListen();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	public synchronized void closeAuction(AuctionObject o) {
		IP.freeIP(o.getGroup().getHostAddress());// riciclo l'ip per nuove aste
	}
}
