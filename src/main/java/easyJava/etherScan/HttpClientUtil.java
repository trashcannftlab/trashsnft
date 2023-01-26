package easyJava.etherScan;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;


/**
 * @author joe
 *
 * 
 */
public class HttpClientUtil {
	
	private static PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
	
	private static HttpHost proxy;
	
	static {
		cm.setMaxTotal(10);
		cm.setDefaultMaxPerRoute(2);		
	}
	
	public static void init(int maxTotal, int maxPerRoute) {
		cm.setMaxTotal(maxTotal);
		cm.setDefaultMaxPerRoute(maxPerRoute);		
	}
	
	
	public static void init(int maxTotal, int maxPerRoute, HttpHost proxy) {
		cm.setMaxTotal(maxTotal);
		cm.setDefaultMaxPerRoute(maxPerRoute);	
		HttpClientUtil.proxy = proxy;	
	}
	
	
	public static String httpGet(String url, Map<String, String> map, Map<String, String> header){
		
		CloseableHttpClient httpClient = null;
		CloseableHttpResponse httpResponse = null;		
		
		String responseBody = null;
		
		try {					
			HttpClientBuilder builder = HttpClients.custom()
				.setKeepAliveStrategy(DefaultConnectionKeepAliveStrategy.INSTANCE)
		        .setConnectionManager(cm);
			
			if(proxy!=null) {
				builder.setProxy(proxy);
			}
			
			httpClient = builder.build();
			        
			
			URIBuilder uriBuilder = new URIBuilder(new StringBuilder(url).toString());
			if(map!=null) {
				for(Entry<String,String> entry: map.entrySet()) {
					uriBuilder.addParameter(entry.getKey(), entry.getValue());
				}
			}
			
 			HttpGet httpGet = new HttpGet(uriBuilder.build());
 			
 			if(header!=null) {
				for(Entry<String, String> entry: header.entrySet()) {
					httpGet.addHeader(entry.getKey(), entry.getValue());
				}
			}
 			
			httpResponse = httpClient.execute(httpGet);
			HttpEntity entity = httpResponse.getEntity();
			responseBody = EntityUtils.toString(entity, "UTF-8");
			
			return responseBody;

		}catch(Exception ex) {
			ex.printStackTrace();
		}finally {
			if(httpResponse!=null){
				try {
					httpResponse.close();
				} catch (IOException e) {
				}
			}
		}
		
		return null;
	}
	
	
	
	public static String httpPost(String url, Map<String, String> map, Map<String, String> header, String requestBody){
		
		CloseableHttpClient httpClient = null;
		CloseableHttpResponse httpResponse = null;
		
				
		String responseBody = null;
		
		try {
			
			HttpClientBuilder builder = HttpClients.custom()
					.setKeepAliveStrategy(DefaultConnectionKeepAliveStrategy.INSTANCE)
			        .setConnectionManager(cm);
			
			if(proxy!=null) {
				builder.setProxy(proxy);
			}
			
			httpClient =  builder.build();
			
			URIBuilder uriBuilder = new URIBuilder(new StringBuilder(url).toString());
			if(map!=null) {
				for(Entry<String,String> entry: map.entrySet()) {
					uriBuilder.addParameter(entry.getKey(), entry.getValue());
				}
			}
			
			HttpPost httpPost = new HttpPost (uriBuilder.build());
			
			if(header!=null) {
				for(Entry<String, String> entry: header.entrySet()) {
					httpPost.addHeader(entry.getKey(), entry.getValue());
				}
			}
			
			if(requestBody!=null) {
				StringEntity userEntity = new StringEntity(requestBody, "UTF-8");
				httpPost.setEntity(userEntity);
			}			
		        
			httpResponse = httpClient.execute(httpPost);
			HttpEntity entity = httpResponse.getEntity();
			responseBody = EntityUtils.toString(entity, "UTF-8");

			return responseBody;
			
		}catch(Exception ex) {
			ex.printStackTrace();
		}finally {
			if(httpResponse!=null){
				try {
					httpResponse.close();
				} catch (IOException e) {
				}
			}
		}
		
		return null;
	}
	
	
	
	static class FileDownloadResponseHandler implements ResponseHandler<File> {

		private final File target;

		public FileDownloadResponseHandler(File target) {
			this.target = target;
		}

		//@Override
		public File handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
			InputStream source = response.getEntity().getContent();
			FileUtils.copyInputStreamToFile(source, this.target);
			return this.target;
		}
		
	}
	
	public static File download(URL url, File dstFile) {
		
		CloseableHttpClient httpClient = null;
		CloseableHttpResponse httpResponse = null;
				
		try {
			
			httpClient = HttpClients.custom()
					.setKeepAliveStrategy(DefaultConnectionKeepAliveStrategy.INSTANCE)
			        .setConnectionManager(cm)
			        .build();
			
			
			HttpGet get = new HttpGet(url.toURI()); // we're using GET but it could be via POST as well
			File downloaded = httpClient.execute(get, new FileDownloadResponseHandler(dstFile));
			return downloaded;
		} catch (Exception e) {
			throw new IllegalStateException(e);
		} finally {
			IOUtils.closeQuietly(httpClient);
		}
	}

}
