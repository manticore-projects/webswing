package com.sun.glass.ui.web;

import org.webswing.javafx.toolkit.WebClipboardDelegate;
import org.webswing.javafx.toolkit.WebMenuBarDelegate;
import org.webswing.javafx.toolkit.WebMenuDelegate;
import org.webswing.javafx.toolkit.WebMenuItemDelegate;
import org.webswing.javafx.toolkit.WebsinwgFxToolkitFactory;

import com.sun.glass.ui.Application;
import com.sun.glass.ui.Menu;
import com.sun.glass.ui.MenuBar;
import com.sun.glass.ui.MenuItem;
import com.sun.glass.ui.PlatformFactory;
import com.sun.glass.ui.delegate.ClipboardDelegate;
import com.sun.glass.ui.delegate.MenuBarDelegate;
import com.sun.glass.ui.delegate.MenuDelegate;
import com.sun.glass.ui.delegate.MenuItemDelegate;

public class WebPlatformFactory extends PlatformFactory {

  @Override
  public Application createApplication() {
    return WebsinwgFxToolkitFactory.getFactory().createApplication();
  }

  @Override
  public MenuBarDelegate createMenuBarDelegate(MenuBar menubar) {
    return new WebMenuBarDelegate();
  }

  @Override
  public MenuDelegate createMenuDelegate(Menu menu) {
    return new WebMenuDelegate();
  }

  @Override
  public MenuItemDelegate createMenuItemDelegate(MenuItem menuItem) {
    return new WebMenuItemDelegate();
  }

  @Override
  public ClipboardDelegate createClipboardDelegate() {
    return new WebClipboardDelegate();
  }
}
