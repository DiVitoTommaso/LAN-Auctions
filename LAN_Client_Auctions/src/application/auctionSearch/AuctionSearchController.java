package application.auctionSearch;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import application.MainApplication;
import components.AuctionObject;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class AuctionSearchController {

	@FXML
	private Pane root;
	@FXML
	private Button sell;
	@FXML
	private Button search;
	@FXML
	private TextField searchBar;
	@FXML
	private RadioButton category;
	@FXML
	private Pagination auctions;
	@FXML
	private Text infoText;

	private List<AuctionObject> objects = Collections.<AuctionObject>synchronizedList(new LinkedList<AuctionObject>());;

	private List<Thread> listeners = Collections.<Thread>synchronizedList(new LinkedList<Thread>());

	@FXML
	private void initialize() {
		root.setBackground(
				new Background(new BackgroundImage(
						new Image(getClass().getResourceAsStream("/components/res/background.jpg"), 600, 600, false,
								true),
						BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
						BackgroundSize.DEFAULT)));

	}

	@FXML
	private void handleSearch() {
		search.setDisable(true);
		infoText.setText("");
		new Thread(this::search).start();
	}

	@SuppressWarnings("unchecked")
	private void search() {

		try (Socket client = new Socket("127.0.0.1", 40000)) {
			ObjectOutputStream writer = new ObjectOutputStream(client.getOutputStream());
			ObjectInputStream reader = new ObjectInputStream(client.getInputStream());

			if (category.isSelected())
				writer.writeUTF("category");
			else
				writer.writeUTF("generic");
			writer.flush();

			writer.writeUTF(searchBar.getText());
			writer.flush();

			objects = Collections.<AuctionObject>synchronizedList((List<AuctionObject>) reader.readObject());

			for (Thread listener : listeners)
				listener.interrupt();

			Platform.runLater(this::createPage);
		} catch (IOException | ClassNotFoundException e) {
			Platform.runLater(new Runnable() {

				@Override
				public void run() {
					infoText.setText("Connection to server failed");
				}
			});
		} finally {
			search.setDisable(false);
		}

	}

	private void createPage() {
		if (objects.size() == 0) {
			auctions.setVisible(false);
			return;
		}

		if (objects.size() % 3 == 0)
			auctions.setPageCount(objects.size() / 3);
		else
			auctions.setPageCount(objects.size() / 3 + 1);

		auctions.setPageFactory(this::buildPagination);
		auctions.setVisible(true);

	}

	private Pane buildPagination(int index) {
		Pane pane = new Pane();
		int page = index * 3;

		int y = 0;

		// costruzuione paginata

		for (int i = page; i < page + 3 && i < objects.size(); i++) {
			final AuctionObject object = objects.get(i);

			final Rectangle rect = new Rectangle(600, 150);
			rect.setStrokeWidth(4);
			rect.setFill(Color.TRANSPARENT);
			rect.setStroke(Color.WHITE);
			rect.setLayoutY(y + 3);

			ImageView image = new ImageView(
					new Image(new ByteArrayInputStream(object.getImage()), 120, 120, false, false));
			image.setLayoutX(3);
			image.setLayoutY(y + 6);

			Label owner = new Label("Owner:" + object.getOwner());
			owner.setStyle("-fx-font-weight:bold;-fx-font-size:14");
			owner.setLayoutX(3);
			owner.setLayoutY(123 + y);
			owner.setMaxWidth(200);

			Label bestOffer;
			if (object.getEnd().before(new Date(System.currentTimeMillis()))) {
				bestOffer = new Label("Winner:" + object.getBestoffer());
				bestOffer.setStyle("-fx-text-fill:green;-fx-font-weight:bold;-fx-font-size:14");
			} else {
				bestOffer = new Label("Best offer:" + object.getBestoffer());
				bestOffer.setStyle("-fx-font-weight:bold;-fx-font-size:14");
			}
			bestOffer.setLayoutX(3);
			bestOffer.setLayoutY(135 + y);
			bestOffer.setMaxWidth(200);

			Label category = new Label("Category: " + object.getCategory());
			category.setStyle("-fx-font-weight:bold;-fx-font-size:20");
			category.setLayoutX(400);
			category.setLayoutY(y + 45);
			category.setMaxWidth(200);

			Label title = new Label(object.getTitle());
			title.setStyle("-fx-font-weight:bold;-fx-Label-color:black;-fx-font-weight:bold;-fx-font-size:36");
			title.setLayoutX(230);
			title.setLayoutY(y - 5);
			title.setMaxWidth(200);

			Label quantity = new Label("Q: " + object.getQuantity());
			quantity.setStyle("-fx-font-weight:bold;-fx-font-size:20");
			quantity.setLayoutX(125);
			quantity.setLayoutY(45 + y);
			quantity.setMaxWidth(100);

			Label price = new Label("Price:");
			price.setStyle("-fx-font-weight:bold;-fx-font-size:20");
			price.setLayoutX(230);
			price.setLayoutY(45 + y);

			Label priceValue = new Label(object.getPrice() + "€");
			priceValue.setStyle("-fx-Label-fill:blue;-fx-font-weight:bold;-fx-font-size:20");
			priceValue.setLayoutX(295);
			priceValue.setLayoutY(45 + y);
			priceValue.setMaxWidth(100);

			Label startDate = new Label("From:" + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(object.getStart()));
			startDate.setStyle("-fx-font-weight:bold;-fx-font-size:20");
			startDate.setLayoutX(125);
			startDate.setLayoutY(104 + y);

			Label endDate = new Label("To:" + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(object.getEnd()));
			endDate.setStyle("-fx-font-weight:bold;-fx-font-size:20");
			endDate.setLayoutX(400);
			endDate.setLayoutY(104 + y);

			Button join = new Button("Join");
			join.setStyle("-fx-background-color:lightblue;-fx-font-size:14");
			join.setPrefWidth(130);
			join.prefHeight(30);
			join.setLayoutX(230);
			join.setLayoutY(70 + y);

			setUpJoinButtonForAnObject(join, object);

			pane.getChildren().addAll(category, rect, bestOffer, owner, image, title, quantity, price, priceValue,
					endDate, startDate, join);
			y += 150;
		}

		return pane;
	}

	@FXML
	private void handleShowSell() {
		MainApplication.showPostMenu();
	}

	private void setUpJoinButtonForAnObject(Button joinAsta, AuctionObject object) {
		createView(joinAsta, object);
		new Thread(new Runnable() {

			@Override
			public void run() {
				createAuctionListener(joinAsta, object);
			}
		}).start();
	}

	private void createAuctionListener(Button joinAsta, AuctionObject object) {
		// creo un thread che abilita e disabilita il bottone di entrata
		try {
			// disabilito
			joinAsta.setDisable(true);
			listeners.add(Thread.currentThread());
			// aspetto che inizi l'asta
			if (object.getStart().after(new Date(System.currentTimeMillis())))
				Thread.sleep(object.getStart().getTime() - System.currentTimeMillis());
			// riabilito il pulsante di entrata
			joinAsta.setDisable(false);
			// aspetto che finisca l'asta
			if (object.getEnd().after(new Date(System.currentTimeMillis())))
				Thread.sleep(object.getEnd().getTime() - System.currentTimeMillis());
			// disabilito l'asta
			joinAsta.setDisable(true);
			// imposto a false per far in modo che quando l'utente chiude la view dell'asta
			// NON si riabiliti il pulsante di entrata se il tempo è scaduto
			listeners.remove(Thread.currentThread());
		} catch (InterruptedException e) {
			joinAsta.setDisable(false);
			listeners.remove(Thread.currentThread());
		}
	}

	private void createView(Button joinAsta, AuctionObject object) {
		// on click apro lo stage dell'asta specifica
		joinAsta.setOnMouseClicked(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				try {
					MainApplication.newAstaStage(object);
				} catch (Exception e1) {
					e1.printStackTrace();
				}

			}
		});
	}

}
