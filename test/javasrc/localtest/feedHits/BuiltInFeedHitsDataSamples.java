/*
Copyright (c) 2024, Damon Hart-Davis

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

package localtest.feedHits;

/**Significant built-in test data, shareable across all test cases. */
public final class BuiltInFeedHitsDataSamples
	{
	/**Prevent instance creation. */
	private BuiltInFeedHitsDataSamples() { }

	public static final String sample_FeedStatus_ALL_record =
	"12857 71404021 200:304:406:429:SH 2987 1993 359 7476 5129 ALL";
	public static final String sample_FeedStatus_byHour_record =
	"539 2295559 200:304:406:429:SH 90 81 0 367 539 00";
	public static final String sample_FeedStatus_empty_UA_record =
	"477 632084 200:304:406:429:SH 22 0 75 380 173 \"-\"";
	public static final String sample_FeedStatus_spaced_UA_record =
	"1701 3248489 200:304:406:429:SH 183 0 0 1518 421 \"Podbean/FeedUpdate 2.1\"";

	/**Number of days coverage of data block 20240527. */
	public static final int intervalDays_20240527 = 8;
	/**One block of feed hit types by hour of day (UTC).
	 * From https://www.earth.org.uk/img/research/RSS-efficiency/data/20240527/feedStatusByHour.log
	 */
	public static final String feedStatusByHour_20240527 = """
539 2295559 200:304:406:429:SH 90 81 0 367 539 00
530 2144355 200:304:406:429:SH 71 81 0 378 530 01
485 2062380 200:304:406:429:SH 76 71 0 334 485 02
499 2416195 200:304:406:429:SH 96 91 0 310 499 03
483 2086407 200:304:406:429:SH 71 66 0 342 483 04
506 2048882 200:304:406:429:SH 55 95 0 356 506 05
460 1874908 200:304:406:429:SH 53 74 0 333 460 06
463 1937295 200:304:406:429:SH 60 85 0 318 463 07
446 2036141 200:304:406:429:SH 68 84 3 278 0 08
419 2460208 200:304:406:429:SH 108 99 23 189 0 09
406 3349288 200:304:406:429:SH 188 70 51 97 0 10
482 3466124 200:304:406:429:SH 200 79 62 141 0 11
536 8730826 200:304:406:429:SH 446 90 0 0 0 12
550 3596588 200:304:406:429:SH 196 104 45 201 0 13
603 6552736 200:304:406:429:SH 285 64 69 185 0 14
549 3800480 200:304:406:429:SH 226 72 53 198 0 15
586 3208385 200:304:406:429:SH 144 81 20 341 0 16
665 2641690 200:304:406:429:SH 88 98 9 470 0 17
732 2592064 200:304:406:429:SH 68 74 6 581 0 18
658 2821442 200:304:406:429:SH 82 90 6 480 0 19
520 2691956 200:304:406:429:SH 90 73 4 347 0 20
576 2111559 200:304:406:429:SH 67 87 8 410 0 21
656 2275472 200:304:406:429:SH 77 83 0 495 656 22
508 2203081 200:304:406:429:SH 82 101 0 325 508 23
12857 71404021 200:304:406:429:SH 2987 1993 359 7476 5129 ALL
	""";
	}
