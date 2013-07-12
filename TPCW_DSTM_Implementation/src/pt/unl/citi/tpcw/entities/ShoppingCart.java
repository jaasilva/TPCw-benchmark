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

import pt.unl.citi.tpcw.util.RBTree;

public final class ShoppingCart implements Entity
{
	public final int SC_SHOPPING_ID;
	public final int SC_C_ID;
	public final String SC_C_FNAME; // C_FNAME of the Customer
	public final String SC_C_LNAME; // C_LNAME of the Customer
	public final double SC_C_DISCOUNT; // C_DISCOUNT of the Customer

	public Date SC_DATE; // Date and time when the CART was last updated
	public double SC_SUB_TOTAL; // Gross total amount of all items in the CART
	public double SC_TAX; // Tax based on the gross total amount
	public double SC_SHIP_COST; // Total shipping and handling charges
	public double SC_TOTAL; // Total amount of the order
	public RBTree cartLines;

	public ShoppingCart(int sC_SHOPPING_ID, int sC_C_ID, Date sC_DATE,
			double sC_SUB_TOTAL, double sC_TAX, double sC_SHIP_COST,
			double sC_TOTAL, String sC_C_FNAME, String sC_C_LNAME,
			double sC_C_DISCOUNT)
	{
		super();
		SC_SHOPPING_ID = sC_SHOPPING_ID;
		SC_C_ID = sC_C_ID;
		SC_DATE = sC_DATE;
		SC_SUB_TOTAL = sC_SUB_TOTAL;
		SC_TAX = sC_TAX;
		SC_SHIP_COST = sC_SHIP_COST;
		SC_TOTAL = sC_TOTAL;
		SC_C_FNAME = sC_C_FNAME;
		SC_C_LNAME = sC_C_LNAME;
		SC_C_DISCOUNT = sC_C_DISCOUNT;
		cartLines = new RBTree();
	}

	public TreeMap<String, Object> getValuesToInsert()
	{
		throw new RuntimeException("Not implemented.");
	}

	public String getKeyName()
	{
		return "SC_SHOPPING_ID";
	}
}
