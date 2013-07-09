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

/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package pt.unl.citi.tpcw.entities;

import java.util.Date;
import java.util.List;
import java.util.TreeMap;

import org.uminho.gsd.benchmarks.interfaces.Entity;

public final class Order implements Entity
{
	public final int O_ID;
	public final int O_C_ID;
	// public final Customer O_C;
	public final Date O_DATE; // date and time
	public final double O_SUB_TOTAL;
	public final double O_TAX;
	public final double O_TOTAL;
	public final String O_SHIP_TYPE;
	public final Date O_SHIP_DATE; // date and time
	public final int O_BILL_ADDR_ID;
	// public final Address O_BILL_ADDR;
	public final int O_SHIP_ADDR_ID;
	// public final Address O_SHIP_ADDR;
	public final String O_STATUS;
	// public RBTree orderLines;
	public final List<OrderLine> orderLines;

	public Order(int o_ID, int o_C_ID, Date o_DATE, double o_SUB_TOTAL,
			double o_TAX, double o_TOTAL, String o_SHIP_TYPE, Date o_SHIP_DATE,
			int o_BILL_ADDR_ID, int o_SHIP_ADDR_ID, String o_STATUS)
	{
		super();
		O_ID = o_ID;
		// O_C = o_C;
		O_C_ID = o_C_ID;
		O_DATE = o_DATE;
		O_SUB_TOTAL = o_SUB_TOTAL;
		O_TAX = o_TAX;
		O_TOTAL = o_TOTAL;
		O_SHIP_TYPE = o_SHIP_TYPE;
		O_SHIP_DATE = o_SHIP_DATE;
		// O_BILL_ADDR = o_BILL_ADDR;
		O_BILL_ADDR_ID = o_BILL_ADDR_ID;
		// O_SHIP_ADDR = o_SHIP_ADDR;
		O_SHIP_ADDR_ID = o_SHIP_ADDR_ID;
		O_STATUS = o_STATUS;
		// orderLines = new RBTree();
		orderLines = new java.util.LinkedList<OrderLine>();
	}

	public TreeMap<String, Object> getValuesToInsert()
	{
		throw new RuntimeException("Not implemented.");
	}

	public String getKeyName()
	{
		return "O_ID";
	}
}
