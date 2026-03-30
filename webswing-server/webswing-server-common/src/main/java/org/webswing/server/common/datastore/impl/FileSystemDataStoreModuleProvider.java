package org.webswing.server.common.datastore.impl;

import java.util.Arrays;
import java.util.List;

import org.webswing.server.common.datastore.WebswingDataStoreModuleProvider;

public class FileSystemDataStoreModuleProvider implements WebswingDataStoreModuleProvider {

  @Override
  public List<String> getDataStoreModuleClassNames() {
    return Arrays.asList(FileSystemDataStoreModule.class.getCanonicalName());
  }

}
