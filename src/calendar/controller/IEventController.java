package calendar.controller;

import calendar.model.Calendar;
import calendar.model.Event;

/**
 * Interface for event controller operations.
 * Defines the core functionality that any event controller implementation must provide.
 */
public interface IEventController {
  /**
   * Creates a new event in the calendar system based on user input.
   *
   * @return the subject of the created event
   * @throws IllegalArgumentException if the input format is invalid
   */
  public String createEvent(String input);

  /**
   * Edits an existing event in the calendar system based on user input.
   *
   * @return the edited event
   * @throws IllegalArgumentException if the input format is invalid or if the event is not found
   */
  public Event editEvent(String input);

  /**
   * Prints event information based on user input.
   *
   * @return a string containing the requested event information
   * @throws IllegalArgumentException if the input format is invalid
   */
  public String printEvent(String input);

  /**
   * Creates a new calendar based on user input.
   *
   * @param input String input
   * @return the created calendar
   * @throws IllegalArgumentException if the input format is invalid
   */
  public Calendar createCalendar(String input);
}
