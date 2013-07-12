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

import java.util.TreeMap;

import org.uminho.gsd.benchmarks.interfaces.Entity;

public final class Address implements Entity
{
	public final int ADDR_ID;
	public final String ADDR_STREET1;
	public final String ADDR_STREET2;
	public final String ADDR_CITY;
	public final String ADDR_STATE;
	public final String ADDR_ZIP;
	public final int ADDR_CO_ID;

	public Address(int aDDR_ID, String aDDR_STREET1, String aDDR_STREET2,
			String aDDR_CITY, String aDDR_STATE, String aDDR_ZIP, int aDDR_CO_ID)
	{
		super();
		ADDR_ID = aDDR_ID;
		ADDR_STREET1 = aDDR_STREET1;
		ADDR_STREET2 = aDDR_STREET2;
		ADDR_CITY = aDDR_CITY;
		ADDR_STATE = aDDR_STATE;
		ADDR_ZIP = aDDR_ZIP;
		ADDR_CO_ID = aDDR_CO_ID;
	}

	public TreeMap<String, Object> getValuesToInsert()
	{
		throw new RuntimeException("Not implemented.");
	}

	public String getKeyName()
	{
		return "ADDR_ID";
	}
}
