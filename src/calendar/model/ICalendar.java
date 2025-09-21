package calendar.model;

import calendar.model.Event;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.TimeZone;

/**
 * Interface for calendar operations.
 * Defines the core functionality that any calendar implementation must provide.
 */
public interface ICalendar {
  /**
   * Gets the name of the calendar.
   *
   * @return the name of the calendar
   */
  String getName();

  /**
   * Gets the timezone of the calendar.
   *
   * @return the timezone of the calendar
   */
  ZoneId getTimezone();

  /**
   * Gets all events in the calendar.
   *
   * @return list of all events
   */
  EventModel getEventModel();

  /**
   * Sets the timezone of the calendar.
   *
   * @param timezone the new timezone
   * @throws IllegalArgumentException if the timezone is invalid
   */
  void setTimezone(ZoneId timezone);
}