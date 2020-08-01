import java.io.*;
import java.util.*;
import org.json.simple.JSONObject;

public class NewsItem {

    public int id;
    public String title;
    public String url;
    public String description;
    public String image;
    public Date pubdate;
    // guid and source are together a unique key in the database, to allow the same guid from different
    // news sources
    public int guid;
    public String source;

	public NewsItem(
			String title, String url, String description, String imageurl, Date pubdate,
			int guid, String source, int id
		) {
		// create news item
		setTitle(title);
		setUrl(url);
		setDescription(description);
		setImage(imageurl);
		setPubdate(pubdate);
		setGuid(guid);
		setSource(source);
		setId(id);
	}

	public JSONObject toJSON() {
		// convert object to json object
        JSONObject jo = new JSONObject();
        jo.put("title", this.title);
        jo.put("url", this.url);
        jo.put("description", this.description);
        jo.put("image", this.image);
        jo.put("url", this.url);
        jo.put("pubdate", this.pubdate.toString());
        jo.put("source", this.source);
        jo.put("guid", this.guid);
        return jo;
    }

	public void setTitle(String title) {
		this.title = title;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setImage(String imageurl) {
		this.image = imageurl;
	}

	public void setPubdate(Date pubdate) {
		this.pubdate = pubdate;
	}

	public void setGuid(int guid) {
		this.guid = guid;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public void setId(int id) {
		this.id = id;
	}
}




