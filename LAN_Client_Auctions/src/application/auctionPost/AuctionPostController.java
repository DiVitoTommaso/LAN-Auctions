package application.auctionPost;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import javax.imageio.ImageIO;

import org.json.JSONException;
import org.json.JSONObject;

import application.MainApplication;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class AuctionPostController {

	@FXML
	private Pane root;
	@FXML
	private ImageView productImage;
	@FXML
	private TextField productTitle;
	@FXML
	private TextField productCategory;
	@FXML
	private TextField productPrice;
	@FXML
	private TextField productQuantity;
	@FXML
	private DatePicker startDate;
	@FXML
	private DatePicker endDate;
	@FXML
	private TextField startHour;
	@FXML
	private TextField endHour;
	@FXML
	private Text info;

	@FXML
	private void initialize() {
		root.setBackground(
				new Background(new BackgroundImage(
						new Image(getClass().getResourceAsStream("/components/res/background.jpg"), 500, 350, false,
								true),
						BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
						BackgroundSize.DEFAULT)));
		productImage.setImage(
				new Image(getClass().getResourceAsStream("/components/res/blackImage.png"), 150, 150, false, true));

	}

	@FXML
	private void handleImmagine() {
		File f = MainApplication.showFilePost();
		if (f != null)
			try {
				productImage.setImage(new Image(new FileInputStream(f), 150, 150, false, true));
			} catch (FileNotFoundException e) {
			}

	}

	@FXML
	private void handleSend() {
		productImage.setDisable(true);
		productQuantity.setDisable(true);
		startDate.setDisable(true);
		endDate.setDisable(true);
		endHour.setDisable(true);
		startHour.setDisable(true);
		productPrice.setDisable(true);
		productCategory.setDisable(true);
		productTitle.setDisable(true);

		new Thread(this::post).start();
	}

	private void post() {

		try (Socket client = new Socket("127.0.0.1", 40001)) {
			ObjectOutputStream writer = new ObjectOutputStream(client.getOutputStream());
			ObjectInputStream reader = new ObjectInputStream(client.getInputStream());
			JSONObject json = new JSONObject();

			json.put("title", productTitle.getText()).put("username", MainApplication.getUsername())
					.put("quantity", productQuantity.getText()).put("category", productCategory.getText())
					.put("price", productPrice.getText()).put("start", startDate.getValue() + " " + startHour.getText())
					.put("end", endDate.getValue() + " " + endHour.getText());

			writer.writeObject(getImage());
			writer.flush();

			writer.writeUTF(json.toString());
			writer.flush();

			if (reader.readBoolean())
				Platform.runLater(new Runnable() {

					@Override
					public void run() {
						info.setFill(Color.GREEN);
						info.setLayoutY(333);
						info.setStyle("-fx-font-size:20;-fx-font-weight:bold");
						info.setText("Success!");

					}
				});
			else {
				Platform.runLater(new Runnable() {

					@Override
					public void run() {
						info.setFill(Color.RED);
						info.setLayoutY(313);
						info.setStyle("-fx-font-size:13.5;-fx-font-weight:bold");
						info.setText("Form Error.\nStart date -> After actual date\nPrice,Amount -> Numbers");

					}
				});
				return;
			}

			Platform.runLater(new Runnable() {

				@Override
				public void run() {
					productImage.setImage(new Image(getClass().getResourceAsStream("/components/res/blackImage.png"),
							150, 150, false, true));
					productTitle.setText("");
					productPrice.setText("");
					startHour.setText("");
					productCategory.setText("");
					endHour.setText("");
					productQuantity.setText("");
					startDate.getEditor().setText("");
					endDate.getEditor().setText("");

				}
			});

		} catch (IOException e) {
			Platform.runLater(new Runnable() {

				@Override
				public void run() {
					info.setFill(Color.RED);
					info.setLayoutY(333);
					info.setStyle("-fx-font-size:20;-fx-font-weight:bold");
					info.setText("Connection to server failed");

				}
			});
		} catch (JSONException e) {
		} finally {
			productImage.setDisable(false);
			productQuantity.setDisable(false);
			startDate.setDisable(false);
			endDate.setDisable(false);
			endHour.setDisable(false);
			startHour.setDisable(false);
			productPrice.setDisable(false);
			productCategory.setDisable(false);
			productTitle.setDisable(false);
		}
	}

	// traduco l'immagine in byte[]
	private byte[] getImage() {
		try {
			BufferedImage bImage = SwingFXUtils.fromFXImage(productImage.getImage(), null);
			ByteArrayOutputStream s = new ByteArrayOutputStream();
			ImageIO.write(bImage, "jpg", s);
			byte[] immagine = s.toByteArray();
			return immagine;
		} catch (Exception e) {
		}

		return null;
	}
}
