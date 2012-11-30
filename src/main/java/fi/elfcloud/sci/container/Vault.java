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

import org.json.JSONException;
import org.json.JSONObject;

import fi.elfcloud.sci.Client;
import fi.elfcloud.sci.User;
import fi.elfcloud.sci.exception.ECException;

/**
 * Model for elfcloud.fi server Vault objects. <p>
 * Top-level container of data. <p>
 * Provides methods for structuring data. <p>
 *
 */
public class Vault extends Cluster {
	private String vaultType;
	private User owner;
	
	public Vault(Client client) {
		this.client = client;
	}
	
	public Vault(Client client, JSONObject object) throws JSONException {
		this.client = client;
		this.id = object.getInt("id");
		this.name = object.getString("name");
		this.childCount = object.getInt("descendants");
		this.dataItemCount = object.getInt("dataitems");
		this.permissions = object.getJSONArray("permissions");
		this.last_accessed_date = (object.get("last_accessed_date") != JSONObject.NULL ? object.getString("last_accessed_date"): "");
		this.last_modified_date = (object.get("modified_date") != JSONObject.NULL ? object.getString("modified_date"): "");
		this.vaultType = (object.get("vault_type") != JSONObject.NULL ? object.getString("vault_type") : "");
		this.owner = new User(object.getJSONObject("owner"));
	}
	
	public void remove() throws ECException, JSONException, IOException {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("vault_id", this.id);
		this.client.getConnection().sendRequest("remove_vault", params);
	}
	
	public void rename(String newName) throws ECException, JSONException, IOException {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("vault_id", this.id);
		params.put("vault_name", newName);
		this.client.getConnection().sendRequest("rename_vault", params);
		this.name = newName;
	}
	
	/**
	 * Vaults are top-level containers and cannot have parent container.
	 * @return -1
	 */
	@Override
	public int getParentId() {
		return -1;
	}

	public User getOwner() {
		return owner;
	}

}
