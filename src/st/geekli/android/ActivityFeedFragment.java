package st.geekli.android;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import st.geekli.android.ImageThreadLoader.ImageLoadedListener;

import android.app.Fragment;
import android.app.ListFragment;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

public class ActivityFeedFragment extends ListFragment {
	private Resources resources;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		List<FeedItem> feedItems = new ArrayList<FeedItem>(); 
		try {
			// String rawFeedData = LoadFile("activities.json", false);
			InputStream is = getResources().openRawResource(R.raw.activites);
			Writer writer = new StringWriter();
			char[] buffer = new char[1024];
			try {
				Reader reader = new BufferedReader(new InputStreamReader(is,
						"UTF-8"));
				int n;
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
				}
			} finally {
				is.close();
			}

			String rawFeedData = writer.toString();
			JSONArray jsonActivities = new JSONArray(rawFeedData);
			for (int i = 0; i < jsonActivities.length(); i++) {

				JSONObject activity = jsonActivities.getJSONObject(i);
				JSONObject user = activity.getJSONObject("user");
				String activityType = activity.getString("type");
				JSONObject avatar = user.getJSONObject("avatar");
				JSONObject gfk = activity.getJSONObject("gfk");
				FeedItem item = new FeedItem();
				item.user = user.getString("screen_name");
				item.thumbnail = avatar.getString("small");
				if (activityType.equals("micro")) {
					item.content = gfk.getString("status");
				} else if (activityType.equals("card")) {
					item.content = gfk.getString("headline");
				} else if (activityType.equals("repo_contributor")) {
					item.content = "contributes to " + gfk.getString("name");
				} else if (activityType.equals("follow")) {
					item.content = "follows " + gfk.getString("screen_name");
				} else if (activityType.equals("ping")) {
					item.content = "pinged " + gfk.getString("screen_name");
				}
				feedItems.add(item);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		FeedItemAdapter adapter = new FeedItemAdapter(getActivity(),
				R.layout.feeditem, feedItems);
		
		setListAdapter(adapter);
	}
	
    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Place an action bar item for searching.
        MenuItem item = menu.add("Search");
        item.setIcon(android.R.drawable.ic_menu_search);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        SearchView sv = new SearchView(getActivity()); 
    }
	// load file from apps res/raw folder or Assets folder
	public String LoadFile(String fileName, boolean loadFromRawFolder)
			throws IOException {
		// Create a InputStream to read the file into
		InputStream iS;

		if (loadFromRawFolder) {
			// get the resource id from the file name
			int rID = resources.getIdentifier("android.geekli.st:raw/"
					+ fileName, null, null);
			// get the file as a stream
			iS = resources.openRawResource(rID);
		} else {
			// get the file as a stream
			iS = resources.getAssets().open(fileName);
		}

		// create a buffer that has the same size as the InputStream
		byte[] buffer = new byte[iS.available()];
		// read the text file as a stream, into the buffer
		iS.read(buffer);
		// create a output stream to write the buffer into
		ByteArrayOutputStream oS = new ByteArrayOutputStream();
		// write this buffer to the output stream
		oS.write(buffer);
		// Close the Input and Output streams
		oS.close();
		iS.close();

		// return the output stream as a String
		return oS.toString();
	}
	public class FeedItemAdapter extends ArrayAdapter<FeedItem> {
		  private final static String TAG = "MediaItemAdapter";
		  private int resourceId = 0;
		  private LayoutInflater inflater;
		  private Context context;

		  private ImageThreadLoader imageLoader = new ImageThreadLoader();

		  public FeedItemAdapter(Context activityFeedFragment, int resourceId, List<FeedItem> feedItems) {
		    super(activityFeedFragment, 0, feedItems);
		    this.resourceId = resourceId;
		    inflater = (LayoutInflater)activityFeedFragment.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		    this.context = activityFeedFragment;
		  }

		  @Override
		  public View getView(int position, View convertView, ViewGroup parent) {

		    View view;
		    TextView textUser;
		    TextView textContent;
		    final ImageView image;

		    view = inflater.inflate(resourceId, parent, false);

		    try {
		      textUser = (TextView)view.findViewById(R.id.user);
		      textContent = (TextView)view.findViewById(R.id.content);
		      image = (ImageView)view.findViewById(R.id.icon);
		    } catch( ClassCastException e ) {
		      Log.e(TAG, "Your layout must provide an image and a text view with ID's icon and text.", e);
		      throw e;
		    }

		    FeedItem item = getItem(position);
		    Bitmap cachedImage = null;
		    try {
		      cachedImage = imageLoader.loadImage(item.thumbnail, new ImageLoadedListener() {
		      public void imageLoaded(Bitmap imageBitmap) {
		      image.setImageBitmap(imageBitmap);
		      notifyDataSetChanged();                }
		      });
		    } catch (MalformedURLException e) {
		      Log.e(TAG, "Bad remote image URL: " + item.thumbnail, e);
		    }

		    textUser.setText(item.user);
		    textContent.setText(item.content);

		    if( cachedImage != null ) {
		      image.setImageBitmap(cachedImage);
		    }

		    return view;
		  }
		}
}