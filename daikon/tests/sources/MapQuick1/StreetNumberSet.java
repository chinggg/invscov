package MapQuick1;

import MapQuick.*;
import java.util.StringTokenizer;
import junit.framework.Assert;

/**
 * TODO
 */
public class StreetNumberSet
{

  /**
   * Creates a StreetNumberSet containing the numbers indicated in the
   * argument.
   *
   * @requires numbers != null && numbers is a space-free comma-delimited
   * list of one or more parity ranges.
   *
   * @throws IllegalArgumentException if the requires clause is broken.
   *
   * <p> A parity range is either a single nonnegative integer "n" or a
   * hyphen-separated pair of nonnegative integers "m-n", where m and n
   * have the same parity and m is no greater than n.  For instance, legal
   * arguments include "5", "22,253", "3-101", and "1914-1918,1939-1945".
   */
  public StreetNumberSet(String numbers) throws IllegalArgumentException
  {
    Assert.assertTrue(numbers != null);

    StringTokenizer tok = new StringTokenizer(numbers, ",");
    int range_count = tok.countTokens();

    if (!(range_count >= 1)) {
      throw new IllegalArgumentException("No ranges");
    }

    begins = new int[range_count];
    ends = new int[range_count];

    for (int i = 0; i < range_count; i++) {
      String range = tok.nextToken();

      int low, high;

      try {
	int whereDash = range.indexOf('-');
	if (whereDash >= 0) {
	  low = Integer.parseInt(range.substring(0, whereDash));
	  high = Integer.parseInt(range.substring(whereDash+1));
	} else {
	  low = high = Integer.parseInt(range);
	}
      } catch (NumberFormatException e) {
	throw new IllegalArgumentException(e.toString());
      }

      Assert.assertTrue(low >= 0);
      Assert.assertTrue(high >= 0);
      Assert.assertTrue(low <= high);
      Assert.assertTrue(parityOf(low) == parityOf(high));

      begins[i] = low;
      ends[i] = high;
    }

    checkRep();
  }

  private static int parityOf(int i)
  {
    return i & 1;
  }

  private final int[] begins;
  private final int[] ends;

  public void checkRep()
  {
    // parallel arrays
    Assert.assertTrue(begins != null);
    Assert.assertTrue(ends != null);
    Assert.assertTrue(begins.length == ends.length);

    for (int i = 0; i < begins.length; i++) {
      int b = begins[i];
      int e = ends[i];

      // increasing from begin to end, matched parity
      Assert.assertTrue(b <= e);
      Assert.assertTrue(parityOf(b) == parityOf(e));

      for (int j = i+1; j < begins.length; j++) {
	int b2 = begins[j];
	int e2 = ends[j];

	// if ranges are same parity, must not overlap
	if (parityOf(b) == parityOf(b2)) {
	  Assert.assertTrue(!((b <= b2) && (b2 <= e)));
	  Assert.assertTrue(!((b <= e2) && (e2 <= e)));
	}
      }
    }
  }

  /** @return true iff n is in this */
  public boolean contains(int n)
  {
    checkRep();
    try {

      int parity = parityOf(n);
      for (int i = 0; i < begins.length; i++) {
	int b = begins[i];
	int e = ends[i];
	if (parity == parityOf(b)) {
	  if ((b <= n) && (n <= e)) {
	    return true;
	  }
	}
      }

      return false;

    } finally {
      checkRep();
    }
  }

  /** @return the number of elements less than n in this */
  public int orderStatistic(int n)
  {
    checkRep();

    int result = 0;

    int parity = parityOf(n);
    for (int i = 0; i < begins.length; i++) {
      int b = begins[i];
      int e = ends[i];

      int n2;
      if (parity == parityOf(b)) {
	n2 = n;
      } else {
	n2 = n+1;
      }

      if (n2 > b) {
	if (n2 > e) {
	  result += (e - b) / 2 + 1;
	} else {
	  result += (n2 - b) / 2;
	}
      }

    }

    checkRep();

    Assert.assertTrue((0 <= result) && (result <= size()));
    return result;
  }

  /** @return the number of elements in this */
  public int size()
  {
    checkRep();

    int result = 0;

    for (int i = 0; i < begins.length; i++) {
      int b = begins[i];
      int e = ends[i];

      result += (e - b) / 2 + 1;
    }

    checkRep();
    Assert.assertTrue(result >= 0);
    return result;
  }

  /**
   * @returns true iff the set is the empty set
   */
  public boolean isEmpty()
  {
    checkRep();
    return (begins.length == 0);
  }

  /**
   * @requires !this.isEmpty()
   * @returns the minimal number in the set
   */
  public int min()
  {
    checkRep();

    Assert.assertTrue(begins.length > 0);

    int low = Integer.MAX_VALUE;
    for (int i = 0; i < begins.length; i++) {
      if (begins[i] < low) low = begins[i];
    }

    checkRep();

    return low;
  }

  /**
   * @requires !this.isEmpty()
   * @returns the maximal number in the set
   */
  public int max()
  {
    checkRep();

    Assert.assertTrue(begins.length > 0);

    int high = Integer.MIN_VALUE;
    for (int i = 0; i < ends.length; i++) {
      if (ends[i] > high) high = ends[i];
    }

    checkRep();

    return high;
  }

  /** @return true iff n is in this */
  public boolean intersects(StreetNumberSet other)
  {
    checkRep();
    try {

      if (isEmpty()) return false;
      if (other.isEmpty()) return false;

      int min = min();
      int max = max();
      {
	int min2 = other.min();
	int max2 = other.max();
	if (min2 > min) min = min2;
	if (max2 < max) max = max2;
      }

      for (int n = min; n <= max; n++) {
	if (this.contains(n) && other.contains(n)) {
	  return true;
	}
      }

      return false;

    } finally {
      checkRep();
    }
  }

  public boolean equals(Object o)
  {
    return (o instanceof StreetNumberSet) && equals((StreetNumberSet) o);
  }

  public boolean equals(StreetNumberSet other)
  {
    if (other == null) return false;
    if (other == this) return true;

    if (begins.length == 0) {
      return (other.begins.length == 0);
    }

    int min = min();
    int max = max();

    if (min != other.min()) return false;
    if (max != other.max()) return false;

    for (int n = min; n <= max; n++) {
      if (this.contains(n) != other.contains(n)) {
	return false;
      }
    }

    return true;
  }

  public int hashCode()
  {
    return 3 * min() + 17 * max();
  }

}
