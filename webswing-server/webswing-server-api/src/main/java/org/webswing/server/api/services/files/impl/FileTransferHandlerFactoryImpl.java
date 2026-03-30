package org.webswing.server.api.services.files.impl;

import org.webswing.server.api.services.application.AppPathHandler;
import org.webswing.server.api.services.files.FileTransferHandler;
import org.webswing.server.api.services.files.FileTransferHandlerFactory;

import com.google.inject.Singleton;

@Singleton
public class FileTransferHandlerFactoryImpl implements FileTransferHandlerFactory {

  public FileTransferHandler create(AppPathHandler manager) {
    return new FileTransferHandlerImpl(manager);
  }
}
