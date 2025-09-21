package calendar.view;

import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import calendar.model.CalendarManager;

public class CalendarGUI {
  private final JFrame mainFrame;
  private final JLabel month;
  private YearMonth currentMonth;
  private final JPanel calendarPanel;
  private final CalendarManager mainCalendarManager;



  public CalendarGUI() {
    mainFrame = new JFrame("Calendar App");
    mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    mainFrame.setSize(500, 500);
    mainFrame.setLayout(new BorderLayout());

    mainCalendarManager = new CalendarManager();
    mainCalendarManager.createCalendar("Main Calendar", ZoneId.systemDefault());
    mainCalendarManager.useCalendar("Main Calendar");

    currentMonth = YearMonth.now();

    JPanel topPanel = new JPanel();
    JButton prevButton = new JButton("<");
    JButton nextButton = new JButton(">");
    JButton viewEvents = new JButton("view events");
    month = new JLabel();
    topPanel.add(prevButton);
    topPanel.add(month);
    topPanel.add(nextButton);
    topPanel.add(viewEvents);

    mainFrame.add(topPanel, BorderLayout.NORTH);

    calendarPanel = new JPanel();
    mainFrame.add(calendarPanel, BorderLayout.CENTER);

    prevButton.addActionListener(e -> changeMonth(-1));
    nextButton.addActionListener(e -> changeMonth(1));
    viewEvents.addActionListener(e -> viewEvents());

    updateCalendar();
    mainFrame.setVisible(true);
  }

  private void changeMonth(int offset) {
    currentMonth = currentMonth.plusMonths(offset);
    updateCalendar();
  }

  private void updateCalendar() {
    calendarPanel.removeAll();
    calendarPanel.setLayout(new GridLayout(0, 7));
    month.setText(currentMonth.getMonth() + " " + currentMonth.getYear());

    for (int day = 1; day <= currentMonth.lengthOfMonth(); day++) {
      LocalDate date = currentMonth.atDay(day);
      JButton dayButton = new JButton(String.valueOf(day));
      dayButton.addActionListener(e -> createEvents(date));
      calendarPanel.add(dayButton);
    }

    mainFrame.revalidate();
    mainFrame.repaint();
  }

  private void createEvents(LocalDate date) {

    String createEvent = JOptionPane.showInputDialog(mainFrame,
            "Create Event (Format:<name> from <time> to <time>): ");
    if (createEvent != null && !createEvent.trim().isEmpty()) {
      String[] details = createEvent.split("\\s*from\\s*|\\s*to\\s*");

      //initialising event name, start time and end time
      String eventName = details[0];
      LocalTime startTime = LocalTime.parse(details[1]);
      LocalTime endTime = LocalTime.of(17, 0);
      if (details.length == 3) {
        endTime = LocalTime.parse(details[2]);
      }

      //converting start and end times to dateTimes
      LocalDateTime startDateTime = date.atTime(startTime);
      LocalDateTime endDateTime = date.atTime(endTime);

      mainCalendarManager.getCalendarInUse().getEventModel().
              createEvent(eventName, null, startDateTime, endDateTime, null, null, null, 0);

    }
  }

  private void viewEvents() {
    List<calendar.model.Event> eventsToBeViewed = new ArrayList<>();
    String viewingEvents = JOptionPane.showInputDialog(mainFrame, "Input a starting date: ");
    if (viewingEvents != null && !viewingEvents.trim().isEmpty()) {
      eventsToBeViewed = mainCalendarManager.getCalendarInUse().getEventModel().eventsToView(viewingEvents);
    }

    String viewEvents = eventsToString(eventsToBeViewed);
    JOptionPane.showMessageDialog(mainFrame, viewEvents);
  }

  private String eventsToString(List<calendar.model.Event> events) {
    String viewEventsResult = "";
    for (calendar.model.Event event : events) {
      viewEventsResult = viewEventsResult + event.getSubject() + " at "
              + event.getStartDateTime().toString() +
              "\n";
    }

    return viewEventsResult;
  }



  public static void main(String[] args) {
    /*
      Runs the GUI asynchronously without blocking.
     */
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        new CalendarGUI();
      }
    });
  }
}
