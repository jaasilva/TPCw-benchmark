/*
 * *********************************************************************
 * Copyright (c) 2011 Valter Balegas and Universidade Nova de Lisboa.
 * All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ********************************************************************
 */
package pt.unl.citi.tpcw.entities;

import java.util.Date;
import java.util.TreeMap;

import org.uminho.gsd.benchmarks.interfaces.Entity;

public final class Item implements Entity {
	public final int I_ID;
	public final String I_TITLE;
	public final int I_A_ID;
	// public final Author I_A;
	public Date I_PUB_DATE;
	public final String I_PUBLISHER;
	public final String I_SUBJECT;
	public final String I_DESC;
	public int I_RELATED1;
	public int I_RELATED2;
	public int I_RELATED3;
	public int I_RELATED4;
	public int I_RELATED5;
	public String I_THUMBNAIL;
	public String I_IMAGE;
	public final double I_SRP;
	public double I_COST;
	public final Date I_AVAIL;
	public int I_STOCK;
	public final String I_ISBN;
	public final int I_PAGE;
	public final String I_BACKING;
	public final String I_DIMENSION;
//	public final RBTree orderLines;

	public Item(int i_ID, String i_TITLE, int i_A_ID, Date i_PUB_DATE,
			String i_PUBLISHER, String i_SUBJECT, String i_DESC,
			int i_RELATED1, int i_RELATED2, int i_RELATED3, int i_RELATED4,
			int i_RELATED5, String i_THUMBNAIL, String i_IMAGE, double i_SRP,
			double i_COST, Date i_AVAIL, int i_STOCK, String i_ISBN,
			int i_PAGE, String i_BACKING, String i_DIMENSION) {
		super();
		I_ID = i_ID;
		I_TITLE = i_TITLE;
		// I_A = i_A;
		I_A_ID = i_A_ID;
		I_PUB_DATE = i_PUB_DATE;
		I_PUBLISHER = i_PUBLISHER;
		I_SUBJECT = i_SUBJECT;
		I_DESC = i_DESC;
		I_RELATED1 = i_RELATED1;
		I_RELATED2 = i_RELATED2;
		I_RELATED3 = i_RELATED3;
		I_RELATED4 = i_RELATED4;
		I_RELATED5 = i_RELATED5;
		I_THUMBNAIL = i_THUMBNAIL;
		I_IMAGE = i_IMAGE;
		I_SRP = i_SRP;
		I_COST = i_COST;
		I_AVAIL = i_AVAIL;
		I_STOCK = i_STOCK;
		I_ISBN = i_ISBN;
		I_PAGE = i_PAGE;
		I_BACKING = i_BACKING;
		I_DIMENSION = i_DIMENSION;
//		orderLines = new RBTree();
	}

	public TreeMap<String, Object> getValuesToInsert() {
		throw new RuntimeException("Not implemented.");
	}

	public String getKeyName() {
		return "I_ID";
	}
}