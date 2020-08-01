import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.Headers;

/*
 * a simple static http server
*/
public class Server {
  final static int SOCKET = 8000;

  public static void main(String[] args) throws Exception {
    HttpServer server = HttpServer.create(new InetSocketAddress(SOCKET), 0);
    // fetch all news items in the database
    server.createContext("/newsitems/all", new FetchAllHandler());
    // list all news items in the database (guid and title)
    server.createContext("/newsitems/list", new ListHandler());
    // fetch one specific item from the database (by id)
    server.createContext("/newsitems/item", new ItemHandler());
    server.setExecutor(null); // creates a default executor
    server.start();
    System.out.println("The server is running");
  }


  static class FetchAllHandler implements HttpHandler {
    // fetch all news items in the database
    public void handle(HttpExchange t) throws IOException {
      // set json header
      Headers h = t.getResponseHeaders();
      h.add( "Content-Type", "application/json" );
      // get content from application
      String json = NewsFeedPoll.getAllNewsItems();
      byte [] response = json.getBytes();
      t.sendResponseHeaders(200, response.length);
      OutputStream os = t.getResponseBody();
      os.write(response);
      os.close();
    }
  }

  static class ListHandler implements HttpHandler {
    // list all news items in the database (guid and title)
    public void handle(HttpExchange t) throws IOException {
      // set json header
      Headers h = t.getResponseHeaders();
      h.add( "Content-Type", "application/json" );
      // get content from application
      String json = NewsFeedPoll.getNewsItemsList();
      byte [] response = json.getBytes();
      t.sendResponseHeaders(200, response.length);
      OutputStream os = t.getResponseBody();
      // send response back to client
      os.write(response);
      os.close();
    }
  }

  static class ItemHandler implements HttpHandler {
    // fetch one specific item from the database (by id)
    public void handle(HttpExchange t) throws IOException {
      // set json header
      Headers h = t.getResponseHeaders();
      h.add( "Content-Type", "application/json" );
      String rspns = null;
      // get id from url (query is for example id=1)
      Map<String, String> params = queryToMap(t.getRequestURI().getQuery());
      try {
        Integer id = Integer.parseInt(params.get("id"));
        // get content from application
        rspns = NewsFeedPoll.getNewsItem(id);
      } catch (Exception e) {
        System.out.println(e.toString());
        rspns = "Something went wrong, please try again with different parameters";
      }
      byte [] response = rspns.getBytes();
      t.sendResponseHeaders(200, response.length);
      OutputStream os = t.getResponseBody();
      // send response back to client
      os.write(response);
      os.close();
    }
  }

  public static Map<String, String> queryToMap(String query) {
    // split query string into parameters
    Map<String, String> result = new HashMap<>();
    for (String param : query.split("&")) {
        String[] entry = param.split("=");
        if (entry.length > 1) {
            result.put(entry[0], entry[1]);
        }else{
            result.put(entry[0], "");
        }
    }
    return result;
}
}