package calendar.controller;

import calendar.model.Calendar;
import calendar.model.CalendarManager;
import calendar.model.Event;
import calendar.model.EventModel;
import calendar.view.CalendarGUI;
import calendar.view.EventView;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;


/**
 * Controller class for managing calendar events.
 */
public class EventController implements IEventController {
  protected EventView view;
  protected EventModel model;
  final Readable in;
  final Appendable out;
  public Calendar currentCalendar;
  public CalendarManager calendarManager;

  /**
   * Constructor for EventController.
   **/
  public EventController(Readable in, Appendable out) {
    calendarManager = new CalendarManager();
    this.view = new EventView(System.out);
    this.in = in;
    this.out = out;
  }

  /**
   * Constructor for EventController(For testing purpose).
   **/
  public EventController(Readable in, Appendable out, EventModel model) {
    this.view = new EventView(System.out);
    this.model = model;
    this.in = in;
    this.out = out;
  }

  public void controllerHelper() {
    Scanner scan = new Scanner(this.in);
    while (true) {
      String input = scan.nextLine();
      if (input.equals("exit")) {
        break;
      } else {
        if (input.startsWith("create ")) {
          if (input.contains("calendar")) {
            this.createCalendar(input);
          } else {
            this.createEvent(input);
          }
        } else if (input.startsWith("edit ")) {
          if (input.contains("calendar")) {
            this.editCalendar(input);
          } else {
            this.editEvent(input);
          }
        } else if (input.startsWith("print ")) {
          this.printEvent(input);
        } else if (input.startsWith("use ")) {
          this.useCalendar(input);
        } else if (input.startsWith("copy ")) {
          this.copyEvent(input);
        } else if (input.isEmpty()) {
          this.createGui();
        } else if (input.startsWith("mock ")) {
          this.mockView();
        } else {
          view.printError("Invalid command: " + input);
        }
      }
    }
  }

  /**
   * Creates a new event using the provided EventBuilder. This method validates the event
   * properties and delegates the actual creation to the model.
   *
   * @param builder The EventBuilder containing the event properties
   * @throws IllegalArgumentException if the subject is null
   * @throws IllegalArgumentException if the start time is null
   * @throws IllegalArgumentException if the end time is before the start time
   */
  public Event createEventHelper(Event.EventBuilder builder) {
    Event newEvent = builder.build();

    // If Subject is null
    if (newEvent.getSubject() == null) {
      throw new IllegalArgumentException("Event subject cannot be null");
    } // If StartDateTime is null
    if (newEvent.getStartDateTime() == null) {
      throw new IllegalArgumentException("Event start time cannot be null");
    }

    // If EndDateTime is earlier than StartDateTime
    if (newEvent.getEndDateTime() != null &&
            newEvent.getEndDateTime().isBefore(newEvent.getStartDateTime())) {
      throw new IllegalArgumentException("Event end time cannot be before start time");
    }

    // Create the event in the model
    getActiveModel().createEvent(
            newEvent.getSubject(),
            newEvent.getDescription(),
            newEvent.getStartDateTime(),
            newEvent.getEndDateTime(),
            newEvent.getLocation(),
            newEvent.getStatus(),
            builder.getWeekDays(),
            builder.getRepeatCount()
    );

    return newEvent;
  }

  @Override
  public Event editEvent(String input) {

    String property = null;
    String subject = null;
    LocalDateTime startTime = null;
    String newValue = null;

    if (input.startsWith("edit event ")) {
      String propAndSubject = input.substring("edit event ".length());
      int indexOfSplit = propAndSubject.indexOf(" ");
      property = propAndSubject.substring(0, indexOfSplit);
      subject = propAndSubject.substring(indexOfSplit + 1, propAndSubject.indexOf(" from "));
      String[] details = input.split("\\s*from\\s*|\\s*to\\s*"
              + "|\\s*with\\s*");

      startTime = LocalDateTime.parse(details[1]);
      newValue = details[3];
      return getActiveModel().editEvent(property, subject, startTime, newValue);

    } else if (input.startsWith("edit events ")) {
      String propAndSubject = input.substring("edit events ".length());
      int indexOfSplit = propAndSubject.indexOf(" ");
      property = propAndSubject.substring(0, indexOfSplit);
      subject = propAndSubject.substring(indexOfSplit + 1, propAndSubject.indexOf(" from "));
      String[] details = input.split("\\s*from\\s*|\\s*with\\s*");
      startTime = LocalDateTime.parse(details[1]);
      newValue = details[2];
      return getActiveModel().editEvents(property, subject, startTime, newValue);

    } else if (input.startsWith("edit series ")) {
      String propAndSubject = input.substring("edit series ".length());
      int indexOfSplit = propAndSubject.indexOf(" ");
      property = propAndSubject.substring(0, indexOfSplit);
      subject = propAndSubject.substring(indexOfSplit + 1, propAndSubject.indexOf(" from "));
      String[] details = input.split("\\s*from\\s*|\\s*with\\s*");
      startTime = LocalDateTime.parse(details[1]);
      newValue = details[2];
      return getActiveModel().editSeries(property, subject, startTime, newValue);
    }
    // Validate inputs
    if (property == null) {
      throw new IllegalArgumentException("Property to edit cannot be null");
    }
    if (subject == null) {
      throw new IllegalArgumentException("Event subject cannot be null");
    }
    if (startTime == null) {
      throw new IllegalArgumentException("Event start time cannot be null");
    }
    if (newValue == null) {
      throw new IllegalArgumentException("New value cannot be null");
    }
    return null;
  }

  @Override
  public String printEvent(String input) {
    if (input.startsWith("print events on ")) {
      LocalDate date = LocalDate.parse(input.substring
              ("print events on ".length()));
      return view.printEvent(getActiveModel().printDate(date));
    } else if (input.startsWith("print events from ")) {
      int fromIndex = "print events from ".length();
      int toIndex = input.indexOf(" to ");
      LocalDateTime startTime = LocalDateTime.parse(input.substring(fromIndex, toIndex));
      LocalDateTime endTime = LocalDateTime.parse(input.substring(toIndex + 4));
      return view.printEvent(getActiveModel().printDateTimeString(startTime, endTime));
    } else if (input.startsWith("show status on ")) {
      LocalDateTime dateTime = LocalDateTime.parse(input.substring("show status on ".length()));
      return view.printEvent(getActiveModel().printStatus("show status on ", dateTime));
    } else {
      throw new IllegalArgumentException("Invalid formatting of input");
    }
  }

  /**
   * Creates a new event in the calendar system based on user input.
   *
   * @return the subject of the created event
   * @throws IllegalArgumentException if the input format is invalid
   */
  public String createEvent(String input) {

    // Check if this is an "until" format event
    if (input.contains(" until ")) {
      // Find index for repeats
      int repeatsIndex = input.indexOf(" repeats ");
      // Find the last occurrence of either "on" or "from" before repeats
      String beforeRepeats = input.substring(0, repeatsIndex);
      int lastOnIndex = beforeRepeats.lastIndexOf(" on ");
      int lastFromIndex = beforeRepeats.lastIndexOf(" from ");
      int lastIndex = Math.max(lastOnIndex, lastFromIndex);

      if (lastIndex == -1) {
        throw new IllegalArgumentException("Invalid input format: missing 'on' or 'from' keyword");
      }

      // Extract subject by cutting off before on/from, and after create event
      String subject = input.substring("create event ".length(), lastIndex);

      // Extract start date and time
      int dateStart;
      if (lastIndex == lastOnIndex) {
        dateStart = lastIndex + 4;  // Space + on + Space
      } else {
        dateStart = lastIndex + 6;  // Space + from + Space
      }

      // Start searching of "to" from dataStart
      int toIndex = input.indexOf(" to ", dateStart);
      if (toIndex == -1) {         // If no "to" is found, treat as all-day event
        String startDate = input.substring(dateStart, repeatsIndex);
        LocalDateTime startDateTime = LocalDateTime.parse(startDate + "T08:00");
        LocalDateTime endDateTime = LocalDateTime.parse(startDate + "T17:00");

        // Extract weekdays and until date
        int weekdaysStart = repeatsIndex + 9;  // After " repeats "
        // Find index of "until"
        int weekdaysEnd = input.indexOf(" until ");
        String weekdays = input.substring(weekdaysStart, weekdaysEnd);
        String untilDate = input.substring(input.indexOf(" until ") + 7);

        // Calculate days between
        LocalDateTime untilDateTime = LocalDateTime.parse(untilDate + "T08:00");
        int daysBetween = java.time.Period.between(
                startDateTime.toLocalDate(),
                untilDateTime.toLocalDate()
        ).getDays();

        // Create the initial event
        Event.EventBuilder builder = Event.getBuilder()
                .setSubject(subject)
                .setStartDateTime(startDateTime)
                .setEndDateTime(endDateTime)
                // Separate weekDays
                .setWeekDays(weekdays.split("\\s+"))
                .setRepeatCount(daysBetween);

        // Create the event
        return createEventHelper(builder).getSubject();
      } else {
        String startDateTimeStr = input.substring(dateStart, toIndex);
        String endDateTimeStr = input.substring(toIndex + 4, repeatsIndex);

        // Parse the actual start and end times
        LocalDateTime startDateTime = LocalDateTime.parse(startDateTimeStr);
        LocalDateTime endDateTime = LocalDateTime.parse(endDateTimeStr);

        // Extract weekdays and until date
        int weekdaysStart = repeatsIndex + 9;  // After " repeats "
        int weekdaysEnd = input.indexOf(" until ");
        String weekdays = input.substring(weekdaysStart, weekdaysEnd);
        String untilDate = input.substring(input.indexOf(" until ") + 7);

        // Calculate days between
        LocalDateTime untilDateTime = LocalDateTime.parse(untilDate);
        int daysBetween = java.time.Period.between(
                startDateTime.toLocalDate(),
                untilDateTime.toLocalDate()
        ).getDays();

        Set<DayOfWeek> daysToCount = mapLettersToDays(weekdays.split("\\s+"));
        int count = countMatchingDays(startDateTime, untilDateTime, daysToCount);

        // Create the event with actual times
        Event.EventBuilder builder = Event.getBuilder()
                .setSubject(subject)
                .setStartDateTime(startDateTime)
                .setEndDateTime(endDateTime)
                // Separate weekDays
                .setWeekDays(weekdays.split("\\s+"))
                .setRepeatCount(count);

        // Create the event
        return createEventHelper(builder).getSubject();
      }
    }

    // Original format handling
    String[] userDetails = input.split("\\s*create event\\s*|\\s*from\\s*|\\s*to\\s*"
            + "|\\s*repeats\\s*|\\s*for\\s*|\\s*times\\s*");

    String userSubject = userDetails[1];
    String userStartTime = userDetails[2];
    LocalDate tempEndTime = LocalDateTime.parse(userStartTime).toLocalDate();
    String userEndTime = tempEndTime.atTime(17, 0).toString();
    if (userDetails.length == 4) {
      userEndTime = userDetails[3];
    }
    int userRepeat = 0;
    String userStatus = null;
    String userLocation = null;


    try {
      String weekdays = userDetails[4];
      Set<DayOfWeek> daysToRepeat = mapLettersToDays(weekdays.split("\\s+"));
      userRepeat = Integer.valueOf(userDetails[5]) * daysToRepeat.size();
    } catch (Exception e) {
      userRepeat = 0;
    }

    try {
      userStatus = userDetails[5];
    } catch (Exception e) {
      userStatus = null;
    }

    try {
      userLocation = userDetails[6];
    } catch (Exception e) {
      userLocation = null;
    }
    Event.EventBuilder builder = Event.getBuilder()
            .setSubject(userSubject)
            .setStartDateTime(LocalDateTime.parse(userStartTime))
            .setEndDateTime(LocalDateTime.parse(userEndTime))
            .setRepeatCount(userRepeat);

    return createEventHelper(builder).getSubject();
  }

  /**
   * Creates a new calendar based on user input.
   *
   * @return the created calendar
   * @throws IllegalArgumentException if the input format is invalid
   */
  public Calendar createCalendar(String input) {

    // Remove "create calendar "
    String remaining = input.substring("create calendar ".length());

    // Find the last space to separate timezone from calendar name
    int lastSpaceIndex = remaining.lastIndexOf(" ");

    ZoneId userTimeZone = ZoneId.of(remaining.
            substring(lastSpaceIndex + 1));
    String userCalendarName = remaining.substring(0, lastSpaceIndex);

    return calendarManager.createCalendar(userCalendarName, userTimeZone);
  }

  /**
   * edit a new calendar based on user input.
   *
   * @return the created calendar
   * @throws IllegalArgumentException if the input format is invalid
   */
  public Calendar editCalendar(String input) {

    // Remove "edit calendar "
    String remaining = input.substring("edit calendar ".length());

    // If editing field timezone
    if (input.contains("timezone")) {
      int lastSpaceIndex = remaining.lastIndexOf(" ");
      String newtimeZone = remaining.substring(lastSpaceIndex + 1);
      String userCalendarName = remaining.substring(0, lastSpaceIndex);
      return calendarManager.editCalendar(userCalendarName, "timezone", newtimeZone);
    } else if (input.contains("name")) {
      String[] details = remaining.split("\\s*name\\s*");
      String userCalendarName = details[0];
      String newValue = details[1];
      return calendarManager.editCalendar(userCalendarName, "name", newValue);
    }
    return null;
  }

  public Calendar useCalendar(String input) {

    String calendarName = input.substring("use calendar ".length());
    currentCalendar = calendarManager.useCalendar(calendarName);

    if (currentCalendar == null) {
      throw new IllegalArgumentException("Calendar not found");
    }

    return currentCalendar;
  }

  private EventModel getActiveModel() {
    if (currentCalendar == null) {
      throw new IllegalStateException("No calendar is currently in use.");
    }
    return currentCalendar.getEventModel();
  }

  public Calendar getCurrentCalendar() {
    if (currentCalendar == null) {
      throw new IllegalStateException("No calendar is currently in use.");
    }
    return currentCalendar;
  }

  /**
   * Handle copy event commands based on user input
   *
   * @param input user input command
   * @throws IllegalArgumentException if the input format is invalid
   */
  public void copyEvent(String input) {
    if (input.startsWith("copy event ")) {
      // Format: copy event [eventName] from [startDate] to [calendarName] at [newStartDate]
      String[] parts = input.split("\\s*from\\s*|\\s*to\\s*|\\s*at\\s*");
      if (parts.length != 4) {
        throw new IllegalArgumentException("Invalid format for copy event command");
      }
      String eventName = parts[0].substring("copy event ".length());
      String startDate = parts[1];
      String calendarName = parts[2];
      String newStartDate = parts[3];

      calendarManager.copyEvent(eventName, startDate, calendarName, newStartDate);

    } else if (input.startsWith("copy events on ")) {
      // Format: copy events on [startDate] to [calendarName] at [newStartDate]
      String[] parts = input.split("\\s*on\\s*|\\s*to\\s*|\\s*at\\s*");
      if (parts.length != 4) {
        throw new IllegalArgumentException("Invalid format for copy events on command");
      }
      String startDate = parts[1];
      String calendarName = parts[2];
      String newStartDate = parts[3];

      calendarManager.copyEventsOn(startDate, calendarName, newStartDate);

    } else if (input.startsWith("copy events between ")) {
      // Format: copy events between [startDate] and [endDate] to [calendarName] at [newStartDate]
      String[] parts = input.split("\\s*between\\s*|\\s*and\\s*|\\s*to\\s*|\\s*at\\s*");
      if (parts.length != 5) {
        throw new IllegalArgumentException("Invalid format for copy events between command");
      }
      String startDate = parts[1];
      String endDate = parts[2];
      String calendarName = parts[3];
      String newStartDate = parts[4];

      calendarManager.copyEventsBetween(startDate, endDate, calendarName, newStartDate);
    } else {
      throw new IllegalArgumentException("Invalid copy command format");
    }
  }

  protected Set<DayOfWeek> mapLettersToDays(String[] dayLetters) {
    Set<DayOfWeek> days = new HashSet<>();
    for (String letter : dayLetters) {
      switch (letter.toUpperCase()) {
        case "M":
          days.add(DayOfWeek.MONDAY);
          break;
        case "T":
          days.add(DayOfWeek.TUESDAY);
          break;
        case "W":
          days.add(DayOfWeek.WEDNESDAY);
          break;
        case "R":
          days.add(DayOfWeek.THURSDAY);
          break;// some use R for Thursday
        case "F":
          days.add(DayOfWeek.FRIDAY);
          break;
        case "S":
          days.add(DayOfWeek.SATURDAY);
          break;// or add SUNDAY too
        case "U":
          days.add(DayOfWeek.SUNDAY);
          break;
      }
    }
    return days;
  }

  protected int countMatchingDays(LocalDateTime start, LocalDateTime end, Set<DayOfWeek> targetDays) {
    LocalDate current = start.toLocalDate();
    LocalDate endDate = end.toLocalDate();
    int count = 0;

    while (!current.isAfter(endDate)) {
      if (targetDays.contains(current.getDayOfWeek())) {
        count++;
      }
      current = current.plusDays(1);
    }

    return count;
  }

  protected int countRepeatsBetween(LocalDate startDate, LocalDate endDate, Set<DayOfWeek> repeatDays) {
    int count = 0;
    for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
      if (repeatDays.contains(date.getDayOfWeek())) {
        count++;
      }
    }
    return count;
  }

  protected void createGui() {
    new CalendarGUI();
  }

  public String mockView() {
    return view.printEvent("controller to view");
  }

}