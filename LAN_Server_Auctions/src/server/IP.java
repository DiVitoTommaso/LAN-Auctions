package server;

import java.util.LinkedList;

public class IP {

	private static LinkedList<String> reUsableIPs = new LinkedList<String>();
	private static String ip = "224.0.0.0";

	public static synchronized String nextAvailableIP() throws IndexOutOfBoundsException {
		if (reUsableIPs.size() > 0)
			return reUsableIPs.removeLast();

		String[] temp = ip.split("\\.");

		if (Integer.parseInt(temp[3]) < 255)
			temp[3] = "" + (Integer.parseInt(temp[3]) + 1);
		else if (Integer.parseInt(temp[2]) < 255) {
			temp[3] = "0";
			temp[2] = "" + (Integer.parseInt(temp[2]) + 1);
		} else if (Integer.parseInt(temp[1]) < 255) {
			temp[3] = "0";
			temp[2] = "0";
			temp[1] = "" + (Integer.parseInt(temp[1]) + 1);
		} else if (Integer.parseInt(temp[0]) < 240) {
			temp[3] = "0";
			temp[2] = "0";
			temp[1] = "0";
			temp[0] = "" + (Integer.parseInt(temp[0]) + 1);
		} else
			throw new IndexOutOfBoundsException("ip finiti");

		return ip = String.join(".", temp);
	}

	public static synchronized void freeIP(String ip) {
		reUsableIPs.add(ip);
	}
}
