/*
Copyright (c) 2023, Damon Hart-Davis

Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package localtest.support;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import org.hd.d.statsHouse.data.EOUDataCSV;
import org.hd.d.statsHouse.generic.DataCadence;

/**External data in files.
 * Such data tends to be more fragile.
 *
 * @param name  relative path within data sample directory; never null
 * @param recordsExpected  count of data records expected; non-negative
 * @param cadenceExpected  data cadence expected; non-negative
 */
public record ExternalFile(String name, int recordsExpected, DataCadence cadenceExpected)
	{
	public ExternalFile
		{
		Objects.requireNonNull(name);
		if(recordsExpected < 0) { throw new IllegalArgumentException(); }
		Objects.requireNonNull(cadenceExpected);
		}

	/**Path of data sample (top) directory relative to the project root; not null. */
	public final static File DATA_SAMPLE_DIR = new File("dataSample");

	/**Path of test output (top) directory relative to the project root; not null. */
	public final static File TEST_OUTPUT_DIR = new File("test/out");

	/**Get full File path for sample; never null. */
	public File getFullPath() { return(new File(DATA_SAMPLE_DIR, name)); }

	/**Load EOU CSV data; never null but may be empty.
	 * @throws IOException
	 */
	public EOUDataCSV loadEOUDataCSV() throws IOException
	    { return(EOUDataCSV.loadEOUDataCSV(getFullPath())); }
	}

