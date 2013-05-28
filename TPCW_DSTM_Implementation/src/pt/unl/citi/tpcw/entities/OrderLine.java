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

import java.util.TreeMap;

import org.uminho.gsd.benchmarks.interfaces.Entity;

public final class OrderLine implements Entity, Copyable<OrderLine> {
	public final int OL_ID;
	public final int OL_O_ID;
	// public final Order OL_O;
	public final int OL_I_ID;
	// public final Item OL_I;
	public final int OL_QTY;
	public final double OL_DISCOUNT;
	public final String OL_COMMENT;

	public OrderLine(int oL_ID, int oL_O_ID, int oL_I_ID, int oL_QTY,
			double oL_DISCOUNT, String oL_COMMENT) {
		super();
		OL_ID = oL_ID;
		// OL_O = oL_O;
		OL_O_ID = oL_O_ID;
		// OL_I = oL_I;
		OL_I_ID = oL_I_ID;
		OL_QTY = oL_QTY;
		OL_DISCOUNT = oL_DISCOUNT;
		OL_COMMENT = oL_COMMENT;
	}

	public TreeMap<String, Object> getValuesToInsert() {
		throw new RuntimeException("Not implemented.");
	}

	public String getKeyName() {
		return "OL_ID";
	}

	public final OrderLine copy() {
		return new OrderLine(OL_ID, OL_O_ID, OL_I_ID, OL_QTY, OL_DISCOUNT,
				OL_COMMENT);
	}
}