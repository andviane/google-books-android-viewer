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
package ames.com.uncover.primary;

import java.io.Serializable;

/**
 * Represents the Query that PrimaryDataProvider must know to get data. This class can
 * later be extended as needed, the current version simply wraps String
 */
public class Query implements Serializable {

  private final String queryString;

  public Query(String queryString) {
    this.queryString = queryString;
  }

  public String getQueryString() {
    return queryString;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Query query = (Query) o;

    return queryString != null ? queryString.equals(query.queryString) : query.queryString == null;

  }

  @Override
  public int hashCode() {
    return queryString != null ? queryString.hashCode() : 0;
  }
}
