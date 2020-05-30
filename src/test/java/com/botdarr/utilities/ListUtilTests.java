package com.botdarr.utilities;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class ListUtilTests {
  @Test
  public void subList_maxEqualToZero_emptyList() {
    Assert.assertEquals(0, ListUtils.subList(Arrays.asList(new Object(), new Object()), 0).size());
  }

  @Test
  public void subList_maxEqualToOne_listWithOneItem() {
    Assert.assertEquals(1, ListUtils.subList(Arrays.asList(new Object(), new Object()), 1).size());
  }

  @Test
  public void subList_maxEqualToGreaterThanOne_listWithExactMaxCount() {
    //4 items in list, max 2
    Assert.assertEquals(2,
      ListUtils.subList(Arrays.asList(new Object(), new Object(), new Object(), new Object()), 2).size());
  }
}
