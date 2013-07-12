/*
 * *********************************************************************
 * Copyright (c) 2010 Pedro Gomes and Universidade do Minho. All rights
 * reserved. Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless
 * required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * ********************************************************************
 */

package org.uminho.gsd.benchmarks.helpers;

import org.deuce.transform.ExcludeTM;
import org.uminho.gsd.benchmarks.benchmark.BenchmarkMain;

import java.util.Random;

@ExcludeTM
public class ThinkTime
{

	private static Random random = new Random();

	public static long getThinkTime()
	{

		if (BenchmarkMain.thinkTime == -1)
		{
			long simulatedDelay = (long) ((-Math.log(random.nextDouble()) * 7) * 1000d);

			if (simulatedDelay > 70000)
			{
				simulatedDelay = 70000;
			}

			return simulatedDelay;
		}
		else
		{
			return BenchmarkMain.thinkTime;
		}
	}
}
