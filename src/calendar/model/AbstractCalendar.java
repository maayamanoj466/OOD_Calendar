package calendar.model;


import java.time.ZoneId;

/**
 * Abstract class for calendar operations.
 * Provides common functionality for all calendar implementations.
 */
public abstract class AbstractCalendar {
  protected String name;
  protected ZoneId timezone;
  protected EventModel eventModel;

  /**
   * Constructs a new AbstractCalendar with the specified name and timezone.
   *
   * @param name     name of the calendar
   * @param timezone timezone format
   */
  protected AbstractCalendar(String name, ZoneId timezone) {
    this.name = name;
    this.timezone = timezone;
    this.eventModel = new EventModel();
  }

  /**
   * Gets the name of the calendar.
   *
   * @return the name of the calendar
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the timezone of the calendar.
   *
   * @return the timezone of the calendar
   */
  public ZoneId getTimezone() {
    return timezone;
  }

  /**
   * Gets this calendar's event model
   *
   * @return this calendar's event model.
   */
  public EventModel getEventModel() {
    return this.eventModel;
  }
}