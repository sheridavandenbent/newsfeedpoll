import java.io.*;
import java.util.*;
import java.net.*;
import java.lang.*;
import java.nio.file.*;
import java.util.ArrayList;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import org.json.simple.JSONObject;
import java.text.SimpleDateFormat;

public class NewsFeedPoll {

	// source of the xml data
    final static String URL = "http://feeds.nos.nl/nosjournaal?format=xml";

    // temporary storage of file
    final static String TARGET = "./temp.xml";

    // name of the XML Node that contains the article
	final static String ARTICLE_NODE = "item";

	// prefix needed to extract the unique ID for the article of the source
	final static String GUID_PREFIX = "https://nos.nl/l/";

	// source of news - needed to create a unique guid / source combo in the database
	final static String SOURCE = "nos";

	// date format that the current source uses
    final static String DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss ZZZZ";

    final static Integer WAIT_TIME = 300000; // in milliseconds, equals five minutes

    // names of attributes of the news item
    final static String ATTR_TITLE = "title";
    final static String ATTR_URL = "link";
    final static String ATTR_DESC = "description";
    final static String ATTR_IMG = "enclosure";
    final static String ATTR_DATE = "pubDate";
    final static String ATTR_GUID = "guid";

	public static void main(String[] args) throws Exception{
        NewsFeedPoll object = new NewsFeedPoll();
        object.runProgram();
	}

    private synchronized void runProgram() {
        // keep it running over and over again
        while(true) {
            // Get the xml file from the url
            getFile(URL, TARGET);

            try {
            	// parse the file
                DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
                Document document = docBuilder.parse(new File(TARGET));
                // walk recursively through the tree until the article node has been found
                traverseTree(document.getDocumentElement());
                // wait!
                this.wait(WAIT_TIME);
                System.out.println("Done waiting!");
            } catch (Exception e) {
                System.out.println(e.toString());
            }

        }

    }

	private static void traverseTree(Node node) {
	    // Traverse recursively through the tree until the correct nodes have been reached
	    NodeList nodeList = node.getChildNodes();
	    for (int i = 0; i < nodeList.getLength(); i++) {
	        Node currentNode = nodeList.item(i);
	        if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
	            //calls this method for all the children which is Element
	            if(currentNode.getNodeName() != "item") {
	            	// not the article node yet, continue
	            	traverseTree(currentNode);
	            } else {
	            	// found the article node!
	            	saveNewsItem(currentNode);
	            }
	        }
	    }
	}

	private static void getFile(String url, String target) {
		// Fetch and save the file from the given URL
		try {
			InputStream inputStream = new URL(url).openStream();
			Files.copy(inputStream, Paths.get(target), StandardCopyOption.REPLACE_EXISTING);
		} catch (Exception e) {
			System.out.println(e.toString());
		}
		return;
	}

	private static void saveNewsItem(Node node) {
	    // Create and save the news item
	    try{
		    String title = null;
		    String url = null;
		    String description = null;
		    String image = null;
		    Date pubdate = null;
		    int guid = 0;
		    NodeList attributes = node.getChildNodes();
		    // loop through all attributes of the news item
		    for (int i = 0; i < attributes.getLength(); i++) {
		    	Node attribute = attributes.item(i);
		    	switch(attribute.getNodeName()) {
		    		case ATTR_TITLE:
		    			title = attribute.getTextContent();
		    			break;
		    		case ATTR_URL:
		    			url = attribute.getTextContent();
		    			break;
		    		case ATTR_DESC:
		    			description = attribute.getTextContent();
		    			break;
		    		case ATTR_IMG:
		    			// convert to element so we can get the url attribute out
		    			Element img = (Element) attribute;
		    			image = img.getAttribute("url");
		    			break;
		    		case ATTR_DATE:
		    			// parse pubdate to java Date attribute
		    			pubdate = parsePubdate(attribute.getTextContent());
		    			break;
		    		case ATTR_GUID:
		    			// parse guid (unique id for source) from the static url of the article
		    			// this may need to be adjusted for different sources
		    			guid = Integer.parseInt(attribute.getTextContent().replace(GUID_PREFIX, ""));
		    			break;
		    		default:
		    			// ignore possible other attributes because it's not info we need currently
		    			break;
		    	}
		    }
		    // '0' is a placeholder for the database id, which the item doesn't have yet
		    // as it's not inserted into the database yet
		    NewsItem item = new NewsItem(title, url, description, image, pubdate, guid, SOURCE, 0);
		    // insert NewsItem into database
            MySQLAccess dao = new MySQLAccess();
		    dao.insertNewsItem(item);

	    } catch (Exception e) { 
	    	System.out.println(e.toString());
	    }
	}

	public static String getAllNewsItems() {
		// fetch all news items from the database and return a JSON string
		JSONObject results = new JSONObject();
		try {
	        MySQLAccess dao = new MySQLAccess();
			ArrayList<NewsItem> newsitems = dao.fetchAllNewsItems();
			// iteratively store the newsitems in json format
	        for (NewsItem item : newsitems) {
		        JSONObject item_json = item.toJSON();
		        results.put(item.id, item_json);
	        }
		} catch (Exception e) {
			System.out.println(e.toString());
		}
		// return a JSON string
		return results.toString();
	}

	public static String getNewsItemsList() {
		// fetch id's and titles from all news items in the database and return a JSON string
		JSONObject results = new JSONObject();
		try {
	        MySQLAccess dao = new MySQLAccess();
			ArrayList<NewsItem> newsitems = dao.fetchAllNewsItems();
			// iteratively store the newsitems in json format
	        for (NewsItem item : newsitems) {
		        results.put(item.id, item.title);
	        }
		} catch (Exception e) {
			System.out.println(e.toString());
		}
		// return a JSON string
		return results.toString();
	}

	public static String getNewsItem(Integer id) {
		// fetch one news item from the database and return a JSON string
		JSONObject results = new JSONObject();
		try {
	        MySQLAccess dao = new MySQLAccess();
			NewsItem item = dao.fetchOneNewsItem(id);
		    JSONObject item_json = item.toJSON();
			results.put(id, item_json);
		} catch (Exception e) {
			System.out.println(e.toString());
		}
		// return a JSON string
		return results.toString();
	}

	public static Date parsePubdate(String pubdate) {
		Date d = null;
		try {
			d = new SimpleDateFormat(DATE_FORMAT).parse(pubdate);
		} catch (Exception e) {
			System.out.println(e.toString());
		}
		return d;
	}

}




