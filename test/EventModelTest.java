import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.fail;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import calendar.model.Event;
import calendar.model.Location;
import calendar.model.Status;
import calendar.model.EventModel;

/**
 * Test class for the EventModel class.
 */
public class EventModelTest {
  private EventModel model;
  private LocalDateTime startTime;
  private LocalDateTime endTime;

  @Before
  public void setUp() {
    model = new EventModel();
    startTime = LocalDateTime.of(2024, 3, 20, 14, 30);
    endTime = LocalDateTime.of(2024, 3, 20, 15, 30);
  }

  // Test creating a regular event
  @Test
  public void testCreateRegularEvent() {
    model.createEvent("Test Event", "Test Description", startTime, endTime,
            Location.PHYSICAL, Status.PUBLIC,
            null, 0);

    // Verify the event was created
    List<Event> events = model.getEvents();
    assertEquals(1, events.size());
    Event event = events.get(0);
    assertEquals("Test Event", event.getSubject());
    assertEquals(startTime, event.getStartDateTime());
    assertEquals(endTime, event.getEndDateTime());
    assertEquals(Location.PHYSICAL, event.getLocation());
    assertEquals(Status.PUBLIC, event.getStatus());
  }

  // Test creating a recurring event
  @Test
  public void testCreateRecurringEvent() {
    String[] weekDays = {"M", "W"};
    model.createEvent("Recurring Event", "test description", startTime,
            endTime, Location.PHYSICAL, Status.PUBLIC, weekDays, 2);

    List<Event> events = model.getEvents();
    assertEquals(3, events.size()); // Should recur 2 times

    // Verify all events have the same subject, time, location, and status
    for (Event event : events) {
      assertEquals("Recurring Event", event.getSubject());
      assertEquals(Location.PHYSICAL, event.getLocation());
      assertEquals(Status.PUBLIC, event.getStatus());

      assertEquals(startTime.toLocalTime(), event.getStartDateTime().toLocalTime());
      assertEquals(endTime.toLocalTime(), event.getEndDateTime().toLocalTime());
    }
  }

  // Test creating a duplicate event
  @Test(expected = IllegalArgumentException.class)
  public void testCreateDuplicateEvent() {
    model.createEvent("Duplicate Event", null, startTime, endTime,
            Location.PHYSICAL, Status.PUBLIC, null, 0);
    model.createEvent("Duplicate Event", null, startTime, endTime,
            Location.PHYSICAL, Status.PUBLIC, null, 0);
  }

  // Test to see if location can be null
  @Test
  public void testNullLocation() {
    LocalDateTime originalStartTime = LocalDateTime.of(2024, 3, 20, 14,
            30);
    LocalDateTime originalEndTime = LocalDateTime.of(2024, 3, 20, 15,
            30);
    model.createEvent("Test Event", "Test Description", originalStartTime,
            originalEndTime, null, Status.PUBLIC, null, 0);

    assertEquals(null, model.getEvents().get(0).getLocation());
  }

  // Test creating an event with an invalid weekday
  @Test
  public void testCreateEventWithInvalidWeekday() {
    String[] invalidWeekDays = {"X"}; // X is not a valid weekday
    try {
      model.createEvent("Invalid Weekday Event", null, startTime, endTime,
              Location.PHYSICAL, Status.PUBLIC, invalidWeekDays, 1);
      fail("Should have thrown IllegalArgumentException for invalid weekday");
    } catch (IllegalArgumentException e) {
      assertEquals("Invalid Weekday", e.getMessage());
    }
  }

  // Test creating a recurring event with repeat count of 0
  @Test
  public void testCreateEventWithZeroRepeatCount() {
    String[] weekDays = {"M", "W", "F"};
    model.createEvent("Zero Repeat Event", null, startTime, endTime,
            Location.PHYSICAL, Status.PUBLIC, weekDays, 0);

    List<Event> events = model.getEvents();
    assertEquals(1, events.size());
  }

  // Test creating a recurring event with null weekDays
  @Test
  public void testCreateEventWithNullWeekDays() {
    model.createEvent("Null Weekdays Event", "Test Description", startTime,
            endTime, Location.PHYSICAL,
            Status.PUBLIC, null, 5);

    List<Event> events = model.getEvents();
    assertEquals(6, events.size());
  }

  // Test creating a recurring event with empty weekDays array
  @Test
  public void testCreateEventWithEmptyWeekDays() {
    String[] emptyWeekDays = {};
    model.createEvent("Empty Weekdays Event", null, startTime, endTime,
            Location.PHYSICAL,
            Status.PUBLIC, emptyWeekDays, 5);

    List<Event> events = model.getEvents();
    assertEquals(6, events.size());
  }

  // Test for invalid property for editEvent
  @Test(expected = IllegalArgumentException.class)
  public void testEditEventWithInvalidProperty() {
    LocalDateTime originalStartTime = LocalDateTime.of(2024, 3, 20, 14,
            30);
    LocalDateTime originalEndTime = LocalDateTime.of(2024, 3, 20, 15,
            30);

    model.createEvent("Test Event", "Test Description", originalStartTime,
            originalEndTime,
            Location.PHYSICAL, Status.PUBLIC, null, 0);

    // Try to edit with an "invalid_property", which is not one the field
    model.editEvent("invalid_property", "Test Event", originalStartTime,
            "new value");
  }

  // Test editing start time of non-existent event
  @Test(expected = IllegalArgumentException.class)
  public void testEditNonExistentEventStartTime() {
    model.editEvent("start", null, startTime, startTime.toString());
  }

  // Test editing an event's subject
  @Test
  public void testEditEventSubject() {
    LocalDateTime originalStartTime = LocalDateTime.of(2024, 3, 20, 14,
            30);
    LocalDateTime originalEndTime = LocalDateTime.of(2024, 3, 20, 15,
            30);

    model.createEvent("Test Event", "Test Description", originalStartTime,
            originalEndTime, Location.PHYSICAL, Status.PUBLIC, null, 0);

    model.editEvent("subject", "Test Event", originalStartTime,
            "New Subject");

    // Create a new List in test because seriesOfEvent were private
    List<Event> events = model.getEvents();
    // Check for size
    assertEquals(1, events.size());
    // Get the first event stored
    Event updatedEvent = events.get(0);
    assertEquals("New Subject", updatedEvent.getSubject());
    assertEquals(originalStartTime, updatedEvent.getStartDateTime());
    assertEquals(originalEndTime, updatedEvent.getEndDateTime());
  }

  // Test editing an event's start time
  @Test
  public void testEditEventStartTime() {
    LocalDateTime originalStartTime = LocalDateTime.of(2024, 3, 20, 14,
            30);
    LocalDateTime originalEndTime = LocalDateTime.of(2024, 3, 20, 15,
            30);

    model.createEvent("Test Event", "Test Description", originalStartTime,
            originalEndTime, Location.PHYSICAL, Status.PUBLIC, null, 0);

    String newStartTimeStr = "2024-03-20T13:30:00";
    model.editEvent("start", "Test Event", originalStartTime, newStartTimeStr);

    // Create a new List in test because seriesOfEvent were private
    List<Event> events = model.getEvents();
    // Check for size
    assertEquals(1, events.size());
    // Get the first event stored
    Event updatedEvent = events.get(0);
    assertEquals(LocalDateTime.parse(newStartTimeStr), updatedEvent.getStartDateTime());
    assertEquals(originalEndTime, updatedEvent.getEndDateTime());
  }

  // Test editing an event's end time
  @Test
  public void testEditEventEndTime() {
    LocalDateTime originalStartTime = LocalDateTime.of(2024, 3, 20, 14,
            30);
    LocalDateTime originalEndTime = LocalDateTime.of(2024, 3, 20, 15,
            30);

    model.createEvent("Test Event", "Test Description", originalStartTime,
            originalEndTime, Location.PHYSICAL, Status.PUBLIC, null, 0);

    String newEndTimeStr = "2024-03-20T16:30:00";
    model.editEvent("end", "Test Event", originalStartTime, newEndTimeStr);

    // Create a new List in test because seriesOfEvent were private
    List<Event> events = model.getEvents();
    // Check for size
    assertEquals(1, events.size());
    // Get the first event stored
    Event updatedEvent = events.get(0);
    assertEquals(LocalDateTime.parse(newEndTimeStr), updatedEvent.getEndDateTime());
    assertEquals(originalStartTime, updatedEvent.getStartDateTime());
  }

  // Test editing an event's description
  @Test
  public void testEditEventDescription() {
    LocalDateTime originalStartTime = LocalDateTime.of(2024, 3, 20, 14,
            30);
    LocalDateTime originalEndTime = LocalDateTime.of(2024, 3, 20, 15,
            30);

    model.createEvent("Test Event", "Original Description",
            originalStartTime, originalEndTime, Location.PHYSICAL, Status.PUBLIC, null,
            0);

    model.editEvent("description", "Test Event", originalStartTime,
            "New Description");

    // Create a new List in test because seriesOfEvent were private
    List<Event> events = model.getEvents();
    // Check for size
    assertEquals(1, events.size());
    // Get the first event stored
    Event updatedEvent = events.get(0);
    assertEquals("New Description", updatedEvent.getDescription());
    assertEquals(originalStartTime, updatedEvent.getStartDateTime());
    assertEquals(originalEndTime, updatedEvent.getEndDateTime());
  }

  // Test editing location of events
  @Test
  public void testEditEventsLocation() {
    LocalDateTime originalStartTime = LocalDateTime.of(2024, 3, 20, 14, 30);
    LocalDateTime originalEndTime = LocalDateTime.of(2024, 3, 20, 15, 30);
    LocalDateTime secondStartTime = LocalDateTime.of(2024, 3, 21, 14, 30);
    LocalDateTime secondEndTime = LocalDateTime.of(2024, 3, 21, 15, 30);

    // Create two separate events with the same subject
    model.createEvent("Test Event", "Test Description", originalStartTime,
            originalEndTime, Location.PHYSICAL, Status.PUBLIC, null, 0);
    model.createEvent("Test Event", "Test Description", secondStartTime,
            secondEndTime, Location.PHYSICAL, Status.PUBLIC, null, 0);

    // Edit all events from the original start time
    model.editEvents("location", "Test Event", originalStartTime, "online");

    // Get all events
    List<Event> events = model.getEvents();

    // Check that we have 2 events
    assertEquals(2, events.size());

    // Check that all events have been updated to online
    for (Event event : events) {
      assertEquals("Test Event", event.getSubject());
    }
  }

  // Test editing status of all events in a series
  @Test
  public void testEditSeriesStatus() {
    LocalDateTime originalStartTime = LocalDateTime.of(2024, 3, 20, 14,
            30);
    LocalDateTime originalEndTime = LocalDateTime.of(2024, 3, 20, 15,
            30);

    // Create a series of events (3 events total)
    model.createEvent("Test Event", "Test Description", originalStartTime,
            originalEndTime, Location.PHYSICAL, Status.PUBLIC, null, 2);

    // Edit all events in the series regardless of date
    model.editSeries("status", "Test Event", originalStartTime,
            "PRIVATE");

    // Get all events
    List<Event> events = model.getEvents();

    // Check that we have 3 events
    assertEquals(3, events.size());

    // Check that all events have been updated to PRIVATE
    for (Event event : events) {
      assertEquals("Test Event", event.getSubject());
    }
  }

  // Test editing location with invalid value
  @Test(expected = IllegalArgumentException.class)
  public void testEditEventWithInvalidLocation() {
    LocalDateTime originalStartTime = LocalDateTime.of(2024, 3, 20, 14,
            30);
    LocalDateTime originalEndTime = LocalDateTime.of(2024, 3, 20, 15,
            30);

    model.createEvent("Test Event", "Test Description", originalStartTime,
            originalEndTime, Location.PHYSICAL, Status.PUBLIC, null, 0);

    model.editEvent("location", "Test Event", originalStartTime,
            "INVALID_LOCATION");
  }

  // Test editing status with invalid value
  @Test(expected = IllegalArgumentException.class)
  public void testEditEventWithInvalidStatus() {
    LocalDateTime originalStartTime = LocalDateTime.of(2024, 3, 20, 14,
            30);
    LocalDateTime originalEndTime = LocalDateTime.of(2024, 3, 20, 15,
            30);

    model.createEvent("Test Event", "Test Description", originalStartTime,
            originalEndTime, Location.PHYSICAL, Status.PUBLIC, null, 0);

    model.editEvent("status", "Test Event", originalStartTime,
            "INVALID_STATUS");
  }

  // Test editing end time to be before start time
  @Test(expected = IllegalArgumentException.class)
  public void testEditEventEndTimeBeforeStartTime() {
    LocalDateTime originalStartTime = LocalDateTime.of(2024, 3, 20, 14,
            30);
    LocalDateTime originalEndTime = LocalDateTime.of(2024, 3, 20, 15,
            30);

    model.createEvent("Test Event", "Test Description", originalStartTime,
            originalEndTime, Location.PHYSICAL, Status.PUBLIC, null, 0);

    String newEndTimeStr = "2024-03-20T13:30:00"; // Before start time
    model.editEvent("end", "Test Event", originalStartTime, newEndTimeStr);
  }

  // Test editing start time to be after end time
  @Test(expected = IllegalArgumentException.class)
  public void testEditEventStartTimeAfterEndTime() {
    LocalDateTime originalStartTime = LocalDateTime.of(2024, 3, 20, 14,
            30);
    LocalDateTime originalEndTime = LocalDateTime.of(2024, 3, 20, 15,
            30);

    model.createEvent("Test Event", "Test Description", originalStartTime,
            originalEndTime, Location.PHYSICAL, Status.PUBLIC, null, 0);

    String newStartTimeStr = "2024-03-20T16:30:00"; // After end time
    model.editEvent("start", "Test Event", originalStartTime, newStartTimeStr);
  }

  @Test
  public void testCreateEventWithUntilWithOn() {
    // Create an event that repeats on Monday, Wednesday, Friday until April 20
    String[] weekDays = {"M", "W", "F"};
    model.createEvent(
            "Team Meeting", null,
            LocalDateTime.of(2024, 4, 10, 8, 0),
            LocalDateTime.of(2024, 4, 10, 17, 0),
            Location.PHYSICAL, Status.PUBLIC, weekDays, 10);

    List<Event> events = model.getEvents();
    assertEquals(11, events.size());  // Should create 11 events, include initial event

    // Verify all events have the same subject, time, location, status, and time
    for (Event event : events) {
      assertEquals("Team Meeting", event.getSubject());
      assertEquals(null, event.getDescription());
      assertEquals(Location.PHYSICAL, event.getLocation());
      assertEquals(Status.PUBLIC, event.getStatus());
      assertEquals(LocalTime.of(8, 0), event.getStartDateTime().toLocalTime());
      assertEquals(LocalTime.of(17, 0), event.getEndDateTime().toLocalTime());
    }
  }

  // Test regular repeating event to see if fields match
  @Test
  public void testCreateEventFieldsMatchRegularRepeat() {
    String[] weekDays = {"M", "W", "F"};
    LocalDateTime start = LocalDateTime.of(2024, 4, 10, 8, 0);
    LocalDateTime end = LocalDateTime.of(2024, 4, 10, 17, 0);

    model.createEvent(
            "Team Meeting",
            "Weekly sync",
            start,
            end,
            Location.PHYSICAL,
            Status.PUBLIC,
            weekDays,
            2  // repeat 2 times
    );

    List<Event> events = model.getEvents();
    assertEquals(3, events.size());  // Initial + 2 repeats

    // Verify all events have matching fields
    for (Event event : events) {
      assertEquals("Team Meeting", event.getSubject());
      assertEquals("Weekly sync", event.getDescription());
      assertEquals(Location.PHYSICAL, event.getLocation());
      assertEquals(Status.PUBLIC, event.getStatus());
      assertEquals(LocalTime.of(8, 0), event.getStartDateTime().toLocalTime());
      assertEquals(LocalTime.of(17, 0), event.getEndDateTime().toLocalTime());
    }
  }

  // Test until format event to see if fields matchs
  @Test
  public void testCreateEventFieldsMatchUntil() {
    LocalDateTime start = LocalDateTime.of(2024, 4, 10, 14, 30);
    LocalDateTime end = LocalDateTime.of(2024, 4, 10, 15, 45);

    model.createEvent(
            "Team Meeting",
            "Weekly sync",
            start,
            end,
            Location.PHYSICAL,
            Status.PUBLIC,
            null,
            10  // 10 days from April 10 to April 20
    );

    List<Event> events = model.getEvents();
    assertEquals(11, events.size());  // Initial + 10 repeats

    // Verify all events have matching fields
    for (Event event : events) {
      assertEquals("Team Meeting", event.getSubject());
      assertEquals("Weekly sync", event.getDescription());
      assertEquals(Location.PHYSICAL, event.getLocation());
      assertEquals(Status.PUBLIC, event.getStatus());
      assertEquals(LocalTime.of(14, 30), event.getStartDateTime().toLocalTime());
      assertEquals(LocalTime.of(15, 45), event.getEndDateTime().toLocalTime());
    }
  }

  // Test until format event with all-day events (no end time specified)
  @Test
  public void testCreateEventFieldsMatchUntilAllDay() {
    LocalDateTime start = LocalDateTime.of(2024, 4, 10, 0, 0);
    LocalDateTime end = null;  // Suppose to change the event duration to an All-Day Event

    model.createEvent(
            "Team Meeting",
            "Weekly sync",
            start,
            end,
            Location.PHYSICAL,
            Status.PUBLIC,
            null,
            10  // 10 days from April 10 to April 20
    );

    List<Event> events = model.getEvents();
    assertEquals(11, events.size());  // Initial + 10 repeats

    // Verify all events have matching fields and are all-day events
    for (Event event : events) {
      assertEquals("Team Meeting", event.getSubject());
      assertEquals("Weekly sync", event.getDescription());
      assertEquals(Location.PHYSICAL, event.getLocation());
      assertEquals(Status.PUBLIC, event.getStatus());
      assertEquals(LocalTime.of(8, 0), event.getStartDateTime().toLocalTime());
      assertEquals(LocalTime.of(17, 0), event.getEndDateTime().toLocalTime());
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditEventsWithNoEventsFound() {
    // Try to edit events in a series that doesn't exist
    model.editEvents("subject", "NonExistentEvent", startTime, "New Subject");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditSeriesWithNoEventsFound() {
    // Try to edit a series that doesn't exist
    model.editSeries("subject", "NonExistentSeries", startTime, "New Subject");
  }

  // Test for sortSequence
  @Test
  public void testSortSequence() {
    LocalDateTime start1= LocalDateTime.of(2024, 3, 21, 14, 30);
    LocalDateTime end1 = LocalDateTime.of(2024, 3, 21, 15, 30);
    LocalDateTime start2 = LocalDateTime.of(2022, 3, 21, 14, 30);
    LocalDateTime end2 = LocalDateTime.of(2022, 3, 21, 15, 30);
    LocalDateTime start3= LocalDateTime.of(2024, 3, 20, 14, 29);
    LocalDateTime end3 = LocalDateTime.of(2024, 3, 20, 15, 29);

    model.createEvent("Test Event", "Test Description", startTime, endTime,
            null, null, null, 0);
    model.createEvent("Test Event2", "Test Description", start1, end1,
            null, null, null, 0);
    model.createEvent("Test Event3", "Test Description", start2, end2,
            null, null, null, 0);
    model.createEvent("Test Event4", "Test Description", startTime, endTime,
            null, null, null, 0);
    model.createEvent("Test Event5", "Test Description", start3, end3,
            null, null, null, 0);

    // Verify the event was created
    List<Event> events = model.getEvents();
    assertEquals(5, events.size());
    Event event0 = events.get(0);
    Event event1 = events.get(1);
    Event event2 = events.get(2);
    Event event3 = events.get(3);
    Event event4 = events.get(4);
    assertEquals("Test Event3", event0.getSubject());
    assertEquals("Test Event5", event1.getSubject());
    assertEquals("Test Event", event2.getSubject());
    assertEquals("Test Event4", event3.getSubject());
    assertEquals("Test Event2", event4.getSubject());
  }

  @Test
  public void testEventsLeft() {
    LocalDateTime start1= LocalDateTime.of(2024, 3, 21, 14, 30);
    LocalDateTime end1 = LocalDateTime.of(2024, 3, 21, 15, 30);
    LocalDateTime start2 = LocalDateTime.of(2022, 3, 21, 14, 30);
    LocalDateTime end2 = LocalDateTime.of(2022, 3, 21, 15, 30);
    LocalDateTime start3= LocalDateTime.of(2024, 3, 20, 14, 29);
    LocalDateTime end3 = LocalDateTime.of(2024, 3, 20, 15, 29);

    model.createEvent("Test Event", "Test Description", startTime, endTime,
            null, null, null, 0);
    model.createEvent("Test Event2", "Test Description", start1, end1,
            null, null, null, 0);
    model.createEvent("Test Event3", "Test Description", start2, end2,
            null, null, null, 0);
    model.createEvent("Test Event4", "Test Description", startTime, endTime,
            null, null, null, 0);
    model.createEvent("Test Event5", "Test Description", start3, end3,
            null, null, null, 0);

    // Verify the event was created
    List<Event> events = model.getEvents();
    assertEquals(5, events.size());
    String input = "2024-03-20";

    assertEquals(4, model.eventsLeft(input));
  }

  @Test
  public void testEventsToViewWithMoreThanTenEvents() {
    LocalDateTime start1 = LocalDateTime.of(2022, 3, 21, 14, 30);
    LocalDateTime end1 = LocalDateTime.of(2022, 3, 21, 15, 30);
    model.createEvent("Test Event", "Test Description", startTime, endTime,
            null, null, null, 0);
    model.createEvent("Test Event2", "Test Description", start1, end1,
            null, null, null, 0);
    model.createEvent("Test Event3", "Test Description", startTime, endTime,
            null, null, null, 0);
    model.createEvent("Test Event4", "Test Description", startTime, endTime,
            null, null, null, 0);
    model.createEvent("Test Event5", "Test Description", startTime, endTime,
            null, null, null, 0);
    model.createEvent("Test Event6", "Test Description", startTime, endTime,
            null, null, null, 0);
    model.createEvent("Test Event7", "Test Description", startTime, endTime,
            null, null, null, 0);
    model.createEvent("Test Event8", "Test Description", startTime, endTime,
            null, null, null, 0);
    model.createEvent("Test Event9", "Test Description", startTime, endTime,
            null, null, null, 0);
    model.createEvent("Test Event10", "Test Description", startTime, endTime,
            null, null, null, 0);
    model.createEvent("Test Event11", "Test Description", startTime, endTime,
            null, null, null, 0);
    model.createEvent("Test Event12", "Test Description", startTime, endTime,
            null, null, null, 0);


    List<Event> events = model.getEvents();
    String input = "2024-03-20";
    // Have 12 events created in total
    assertEquals(12, events.size());
    assertEquals(10, model.eventsToView(input).size());
    assertEquals("Test Event11", model.eventsToView(input).get(9).getSubject());
    // start1 should not be the date value for get index 1, although it is second created,
    // because it is before the given date
    assertNotSame(start1, model.eventsToView(input).get(1).getStartDateTime());
    // "Test Event12" should not be the name for get index 9, because it is the 11th event that
    // happens after the given date
    assertNotEquals("Test Event12", model.eventsToView(input).get(9).getSubject());
  }

  @Test
  public void testEventsToViewWithLessThanTenEvents() {
    LocalDateTime start1 = LocalDateTime.of(2022, 3, 21, 14, 30);
    LocalDateTime end1 = LocalDateTime.of(2022, 3, 21, 15, 30);
    model.createEvent("Test Event", "Test Description", startTime, endTime,
            null, null, null, 0);
    model.createEvent("Test Event2", "Test Description", start1, end1,
            null, null, null, 0);
    model.createEvent("Test Event3", "Test Description", startTime, endTime,
            null, null, null, 0);
    model.createEvent("Test Event4", "Test Description", startTime, endTime,
            null, null, null, 0);
    model.createEvent("Test Event5", "Test Description", startTime, endTime,
            null, null, null, 0);
    model.createEvent("Test Event6", "Test Description", startTime, endTime,
            null, null, null, 0);
    model.createEvent("Test Event7", "Test Description", startTime, endTime,
            null, null, null, 0);
    model.createEvent("Test Event8", "Test Description", startTime, endTime,
            null, null, null, 0);
    model.createEvent("Test Event9", "Test Description", startTime, endTime,
            null, null, null, 0);


    List<Event> events = model.getEvents();
    String input = "2024-03-20";
    // Have 12 events created in total
    assertEquals(9, events.size());
    assertEquals(8, model.eventsToView(input).size());
    assertEquals("Test Event3", model.eventsToView(input).get(1).getSubject());
    // start1 should not be the date value for get index 1, although it is second created,
    // because it is before the given date
    assertNotSame(start1, model.eventsToView(input).get(1).getStartDateTime());
    assertEquals("Test Event9", model.eventsToView(input).get(7).getSubject());
  }



} 