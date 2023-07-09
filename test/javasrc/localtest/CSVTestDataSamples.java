package localtest;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.hd.d.statsHouse.data.EOUDataCSV;
import org.hd.d.statsHouse.generic.DataCadence;

/**Significant built-in test CSV data, shareable across all test cases. */
public final class CSVTestDataSamples
	{
	/**Prevent creation of instances. */
	private CSVTestDataSamples() { }


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

		/**Get full File path for sample; never null. */
		public File getFullPath() { return(new File(DATA_SAMPLE_DIR, name)); }

		/**Load EOU CSV data; never null but may be empty.
		 * @throws IOException
		 */
		public EOUDataCSV loadEOUDataCSV() throws IOException
		    { return(EOUDataCSV.loadEOUDataCSV(getFullPath())); }
		}

	/**Main external data samples that are available; non-null, non-empty and immutable. */
	public static List<ExternalFile> mainFileDataSamples()
		{
		return(Collections.unmodifiableList(Arrays.asList(
			new ExternalFile("imp-M.csv", 163, DataCadence.M),
			new ExternalFile("gen-D.csv", 5611, DataCadence.D),
			new ExternalFile("gen-M.csv", 186, DataCadence.M),
			new ExternalFile("gen-Y.csv", 16, DataCadence.Y)
			)));
		}


	/**Single data point, yearly cadence. */
	public static final String minimal_sample_Y = """
#YYYY,device,coverage,gen
2009,meter,1,2956.1
	""";

	/**First 10 lines of monthly-cadence PV generation data CSV, including comment rows.
     * Sample from:
     * <pre>
% head data/consolidated/energy/std/gen/M/gen-M.csv
     * </pre>
     * <p>
     * Is public so as to be usable from other test cases.
     */
	public static final String sample_gen_M = """
#YYYY-MM,device,coverage,gen,device,coverage,gen,device,coverage,gen
#input,"data/consolidated/energy/std/gen/M/Enphase/gen-M-Enphase.csv"
#input,"data/consolidated/energy/std/gen/M/meter/gen-M-meter.csv"
#input,"data/consolidated/energy/std/gen/M/SunnyBeam/gen-M-SunnyBeam.csv"
2008-02,,,,meter,1,4,SunnyBeam,0.142857,3.54
2008-03,,,,meter,1,70,SunnyBeam,1,68.55
2008-04,,,,meter,1,108,SunnyBeam,1,106.13
2008-05,,,,meter,1,135,SunnyBeam,1,134.03
2008-06,,,,meter,1,160,SunnyBeam,1,158.1
2008-07,,,,meter,1,161,SunnyBeam,1,146.12
	""";

	/**Full yearly-cadence PV generation data CSV to partial 2023, including comment rows.
	 * Sample from:
	 * <pre>
% cat data/consolidated/energy/std/gen/Y/gen-Y.csv
     * </pre>
     * <p>
     * Is public so as to be usable from other test cases.
	 */
	public static final String sample_gen_Y = """
#YYYY,device,coverage,gen,device,coverage,gen,device,coverage,gen
#input,"data/consolidated/energy/std/gen/Y/Enphase/gen-Y-Enphase.csv"
#input,"data/consolidated/energy/std/gen/Y/meter/gen-Y-meter.csv"
#input,"data/consolidated/energy/std/gen/Y/SunnyBeam/gen-Y-SunnyBeam.csv"
2008,,,,meter,0.916667,915,SunnyBeam,0.845238,889.93
2009,,,,meter,1,2956.1,SunnyBeam,1,2907.15
2010,,,,meter,1,3546.9,SunnyBeam,1,3482.76
2011,,,,meter,1,3988.1,SunnyBeam,1,3922.27
2012,,,,meter,1,3777.8,SunnyBeam,1,3712.68
2013,,,,meter,1,3749.7,SunnyBeam,1,3687.79
2014,,,,meter,1,3944,SunnyBeam,1,3881.99
2015,,,,meter,1,3828.6,SunnyBeam,1,3766.9
2016,,,,meter,1,3703.2,SunnyBeam,1,3676.54
2017,,,,meter,1,3794.4,SunnyBeam,1,3736.89
2018,Enphase,0.410714,1069.29,meter,1,3927.8,SunnyBeam,1,3931.44
2019,Enphase,0.999888,3870.89,meter,1,3855.5,SunnyBeam,1,3800.95
2020,Enphase,0.999888,4084.42,meter,1,4069.9,SunnyBeam,1,4020.86
2021,Enphase,0.999888,3514.19,meter,1,3500.8,SunnyBeam,1,3448.6
2022,Enphase,0.999888,3943.38,meter,1,3925.1,SunnyBeam,1,3865.5
2023,Enphase,0.416555,1415.92,meter,0.416667,1411,SunnyBeam,0.440476,1554.63
	""";

	/**Single-source multi-year year-aligned monthly-cadence sample (electricity consumption minus DHW) to mid-2023. */
    public static final String conexDHW_M_to_202305 = """
#YYYY-MM,device,coverage,conexDHW
#synth,"meter.con-Eddi.h1"
2008-01,synth,1,214
2008-02,synth,1,195
2008-03,synth,1,187
2008-04,synth,1,165
2008-05,synth,1,146
2008-06,synth,1,146
2008-07,synth,1,139
2008-08,synth,1,147
2008-09,synth,1,152
2008-10,synth,1,160
2008-11,synth,1,191
2008-12,synth,1,210
2009-01,synth,1,214
2009-02,synth,1,178
2009-03,synth,1,177
2009-04,synth,1,149
2009-05,synth,1,147
2009-06,synth,1,130
2009-07,synth,1,163
2009-08,synth,1,143
2009-09,synth,1,154
2009-10,synth,1,155
2009-11,synth,1,161
2009-12,synth,1,169
2010-01,synth,1,181
2010-02,synth,1,141
2010-03,synth,1,154
2010-04,synth,1,126
2010-05,synth,1,129
2010-06,synth,1,132
2010-07,synth,1,112
2010-08,synth,1,124
2010-09,synth,1,141
2010-10,synth,1,135
2010-11,synth,1,163
2010-12,synth,1,181
2011-01,synth,1,159
2011-02,synth,1,132
2011-03,synth,1,158
2011-04,synth,1,122
2011-05,synth,1,124
2011-06,synth,1,120
2011-07,synth,1,122
2011-08,synth,1,103
2011-09,synth,1,123
2011-10,synth,1,134
2011-11,synth,1,149
2011-12,synth,1,136
2012-01,synth,1,174
2012-02,synth,1,153
2012-03,synth,1,139
2012-04,synth,1,117
2012-05,synth,1,128
2012-06,synth,1,103
2012-07,synth,1,99
2012-08,synth,1,93
2012-09,synth,1,126
2012-10,synth,1,119
2012-11,synth,1,149
2012-12,synth,1,142
2013-01,synth,1,155
2013-02,synth,1,124
2013-03,synth,1,147
2013-04,synth,1,127
2013-05,synth,1,122
2013-06,synth,1,143
2013-07,synth,1,106
2013-08,synth,1,106
2013-09,synth,1,151
2013-10,synth,1,141
2013-11,synth,1,167
2013-12,synth,1,170
2014-01,synth,1,190
2014-02,synth,1,131
2014-03,synth,1,162
2014-04,synth,1,123
2014-05,synth,1,136
2014-06,synth,1,135
2014-07,synth,1,100
2014-08,synth,1,114
2014-09,synth,1,130
2014-10,synth,1,143
2014-11,synth,1,155
2014-12,synth,1,159
2015-01,synth,1,181
2015-02,synth,1,155
2015-03,synth,1,161
2015-04,synth,1,143
2015-05,synth,1,148
2015-06,synth,1,140
2015-07,synth,1,129
2015-08,synth,1,114
2015-09,synth,1,147
2015-10,synth,1,140
2015-11,synth,1,168
2015-12,synth,1,168
2016-01,synth,1,196
2016-02,synth,1,160
2016-03,synth,1,180
2016-04,synth,1,143
2016-05,synth,1,147
2016-06,synth,1,132
2016-07,synth,1,135
2016-08,synth,1,102
2016-09,synth,1,132
2016-10,synth,1,146
2016-11,synth,1,179
2016-12,synth,1,180
2017-01,synth,1,207
2017-02,synth,1,163
2017-03,synth,1,166
2017-04,synth,1,143
2017-05,synth,1,149
2017-06,synth,1,139
2017-07,synth,1,147
2017-08,synth,1,97
2017-09,synth,1,143
2017-10,synth,1,150
2017-11,synth,1,179
2017-12,synth,1,178
2018-01,synth,1,214
2018-02,synth,1,186
2018-03,synth,1,190
2018-04,synth,1,93
2018-05,synth,1,160
2018-06,synth,1,137
2018-07,synth,1,139
2018-08,synth,1,107
2018-09,synth,1,159
2018-10,synth,1,172
2018-11,synth,1,182
2018-12,synth,1,194
2019-01,synth,1,198
2019-02,synth,1,164
2019-03,synth,1,196
2019-04,synth,1,150
2019-05,synth,1,171
2019-06,synth,1,161
2019-07,synth,1,150
2019-08,synth,1,100
2019-09,synth,1,172
2019-10,synth,1,173
2019-11,synth,1,187
2019-12,synth,1,179
2020-01,synth,1,230
2020-02,synth,1,197
2020-03,synth,1,213
2020-04,synth,1,218
2020-05,synth,1,217
2020-06,synth,1,196
2020-07,synth,1,189
2020-08,synth,1,151
2020-09,synth,1,157
2020-10,synth,1,192
2020-11,synth,1,210
2020-12,synth,1,195
2021-01,synth,1,243
2021-02,synth,1,226
2021-03,synth,1,165
2021-04,synth,1,168
2021-05,synth,1,166
2021-06,synth,1,152
2021-07,synth,1,136
2021-08,synth,1,138
2021-09,synth,1,168
2021-10,synth,1,174
2021-11,synth,1,210
2021-12,synth,1,199
2022-01,synth,1,225
2022-02,synth,0.21131,189
2022-03,synth,1,212.689
2022-04,synth,1,163.041
2022-05,synth,1,193.308
2022-06,synth,1,172.756
2022-07,synth,1,149.657
2022-08,synth,1,120.93
2022-09,synth,1,174.28
2022-10,synth,1,174.573
2022-11,synth,1,194.081
2022-12,synth,1,186.007
2023-01,synth,1,214.285
2023-02,synth,1,194.503
2023-03,synth,0.998656,210.209
2023-04,synth,1,171.44
2023-05,synth,1,191.661
	""";
	}
