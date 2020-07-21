package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.scene.Scene;
import javafx.scene.image.Image;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

import application.auctionView.AuctionViewController;
import components.AuctionObject;

public class MainApplication extends Application {

	private static Stage logStage = new Stage();
	private static Stage searchStage = new Stage();
	private static Stage postStage = new Stage();
	private static FileChooser file = new FileChooser();

	private static String username;

	@Override
	public void stop() {
		System.exit(0);
	}

	@Override
	public void start(Stage primaryStage) {
		try {
			// inizializzo gli stage principali
			Scene logScene = new Scene(FXMLLoader.load(getClass().getResource("/application/login/Login.fxml")), 300,
					300);
			Scene auctionSearchScene = new Scene(
					FXMLLoader.load(getClass().getResource("/application/auctionSearch/AuctionSearch.fxml")), 600, 600);
			Scene postScene = new Scene(
					FXMLLoader.load(getClass().getResource("/application/auctionPost/AuctionPost.fxml")), 500, 350);

			searchStage.setScene(auctionSearchScene);
			logStage.setScene(logScene);
			postStage.setScene(postScene);

			postStage.setTitle("Jebay");
			logStage.setTitle("Jebay");
			searchStage.setTitle("Jebay");
			postStage.setTitle("Jebay");

			logStage.getIcons().add(
					new Image(getClass().getResourceAsStream("/components/res/background.jpg"), 300, 300, false, true));
			searchStage.getIcons().add(
					new Image(getClass().getResourceAsStream("/components/res/background.jpg"), 600, 600, false, true));
			postStage.getIcons().add(
					new Image(getClass().getResourceAsStream("/components/res/background.jpg"), 500, 350, false, true));

			// aggiungo un filtro in modo che solo i file .jpg possano essere selezionati
			// durante la scelta dell'immagine dell'oggetto messo in vendita
			file.getExtensionFilters().add(new ExtensionFilter("JPG files (*.jpg)", "*.jpg"));

			logStage.setResizable(false);
			searchStage.setResizable(false);
			postStage.setResizable(false);
			logStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		launch(args);
	}

	public static void showSearchMenu() {
		logStage.hide();
		searchStage.show();
	}

	public static File showFilePost() {
		// faccio scegliere l'immagine all'utente
		return file.showOpenDialog(postStage);
	}

	public static void showPostMenu() {
		postStage.show();
	}

	public static void setUsername(String username) {
		MainApplication.username = username;
	}

	public static Stage newAstaStage(AuctionObject o) throws IOException {
		Stage auction = new Stage();
		FXMLLoader auctionView = new FXMLLoader(
				MainApplication.class.getResource("/application/auctionView/AuctionView.fxml"));
		Scene auctionScene = new Scene(auctionView.load(), 500, 350);
		AuctionViewController viewController = auctionView.getController();
		viewController.connectView(new Image(new ByteArrayInputStream(o.getImage()), 157, 157, false, false),
				o.getTitle(), "" + o.getPrice(), "" + o.getQuantity(), o.getCategory(),
				new SimpleDateFormat("dd/MM/yyyy HH:mm").format(o.getStart()),
				new SimpleDateFormat("dd/MM/yyyy HH:mm").format(o.getEnd()), o.getGroup(), o.getOwner());

		auction.setScene(auctionScene);
		auction.setTitle("Jebay");
		auction.getIcons().add(new Image(MainApplication.class.getResourceAsStream("/components/res/background.jpg"),
				500, 350, false, true));

		auction.setOnCloseRequest(viewController::disconnectView);
		auction.setResizable(false);
		auction.show();
		return auction;

	}

	public static String getUsername() {
		return username;
	}

}
