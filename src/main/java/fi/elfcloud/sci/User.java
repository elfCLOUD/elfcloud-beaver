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

package fi.elfcloud.sci;

import org.json.JSONException;
import org.json.JSONObject;

public class User {
	private int id;
	private String email;
	private String name;
	private String firstname;
	private String lastname;
	private boolean eulaIsAccepted;
	private String telephone;
	private String language;
	private Account account;

	public User(JSONObject jsonUser) throws JSONException {
		id = jsonUser.getInt("id");
		email = jsonUser.getString("email");
		name = jsonUser.getString("name");
		firstname = (jsonUser.get("firstname") != JSONObject.NULL ? jsonUser.getString("firstname") : "");
		lastname = (jsonUser.get("lastname") != JSONObject.NULL ? jsonUser.getString("lastname") : "");
		eulaIsAccepted = jsonUser.getBoolean("eula_accepted");
		telephone = (jsonUser.get("telephone") != JSONObject.NULL ? jsonUser.getString("telephone") : "");
		language = jsonUser.getString("lang");
		account = new Account(jsonUser.getJSONObject("account"));
	}
	
	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	public String getLanguage() {
		return language;
	}

	public String getTelephone() {
		return telephone;
	}

	public boolean isEulaIsAccepted() {
		return eulaIsAccepted;
	}

	public String getLastname() {
		return lastname;
	}

	public String getFirstname() {
		return firstname;
	}

	public String getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}

	public int getId() {
		return id;
	}

	public class Account {
		private int id;
		private String companyName;
		private String name;
		private String accountType;

		public Account(JSONObject jsonAccount) throws JSONException {
			id = jsonAccount.getInt("id");
			companyName = (jsonAccount.get("company_name") != JSONObject.NULL ? jsonAccount.getString("company_name") : "");
			name = (jsonAccount.get("name") != JSONObject.NULL ? jsonAccount.getString("name") : "");
			accountType = jsonAccount.getString("type");
		}

		public int getId() {
			return id;
		}

		public String getCompanyName() {
			if (companyName == null) {
				return "";
			}
			return companyName;
		}

		public String getName() {
			if (name == null) {
				return "";
			}
			return name;
		}

		public String getAccountType() {
			return accountType;
		}
	}
}
