package application.login;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import application.MainApplication;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

public class LoginController {

	@FXML
	private Pane root;
	@FXML
	private TextField username;
	@FXML
	private PasswordField password;
	@FXML
	private Text info;
	@FXML
	private Button login;

	@FXML
	private void initialize() {
		root.setBackground(
				new Background(new BackgroundImage(
						new Image(getClass().getResourceAsStream("/components/res/background.jpg"), 300, 300, false,
								true),
						BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
						BackgroundSize.DEFAULT)));
	}

	@FXML
	private void handleLogin() {
		username.setDisable(true);
		password.setDisable(true);
		login.setDisable(true);

		new Thread(this::login).start();
	}

	private void login() {
		try (Socket client = new Socket("127.0.0.1", 40002)) {

			if (username.getText().isEmpty() || password.getText().isEmpty()) {
				Platform.runLater(new Runnable() {

					@Override
					public void run() {
						info.setText("Invalid username or password");
					}
				});
				return;
			}

			ObjectOutputStream writer = new ObjectOutputStream(client.getOutputStream());
			ObjectInputStream reader = new ObjectInputStream(client.getInputStream());

			JSONObject json = new JSONObject();
			json.put("username", username.getText()).put("password", password.getText());
			writer.writeUTF(json.toString());
			writer.flush();

			if (!reader.readBoolean()) {
				Platform.runLater(new Runnable() {

					@Override
					public void run() {
						info.setText("Invalid password for this account");
					}
				});
				return;
			}

			MainApplication.setUsername(username.getText());
			Platform.runLater(MainApplication::showSearchMenu);
		} catch (IOException e) {

			Platform.runLater(new Runnable() {

				@Override
				public void run() {
					info.setText("Connection to server failed");
				}
			});
		} catch (JSONException e) {
			Platform.runLater(new Runnable() {

				@Override
				public void run() {
					info.setText("Invalid characters");
				}
			});
		} finally {
			username.setDisable(false);
			password.setDisable(false);
			login.setDisable(false);
		}
	}
}
