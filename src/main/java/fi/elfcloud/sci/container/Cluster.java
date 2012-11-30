/*
 * Copyright 2010-2012 elfCLOUD / elfcloud.fi - SCIS Secure Cloud Infrastructure Services
 *	
 *		Licensed under the Apache License, Version 2.0 (the "License");
 *		you may not use this file except in compliance with the License.
 *		You may obtain a copy of the License at
 *	
 *			http://www.apache.org/licenses/LICENSE-2.0
 *	
 *	   	Unless required by applicable law or agreed to in writing, software
 *	   	distributed under the License is distributed on an "AS IS" BASIS,
 *	   	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *	   	See the License for the specific language governing permissions and
 *	   	limitations under the License.
 */

package fi.elfcloud.sci.container;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fi.elfcloud.sci.DataItem;
import fi.elfcloud.sci.Client;
import fi.elfcloud.sci.User;
import fi.elfcloud.sci.exception.ECException;

/**
 * Model for elfcloud.fi server cluster objects.<p>
 * Provides methods for structuring data.
 *
 */
public class Cluster {
	protected String name;
	protected int id;
	protected int childCount;
	protected int dataItemCount;
	protected Client client;
	protected int parent_id = 0;
	protected String last_accessed_date;
	protected String last_modified_date;
	protected JSONArray permissions;
	
	public Cluster() {

	}
	
	public Cluster(Client client) {
		this.client = client;
	}
	
	public Cluster(Client client, int id) {
		this.client = client;
		this.id = id;
	}
	
	public Cluster(Client client, JSONObject object) throws JSONException {
		this.client = client;
		this.id = object.getInt("id");
		this.name = object.getString("name");
		this.childCount = object.getInt("descendants");
		this.dataItemCount = object.getInt("dataitems");
		this.parent_id = object.getInt("parent_id");
		this.last_accessed_date = (object.get("last_accessed_date") != JSONObject.NULL ? object.getString("last_accessed_date"): "");
		this.last_modified_date = (object.get("modified_date") != JSONObject.NULL ? object.getString("modified_date"): "");
		this.permissions = object.getJSONArray("permissions");
	}
	
	/**
	 * Returns name of the {@link Cluster}
	 * @return name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Set name for the {@link Cluster}
	 * @param name new name for the {@link Cluster}
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns ID of the {@link Cluster}
	 * @return id
	 */
	public int getId() {
		return this.id;
	}

	/**
	 * Returns amount of children of {@link Cluster}
	 * @return amount of children
	 */
	public int getChildCount() {
		return this.childCount;
	}

	/**
	 * Returns amount of data items in {@link Cluster}
	 * @return amount of data items
	 */
	public int getDataItemCount() {
		return this.dataItemCount;
	}
	
	/**
	 * Returns last accessed date.
	 * @return
	 */
	public String getLastAccessedDate() {
		return this.last_accessed_date;
	}
	
	/**
	 * Returns last modified date.
	 * @return
	 */
	public String getLastModifiedDate() {
		return this.last_modified_date;
	}
	
	/**
	 * Returns permissions the user has.
	 * @return 
	 */
	public String[] getPermissions() {
		String[] array = new String[permissions.length()];
		for (int i=0; i<this.permissions.length(); i++) {
			try {
				array[i] = this.permissions.getString(i);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return array;
	}
	
	/**
	 * Returns child {@link DataItem}s and {@link Cluster}s
	 * @return {@link HashMap} with keys <code>clusters</code> and <code>dataitems</code>
	 * @throws ECException
	 * @throws IOException
	 */
	public HashMap<String, Object[]> getElements() throws ECException, IOException {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("parent_id", this.id);
		JSONObject response;
		try {
			response = (JSONObject)this.client.getConnection().sendRequest("list_contents", params);
			JSONArray jsonArray = response.getJSONArray("clusters");
			Cluster clusterArray[] = new Cluster[jsonArray.length()];
			
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject object = jsonArray.getJSONObject(i);
				clusterArray[i] = new Cluster(this.client, object);
			}
			this.childCount = clusterArray.length;
			jsonArray = response.getJSONArray("dataitems");
			DataItem dataitemArray[] = new DataItem[jsonArray.length()];
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject object = jsonArray.getJSONObject(i);
				dataitemArray[i] = new DataItem(this.client, object, this.id);
			}
			this.dataItemCount = dataitemArray.length;
			HashMap<String, Object[]> objects = new HashMap<String, Object[]>();
			objects.put("clusters", clusterArray);
			objects.put("dataitems", dataitemArray);
			return objects;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
		
	}
	
	/**
	 * Returns child {@link Cluster}s 
	 * @return child {@link Cluster}s
	 * @throws ECException
	 * @throws IOException
	 */
	public Cluster[] getChildren() throws ECException, IOException {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("parent_id", this.id);
		Object response;
		try {
			response = this.client.getConnection().sendRequest("list_clusters", params);
			JSONArray clusters = (JSONArray)response;
			Cluster result[] = new Cluster[clusters.length()];
			for (int i = 0; i < clusters.length(); i++) {
				JSONObject object = clusters.getJSONObject(i);
				result[i] = new Cluster(this.client, object);
			}
			this.childCount = result.length;
			return result;
		} catch (JSONException e) {
			e.printStackTrace();
		}		
		return null;
	}
	
	/**
	 * Returns child {@link DataItem}s
	 * @param 	keynames array of {@link DataItem} names. Can be empty Array to list all.
	 * @return {@link DataItem}s filtered by <code>keynames</code> or all direct {@link DataItem}s if
	 * 		array is empty
	 * @throws ECException
	 * @throws IOException
	 */
	public DataItem[] getDataItems(String[] keynames) throws ECException, IOException {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("parent_id", this.id);
		params.put("names", keynames);
		Object response;
		try {
			response = (JSONArray) this.client.getConnection().sendRequest("list_dataitems", params);
			JSONArray dataItems = (JSONArray) response;
			DataItem result[] = new DataItem[dataItems.length()];
			for (int i = 0; i < dataItems.length(); i++) {
				result[i] = new DataItem(this.client, dataItems.getJSONObject(i), this.id);
			}
			this.dataItemCount = result.length;
			return result;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Performs {@link Cluster} remove operation.
	 * @throws ECException
	 * @throws JSONException
	 * @throws IOException
	 */
	public void remove() throws ECException, JSONException, IOException {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("cluster_id", this.id);
		this.client.getConnection().sendRequest("remove_cluster", params);
	}
	
	/**
	 * Retuns ID of the parent to the {@link Cluster}.
	 * @return id of the parent container
	 */
	public int getParentId() {
		return this.parent_id;
	}
	
	/**
	 * Sets new name to {@link Cluster}
	 * @param newName new name for the {@link Cluster}
	 * @throws ECException
	 * @throws JSONException
	 * @throws IOException
	 */
	public void rename(String newName) throws ECException, JSONException, IOException {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("cluster_id", this.id);
		params.put("name", newName);
		this.client.getConnection().sendRequest("rename_cluster", params);
		this.name = newName;
	}
	
	public User getOwner() {
		return null;
	}
}
