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

public final class CCXact implements Entity, Copyable<CCXact> {
	public final int CX_O_ID;
	public final String CX_TYPE;
	public final int CX_CC_NUM;
	public final String CX_CC_NAME;
	public final Date CX_EXPIRY;
	public final int CX_AUTH_ID;
	public final double CX_XACT_AMT;
	public final Date CX_XACT_DATE;
	public final int CX_CO_ID;

	// public final Country CX_CO;

	public CCXact(int cX_O_ID, String cX_TYPE, int cX_CC_NUM,
			String cX_CC_NAME, Date cX_EXPIRY, int cX_AUTH_ID,
			double cX_XACT_AMT, Date cX_XACT_DATE, int cX_CO_ID) {
		super();
		CX_O_ID = cX_O_ID;
		CX_TYPE = cX_TYPE;
		CX_CC_NUM = cX_CC_NUM;
		CX_CC_NAME = cX_CC_NAME;
		CX_EXPIRY = cX_EXPIRY;
		CX_AUTH_ID = cX_AUTH_ID;
		CX_XACT_AMT = cX_XACT_AMT;
		CX_XACT_DATE = cX_XACT_DATE;
		// CX_CO = cX_CO;
		CX_CO_ID = cX_CO_ID;
	}

	public TreeMap<String, Object> getValuesToInsert() {
		throw new RuntimeException("Not implemented.");
	}

	public String getKeyName() {
		return "CX_O_ID";
	}

	public final CCXact copy() {
		return new CCXact(CX_O_ID, CX_TYPE, CX_CC_NUM, CX_CC_NAME, CX_EXPIRY,
				CX_AUTH_ID, CX_XACT_AMT, CX_XACT_DATE, CX_CO_ID);
	}
}