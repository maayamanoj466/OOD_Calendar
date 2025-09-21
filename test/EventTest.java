import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

import java.time.LocalDateTime;

import calendar.model.Location;
import calendar.model.Status;
import calendar.model.Event;

/**
 * Test class for the Event class.
 */
public class EventTest {
  private LocalDateTime startTime;
  private LocalDateTime endTime;

  // Set up base times for testing
  @Before
  public void setUp() {
    startTime = LocalDateTime.of(2024, 3, 20, 14, 30);
    endTime = LocalDateTime.of(2024, 3, 20, 15, 30);
  }

  // Test creating a regular event
  @Test
  public void testCreateRegularEvent() {
    Event event = Event.getBuilder()
            .setSubject("Test Event")
            .setStartDateTime(startTime)
            .setEndDateTime(endTime)
            .setLocation(Location.PHYSICAL)
            .setStatus(Status.PUBLIC)
            .build();

    assertEquals("Test Event", event.getSubject());
    assertEquals(startTime, event.getStartDateTime());
    assertEquals(endTime, event.getEndDateTime());
    assertEquals(Location.PHYSICAL, event.getLocation());
    assertEquals(Status.PUBLIC, event.getStatus());
  }

  // Test creating an event with null subject
  @Test(expected = IllegalArgumentException.class)
  public void testCreateEventWithNullSubject() {
    Event.getBuilder()
            .setSubject(null)
            .setStartDateTime(startTime)
            .setEndDateTime(endTime)
            .setLocation(Location.PHYSICAL)
            .setStatus(Status.PUBLIC)
            .build();
  }

  // Test creating an event with null start time
  @Test(expected = IllegalArgumentException.class)
  public void testCreateEventWithNullStartTime() {
    Event.getBuilder()
            .setSubject("Test Event")
            .setStartDateTime(null)
            .setEndDateTime(endTime)
            .setLocation(Location.PHYSICAL)
            .setStatus(Status.PUBLIC)
            .build();
  }

  // Test creating an event with end time before start time
  @Test(expected = IllegalArgumentException.class)
  public void testCreateEventWithEndTimeBeforeStartTime() {
    LocalDateTime earlierTime = startTime.minusHours(1);
    Event.getBuilder()
            .setSubject("Test Event")
            .setStartDateTime(startTime)
            .setEndDateTime(earlierTime)
            .setLocation(Location.PHYSICAL)
            .setStatus(Status.PUBLIC)
            .build();
  }

  // Test creating an event with null end time
  @Test
  public void testCreateEventWithNullEndTime() {

    Event event = Event.getBuilder()
            .setSubject("Test Event")
            .setStartDateTime(startTime)
            .setEndDateTime(null)
            .setLocation(Location.PHYSICAL)
            .setStatus(Status.PUBLIC)
            .build();

    // Verify it's an all-day event (8 AM to 5 PM)
    assertEquals(startTime.toLocalDate().atTime(8, 0), event.getStartDateTime());
    assertEquals(startTime.toLocalDate().atTime(17, 0), event.getEndDateTime());
  }
} 