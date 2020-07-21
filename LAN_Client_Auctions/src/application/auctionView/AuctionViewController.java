package application.auctionView;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;

import org.json.JSONException;
import org.json.JSONObject;

import application.MainApplication;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Pane;
import javafx.stage.WindowEvent;

public class AuctionViewController {

	@FXML
	private Pane root;
	@FXML
	private ImageView productImage;
	@FXML
	private Label productPrice;
	@FXML
	private Label productQuantity;
	@FXML
	private Label productCategory;
	@FXML
	private Label startDate;
	@FXML
	private Label endDate;
	@FXML
	private Label productTitle;
	@FXML
	private TextField makeOffer;
	@FXML
	private Label owner;
	@FXML
	private Label bestOffer;
	@FXML
	private Label infoText;
	@FXML
	private Button sendButton;

	private MulticastSocket socket;

	private DatagramPacket send;
	private DatagramPacket recv;

	@FXML
	private void initialize() {
		root.setBackground(
				new Background(new BackgroundImage(
						new Image(getClass().getResourceAsStream("/components/res/background.jpg"), 500, 350, false,
								true),
						BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
						BackgroundSize.DEFAULT)));
	}

	public void connectView(Image image, String title, String price, String quantity, String category, String start,
			String end, InetAddress group, String owner) {

		this.productImage.setImage(image);
		this.productTitle.setText(this.productTitle.getText() + title);
		this.productPrice.setText(this.productPrice.getText() + price);
		this.productQuantity.setText(this.productQuantity.getText() + quantity);
		this.productCategory.setText(this.productCategory.getText() + category);
		this.startDate.setText(this.startDate.getText() + start);
		this.endDate.setText(this.endDate.getText() + end);
		this.owner.setText("Proprietario:" + owner);

		try {
			socket = new MulticastSocket(50001);
			socket.joinGroup(group);
			recv = new DatagramPacket(new byte[1024], 1024);
			send = new DatagramPacket(new byte[1024], 1024, group, 50000);

			new Thread(this::listenBets).start();

			if (owner.equals(MainApplication.getUsername())) {
				sendButton.setDisable(true);
				infoText.setText("You can't make offer\n  inside your auction");
			}

			handleSendBet();
		} catch (IOException e) {
		}

		makeOffer.textProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				try {
					Double.parseDouble(newValue);
				} catch (Exception e) {
					if (!newValue.isEmpty())
						makeOffer.setText(oldValue);
				}
			}
		});
	}

	@FXML
	private void handleSendBet() {
		try {
			JSONObject json = new JSONObject();

			if (makeOffer.getText().isEmpty())
				makeOffer.setText("0");

			json.put("username", MainApplication.getUsername()).put("offeredPrice", makeOffer.getText());

			send.setData(json.toString().getBytes());
			send.setLength(json.toString().getBytes().length);
			socket.send(send);
		} catch (IOException | JSONException e) {
		}
	}

	private void listenBets() {
		while (true)
			try {
				socket.receive(recv);
				JSONObject json = new JSONObject(new String(recv.getData()));
				Platform.runLater(new Runnable() {

					@Override
					public void run() {
						try {
							bestOffer.setText("Best offer:" + json.getString("bestoffer"));
							productPrice.setText("Price:" + json.getString("price") + "€");

							if (json.getBoolean("isover")) {
								sendButton.setDisable(true);
								infoText.setText("Auction is ended.\n The winner is " + json.getString("bestoffer"));
							}
						} catch (JSONException e) {
						}
					}
				});
			} catch (SocketException e) {
				break;
			} catch (IOException e) {
			} catch (JSONException e) {
			} catch (Exception e) {
			}
	}

	public void disconnectView(WindowEvent event) {
		socket.close();

	}
}
