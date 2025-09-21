package calendar;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;

import javax.swing.*;

import calendar.controller.EventController;
import calendar.view.CalendarGUI;
import calendar.view.EventView;

/**
 * The main class that serves as the entry point for the Calendar application.
 */
public class MainMethod {
  /**
   * The main method that processes user input and executes calendar operations.
   *
   * @param args Command line arguments that takes either:
   *             --mode interactive: Run in interactive mode
   *             --mode headless filename: Run in headless mode with commands from file
   */
  public static void main(String[] args) {
    EventController controller;
    EventView view = new EventView(System.out);
    controller = new EventController(new InputStreamReader(System.in), System.out);

    if (args.length == 0) {
      controller.controllerHelper();
      return;
    }

    if (args[0].equals("--mode")) {
      if (args.length < 2) {
        view.printError("Invalid mode. Use 'interactive' or 'headless'");
        return;
      }

      if (args[1].equals("interactive")) {
        if (args.length > 2) {
          view.printError("Error: Interactive mode does not take additional arguments");
          return;
        }
        controller.controllerHelper();
      } else if (args[1].equals("headless")) {
        if (args.length < 3) {
          view.printError("Error: Headless mode requires a filename");
          return;
        }
        String filename = args[2];
        try {
          FileReader fileReader = new FileReader(filename);
          controller = new EventController(fileReader, System.out);
          controller.controllerHelper();
        } catch (FileNotFoundException e) {
          view.printError("Error: File not found: " + filename);
        }
      } else {
        view.printError("Error: Invalid mode. Use 'interactive' or 'headless'");
      }
    } else {
      view.printError("Error: Invalid arguments");
    }
  }
}
