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

public final class Author implements Entity
{
	public final int A_ID;
	public final String A_FNAME;
	public final String A_LNAME;
	public final String A_MNAME;
	public final Date A_DOB;
	public final String A_BIO;

	public Author(int a_ID, String a_FNAME, String a_LNAME, String a_MNAME,
			Date a_DOB, String a_BIO)
	{
		super();
		A_ID = a_ID;
		A_FNAME = a_FNAME;
		A_LNAME = a_LNAME;
		A_MNAME = a_MNAME;
		A_DOB = a_DOB;
		A_BIO = a_BIO;
	}

	public TreeMap<String, Object> getValuesToInsert()
	{
		throw new RuntimeException("Not implemented.");
	}

	public String getKeyName()
	{
		return "A_ID";
	}
}
