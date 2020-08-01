import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import java.util.Date;
import java.util.ArrayList;

import java.text.SimpleDateFormat;

public class MySQLAccess {
    private Connection connect = null;
    private Statement statement = null;
    private PreparedStatement preparedStatement = null;
    private ResultSet resultSet = null;

    final static String USER = "root"; // change to database username
    final static String PASS = "root"; // change to database password


    public void insertNewsItem(NewsItem item) throws Exception {
        PreparedStatement stmt = null;
        try {
            // setup the connection with the database
            connect = DriverManager
                    .getConnection("jdbc:mysql://localhost/newsfeedpoll?"
                            + "user="+ USER + "&password=" + PASS);
            // Log (so you know it's working)
            System.out.println("Inserting in the database guid " + Integer.toString(item.guid));
            stmt = connect.prepareStatement(
                "INSERT INTO `newsitems`(title,url,description,image,pubdate,guid,source) "
                    + "VALUES (?,?,?,?,?,?,?);");
            stmt.setString(1, item.title);
            stmt.setString(2, item.url);
            stmt.setString(3, item.description);
            stmt.setString(4, item.image);
            // convert java Date to sql datetime
            java.sql.Timestamp sqldate = new java.sql.Timestamp(item.pubdate.getTime());
            stmt.setTimestamp(5, sqldate);
            // guid and source are together a unique key - this avoids duplicates in the database
            stmt.setInt(6, item.guid);
            stmt.setString(7, item.source);
            stmt.executeUpdate();
        } catch (Exception e) {
            throw e;
        } finally {
            close();
        }
    }

    public ArrayList<NewsItem> fetchAllNewsItems() throws Exception {
        ArrayList<NewsItem> result = new ArrayList();
        try {
            // setup the connection with the database
            connect = DriverManager
                    .getConnection("jdbc:mysql://localhost/newsfeedpoll?"
                            + "user="+ USER + "&password=" + PASS);
            statement = connect.createStatement();
            resultSet = statement
                    .executeQuery("select * from newsfeedpoll.newsitems");
            while (resultSet.next()) {
                String title = resultSet.getString("title");
                String url = resultSet.getString("url");
                String description = resultSet.getString("description");
                String image = resultSet.getString("image");
                // convert back from sql datetime to java Date object
                Date pubdate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(resultSet.getString("pubdate"));
                int guid = Integer.parseInt(resultSet.getString("guid"));
                String source = resultSet.getString("source");
                int id = Integer.parseInt(resultSet.getString("id"));
                NewsItem news_item = new NewsItem(title, url, description, image, pubdate, guid, source, id);
                result.add(news_item);
            }
        } catch (Exception e) {
            throw e;
        } finally {
            close();
        }
        return result;
    }

    public NewsItem fetchOneNewsItem(Integer id) throws Exception {
        NewsItem result = null;
        try {
            // setup the connection with the database
            connect = DriverManager
                    .getConnection("jdbc:mysql://localhost/newsfeedpoll?"
                            + "user="+ USER + "&password=" + PASS);
            statement = connect.createStatement();
            resultSet = statement
                    .executeQuery("select * from newsfeedpoll.newsitems WHERE `id` = " + id);
            while (resultSet.next()) {
                String title = resultSet.getString("title");
                String url = resultSet.getString("url");
                String description = resultSet.getString("description");
                String image = resultSet.getString("image");
                // convert back from sql datetime to java Date object
                Date pubdate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(resultSet.getString("pubdate"));
                int guid = Integer.parseInt(resultSet.getString("guid"));
                String source = resultSet.getString("source");
                int item_id = Integer.parseInt(resultSet.getString("id"));
                result = new NewsItem(title, url, description, image, pubdate, guid, source, item_id);
            }
        } catch (Exception e) {
            throw e;
        } finally {
            close();
        }
        return result;
    }

    private void close() {
        // clean up after the database connection
        try {
            if (resultSet != null) {
                resultSet.close();
            }

            if (statement != null) {
                statement.close();
            }

            if (connect != null) {
                connect.close();
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

}