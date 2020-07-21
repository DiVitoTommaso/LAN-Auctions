package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Hashtable;
import java.util.LinkedList;

import org.json.JSONException;
import org.json.JSONObject;

import components.AuctionObject;
import components.ObjectsManager;

public class Server {

	private static ServerSocket objectSearchServer;
	private static ServerSocket objectPostServer;
	private static ServerSocket logServer;

	private static ObjectsManager gestore = ObjectsManager.getInstance();

	private static Hashtable<String, String> users = new Hashtable<String, String>();

	public static ObjectsManager getGestore() {
		return gestore;
	}

	private static void start(int searchPort, int postPort, int logPort) throws IOException {
		objectSearchServer = new ServerSocket(searchPort);
		objectPostServer = new ServerSocket(postPort);
		logServer = new ServerSocket(logPort);

		new Thread(Server::logListener).start();
		new Thread(Server::searchListener).start();
		new Thread(Server::postListener).start();

	}

	private static void logListener() {
		while (true) {
			Socket client;
			try {
				client = logServer.accept();
				new Thread(new Runnable() {

					@Override
					public void run() {
						login(client);
					}
				}).start();
			} catch (IOException e) {
			}
		}
	}

	private static void login(Socket s) {
		try (Socket client = s) {
			ObjectOutputStream writer = new ObjectOutputStream(client.getOutputStream());
			ObjectInputStream reader = new ObjectInputStream(client.getInputStream());

			JSONObject json = new JSONObject(reader.readUTF());

			// se non esiste l'username creo laccount----se esiste controllo se la password
			// è corretta o no -> se corretta scrivo true altrimenti scrivo false
			if (!users.containsKey(json.getString("username"))) {
				users.put(json.getString("username"), json.getString("password"));
				writer.writeBoolean(true);
			} else if (users.get(json.getString("username")).equals(json.getString("password")))
				writer.writeBoolean(true);
			else
				writer.writeBoolean(false);

			writer.flush();
		} catch (IOException | JSONException e) {
		}
	}

	private static void searchListener() {
		while (true) {
			Socket client;
			try {
				client = objectSearchServer.accept();
				new Thread(new Runnable() {

					@Override
					public void run() {
						search(client);
					}
				}).start();
			} catch (IOException e) {
			}

		}
	}

	private static void search(Socket s) {
		try (Socket client = s) {
			ObjectOutputStream writer = new ObjectOutputStream(client.getOutputStream());
			ObjectInputStream reader = new ObjectInputStream(client.getInputStream());

			LinkedList<AuctionObject> foundObjects = null;
			String searchType = reader.readUTF();

			if (searchType.equalsIgnoreCase("generic"))
				foundObjects = Server.getGestore().genericSearch(reader.readUTF());
			if (searchType.equalsIgnoreCase("category"))
				foundObjects = Server.getGestore().getByCategoria(reader.readUTF());

			writer.writeObject(foundObjects);
			writer.flush();

		} catch (IOException e) {
		}
	}

	private static void postListener() {
		while (true) {
			Socket client;
			try {
				client = objectPostServer.accept();
				new Thread(new Runnable() {

					@Override
					public void run() {
						post(client);
					}
				}).start();
			} catch (IOException e) {
			}
		}

	}

	private static void post(Socket s) {
		try (Socket client = s) {
			ObjectInputStream reader = new ObjectInputStream(client.getInputStream());
			ObjectOutputStream writer = new ObjectOutputStream(client.getOutputStream());

			byte[] image = (byte[]) reader.readObject();
			JSONObject json = new JSONObject(reader.readUTF());

			try {

				gestore.newAuction(new AuctionObject(image, json.getInt("quantity"), json.getString("title"),
						new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(json.getString("start")),
						new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(json.getString("end")), json.getDouble("price"),
						json.getString("category"), json.getString("username")));

				writer.writeBoolean(true);
				writer.flush();
			} catch (JSONException | IllegalArgumentException | ArrayIndexOutOfBoundsException | ParseException e) {
				writer.writeBoolean(false);
				writer.flush();
			}

		} catch (Exception e) {
		}
	}

	public static void main(String... args) {
		try {
			Server.start(40000, 40001, 40002);
		} catch (IOException e) {
			System.exit(-1);
		}
	}
}
