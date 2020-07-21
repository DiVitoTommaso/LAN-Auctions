package components;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.SocketException;

import org.json.JSONException;
import org.json.JSONObject;

import server.Server;

public class AuctionManager {

	private AuctionObject object;

	private Thread listener;
	private Thread closer;

	private boolean isOver = false;

	private MulticastSocket socket;

	private DatagramPacket recv;
	private DatagramPacket send;

	static AuctionManager createNewListener(AuctionObject object) {
		try {
			return new AuctionManager(object);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private AuctionManager(AuctionObject object) throws IOException {
		this.object = object;

		socket = new MulticastSocket(50000);
		socket.joinGroup(object.getGroup());

		recv = new DatagramPacket(new byte[1024], 1024);
		send = new DatagramPacket(new byte[1024], 1024, object.getGroup(), 50001);
		// clients port -> 50001, server -> 50000
	}

	public synchronized void startListen() {
		if (listener != null)
			return;

		listener = new Thread(this::listenBets);
		closer = new Thread(this::waitEnd);
		listener.start();
		closer.start();
	}

	private void waitEnd() {
		try {
			Thread.sleep(object.getEnd().getTime() - object.getStart().getTime() + object.getStart().getTime()
					- System.currentTimeMillis());
			isOver = true;
			announceWinnerAndClose();
		} catch (InterruptedException | IOException | JSONException e) {
			return;
		}

	}

	private void announceWinnerAndClose() throws JSONException, IOException {
		notifyClients();
		socket.close();

	}

	private synchronized void notifyClients() throws IOException, JSONException {
		String json = new JSONObject().put("bestoffer", object.getBestoffer()).put("price", object.getPrice())
				.put("isover", isOver).toString();

		send.setData(json.getBytes());
		send.setLength(json.getBytes().length);

		socket.send(send);
	}

	private void listenBets() {
		while (true) {
			try {
				socket.receive(recv);

				if (object.getStart().getTime() > System.currentTimeMillis())
					continue;

				JSONObject json = new JSONObject(new String(recv.getData()));

				if (json.getDouble("offeredPrice") > object.getPrice()
						&& !json.getString("username").equals(object.getOwner())) {

					object.updateBestOffer(json.getString("username"));
					object.updatePrice(json.getDouble("offeredPrice"));
				}
				notifyClients();
			} catch (SocketException e) {
				break;
			} catch (IOException | JSONException e) {
			}
		}

		Server.getGestore().closeAuction(object);
	}

}
