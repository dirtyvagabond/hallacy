package hallacy.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.util.URIUtil;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class Elevation implements ElevationCalculator{

	Cache<String, Double> cache = CacheBuilder.newBuilder()
			.maximumSize(25000)
			.build();

	public static void main(String[] args) {
		Elevation elev = new Elevation();
		ArrayList<String> elevations = new ArrayList<String>();
		elevations.add("34.06,-118.41");
		System.out.println(elev.getElevations(elevations));
		elevations.add("34.07,-118.42");
		System.out.println(elev.getElevations(elevations));
	}

	@Override
	public HashMap<String, Double> getElevations(List<String> coordinates){
		try {
			return getElevationFromGoogleMaps(coordinates);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new HashMap<String, Double>();
	}

	private HashMap<String, Double> getElevationFromGoogleMaps(List<String> coordinates) 
			throws ClientProtocolException, IOException {
		HashMap<String, Double> results = new HashMap<String, Double>();
		if(coordinates == null || coordinates.size() == 0){
			return results;
		}		
		results = getElevationsFromAPI(coordinates);
		return results;
	}

	private HashMap<String, Double> getElevationsFromAPI(
			List<String> coordinates) throws ClientProtocolException, IOException {
		ArrayList<Double> elevations = new ArrayList<Double>();

		HashMap<String, Double> results = getResultsFromCache(coordinates);
		coordinates = removeCachedCoordinates(results, coordinates);

		if(coordinates.size() > 0){
			System.out.println(coordinates);
			String queryUrl = generateQueryURL(coordinates);
			HttpEntity elevationAPIResponseEntity = queryAPIServer(queryUrl);
			if (elevationAPIResponseEntity != null) {
				StringBuffer elevationAPIResponseString = parseResponse(elevationAPIResponseEntity);
				elevations = parseJsonResponse(elevationAPIResponseString);
				results.putAll(createcoordinatePairsToElevationsMap(coordinates, elevations));
			}
		}
		for(String coordinate: results.keySet()){
			cache.put(coordinate, results.get(coordinate));
		}
		
		return results;
	}

	private List<String> removeCachedCoordinates( //TODO: Change this (and everything else) to operate on a set of coord strings instead of a list
			HashMap<String, Double> results, List<String> coordinates) {
		for(String coordinate: results.keySet()){
			while(coordinates.contains(coordinate)){
				coordinates.remove(coordinate);
			}
		}
		return coordinates;
	}

	private HashMap<String, Double> getResultsFromCache(List<String> coordinates) {
		HashMap<String, Double> cachedResults = new HashMap<String, Double>();
		for(String coordinate: coordinates){
			Double result = cache.getIfPresent(coordinate);
			if(result != null){
				cachedResults.put(coordinate, result);
			}
		}
		return cachedResults;
	}

	private HttpEntity queryAPIServer(String queryUrl) throws ClientProtocolException, IOException {
		HttpClient httpClient = new DefaultHttpClient();
		HttpContext localContext = new BasicHttpContext();
		HttpGet httpGet = new HttpGet(URIUtil.encodeQuery(queryUrl));
		HttpResponse elevationAPIResponse = httpClient.execute(httpGet, localContext);
		return elevationAPIResponse.getEntity();
	}

	private HashMap<String, Double> createcoordinatePairsToElevationsMap(
			List<String> coordinates, ArrayList<Double> elevations) {
		HashMap<String, Double> coordinatePairsToElevations = new HashMap<String, Double>();
		for(int i = 0; i < coordinates.size(); i++){
			coordinatePairsToElevations.put(coordinates.get(i), elevations.get(i));
		}
		return coordinatePairsToElevations;
	}

	private StringBuffer parseResponse(HttpEntity elevationAPIResponseEntity) 
			throws IllegalStateException, IOException {
		InputStream instream = elevationAPIResponseEntity.getContent();
		int r = -1;
		StringBuffer elevationAPIResponseString = new StringBuffer();
		while ((r = instream.read()) != -1)
			elevationAPIResponseString.append((char) r);
		instream.close();
		return elevationAPIResponseString;
	}

	private String generateQueryURL(List<String> coordinates) {



		String url = "http://maps.googleapis.com/maps/api/elevation/"
				+ "json?locations=";
		boolean first = true;
		for(String coordinate: coordinates){
			if(!first){
				coordinate =  "|" + coordinate;
			}else{
				first = false;
			}
			url = url + coordinate;
		}
		url = url + "&sensor=true";
		return url;
	}

	private ArrayList<Double> parseJsonResponse(StringBuffer respStr) 
			throws JsonParseException, JsonMappingException, IOException {
		ArrayList<Double> elevations = new ArrayList<Double>();
		ObjectMapper jsonMapper = new ObjectMapper();
		Map<String,Object> jsonInput = jsonMapper.readValue(respStr.toString(), Map.class);
		ArrayList<Map<String, Object>> results = (ArrayList<Map<String, Object>>)jsonInput.get("results");
		for(Map<String, Object> result: results){
			elevations.add((Double)result.get("elevation"));
		}
		return elevations;
	}

}

