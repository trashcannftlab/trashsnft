package easyJava.etherScan;

import org.apache.http.HttpHost;

public class Main {

	public static void main(String[] args) throws Exception {
		HttpHost proxy = new HttpHost("localhost", 7890);
		HttpClientUtil.init(10, 2, proxy);
		
		
		ScanServiceImpl scanServiceImpl = new ScanServiceImpl();

		String contractAddress = "addr";
		String apiKey = "key";
		String url = "https://api.etherscan.io/api";
		
	}

}
