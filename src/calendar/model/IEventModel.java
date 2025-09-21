package calendar.model;

import java.time.LocalDateTime;
import java.util.List;

public interface IEventModel {

  public void createEvent(String userSubject, String userDescription, LocalDateTime userStartTime,
                          LocalDateTime userEndTime, Location location, Status status,
                          String[] weekDays, int repeatCount);

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
                         LocalDateTime userStartTime, String newPropertyValue);

  /**
   * Edits all events in a series from a given date.
   *
   * @param property         property to change
   * @param userSubject      subject of event
   * @param userStartTime    start date/time of event
   * @param newPropertyValue new value for the property
   */
  public Event editEvents(String property, String userSubject,
                          LocalDateTime userStartTime, String newPropertyValue);

  /**
   * Edits all events in a series regardless of date.
   *
   * @param property         property to change
   * @param userSubject      subject of event
   * @param userStartTime    start date/time of event
   * @param newPropertyValue new value for the property
   */
  public Event editSeries(String property, String userSubject,
                          LocalDateTime userStartTime, String newPropertyValue);

  /**
   * prints the events existent between the given dateTimes
   *
   * @param startTime start date/time of event
   * @param endTime   end date/time of event
   * @return a string in a specific format of the events during the given times
   */
  public String printDateTimeString(LocalDateTime startTime, LocalDateTime endTime);

  /**
   * prints status or output of events existent at the dateTime given
   *
   * @param userInput input of whether printing status/printing events
   * @param dateTime  for whether events begin at specific dateTime
   * @return string of status or events at the specific time
   */
  public String printDateString(String userInput, LocalDateTime dateTime);

  /**
   * Gets all events in the model. This method is primarily for testing purposes.
   *
   * @return List of all events
   */
  public List<Event> getEvents();

  /**
   * Creates a new calendar.
   *
   * @param userCalendarName Name of calendar
   * @param userTimeZone     TimeZone of calendar
   * @throws IllegalArgumentException if duplicate events
   */
  public Calendar createCalendar(String userCalendarName, String userTimeZone,
                                 List<Event> seriesOfEvents);
}