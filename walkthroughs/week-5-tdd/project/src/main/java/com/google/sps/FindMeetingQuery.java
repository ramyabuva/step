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
    // Compile events that will impact request attendees
    ArrayList<TimeRange> eventTimesOnlyMandatory = new ArrayList<TimeRange>();
    ArrayList<TimeRange> eventTimesWithOptional = new ArrayList<TimeRange>();
    for (Event event : events) {
      Set<String> intersection = new HashSet<String>(request.getAttendees());
      intersection.retainAll(event.getAttendees());
      if (intersection.size() > 0) {
        eventTimesOnlyMandatory.add(event.getWhen());
        eventTimesWithOptional.add(event.getWhen());
      } else {
        intersection = new HashSet<String>(request.getOptionalAttendees());
        intersection.retainAll(event.getAttendees());
        if (intersection.size() > 0) {
          eventTimesWithOptional.add(event.getWhen());
        }
      }
    }
    Collection<TimeRange> options =  findFreeTimes(eventTimesWithOptional, request.getDuration());
    if (options.size() > 0 || request.getAttendees().size() == 0 || request.getOptionalAttendees().size() == 0) {
        return options;
    }
    return findFreeTimes(eventTimesOnlyMandatory, request.getDuration());
  }

  private Collection<TimeRange> findFreeTimes(ArrayList<TimeRange> eventTimes, long duration) {
    eventTimes.add(TimeRange.fromStartDuration(TimeRange.END_OF_DAY + 1, 0));
    Collections.sort(eventTimes, TimeRange.ORDER_BY_START);
    int start = TimeRange.START_OF_DAY;
    ArrayList<TimeRange> options = new ArrayList<TimeRange>();
    for (TimeRange eventTime : eventTimes) { 
      // Checks if not overlapping with the previous event
      if (eventTime.start() > start) {
        // Adds gap between meeting times if it exceeds the requested duration
        if (eventTime.start() - start >= duration) {
          options.add(TimeRange.fromStartEnd(start, eventTime.start(), /*inclusive=*/false));
        }
        start = eventTime.end();
      } else if (eventTime.end() > start) {
        // Alters start time if event ends after a previous event
        start = eventTime.end();
      }
    }
    return options;
  }
}
