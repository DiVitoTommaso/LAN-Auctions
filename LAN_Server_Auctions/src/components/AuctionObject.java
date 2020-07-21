package components;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Date;

public class AuctionObject implements Serializable {

	private static final long serialVersionUID = 1L;

	private InetAddress group;
	private byte[] image;
	private String owner;
	private String bestoffer = "";

	private int quantity;
	private double price;
	private String title;
	private String category;

	private Date start;
	private Date end;

	public AuctionObject(byte[] image, int quantity, String title, Date start, Date end, double price, String category,
			String owner) {
		super();
		if (image == null || quantity < 0 || title == null || start.after(new Date(end.getTime() - 1))
				|| start.before(new Date(System.currentTimeMillis())) || price < 0)
			throw new IllegalArgumentException("Invalid values");

		this.image = image;
		this.owner = owner;
		this.quantity = quantity;
		this.price = price;
		this.title = title;
		this.category = category;
		this.start = start;
		this.end = end;
	}

	public byte[] getImage() {
		return image;
	}

	public String getOwner() {
		return owner;
	}

	public String getBestoffer() {
		return bestoffer;
	}

	public int getQuantity() {
		return quantity;
	}

	public double getPrice() {
		return price;
	}

	public String getTitle() {
		return title;
	}

	public Date getStart() {
		return start;
	}

	public Date getEnd() {
		return end;
	}

	public InetAddress getGroup() {
		return group;
	}

	public void setGroup(InetAddress group) {
		this.group = group;
	}

	public void updateBestOffer(String offer) {
		this.bestoffer = offer;
	}

	public void updatePrice(double price) {
		if (price > 0)
			this.price = price;
	}

	public String getCategory() {
		return category;
	}

}
