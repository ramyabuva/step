// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    ArrayList<TimeRange> eventTimes = new ArrayList<TimeRange>();
    for (Event event : events) {
	  Set<String> intersection = new HashSet<String>(request.getAttendees());
	  intersection.retainAll(event.getAttendees());
      if(intersection.size() > 0) {
        eventTimes.add(event.getWhen());
      }
    }
    Collections.sort(eventTimes, TimeRange.ORDER_BY_START);
    int start = TimeRange.START_OF_DAY;
    ArrayList<TimeRange> options = new ArrayList<TimeRange>();
    for (TimeRange eventTime : eventTimes) { 
      if(eventTime.start() > start) {
        if (eventTime.start() - start >= request.getDuration()){
          options.add(TimeRange.fromStartEnd(start, eventTime.start(), false));
        }
        start = eventTime.end();
      } else if (eventTime.end() > start) {
        start = eventTime.end();
      }
    }
    if (TimeRange.END_OF_DAY - start >= request.getDuration()) {
      options.add(TimeRange.fromStartEnd(start, TimeRange.END_OF_DAY, true));
    }
    return options;
  }
}
