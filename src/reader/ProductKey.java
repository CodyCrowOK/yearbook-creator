package reader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;

import writer.Config;

public class ProductKey {

	public static int productKeyQuery(URL url, String key) throws IOException {
		Map<String,Object> params = new LinkedHashMap<>();
		params.put("key", key);

		StringBuilder postData = new StringBuilder();
		for (Map.Entry<String,Object> param : params.entrySet()) {
			if (postData.length() != 0) postData.append('&');
			postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
			postData.append('=');
			postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
		}
		byte[] postDataBytes = postData.toString().getBytes("UTF-8");

		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
		conn.setDoOutput(true);
		conn.getOutputStream().write(postDataBytes);
		if (conn.getResponseCode() == 403) return 403;
		//If the user isn't connected to the internet, let's still do our best to validate their key. 
		else if (validate_luhn(key)) return 202;
		else return 403;
	}

	/**
	 * Validates potential product keys using the Luhn mod 10 algorithm,
	 * with the assumption that the check digit is 7.
	 * Luhn mod 10 is described in ISO 7812.
	 * Doesn't check for duplicates, because that's obviously not possible.
	 * @param key number to be validated
	 * @return true if check digit is 7
	 */
	public static boolean validate_luhn(String key) {
		int sum = 0;
		boolean alternate = false;
		for (int i = key.length() - 1; i >= 0; i--) {
			int n = Integer.parseInt(key.substring(i, i + 1));
			if (alternate) {
				n *= 2;
				if (n > 9) {
					n = (n % 10) + 1;
				}
			}
			sum += n;
			alternate = !alternate;
		}
		return sum % 10 == 7;
	}

	public static String generateKeys(URL url, int n) throws IOException {
		String json = "";

		Map<String,Object> params = new LinkedHashMap<>();
		params.put("range", Integer.toString(n));

		StringBuilder postData = new StringBuilder();
		for (Map.Entry<String,Object> param : params.entrySet()) {
			if (postData.length() != 0) postData.append('&');
			postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
			postData.append('=');
			postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
		}
		byte[] postDataBytes = postData.toString().getBytes("UTF-8");

		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
		conn.setDoOutput(true);
		conn.getOutputStream().write(postDataBytes);

		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
		for (int c = in.read(); c != -1; c = in.read())
			json += (char) c;
		return json;
	}

	public static String[] parseJSONArray(String json) {
		String stripped = "";
		for (int i = 0; i < json.length(); i++) {
			char c = json.charAt(i);
			if (!(c == '[' || c == ']' || c == '"')) {
				stripped += c;
			}
		}
		return stripped.split(",");
	}

	public static void main() {
		try {
			parseJSONArray(generateKeys(new URL(new Config().generateProductKeyURL), 5));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
