package calendar.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the event model interface.
 * Manages a collection of events and provides methods to query and modify them.
 */
public class EventModel {
  private final List<Event> seriesOfEvents;

  /**
   * Constructs a new EventModel.
   */
  public EventModel() {
    this.seriesOfEvents = new ArrayList<>();
  }

  /**
   * Creates a new event in the calendar system.
   *
   * @param userSubject     subject of event
   * @param userDescription description of event
   * @param userStartTime   start date/time of event
   * @param userEndTime     end date/time of event
   * @param location        location of event
   * @param status          status of event
   * @param weekDays        specific weekdays that event will repeat
   * @param repeatCount     number of times the event should repeat
   * @throws IllegalArgumentException if duplicate events
   */
  public void createEvent(String userSubject, String userDescription, LocalDateTime userStartTime,
                          LocalDateTime userEndTime, Location location, Status status,
                          String[] weekDays, int repeatCount) {
    // Create the initial event
    Event newEvent = Event.getBuilder()
            .setSubject(userSubject)
            .setDescription(userDescription)
            .setStartDateTime(userStartTime)
            .setEndDateTime(userEndTime)
            .setLocation(location)
            .setStatus(status)
            .build();

    // Check for duplicates
    if (seriesOfEvents.contains(newEvent)) {
      throw new IllegalArgumentException("An event with the same subject, start time, and " +
              "end time already exists");
    }

    // Add the event to the list
    if (seriesOfEvents.isEmpty()) {
      seriesOfEvents.add(newEvent);
    } else {
      sortSequence(newEvent);
    }

    // If repeatCount is provided, create recurring events
    if (repeatCount > 0) {
      // Get the time of the start/end time
      LocalTime startTime;
      LocalTime endTime;
      if (userEndTime == null) {
        startTime = LocalTime.of(8, 0);  // Default to 8 AM for all-day events
        endTime = LocalTime.of(17, 0);   // Default to 5 PM for all-day events
      } else {
        startTime = userStartTime.toLocalTime();
        endTime = userEndTime.toLocalTime();
      }

      // Set the current date to start the recurring events as the date of the startDateTime
      LocalDateTime currentDate = userStartTime;
      int createdCount = 0;

      while (createdCount < repeatCount) {
        // Move one day at a time
        currentDate = currentDate.plusDays(1);

        // If weekDays is provided, only create events on specified days
        if (weekDays != null && weekDays.length > 0) {
          // Convert weekDays array to list of DayOfWeek
          List<DayOfWeek> daysToRepeat = new ArrayList<>();
          for (String day : weekDays) {
            switch (day.toUpperCase()) {
              case "M":
                daysToRepeat.add(DayOfWeek.MONDAY);
                break;
              case "T":
                daysToRepeat.add(DayOfWeek.TUESDAY);
                break;
              case "W":
                daysToRepeat.add(DayOfWeek.WEDNESDAY);
                break;
              case "R":
                daysToRepeat.add(DayOfWeek.THURSDAY);
                break;
              case "F":
                daysToRepeat.add(DayOfWeek.FRIDAY);
                break;
              case "S":
                daysToRepeat.add(DayOfWeek.SATURDAY);
                break;
              case "U":
                daysToRepeat.add(DayOfWeek.SUNDAY);
                break;
              default:
                throw new IllegalArgumentException("Invalid Weekday");
            }
          }

          // Only create event if current day is in daysToRepeat
          if (!daysToRepeat.contains(currentDate.getDayOfWeek())) {
            continue;
          }
        }

        // Create new start/end times for the current day
        LocalDateTime newStartTime = currentDate.toLocalDate().atTime(startTime);
        LocalDateTime newEndTime = currentDate.toLocalDate().atTime(endTime);

        // Build the event
        Event recurringEvent = Event.getBuilder()
                .setSubject(userSubject)
                .setDescription(userDescription)
                .setStartDateTime(newStartTime)
                .setEndDateTime(newEndTime)
                .setLocation(location)
                .setStatus(status)
                .build();

        // Add the event to the list and createdCount++
        seriesOfEvents.add(recurringEvent);
        createdCount++;
      }
    }
  }


  /**
   * Edits an existing event in the calendar system.
   *
   * @param property         of which property to change
   * @param userSubject      subject of event
   * @param userStartTime    start date/time of event
   * @param newPropertyValue value of change applied to field
   * @throws IllegalArgumentException if invalid property are given
   * @throws IllegalArgumentException if event is not found
   */
  public Event editEvent(String property, String userSubject,
                         LocalDateTime userStartTime, String newPropertyValue) {
    // Change property to proper format using validateProperty
    String fieldToEdit = validateProperty(property);
    // Find the event with given information
    Object[] found = findEvent(userSubject, userStartTime);
    if (found == null) {
      throw new IllegalArgumentException("Event not found");
    }
    Event eventToEdit = (Event) found[0];
    int index = (int) found[1];
    updateEventProperty(eventToEdit, fieldToEdit, newPropertyValue, index);
    return seriesOfEvents.get(index);
  }

  /**
   * Edits all events in a series from a given date.
   *
   * @param property         property to change
   * @param userSubject      subject of event
   * @param userStartTime    start date/time of event
   * @param newPropertyValue new value for the property
   */
  public Event editEvents(String property, String userSubject,
                          LocalDateTime userStartTime, String newPropertyValue) {
    // Change property to proper format using validateProperty
    String fieldToEdit = validateProperty(property);
    // Find events that match the given information with findEventsInSeries
    List<Integer> indices = findEventsInSeries(userSubject, userStartTime);
    if (indices.isEmpty()) {
      throw new IllegalArgumentException("No events found in series");
    }
    for (int index : indices) {
      Event eventToEdit = seriesOfEvents.get(index);
      updateEventProperty(eventToEdit, fieldToEdit, newPropertyValue, index);
      Event testEvent = seriesOfEvents.get(index);
    }
    return seriesOfEvents.get(0);
  }

  /**
   * Edits all events in a series regardless of date.
   *
   * @param property         property to change
   * @param userSubject      subject of event
   * @param userStartTime    start date/time of event
   * @param newPropertyValue new value for the property
   */
  public Event editSeries(String property, String userSubject,
                          LocalDateTime userStartTime, String newPropertyValue) {
    // Change property to proper format using validateProperty
    String fieldToEdit = validateProperty(property);
    // Find events that match the given information iwht findEventsInSeries
    List<Integer> indices = findAllEventsInSeries(userSubject);
    if (indices.isEmpty()) {
      throw new IllegalArgumentException("No events found in series");
    }
    for (int index : indices) {
      Event eventToEdit = seriesOfEvents.get(index);
      updateEventProperty(eventToEdit, fieldToEdit, newPropertyValue, index);
    }
    return seriesOfEvents.get(0);
  }

  /**
   * Helper method that turn Property to lowercase for edit, if it doesn't throw an IAE.
   */
  private String validateProperty(String property) {
    switch (property.toLowerCase()) {
      case "subject":
      case "start":
      case "end":
      case "description":
      case "location":
      case "status":
        return property.toLowerCase();
      default:
        throw new IllegalArgumentException("Invalid property");
    }
  }

  /**
   * Helper that updates a property of an event.
   */
  private void updateEventProperty(Event event, String fieldToEdit, String newValue, int index) {
    Event updatedEvent = null;
    switch (fieldToEdit) {
      case "subject":
        updatedEvent = Event.getBuilder()
                .setSubject(newValue)
                .setDescription(event.getDescription())
                .setStartDateTime(event.getStartDateTime())
                .setEndDateTime(event.getEndDateTime())
                .setLocation(event.getLocation())
                .setStatus(event.getStatus())
                .build();
        break;
      case "start":
        LocalDateTime newStart = LocalDateTime.parse(newValue);
        if (newStart.isAfter(event.getEndDateTime())) {
          throw new IllegalArgumentException("New start time cannot be after end time");
        }
        updatedEvent = Event.getBuilder()
                .setSubject(event.getSubject())
                .setDescription(event.getDescription())
                .setStartDateTime(newStart)
                .setEndDateTime(event.getEndDateTime())
                .setLocation(event.getLocation())
                .setStatus(event.getStatus())
                .build();
        break;
      case "end":
        LocalDateTime newEnd = LocalDateTime.parse(newValue);
        if (newEnd.isBefore(event.getStartDateTime())) {
          throw new IllegalArgumentException("New end time cannot be before start time");
        }
        updatedEvent = Event.getBuilder()
                .setSubject(event.getSubject())
                .setDescription(event.getDescription())
                .setStartDateTime(event.getStartDateTime())
                .setEndDateTime(newEnd)
                .setLocation(event.getLocation())
                .setStatus(event.getStatus())
                .build();
        break;
      case "description":
        updatedEvent = Event.getBuilder()
                .setSubject(event.getSubject())
                .setDescription(newValue)
                .setStartDateTime(event.getStartDateTime())
                .setEndDateTime(event.getEndDateTime())
                .setLocation(event.getLocation())
                .setStatus(event.getStatus())
                .build();
        break;
      case "location":
        Location newLocation = Location.valueOf(newValue.toUpperCase());
        updatedEvent = Event.getBuilder()
                .setSubject(event.getSubject())
                .setDescription(event.getDescription())
                .setStartDateTime(event.getStartDateTime())
                .setEndDateTime(event.getEndDateTime())
                .setLocation(newLocation)
                .setStatus(event.getStatus())
                .build();
        break;
      case "status":
        Status newStatus = Status.valueOf(newValue.toUpperCase());
        updatedEvent = Event.getBuilder()
                .setSubject(event.getSubject())
                .setDescription(event.getDescription())
                .setStartDateTime(event.getStartDateTime())
                .setEndDateTime(event.getEndDateTime())
                .setLocation(event.getLocation())
                .setStatus(newStatus)
                .build();
        break;
      default:
        throw new IllegalArgumentException("Invalid fieldToEdit");
    }
    seriesOfEvents.set(index, updatedEvent);
  }

  /**
   * Helper method that find all events in a series from a given date.
   */
  private List<Integer> findEventsInSeries(String subject, LocalDateTime startTime) {
    List<Integer> indices = new ArrayList<>();
    for (int i = 0; i < seriesOfEvents.size(); i++) {
      Event event = seriesOfEvents.get(i);
      if (event.getSubject().equals(subject) &&
              !event.getStartDateTime().isBefore(startTime)) {
        indices.add(i);
      }
    }
    return indices;
  }

  /**
   * Finds all events in a series regardless of date.
   *
   * @param subject subject of event
   * @return list of indices of events in the series
   */
  private List<Integer> findAllEventsInSeries(String subject) {
    List<Integer> indices = new ArrayList<>();
    for (int i = 0; i < seriesOfEvents.size(); i++) {
      Event event = seriesOfEvents.get(i);
      if (event.getSubject().equals(subject)) {
        indices.add(i);
      }
    }
    return indices;
  }

  /**
   * Print the event based on the given time interval.
   *
   * @param startTime startTime of the event
   * @param endTime endTime of the event
   */
  public String printDateTimeString(LocalDateTime startTime, LocalDateTime endTime) {
    String output = "";
    for (int i = 0; i < seriesOfEvents.size(); i++) {
      if ((startTime.isAfter(seriesOfEvents.get(i).getStartDateTime())
              || startTime.equals(seriesOfEvents.get(i).getStartDateTime()))
              && (endTime.isBefore(seriesOfEvents.get(i).getEndDateTime())
              || endTime.equals(seriesOfEvents.get(i).getEndDateTime()))) {
        String locationLocal = "";
        try {
          locationLocal = seriesOfEvents.get(i).getLocation().toString();
        } catch (NullPointerException e) {
          // Catching IAE
        }
        output = output + "*" + locationLocal
                + " " + seriesOfEvents.get(i).getSubject() + " between "
                + seriesOfEvents.get(i).getStartDateTime() + " and "
                + seriesOfEvents.get(i).getEndDateTime();
      }
    }
    return output;
  }

  /**
   * Print the event on specific day.
   *
   * @param date date of the event
   */
  public String printDate(LocalDate date) {
    String output = "";

    for (int i = 0; i < seriesOfEvents.size(); i++) {
      if (date.isEqual(seriesOfEvents.get(i).getStartDateTime().toLocalDate())
              || date.isEqual(seriesOfEvents.get(i).getEndDateTime().toLocalDate())) {
        String locationLocal = "";
        try {
          locationLocal = seriesOfEvents.get(i).getLocation().toString();
        } catch (NullPointerException e) {
          // Catching IAE
        }
        output = output + "*" + locationLocal
                + " " + seriesOfEvents.get(i).getSubject() + " on "
                + seriesOfEvents.get(i).getStartDateTime().toLocalDate();
      }
    }
    return output;
  }

  /**
   * Print busy if there is a event in the given time.
   *
   * @param userInput user Input
   * @param dateTime dateTime of the event
   */
  public String printStatus(String userInput, LocalDateTime dateTime) {
    String output = "available";
    for (int i = 0; i < seriesOfEvents.size(); i++) {
      if ((dateTime.isAfter(seriesOfEvents.get(i).getStartDateTime())
              || dateTime.equals(seriesOfEvents.get(i).getStartDateTime()))
              && (dateTime.isBefore(seriesOfEvents.get(i).getEndDateTime())
              || dateTime.equals(seriesOfEvents.get(i).getEndDateTime()))) {
        output = "busy";
      }
    }
    return output;
  }

  /**
   * Helper method to find an event by subject and start time.
   */
  private Object[] findEvent(String userSubject, LocalDateTime userStartTime) {
    for (int i = 0; i < seriesOfEvents.size(); i++) {
      Event event = seriesOfEvents.get(i);
      if (event.getSubject().equals(userSubject) &&
              event.getStartDateTime().equals(userStartTime)) {
        return new Object[]{event, i};
      }
    }
    return null;
  }


  /**
   * Gets all events in the model. This method is primarily for testing purposes.
   *
   * @return List of all events
   */
  public List<Event> getEvents() {
    return new ArrayList<>(seriesOfEvents);
  }

  /**
   * Pastes a list of events into the calendar.
   *
   * @param events the list of events to paste
   */
  public void paste(List<Event> events) {
    for (Event event : events) {
      if (!seriesOfEvents.contains(event)) {
        seriesOfEvents.add(event);
      }
    }
  }

  private void sortSequence(Event e2) {
    int lastOfIndex = seriesOfEvents.size() - 1;
    Event e1 = seriesOfEvents.get(lastOfIndex);

    LocalDateTime o1 = e1.getStartDateTime();
    LocalDateTime o2 = e2.getStartDateTime();

    // Compare the date for two Events
    int result = o1.toLocalDate().compareTo(o2.toLocalDate());

    if (seriesOfEvents.size() > 1) {
      if (result == 0) {
        // Same date — compare time
        result = o1.toLocalTime().compareTo(o2.toLocalTime());
        if (result <= 0) {
          seriesOfEvents.add(e2); // Append if later or equal
        } else {
          seriesOfEvents.add(lastOfIndex, e2); // Insert before last
        }
      } else if (result > 0) {
        // New event is earlier than the last one
        boolean inserted = false;
        for (int i = seriesOfEvents.size() - 1; i >= 0; i--) {
          LocalDateTime existing = seriesOfEvents.get(i).getStartDateTime();
          int dayCompare = existing.toLocalDate().compareTo(o2.toLocalDate());
          if (dayCompare > 0) {
            continue; // Keep moving back
          } else if (dayCompare == 0) {
            int timeCompare = existing.toLocalTime().compareTo(o2.toLocalTime());
            if (timeCompare > 0) {
              continue;
            } else {
              seriesOfEvents.add(i + 1, e2); // Insert after this one
              inserted = true;
              break;
            }
          } else {
            seriesOfEvents.add(i + 1, e2); // Insert after earlier date
            inserted = true;
            break;
          }
        }
        if (!inserted) {
          seriesOfEvents.add(0, e2); // Insert at beginning if earliest
        }
      } else {
        // New event is later — just append
        seriesOfEvents.add(e2);
      }
    } else {
      // Only one event in list
      if (result == 0) {
        result = o1.toLocalTime().compareTo(o2.toLocalTime());
        if (result <= 0) {
          seriesOfEvents.add(e2);
        } else {
          seriesOfEvents.remove(0);
          seriesOfEvents.add(e2);
          seriesOfEvents.add(e1);
        }
      } else if (result > 0) {
        seriesOfEvents.add(0, e2); // Earlier date, insert in front
      } else {
        seriesOfEvents.add(e2); // Later date, append
      }
    }
  }

  /**
   * Helper method for GUI purpose that gets the number of events starting from a given date.
   *
   * @param date date of the day that user calls
   */
  public int eventsLeft(String date) {
    LocalDate dateTime = LocalDate.parse(date);
    List<Event> events = new ArrayList<>();
    for (int i = 0; i < seriesOfEvents.size(); i++) {
      LocalDateTime o1 = seriesOfEvents.get(i).getStartDateTime();
      int result = o1.toLocalDate().compareTo(dateTime);
      if (result <= 0) {
        events.add(seriesOfEvents.get(i));
      }
    }
    return events.size();
  }

  /**
   * Return the list of events starting from a given date.
   *
   * @param input input of the day that user calls
   */
  public List<Event> eventsToView(String input) {
    LocalDate startDate = LocalDate.parse(input);
    List<Event> events = new ArrayList<>(seriesOfEvents);
    for (int i = 0; i < seriesOfEvents.size(); i++) {
      LocalDateTime o1 = seriesOfEvents.get(i).getStartDateTime();
      int result = o1.toLocalDate().compareTo(startDate);
      if (result < 0) {
        events.remove(seriesOfEvents.get(i));
      }
    }
    if (eventsLeft(input) <= 10) {
      return events;
    } else {
      while (events.size() > 10) {
        events.remove(events.size() - 1);
      }
    }
    return events;
  }

}