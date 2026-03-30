package org.webswing.server.api.services.swinginstance.holder.impl;

import org.webswing.server.api.services.swinginstance.holder.SwingInstanceHolder;
import org.webswing.server.api.services.swinginstance.holder.SwingInstanceHolderFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class DefaultSwingInstanceHolderFactoryImpl implements SwingInstanceHolderFactory {

  @Inject
  public DefaultSwingInstanceHolderFactoryImpl() {}

  @Override
  public SwingInstanceHolder createInstanceHolder() {
    return new DefaultSwingInstanceHolderImpl();
  }
}
