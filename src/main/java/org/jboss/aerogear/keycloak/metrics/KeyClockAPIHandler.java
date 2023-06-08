package org.jboss.aerogear.keycloak.metrics;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.DataOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
public class KeyClockAPIHandler{

    private static final String USER_AGENT = "Mozilla/5.0";


   

    // private static final String URL_KEYCLOAK= "https://bpa-nfr-v1.chainthat.net/auth";
	private static final String URL_KEYCLOAK= "http://localhost:8080/auth";


	private static final String POST_URL = "/admin/realms/{{realm}}/clients/{{clientIdServiceAccount}}/session-count";

	private static final String POST_PARAMS = "";

	private static String fetchToken() throws IOException {

		String urlToken = URL_KEYCLOAK + "/realms/master/protocol/openid-connect/token";
		
		String tokenResponse = invokeKeyCloakApi(urlToken,true,null);
		//System.out.println(tokenResponse);
		JsonObject jsonObject = new JsonParser().parse(tokenResponse).getAsJsonObject();

		String token = jsonObject.get("access_token").getAsString();
		System.out.println(token);
		return token;

	}

	private static String invokeKeyCloakApi(String url, boolean isToken, String token) throws IOException {

		URL obj = new URL(url);
		String apiResponse=null;
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");
		

		if(!isToken){
			con.addRequestProperty("Authorization", "Bearer "+token);
		}else{
			String urlParameters = "client_id=ct-security-iam-client&client_secret=h1NNnUaNzSi6QcqDOiPsgQh4fuKEXTbo&grant_type=client_credentials";
			byte[] postData = urlParameters.getBytes( StandardCharsets.UTF_8 );
			int postDataLength = postData.length;
			
			con.setDoOutput(true);
			con.setRequestMethod("POST");
			con.addRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			con.setRequestProperty("charset", "utf-8");
			con.setRequestProperty("Content-Length", Integer.toString(postDataLength ));
			con.setUseCaches(false);
			
			
			DataOutputStream dos = new DataOutputStream(con.getOutputStream()); 
			
			
			
			
			dos.write(postData); 
			dos.flush(); 
			dos.close();


		}	
		
		int responseCode = con.getResponseCode();
		System.out.println("GET Response Code :: " + responseCode);
		if (responseCode == HttpURLConnection.HTTP_OK) { // success
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			// print result
			apiResponse=response.toString();
			System.out.println(apiResponse);
		} else {
			System.out.println("GET request did not work.");
		}

		return apiResponse;
	}
    public static String fetchActiveUserCount() throws IOException {

        String clientId = "f0a23ab5-e222-4569-bc47-93400f317e75";    
        String realm = "onepatch"; 
		String token= fetchToken();
        String urlSessionCount = URL_KEYCLOAK + "/admin/realms/" + realm+ "/clients/"+ clientId+ "/session-count";
		
        URL obj = new URL(urlSessionCount);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");
		con.addRequestProperty("Authorization", "Bearer "+token);
		//con.setRequestProperty("User-Agent", USER_AGENT);
		int responseCode = con.getResponseCode();
		System.out.println("GET Response Code :: " + responseCode);
		JsonObject jsonObject = null;
		if (responseCode == HttpURLConnection.HTTP_OK) { // success
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			// print result
			System.out.println(response.toString());
			jsonObject = new JsonParser().parse(response.toString()).getAsJsonObject();

		} else {
			System.out.println("GET request did not work.");
		}
		
		return jsonObject.get("count").getAsString();


	}
    public static void main(String[] args) {
        
        try{

            System.out.println(fetchActiveUserCount()); 
			//fetchToken();
        }catch(Exception e){
            e.printStackTrace();
        }
     
    }
}