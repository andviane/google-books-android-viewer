/*
Copyright 2017 Audrius Meskauskas

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
package com.ames.uncover;

import java.util.ArrayList;

/**
 * The item used in tests
 */
public class TestItem {

  public String value;

  public String toString() {
    return value;
  }

  public TestItem() {

  }

  public TestItem(String header) {
    this.value = header;
  }

  public TestItem(int position) {
    this("p." + position);
  }

  public static ArrayList<TestItem> range(int from, int to) {
    ArrayList<TestItem> items = new ArrayList<>(to - from);
    for (int i = from; i < to; i++) {
      items.add(new TestItem(i));
    }
    return items;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    TestItem testItem = (TestItem) o;

    return value.equals(testItem.value);

  }

  @Override
  public int hashCode() {
    return value.hashCode();
  }
}
