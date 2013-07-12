/*
 * *********************************************************************
 * Copyright (c) 2011 Valter Balegas and Universidade Nova de Lisboa. All rights
 * reserved. Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless
 * required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * ********************************************************************
 */
package pt.unl.citi.tpcw.entities;

import java.util.Date;
import java.util.TreeMap;

import org.uminho.gsd.benchmarks.interfaces.Entity;

public final class Customer implements Entity
{
	public final int C_ID;
	public final String C_UNAME;
	public final String C_PASSWD;
	public final String C_FNAME;
	public final String C_LNAME;
	public final int C_ADDR_ID;
	public final String C_PHONE;
	public final String C_EMAIL;
	public final Date C_SINCE;
	public final Date C_LAST_VISIT;
	public final double C_DISCOUNT;
	public final double C_BALANCE;
	public final double C_YTD_PMT;
	public final Date C_BIRTHDATE;
	public final String C_DATA;

	public Date C_LOGIN; // date and time
	public Date C_EXPIRATION; // date and time

	public Customer(int c_ID, String c_UNAME, String c_PASSWD, String c_FNAME,
			String c_LNAME, int c_ADDR_ID, String c_PHONE, String c_EMAIL,
			Date c_SINCE, Date c_LAST_VISIT, Date c_LOGIN, Date c_EXPIRATION,
			double c_DISCOUNT, double c_BALANCE, double c_YTD_PMT,
			Date c_BIRTHDATE, String c_DATA)
	{
		super();
		C_ID = c_ID;
		C_UNAME = c_UNAME;
		C_PASSWD = c_PASSWD;
		C_FNAME = c_FNAME;
		C_LNAME = c_LNAME;
		C_ADDR_ID = c_ADDR_ID;
		C_PHONE = c_PHONE;
		C_EMAIL = c_EMAIL;
		C_SINCE = c_SINCE;
		C_LAST_VISIT = c_LAST_VISIT;
		C_LOGIN = c_LOGIN;
		C_EXPIRATION = c_EXPIRATION;
		C_DISCOUNT = c_DISCOUNT;
		C_BALANCE = c_BALANCE;
		C_YTD_PMT = c_YTD_PMT;
		C_BIRTHDATE = c_BIRTHDATE;
		C_DATA = c_DATA;
	}

	public TreeMap<String, Object> getValuesToInsert()
	{
		throw new RuntimeException("Not implemented.");
	}

	public String getKeyName()
	{
		return "C_ID";
	}
}
