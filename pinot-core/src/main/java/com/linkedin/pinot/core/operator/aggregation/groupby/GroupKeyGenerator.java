/**
 * Copyright (C) 2014-2016 LinkedIn Corp. (pinot-core@linkedin.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.linkedin.pinot.core.operator.aggregation.groupby;

import com.linkedin.pinot.core.query.utils.Pair;
import java.util.Iterator;


/**
 * Interface for generating group keys.
 */
public interface GroupKeyGenerator {
  /**
   * Get the global upper bound of the group key. All group keys generated or will be generated should be less than this
   * value. This interface can be called before generating group keys to determine the type and size of the value result
   * holder.
   *
   * @return global upper bound of the group key.
   */
  int getGlobalGroupKeyUpperBound();

  /**
   * Return whether there are any multi value group-by columns. This interface can be used to determine using which
   * interface to generate the group keys.
   *
   * @return whether there are multi value group-by columns.
   */
  boolean hasMultiValueGroupByColumn();

  /**
   * Generate group keys for a given docId set and return the mapping in the passed in docIdToGroupKey array.
   * This interface is for situation where all the group-by columns are single valued.
   *
   * @param docIdSet document id set.
   * @param startIndex start index.
   * @param length length.
   * @param docIdToGroupKey buffer to return the results.
   */
  void generateKeysForDocIdSet(int[] docIdSet, int startIndex, int length, int[] docIdToGroupKey);

  /**
   * Generate group keys for the given docId set and return a mapping from docId to group keys(int[]) in the passed in
   * docIdToGroupKeys array.
   * This interface is for situation where at least one group-by columns are multi valued.
   *
   * @param docIdSet document id set.
   * @param startIndex start index.
   * @param length length.
   * @param docIdToGroupKeys buffer to return the results.
   */
  void generateKeysForDocIdSet(int[] docIdSet, int startIndex, int length, int[][] docIdToGroupKeys);

  /**
   * Get the current upper bound of the group key. All group keys already generated should be less than this value. This
   * interface can be called after generating some group keys and before processing them to determine whether to expand
   * the size of the value result holder.
   *
   * @return current upper bound of the group key.
   */
  int getCurrentGroupKeyUpperBound();

  /**
   * Returns an iterator of group keys. Use this interface to iterate through all the group keys.
   *
   * @return iterator of group keys.
   */
  Iterator<GroupKey> getUniqueGroupKeys();

  /**
   * This class encapsulates the integer group key and the string group key.
   */
  class GroupKey extends Pair<Integer, String> {

    public GroupKey(Integer first, String second) {
      super(first, second);
    }

    public String getStringKey() {
      return getSecond();
    }
  }
}
