package ca.concordia.ibank.ui;

/** What the controller needs from the window: changing screens and showing messages. */
public interface Navigator {

  void show(ScreenId screen);

  void showError(String message);

  void showInfo(String message);
}
