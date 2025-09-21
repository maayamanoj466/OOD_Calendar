package calendar.model;

import java.time.ZoneId;

/**
 * Represents a calendar in the calendar system.
 */
public class Calendar extends AbstractCalendar implements ICalendar {

  /**
   * Constructs a new Calendar with the specified name and timezone.
   *
   * @param name       name of the calendar
   * @param timezone   timezone format
   * @param eventModel new model
   */
  public Calendar(String name, ZoneId timezone, EventModel eventModel) {
    super(name, timezone);
  }

  /**
   * Sets the timezone of the calendar.
   *
   * @param timezone the new timezone
   * @throws IllegalArgumentException if the timezone is invalid
   */
  @Override
  public void setTimezone(ZoneId timezone) {
    this.timezone = timezone;
  }
}