import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.ZoneId;
import java.util.List;

import calendar.model.Calendar;
import calendar.controller.EventController;
import calendar.model.Location;
import calendar.model.Status;
import calendar.model.Event;

/**
 * Test class for the EventController class.
 */
public class EventControllerTest {
  private EventController controller;
  private LocalDateTime startTime;
  private LocalDateTime endTime;
  private StringWriter output;

  @Before
  public void setUp() {
    // Create a new ByteArrayOutputStream to capture the output
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    // Store the original System.out
    PrintStream originalOut = System.out;
    // Set the output to the ByteArrayOutputStream
    System.setOut(new PrintStream(outContent));
    // Create a new StringWriter to capture the output
    output = new StringWriter();
    controller = new EventController(new StringReader(""), new PrintStream(outContent));
    startTime = LocalDateTime.of(2024, 3, 20, 14, 30);
    endTime = LocalDateTime.of(2024, 3, 20, 15, 30);
    LocalDateTime startTime2 = LocalDateTime.of(2024, 4, 23, 14, 30);
    LocalDateTime endTime2 = LocalDateTime.of(2024, 4, 23, 15, 30);
  }

  void provideInput(String data) {
    controller = new EventController(new StringReader(data),
            new PrintStream(new ByteArrayOutputStream()));
  }

  @Test
  public void testCreateEvents() {
    provideInput("create calendar Main Calendar America/Chicago\n" +
            "use Calendar Main Calendar\n" +
            "create event Test Event from 2024-03-20T14:30 to 2024-03-20T15:30\n" +
            "create event AnotherEvent from 2025-04-20T08:00 to 2025-04-20T09:30 " +
            "repeats M W F until 2025-04-28T09:30\nexit");
    controller.controllerHelper();
    //after checking real life calendar, checked that the number of repeating events for the
    //event series should be 5 times + the standalone event created
    assertEquals(6, controller.getCurrentCalendar()
            .getEventModel().getEvents().size());
  }

  @Test
  public void testEditEventsMultiple() {
    provideInput("create calendar Main Calendar America/Chicago\n" +
            "use Calendar Main Calendar\n" +
            "create event Test Event from 2024-03-20T14:30 to 2024-03-20T15:30\n" +
            "create event Test Event from 2024-03-20T14:30 to 2025-04-20T09:30\n" +
            "edit events status Test Event from 2024-03-20T14:30 with private\nexit");
    controller.controllerHelper();

    assertEquals(Status.PRIVATE, controller.getCurrentCalendar().getEventModel()
            .getEvents().get(0).getStatus());
    assertEquals(Status.PRIVATE, controller.getCurrentCalendar().getEventModel()
            .getEvents().get(1).getStatus());
  }

  @Test
  public void testCreateEventsUntil() {
    provideInput("create calendar Main Calendar America/Chicago\n" +
            "use Calendar Main Calendar\n" +
            "create event AnotherEvent from 2025-04-20T08:00 to 2025-04-20T09:30 " +
            "repeats M W F until 2025-04-28T09:30\nexit");
    controller.controllerHelper();
    assertEquals(5, controller.getCurrentCalendar()
            .getEventModel().getEvents().size());
  }

  @Test
  public void testCreateEventAllDay() {
    provideInput("create calendar Main Calendar America/Chicago\n" +
            "use Calendar Main Calendar\n" +
            "create event Test Event from 2024-03-20T14:30\nexit");
    controller.controllerHelper();
    assertEquals(1, controller.getCurrentCalendar()
            .getEventModel().getEvents().size());

    assertEquals(LocalDateTime.of(2024, 3, 20, 17, 0),
            controller.getCurrentCalendar().getEventModel().getEvents().get(0).getEndDateTime());
  }

  @Test
  public void testCreateEventsMultipleCalendars() {
    provideInput("create event Test Event from 2024-03-20T14:30 to 2024-03-20T15:30 " +
            "repeats M W F for 3 times\n" +
            "create event AnotherEvent from 2025-04-20T08:00 to 2025-04-20T09:30 " +
            "repeats M W F until 2025-04-28T09:30\nexit");
    controller.createCalendar("create Calendar Main Calendar America/Chicago");
    controller.createCalendar("create Calendar Mac Calendar America/Chicago");
    controller.useCalendar("use Calendar Main Calendar");
    controller.controllerHelper();
    assertEquals(15, controller.getCurrentCalendar()
            .getEventModel().getEvents().size());
  }


  @Test
  public void testCreateEventBasic() {
    provideInput("create event AnotherEvent from 2025-04-20T08:00 to 2025-04-20T09:30\nexit");
    controller.createCalendar("create Calendar Main Calendar America/Chicago");
    controller.useCalendar("use Calendar Main Calendar");
    controller.controllerHelper();
    assertEquals(1, controller.getCurrentCalendar()
            .getEventModel().getEvents().size());
  }

  @Test
  public void testPrintEvent() {
    // testing the input format of print events on <dateString>
    provideInput("create event AnotherEvent from 2025-04-20T08:00 to 2025-04-20T09:30\n" +
            "print events on 2025-04-20\nexit");
    controller.createCalendar("create Calendar Main Calendar America/Chicago");
    controller.useCalendar("use Calendar Main Calendar");
    controller.controllerHelper();
    assertEquals("* AnotherEvent on 2025-04-20",
            controller.printEvent("print events on 2025-04-20"));
  }

  @Test
  public void testPrintEvents() {
    // testing the input format of
    // print events from <dateStringTtimeString> to <dateStringTtimeString>
    provideInput("create event AnotherEvent from 2025-04-20T08:00 to 2025-04-20T09:30\n" +
            "print events from 2025-04-20T08:00 to 2025-04-20T09:30\nexit");
    controller.createCalendar("create Calendar Main Calendar America/Chicago");
    controller.useCalendar("use Calendar Main Calendar");
    controller.controllerHelper();
    assertEquals("* AnotherEvent between 2025-04-20T08:00 and 2025-04-20T09:30",
            controller.printEvent("print events from 2025-04-20T08:00 to 2025-04-20T09:30"));
  }

  @Test
  public void testPrintStatus() {
    // testing the input format of
    // print events from <dateStringTtimeString> to <dateStringTtimeString>
    provideInput("create event AnotherEvent from 2025-04-20T08:00 to 2025-04-20T09:30\n" +
            "show status on 2024-03-20T14:30\nexit");
    controller.createCalendar("create Calendar Main Calendar America/Chicago");
    controller.useCalendar("use Calendar Main Calendar");
    controller.controllerHelper();
    assertEquals("available", controller.printEvent("show status on 2024-03-20T14:30"));
    assertEquals("busy", controller.printEvent("show status on 2025-04-20T08:00"));

  }

  @Test
  public void testEditEvent() {
    provideInput("create event AnotherEvent from 2025-04-20T08:00 to 2025-04-20T09:30\n" +
            "edit event subject AnotherEvent from 2025-04-20T08:00 to 2025-04-20T09:30 " +
            "with Event2\nexit");
    controller.createCalendar("create Calendar Main Calendar America/Chicago");
    controller.useCalendar("use Calendar Main Calendar");
    controller.controllerHelper();
    assertEquals("Event2", controller.getCurrentCalendar()
            .getEventModel().getEvents().get(0).getSubject());
  }

  @Test
  public void testEditEventStatus() {
    provideInput("create event AnotherEvent from 2025-04-20T08:00 to 2025-04-20T09:30\n" +
            "edit event status AnotherEvent from 2025-04-20T08:00 to 2025-04-20T09:30 " +
            "with private\nexit");
    controller.createCalendar("create Calendar Main Calendar America/Chicago");
    controller.useCalendar("use Calendar Main Calendar");
    controller.controllerHelper();
    assertEquals(Status.PRIVATE, controller.getCurrentCalendar()
            .getEventModel().getEvents().get(0).getStatus());
  }

  @Test
  public void testEditEvents() {
    provideInput("create event AnotherEvent from 2025-04-20T08:00 to 2025-04-20T09:30\n" +
            "create event AnotherEvent from 2025-04-21T08:00 to 2025-04-21T09:30\n" +
            "edit events status AnotherEvent from 2025-04-20T07:00 with private\nexit");
    controller.createCalendar("create Calendar Main Calendar America/Chicago");
    controller.useCalendar("use Calendar Main Calendar");
    controller.controllerHelper();
    assertEquals(Status.PRIVATE, controller.getCurrentCalendar()
            .getEventModel().getEvents().get(0).getStatus());
    assertEquals(Status.PRIVATE, controller.getCurrentCalendar()
            .getEventModel().getEvents().get(1).getStatus());
  }

  @Test
  public void testEditSeries() {
    provideInput("create event AnotherEvent from 2025-04-20T08:00 to 2025-04-20T09:30 " +
            "repeats M until 2025-04-27T09:30\n" +
            "edit series status AnotherEvent from 2024-03-20T14:00 with private\nexit");
    controller.createCalendar("create Calendar Main Calendar America/Chicago");
    controller.useCalendar("use Calendar Main Calendar");
    controller.controllerHelper();

    assertEquals(2, controller.getCurrentCalendar()
            .getEventModel().getEvents().size());
    assertEquals("2025-04-21T08:00", controller.getCurrentCalendar()
            .getEventModel().getEvents().get(1).getStartDateTime().toString());
    assertEquals("2025-04-21T09:30", controller.getCurrentCalendar()
            .getEventModel().getEvents().get(1).getEndDateTime().toString());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateEventWithNullSubject() {
    Event.EventBuilder builder = Event.getBuilder()
            .setDescription("Test Description")
            .setStartDateTime(startTime)
            .setEndDateTime(endTime)
            .setLocation(Location.PHYSICAL)
            .setStatus(Status.PUBLIC);
    controller.createEventHelper(builder);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateEventWithNullStartTime() {
    Event.EventBuilder builder = Event.getBuilder()
            .setSubject("Test Event")
            .setDescription("Test Description")
            .setEndDateTime(endTime)
            .setLocation(Location.PHYSICAL)
            .setStatus(Status.PUBLIC);
    controller.createEventHelper(builder);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateEventWithEndBeforeStart() {
    Event.EventBuilder builder = Event.getBuilder()
            .setSubject("Test Event")
            .setDescription("Test Description")
            .setStartDateTime(endTime)  // Using endTime as start time
            .setEndDateTime(startTime)  // Using startTime as end time
            .setLocation(Location.PHYSICAL)
            .setStatus(Status.PUBLIC);
    controller.createEventHelper(builder);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditEventWithNullProperty() {
    provideInput("edit event Test Event from 2024-03-20T14:30 to 2024-03-20T15:30 " +
            "with New Value");
    controller.createCalendar("create Calendar Main Calendar America/Chicago");
    controller.useCalendar("use Calendar Main Calendar");
    controller.editEvent("edit event Test Event from 2024-03-20T14:30 to 2024-03-20T15:30 " +
            "with New Value");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditEventWithNullSubject() {
    provideInput("edit event subject null from 2024-03-20T14:30 to 2024-03-20T15:30 " +
            "with New Value");
    controller.createCalendar("create Calendar Main Calendar America/Chicago");
    controller.useCalendar("use Calendar Main Calendar");
    controller.editEvent("edit event subject null from 2024-03-20T14:30 to 2024-03-20T15:30 " +
            "with New Value");
  }


  @Test(expected = IllegalArgumentException.class)
  public void testPrintEventWithInvalidFormat() {
    provideInput("print invalid format");
    controller.printEvent("");
  }


  @Test
  public void testEditCalendarName() {
    provideInput("edit calendar Main Calendar name New Calendar\nexit");
    controller.createCalendar("create Calendar Main Calendar America/Chicago");
    controller.useCalendar("use Calendar Main Calendar");
    controller.controllerHelper();

    assertEquals("New Calendar", controller.calendarManager.getCalendars().get(0).getName());
  }


  @Test
  public void testEditCalendarTimezone() {
    provideInput("edit calendar Main Calendar timezone Australia/Sydney\nexit");
    controller.createCalendar("create Calendar Main Calendar America/Chicago");
    controller.useCalendar("use Calendar Main Calendar");
    controller.controllerHelper();

    assertEquals(ZoneId.of("Australia/Sydney"), controller.calendarManager.getCalendars()
            .get(0).getTimezone());
  }

  @Test
  public void testCopyEvent() {
    // Create two calendars with different timezones
    controller.calendarManager.createCalendar("Test Calendar", ZoneId.of("America/New_York"));
    controller.calendarManager.createCalendar("Test Calendar2", ZoneId.of("Asia/Tokyo"));
    controller.calendarManager.useCalendar("Test Calendar");

    // Create an event in the first calendar
    LocalDateTime eventStartTime = LocalDateTime.parse("2024-03-20T14:30:00");
    LocalDateTime eventEndTime = LocalDateTime.parse("2024-03-20T15:30:00");
    controller.calendarManager.useCalendar("Test Calendar").getEventModel()
            .createEvent("Test Event", "Test Description", eventStartTime, eventEndTime,
                    Location.PHYSICAL, Status.PUBLIC, null, 0);

    // Copy the event to the second calendar
    String copyCommand = "copy event Test Event from 2024-03-20T14:30:00 to Test Calendar2 " +
            "at 2024-03-21T14:30:00";
    controller.copyEvent(copyCommand);

    // Switch to the second calendar, verify the event was copied with correct timezone conversion
    Calendar targetCalendar = controller.calendarManager.useCalendar("Test Calendar2");
    List<Event> events = targetCalendar.getEventModel().getEvents();
    assertEquals(1, events.size());

    // Verify event details
    Event copiedEvent = events.get(0);
    assertEquals("Test Event", copiedEvent.getSubject());
    assertEquals("Test Description", copiedEvent.getDescription());
    assertEquals(Location.PHYSICAL, copiedEvent.getLocation());
    assertEquals(Status.PUBLIC, copiedEvent.getStatus());

    // Verify timezone conversion (NY to Tokyo is +13 hours)
    assertEquals(LocalDateTime.parse("2024-03-22T03:30:00"), copiedEvent.getStartDateTime());
    assertEquals(LocalDateTime.parse("2024-03-22T04:30:00"), copiedEvent.getEndDateTime());
  }

  @Test
  public void testCopyEventsOn() {
    controller.calendarManager.createCalendar("Test Calendar", ZoneId.of("America/New_York"));
    controller.calendarManager.createCalendar("Test Calendar2", ZoneId.of("America/Los_Angeles"));

    // Set source calendar and create events
    Calendar sourceCalendar = controller.calendarManager.useCalendar("Test Calendar");

    // Create multiple events on the same day
    LocalDateTime event1StartTime = LocalDateTime.parse("2024-03-20T10:00:00");
    LocalDateTime event1EndTime = LocalDateTime.parse("2024-03-20T11:00:00");
    LocalDateTime event2StartTime = LocalDateTime.parse("2024-03-20T14:00:00");
    LocalDateTime event2EndTime = LocalDateTime.parse("2024-03-20T15:00:00");

    // Create events in source calendar
    sourceCalendar.getEventModel()
            .createEvent("Event1", "", event1StartTime, event1EndTime,
                    null, null, null, 0);
    sourceCalendar.getEventModel()
            .createEvent("Event2", "", event2StartTime, event2EndTime,
                    null, null, null, 0);

    // Verify events were created in source calendar
    List<Event> sourceEvents = sourceCalendar.getEventModel().getEvents();
    assertEquals(2, sourceEvents.size());

    // Make sure we're using the source calendar when copying
    controller.calendarManager.useCalendar("Test Calendar");

    // Copy all events on that day to the second calendar
    String copyCommand = "copy events on 2024-03-20T00:00:00 to Test Calendar2 " +
            "at 2024-03-21T00:00:00";
    controller.copyEvent(copyCommand);

    // Switch to the second calendar and verify the events were copied
    Calendar targetCalendar = controller.calendarManager.useCalendar("Test Calendar2");
    List<Event> events = targetCalendar.getEventModel().getEvents();

    assertEquals(2, events.size());
    assertEquals("Event1", events.get(0).getSubject());
    assertEquals(LocalDateTime.parse("2024-03-21T07:00:00"), events.get(0).getStartDateTime());
    assertEquals("Event2", events.get(1).getSubject());
    assertEquals(LocalDateTime.parse("2024-03-21T11:00:00"), events.get(1).getStartDateTime());
  }

//  @Test
//  public void testEventSeries() {
//    provideInput("create event AnotherEvent from 2025-04-20T08:00 to 2025-04-20T09:30 " +
//            "repeats M W until 2025-04-29T09:30\nexit");
//    controller.createCalendar("create Calendar Main Calendar America/Chicago");
//    controller.useCalendar("use Calendar Main Calendar");
//    controller.controllerHelper();
//
//    assertEquals(2, controller.getCurrentCalendar()
//            .getEventModel().getEvents().size());
//  }

  @Test
  public void testCopyEventsBetween() {
    // Create two calendars with different timezones
    controller.calendarManager.createCalendar("Test Calendar", ZoneId.of("America/New_York"));
    controller.calendarManager.createCalendar("Test Calendar2", ZoneId.of("America/Los_Angeles"));
    controller.calendarManager.useCalendar("Test Calendar");

    // Create events on different days
    LocalDateTime event1StartTime = LocalDateTime.parse("2024-03-20T10:00:00");
    LocalDateTime event1EndTime = LocalDateTime.parse("2024-03-20T11:00:00");
    LocalDateTime event2StartTime = LocalDateTime.parse("2024-03-21T14:00:00");
    LocalDateTime event2EndTime = LocalDateTime.parse("2024-03-21T15:00:00");
    LocalDateTime event3StartTime = LocalDateTime.parse("2024-03-22T14:00:00");
    LocalDateTime event3EndTime = LocalDateTime.parse("2024-03-22T15:00:00");

    controller.calendarManager.useCalendar("Test Calendar").getEventModel()
            .createEvent("Event1", "", event1StartTime, event1EndTime,
                    null, null, null, 0);
    controller.calendarManager.useCalendar("Test Calendar").getEventModel()
            .createEvent("Event2", "", event2StartTime, event2EndTime,
                    null, null, null, 0);
    controller.calendarManager.useCalendar("Test Calendar").getEventModel()
            .createEvent("Event3", "", event3StartTime, event3EndTime,
                    null, null, null, 0);

    // Copy events between March 20 and March 21 to the second calendar
    String copyCommand = "copy events between 2024-03-20T00:00:00 and 2024-03-21T23:59:59 to " +
            "Test Calendar2 at 2024-03-25T00:00:00";
    controller.copyEvent(copyCommand);

    // Switch to the second calendar and verify the events were copied
    Calendar targetCalendar = controller.calendarManager.useCalendar("Test Calendar2");
    List<Event> events = targetCalendar.getEventModel().getEvents();
    assertEquals(2, events.size());
    assertEquals("Event1", events.get(0).getSubject());
    assertEquals(LocalDateTime.parse("2024-03-25T07:00:00"), events.get(0).getStartDateTime());
    assertEquals("Event2", events.get(1).getSubject());
    assertEquals(LocalDateTime.parse("2024-03-26T11:00:00"), events.get(1).getStartDateTime());
  }

  @Test
  public void testCopyEvents() {
    provideInput("copy event AnotherEvent from 2025-04-20T08:00 to Test Calendar2 at " +
            "2024-03-21T14:30:00\nexit");
    controller.createCalendar("create Calendar Main Calendar America/Chicago");
    controller.createCalendar("create Calendar Test Calendar2 America/Chicago");
    controller.useCalendar("use Calendar Main Calendar");
    controller.createEvent("create event AnotherEvent from 2025-04-20T08:00 to 2025-04-20T09:30");
    controller.controllerHelper();

    assertEquals(1, controller.calendarManager.getCalendars().get(1).getEventModel()
            .getEvents().size());

  }


  /**
   * Test invalid copy command format
   */
  @Test(expected = IllegalArgumentException.class)
  public void testInvalidCopyCommand() {
    controller.copyEvent("copy invalid command");
  }

  @Test
  public void testGui() {
    provideInput(" \nexit");
    controller.controllerHelper();
  }

  @Test
  public void mockView() {
    provideInput("mock view\nexit");
    controller.controllerHelper();
  }
}
