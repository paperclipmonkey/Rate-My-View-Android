package uk.co.threeequals.ratemyview;

import org.json.JSONArray;

public interface GetJSONListener {
	public void onRemoteCallComplete(JSONArray json);
}