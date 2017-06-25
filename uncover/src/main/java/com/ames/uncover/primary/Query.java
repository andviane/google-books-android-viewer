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
package com.ames.uncover.primary;

import java.io.Serializable;

/**
 * Represents the Query that PrimaryDataProvider must know to get data. This class can
 * be extended as needed, passing the derived subclass that will make way to your
 * {@link PrimaryDataProvider}. The default implementation wraps a simple string.
 */
public class Query implements Serializable {

  private final String queryString;

  /**
   * Build the new query that only contains the query string.
   * @param queryString
   */
  public Query(String queryString) {
    this.queryString = queryString;
  }

  /**
   * Get the query string
   *
   * @return query string
   */
  public String getQueryString() {
    return queryString;
  }

  /**
   * Compares the query string
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Query query = (Query) o;

    return queryString != null ? queryString.equals(query.queryString) : query.queryString == null;

  }

  /**
   * Uses hash code of the query string
   *
   * @return hash code of the query string or 0 if null.
   */
  @Override
  public int hashCode() {
    return queryString != null ? queryString.hashCode() : 0;
  }

  /**
   * Returns the query string.
   */
  public String toString() {
    return queryString;
  }
}
