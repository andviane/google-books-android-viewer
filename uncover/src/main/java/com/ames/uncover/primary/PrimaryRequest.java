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

/**
 * Request to the primary data source.
 */
public class PrimaryRequest {

  /**
   * Start of the range, inclusive.
   */
  protected final int from;

  /**
   * End of the range, exclusive.
   */
  protected final int to;

  /**
   * Indicates if the total data count is required. If false,
   * total data count is known for this query and need not be
   * fetched again.
   */
  protected final boolean totalDataCountRequired;

  protected long sent;

  /**
   * Data query to fetch.
   */
  protected final Query query;

  public int getChannel() {
    return channel;
  }

  public int page = -1;

  public PrimaryRequest setChannel(int channel) {
    this.channel = channel;
    return this;
  }

  public int getPage() {
    return page;
  }

  public PrimaryRequest setPage(int page) {
    this.page = page;
    return this;
  }

  /**
   * The number of the channel (parallel thread) used for sending this request.

   */
  protected int channel;

  private static final long ofs = System.currentTimeMillis();

  public PrimaryRequest(int from, int to, Query query, boolean totalDataCountRequired) {
    this.from = from;
    this.to = to;
    this.query = query;
    this.totalDataCountRequired = totalDataCountRequired;
  }

  public PrimaryRequest(int from, int to, Query query) {
    this(from, to, query, false);
  }

  public int getFrom() {
    return from;
  }

  public int getTo() {
    return to;
  }

  public Query getQuery() {
    return query;
  }

  public String toString() {
    return from + " .. " + to + (sent > 0?(" sent + " + (sent-ofs)):" not sent") + " ?" + query;
  }

  public void setSent(long sent) {
    this.sent = sent;
  }
}
