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

public final class Country implements Entity, Copyable<Country> {
	public final int CO_ID;
	public final String CO_NAME;
	public final String CO_CURRENCY;
	public final double CO_EXCHANGE;

	public Country(int co_id, String name, String currency, double exchange) {
		this.CO_NAME = name;
		this.CO_CURRENCY = currency;
		this.CO_EXCHANGE = exchange;
		this.CO_ID = co_id;
	}

	public TreeMap<String, Object> getValuesToInsert() {
		throw new RuntimeException("Not implemented.");
	}

	public String getKeyName() {
		return "CO_ID";
	}

	public final Country copy() {
		return new Country(CO_ID, CO_NAME, CO_CURRENCY, CO_EXCHANGE);
	}

}