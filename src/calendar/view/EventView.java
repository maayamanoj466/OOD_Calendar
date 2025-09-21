package calendar.view;

import calendar.model.Event;

import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.Scanner;

/**
 * View class for the calendar event management system.
 */
public class EventView {
  private final PrintStream out;

  /**
   * Constructor for EventView.
   *
   * @param out output stream
   */
  public EventView(PrintStream out) {
    this.out = out;
  }

  /**
   * Prints an error message to the output stream.
   *
   * @param message the error message to be printed
   */
  public void printError(String message) {
    out.println("Error: " + message);
  }

  /**
   * returns the string that is given.
   * Used in the controller to call the model's output and produce the printed string.
   *
   * @param finalOutput prints finalOutput
   */

  public String printEvent(String finalOutput) {
    System.out.println(finalOutput);
    return finalOutput;
  }
}