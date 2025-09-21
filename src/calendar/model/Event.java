package calendar.model;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Represents an event in a calendar system.
 */
public class Event {
  private static final LocalTime StartOfEvent = LocalTime.of(8, 0); // 8:00 AM
  private static final LocalTime EndOfEvent = LocalTime.of(17, 0);  // 5:00 PM

  private final String subject;
  private final String description;
  private final LocalDateTime startDateTime;
  private final LocalDateTime endDateTime;
  private final Location location;
  private final Status status;
  private String[] weekdays;
  private int repeatCount;

  /**
   * Constructs a new Event.Event with the specified parameters.
   *
   * @param subject       the subject or title of the event
   * @param description   the description of the event
   * @param startDateTime the start date and time of the event
   * @param endDateTime   the end date and time of the event, or null for all-day events
   * @param location      whether the event has a location
   * @param status        the status of the event
   * @throws IllegalArgumentException if subject is null
   * @throws IllegalArgumentException if startDateTime is null
   * @throws IllegalArgumentException if endDateTime is before startDateTime
   */
  private Event(String subject, String description, LocalDateTime startDateTime,
                LocalDateTime endDateTime, Location location, Status status) {
    if (subject == null) {
      throw new IllegalArgumentException("Subject cannot be null");
    }
    if (startDateTime == null) {
      throw new IllegalArgumentException("StartDateTime cannot be null");
    }

    this.subject = subject;
    this.description = description;
    this.location = location;
    this.status = status;

    // If end date is not given, it's an all-day event for 8 to 17
    if (endDateTime == null) {
      this.startDateTime = startDateTime.toLocalDate().atTime(StartOfEvent);
      this.endDateTime = startDateTime.toLocalDate().atTime(EndOfEvent);
    } else {
      if (endDateTime.isBefore(startDateTime)) {
        throw new IllegalArgumentException("EndDateTime cannot be before startDateTime");
      }
      this.startDateTime = startDateTime;
      this.endDateTime = endDateTime;
    }
  }

  /**
   * Returns a new EventBuilder instance for creating an Event.Event.
   *
   * @return a new EventBuilder instance
   */
  public static EventBuilder getBuilder() {
    return new EventBuilder();
  }

  /**
   * Builder class for creating Event.Event instances.
   */
  public static class EventBuilder {
    private String subject;
    private String description;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private Location location;
    private Status status;
    private String[] weekDays;
    private int repeatCount;

    private EventBuilder() {
      //initialize
    }

    /**
     * Sets the subject of the event.
     *
     * @param subject the subject or title of the event
     * @return this builder instance for method chaining
     */
    public EventBuilder setSubject(String subject) {
      this.subject = subject;
      return this;
    }

    /**
     * Sets the description of the event (longer version of subject).
     * Is an optional field.
     *
     * @param description a longer description of the event
     * @return this builder instance for method chaining
     */
    public EventBuilder setDescription(String description) {
      this.description = description;
      return this;
    }

    /**
     * Sets the start date and time of the event.
     *
     * @param startDateTime the start date and time
     * @return this builder instance for method chaining
     */
    public EventBuilder setStartDateTime(LocalDateTime startDateTime) {
      this.startDateTime = startDateTime;
      return this;
    }

    /**
     * Sets the end date and time of the event.
     *
     * @param endDateTime the end date and time
     * @return this builder instance for method chaining
     */
    public EventBuilder setEndDateTime(LocalDateTime endDateTime) {
      this.endDateTime = endDateTime;
      return this;
    }

    /**
     * Sets whether the event has a location.
     *
     * @param location true if the event has a location, false otherwise
     * @return this builder instance for method chaining
     */
    public EventBuilder setLocation(Location location) {
      this.location = location;
      return this;
    }

    /**
     * Sets the status of the event.
     *
     * @param status the status of the event
     * @return this builder instance for method chaining
     */
    public EventBuilder setStatus(Status status) {
      this.status = status;
      return this;
    }

    /**
     * Sets the weekDays of the event.
     *
     * @param weekDays specific weekDays that event will repeat
     * @return this builder instance for method chaining
     */
    public EventBuilder setWeekDays(String[] weekDays) {
      this.weekDays = weekDays;
      return this;
    }

    /**
     * Sets the repeatCount of the event.
     *
     * @param repeatCount the number of time the event repeat
     * @return this builder instance for method chaining
     */
    public EventBuilder setRepeatCount(int repeatCount) {
      this.repeatCount = repeatCount;
      return this;
    }

    /**
     * Gets the weekDays of the event.
     *
     * @return the weekDays array
     */
    public String[] getWeekDays() {
      return weekDays;
    }

    /**
     * Gets the repeatCount of the event.
     *
     * @return the number of times the event should repeat
     */
    public int getRepeatCount() {
      return repeatCount;
    }

    /**
     * Builds and returns a new Event.Event instance with the properties set in this builder.
     *
     * @return a new Event.Event instance
     * @throws IllegalArgumentException if required properties are not set or invalid
     */
    public Event build() {
      return new Event(subject, description, startDateTime, endDateTime, location, status);
    }
  }

  /**
   * Returns the subject of the event.
   *
   * @return the subject of the event
   */
  public String getSubject() {
    return subject;
  }

  /**
   * Returns the description of the event, if there is one.
   *
   * @return description of the event.
   */
  public String getDescription() {
    return description;
  }

  /**
   * Returns the start date and time of the event.
   *
   * @return the start date and time
   */
  public LocalDateTime getStartDateTime() {
    return startDateTime;
  }

  /**
   * Returns the end date and time of the event.
   *
   * @return the end date and time
   */
  public LocalDateTime getEndDateTime() {
    return endDateTime;
  }

  /**
   * Returns the location of the event.
   *
   * @return the location
   */
  public Location getLocation() {
    return location;
  }

  /**
   * Returns the status of the event.
   *
   * @return the status
   */
  public Status getStatus() {
    return status;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Event event = (Event) o;
    return subject.equals(event.subject) &&
            startDateTime.equals(event.startDateTime) &&
            endDateTime.equals(event.endDateTime);
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hash(subject, startDateTime, endDateTime);
  }
}