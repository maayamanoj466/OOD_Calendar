package calendar.view;

import java.io.PrintStream;

public interface IEventView {

  /**
   * Prints an error message to the output stream.
   *
   * @param message the error message to be printed
   */
  public void printError(String message);

  /**
   * returns the string that is given.
   * Used in the controller to call the model's output and produce the printed string.
   *
   * @param finalOutput prints finalOutput
   */

  public String printEvent(String finalOutput);


}
