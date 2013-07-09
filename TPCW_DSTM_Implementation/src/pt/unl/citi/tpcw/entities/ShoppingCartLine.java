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

public final class ShoppingCartLine implements Entity
{
	public final int SCL_I_ID;
	// public final Item SCL_I;
	public int SCL_QTY;
	public double SCL_COST;
	public final double SCL_SRP;
	public final String SCL_TITLE;
	public final String SCL_BACKING;

	public ShoppingCartLine(int sCL_I_ID, int sCL_QTY, double sCL_COST,
			double sCL_SRP, String sCL_TITLE, String sCL_BACKING)
	{
		super();
		// SCL_I = sCL_I;
		SCL_I_ID = sCL_I_ID;
		SCL_QTY = sCL_QTY;
		SCL_COST = sCL_COST;
		SCL_SRP = sCL_SRP;
		SCL_TITLE = sCL_TITLE;
		SCL_BACKING = sCL_BACKING;
	}

	public TreeMap<String, Object> getValuesToInsert()
	{
		throw new RuntimeException("Not implemented.");
	}

	@Override
	public String getKeyName()
	{
		return "SCL_I_ID";
	}
}
