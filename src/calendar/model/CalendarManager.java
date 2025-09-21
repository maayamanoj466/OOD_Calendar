package calendar.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class CalendarManager {
  private List<Calendar> seriesOfCalendar = new ArrayList<>();
  private Calendar calendarInUse;

  /**
   * Creates a new calendar.
   *
   * @param userCalendarName Name of calendar
   * @param userTimeZone     TimeZone of calendar
   * @throws IllegalArgumentException if duplicate events
   */
  public Calendar createCalendar(String userCalendarName, ZoneId userTimeZone) {
    Calendar calendar = new Calendar(userCalendarName, userTimeZone, new EventModel());
    seriesOfCalendar.add(calendar);
    return calendar;
  }

  /**
   * Edits an existing calendar.
   *
   * @param name     name of the calendar
   * @param property subject of event
   * @param newValue value of change applied to field
   * @throws IllegalArgumentException if invalid property are given
   * @throws IllegalArgumentException if event is not found
   */
  public Calendar editCalendar(String name, String property, String newValue) {
    // Create a new calendar object with the existing event list
    int idx = findAllCalendarInSeries(name);
    Calendar calendar = new Calendar(name,
            seriesOfCalendar.get(idx).getTimezone(), seriesOfCalendar.get(idx).getEventModel());

    // If editing name, create a new calendar with the new name
    if (property.equalsIgnoreCase("name")) {
      Calendar newCalendar = new Calendar(newValue, calendar.getTimezone(),
              seriesOfCalendar.get(idx).getEventModel());
      return seriesOfCalendar.set(idx, newCalendar);
    } else if (property.equalsIgnoreCase("timezone")) {
      ZoneId newTimezone = ZoneId.of(newValue);
      // If editing timezone, create a new calendar with the new timezone
      Calendar newCalendar = new Calendar(calendar.getName(), newTimezone,
              seriesOfCalendar.get(idx).getEventModel());
      return seriesOfCalendar.set(idx, newCalendar);
    }
    return null;

  }

  /**
   * Copy specific event to the given DateTime in the given Calendar.
   *
   * @param eventName The name of the event to copy
   * @param startDateTime The start date/time of the event to copy
   * @param targetCalendarName The name of the target calendar
   * @param newStartDateTime The new start date/time for the copied event
   */
  public void copyEvent(String eventName, String startDateTime, String targetCalendarName, String newStartDateTime) {
    // Get source and target calendars
    Calendar sourceCalendar = calendarInUse;
    Calendar targetCalendar = seriesOfCalendar.get(findAllCalendarInSeries(targetCalendarName));

    // Parse the date/time strings
    LocalDateTime eventStartTime = LocalDateTime.parse(startDateTime);
    LocalDateTime newStartTime = LocalDateTime.parse(newStartDateTime);

    // Get source and target calendar timezones
    ZoneId sourceZone = sourceCalendar.getTimezone();
    ZoneId targetZone = targetCalendar.getTimezone();

    // Find the event in source calendar using startTime
    Event event = null;
    for (Event e : sourceCalendar.getEventModel().getEvents()) {
      if (e.getSubject().equals(eventName) && e.getStartDateTime().equals(eventStartTime)) {
        event = e;
        break;
      }
    }
    if (event == null) {
      throw new IllegalArgumentException("Event not found: " + eventName);
    }

    // Calculate time difference
    Duration timeDifference = Duration.between(event.getStartDateTime(), newStartTime);

    // Create new event start and end times
    LocalDateTime newEndTime = event.getEndDateTime().plus(timeDifference);

    // If target calendar has timezone, perform timezone conversion
    if (targetZone != null) {
      // Convert times to target timezone
      ZonedDateTime sourceStartZoned = event.getStartDateTime().atZone(sourceZone);
      ZonedDateTime sourceEndZoned = event.getEndDateTime().atZone(sourceZone);

      ZonedDateTime targetStartZoned = sourceStartZoned.withZoneSameInstant(targetZone);
      ZonedDateTime targetEndZoned = sourceEndZoned.withZoneSameInstant(targetZone);

      // Apply time difference
      targetStartZoned = targetStartZoned.plus(timeDifference);
      targetEndZoned = targetEndZoned.plus(timeDifference);

      newStartTime = targetStartZoned.toLocalDateTime();
      newEndTime = targetEndZoned.toLocalDateTime();
    }

    // Create new event
    Event.EventBuilder builder = Event.getBuilder()
            .setSubject(event.getSubject())
            .setDescription(event.getDescription())
            .setStartDateTime(newStartTime)
            .setEndDateTime(newEndTime)
            .setLocation(event.getLocation())
            .setStatus(event.getStatus());

    Event newEvent = builder.build();
    targetCalendar.getEventModel().paste(List.of(newEvent));
  }


  /**
   * Copy all events that occur on the specified date to the given Calendar.
   *
   * @param startDate    start Date of the event
   * @param calendarName name of the calendar
   * @param newStartDate new startDateTime for the event
   * @throws IllegalArgumentException if attempting to copy an event by its name and start time fail
   *                                  due to uniquely identify between events
   */
  public void copyEventsOn(String startDate, String calendarName, String newStartDate) {
    List<Event> copiedEvents = new ArrayList<>();

    Calendar sourceCalendar = calendarInUse;
    EventModel sourceModel = sourceCalendar.getEventModel();
    List<Event> sourceEvents = sourceModel.getEvents();
    LocalDateTime eventDateTime = LocalDateTime.parse(startDate);
    LocalDateTime newEventDateTime = LocalDateTime.parse(newStartDate);

    // Get source and target timezones
    ZoneId sourceZone = sourceCalendar.getTimezone();
    Calendar targetCalendar = seriesOfCalendar.get(findAllCalendarInSeries(calendarName));
    ZoneId targetZone = targetCalendar.getTimezone();

    for (Event event : sourceEvents) {
      // Check if event occurs on the specified date
      if (event.getStartDateTime().toLocalDate().equals(eventDateTime.toLocalDate())) {
        // Convert event times to target timezone
        ZonedDateTime sourceStartZoned = event.getStartDateTime().atZone(sourceZone);
        ZonedDateTime sourceEndZoned = event.getEndDateTime().atZone(sourceZone);

        LocalDateTime targetStartTime = sourceStartZoned.withZoneSameInstant(targetZone).toLocalDateTime();
        LocalDateTime targetEndTime = sourceEndZoned.withZoneSameInstant(targetZone).toLocalDateTime();

        // Calculate the time difference between the target date and new date
        java.time.Duration dateDiff = java.time.Duration.between(
                eventDateTime.toLocalDate().atStartOfDay(),
                newEventDateTime.toLocalDate().atStartOfDay());

        // Create new event with adjusted times
        Event copiedEvent = Event.getBuilder()
                .setSubject(event.getSubject())
                .setDescription(event.getDescription())
                .setStartDateTime(targetStartTime.plus(dateDiff))
                .setEndDateTime(targetEndTime.plus(dateDiff))
                .setLocation(event.getLocation())
                .setStatus(event.getStatus())
                .build();
        copiedEvents.add(copiedEvent);
      }
    }

    if (!copiedEvents.isEmpty()) {
      EventModel targetModel = targetCalendar.getEventModel();
      targetModel.paste(copiedEvents);
    }
  }

  /**
   * Copy all events that occur between the specified dates to the given Calendar.
   * If an event series partly overlaps with the specified range, only those events
   * in the series that overlap with the specified range will be copied.
   *
   * @param startDate    start date of the interval (inclusive)
   * @param endDate      end date of the interval (inclusive)
   * @param calendarName name of the target calendar
   * @param newStartDate new start date for the copied events
   * @throws IllegalArgumentException if the date range is invalid
   */
  public void copyEventsBetween(String startDate, String endDate, String calendarName,
                                String newStartDate) {
    List<Event> copiedEvents = new ArrayList<>();

    Calendar sourceCalendar = calendarInUse;
    EventModel sourceModel = sourceCalendar.getEventModel();
    List<Event> sourceEvents = sourceModel.getEvents();

    LocalDateTime intervalStart = LocalDateTime.parse(startDate);
    LocalDateTime intervalEnd = LocalDateTime.parse(endDate);
    LocalDateTime newStartDateTime = LocalDateTime.parse(newStartDate);

    // Get source and target timezones
    ZoneId sourceZone = sourceCalendar.getTimezone();
    Calendar targetCalendar = seriesOfCalendar.get(findAllCalendarInSeries(calendarName));
    ZoneId targetZone = targetCalendar.getTimezone();

    // Calculate the time difference between the interval start and new start date
    java.time.Duration dateDiff = java.time.Duration.between(
            intervalStart.toLocalDate().atStartOfDay(),
            newStartDateTime.toLocalDate().atStartOfDay());

    // Validate date range
    if (intervalEnd.isBefore(intervalStart)) {
      throw new IllegalArgumentException("End date must be after start date");
    }

    for (Event event : sourceEvents) {
      // Check if event overlaps with the specified interval
      if (!event.getEndDateTime().isBefore(intervalStart) &&
              !event.getStartDateTime().isAfter(intervalEnd)) {

        // Convert event times to target timezone
        ZonedDateTime sourceStartZoned = event.getStartDateTime().atZone(sourceZone);
        ZonedDateTime sourceEndZoned = event.getEndDateTime().atZone(sourceZone);

        LocalDateTime targetStartTime = sourceStartZoned.withZoneSameInstant(targetZone).toLocalDateTime();
        LocalDateTime targetEndTime = sourceEndZoned.withZoneSameInstant(targetZone).toLocalDateTime();

        // Create new event with adjusted times
        Event copiedEvent = Event.getBuilder()
                .setSubject(event.getSubject())
                .setDescription(event.getDescription())
                .setStartDateTime(targetStartTime.plus(dateDiff))
                .setEndDateTime(targetEndTime.plus(dateDiff))
                .setLocation(event.getLocation())
                .setStatus(event.getStatus())
                .build();
        copiedEvents.add(copiedEvent);
      }
    }

    if (!copiedEvents.isEmpty()) {
      EventModel targetModel = targetCalendar.getEventModel();
      targetModel.paste(copiedEvents);
    }
  }

  /**
   * Gets all events in the model. This method is primarily for testing purposes.
   *
   * @return List of all events
   */
  public List<Calendar> getCalendars() {
    return seriesOfCalendar;
  }

  /**
   * Finds all calendar in a series.
   *
   * @param name name of calendar
   * @return index of the calendar in the series of calendars.
   */
  private int findAllCalendarInSeries(String name) {
    int j = 0;
    for (int i = 0; i < seriesOfCalendar.size(); i++) {
      Calendar calendar = seriesOfCalendar.get(i);
      if (calendar.getName().equals(name)) {
        j = i;
      }
    }
    return j;
  }

  /**
   *
   */
  public Calendar useCalendar(String calendarName) {
    for (int i = 0; i < seriesOfCalendar.size(); i++) {
      if (seriesOfCalendar.get(i).getName().equals(calendarName)) {
        calendarInUse = seriesOfCalendar.get(i);
        return seriesOfCalendar.get(i);
      }
    }
    return null;
  }

  public Calendar getCalendarInUse() {
    return calendarInUse;
  }
}


