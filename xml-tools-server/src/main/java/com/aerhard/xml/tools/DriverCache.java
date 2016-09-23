package com.aerhard.xml.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

class DriverCache {
  public static final ConcurrentHashMap<Integer, Driver> drivers = new ConcurrentHashMap<Integer, Driver>();
  private static final AtomicInteger maxSize = new AtomicInteger();

  public static void setMaxSize(int newMaxSize) {
    maxSize.set(newMaxSize);
  }

  public static void clear() {
    drivers.clear();
  }

  public static void adjustSize() {
   int surplusDriverCount = drivers.size() - maxSize.get();
    if (surplusDriverCount > 0) {
      Collection<Driver> drivers = DriverCache.drivers.values();
      List<Driver> driverList = new ArrayList<Driver>(drivers);
      Collections.sort(driverList);
      List<Driver> surplusDrivers =
          driverList.subList(0, surplusDriverCount);
      DriverCache.drivers.values().removeAll(surplusDrivers);
    }
  }

}
