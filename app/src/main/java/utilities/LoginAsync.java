package utilities;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;

import components.DatabaseHandler.AdministrativeDatabase;

import config.Variables.Constants;
import config.Variables.Variables;
import design.Classes.ServiceHandling;

public class LoginAsync extends AsyncTask<Void, Void, String> {

	URL url = null;
	Context mContext;
	String userName;
	String passWord;
	Activity referencingActivity;

	public LoginAsync(Activity ref, String username, String password,
			Context ctx) {
		this.referencingActivity = ref;
		mContext = ctx;
		userName = username;
		passWord = password;
	}

	protected String doInBackground(Void... params) {
		URL url = null;
		try {
			url = new URL((Variables.urlConnection + Variables.userLoginEndpoint));
			HttpURLConnection urlConn = (HttpURLConnection) url
					.openConnection();

			urlConn.setDoInput(true);
			urlConn.setDoOutput(true);
			urlConn.setRequestMethod("POST");
			urlConn.setUseCaches(false);
			urlConn.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
			urlConn.setRequestProperty("Charest", "utf-8");
			// to connect to the server side
			urlConn.connect();

			DataOutputStream dop = new DataOutputStream(
					urlConn.getOutputStream());
			dop.writeBytes("method=" + URLEncoder.encode("login", "utf-8"));
			// it is essential that to add "&" to separate two strings
			dop.writeBytes("&username=" + URLEncoder.encode(userName, "utf-8"));
			dop.writeBytes("&password=" + URLEncoder.encode(passWord, "utf-8"));
			dop.flush();
			dop.close();
			DataInputStream dis = new DataInputStream(urlConn.getInputStream());
			String locPassage = dis.readLine();
            Log.d("My tag login", locPassage);
			dis.close();
			// to disconnect the server side
			urlConn.disconnect();

            if (locPassage.equalsIgnoreCase("username taken")) return "Failed";

            try {
                JSONArray jArray = new JSONArray(locPassage);
                return jArray.getJSONObject(0).getInt("id")+"";
            }
            catch (Exception e){
                return "Failed";
            }

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "Failed";

	}

	protected void onPostExecute(String result) {
		Log.d("MY TAG LOGIN",result);
		if (result.equals("Failed")) {
			Toast.makeText(mContext, "The credentials are wrong",
					Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(mContext, "Successful login", Toast.LENGTH_LONG)
					.show();

			AdministrativeDatabase adminDb = new AdministrativeDatabase(
					Constants.databaseName, Constants.adminTable, mContext);

			adminDb.updateUserId(Integer.valueOf(result.replace(" ", "")));
			//adminDb.closeDb();

			try {
				this.referencingActivity.startActivity(new Intent(
						this.referencingActivity.getApplicationContext(),
						ServiceHandling.class));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Log.e("ERROR", e.toString());
			}
			

		}
		super.onPostExecute(result);
	}

}
