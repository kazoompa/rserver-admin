/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.rserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

public class Resources {

  private static final Logger log = LoggerFactory.getLogger(Resources.class);

  private static final int RSERVE_DEFAULT_PORT = 6311;

  private static final String RSERVE_DEFAULT_ENCODING = "native";

  @SuppressWarnings("StaticNonFinalField")
  private static Map<String, String> conf;

  @SuppressWarnings("StaticNonFinalField")
  private static File rServerHomeFile;

  private Resources() {}

  public static File getRServerHomeDir() {
    if(rServerHomeFile == null) {
      if(System.getenv().containsKey("RSERVER_HOME")) {
        rServerHomeFile = new File(System.getenv("RSERVER_HOME"));
      } else if(System.getProperties().containsKey("RSERVER_HOME")) {
        rServerHomeFile = new File(System.getProperty("RSERVER_HOME"));
      } else {
        throw new IllegalStateException("Cannot find RSERVER_HOME environment variable or system property");
      }
    }
    return rServerHomeFile;
  }

  public static File getRservConfFile() {
    return new File(getRServerHomeDir(), "conf" + File.separator + "Rserv.conf");
  }

  public static Map<String, String> getRservConf() {
    if (conf != null) return conf;

    conf = Maps.newHashMap();

    try(BufferedReader br = new BufferedReader(new FileReader(getRservConfFile()))) {
      putEntries(br, conf);
    } catch(IOException e) {
      log.error("Failed reading Rserv.conf file", e);
    }

    return conf;
  }

  public static int getRservePort() {
    if (getRservConf().containsKey("port")) {
      return Integer.parseInt(getRservConf().get("port"));
    } else {
      return RSERVE_DEFAULT_PORT;
    }
  }

  public static String getRserveEncoding() {
    if (getRservConf().containsKey("encoding")) {
      return getRservConf().get("encoding");
    } else {
      return RSERVE_DEFAULT_ENCODING;
    }
  }

  public static Map<String,String> getUsernamePasswords(String pwdfile) {
    Map<String, String> entries = Maps.newHashMap();

    try(BufferedReader br = new BufferedReader(new FileReader(new File(pwdfile)))) {
      putEntries(br, entries);
    } catch(IOException e) {
      log.error("Failed reading password file: " + pwdfile, e);
    }

    return entries;
  }

  private static void putEntries(BufferedReader br, Map<String, String> entries) throws IOException {
    for(String line; (line = br.readLine()) != null; ) {
      String lineStr = line.trim();
      if(!lineStr.startsWith("#")) {
        String[] entry = lineStr.split(" ");
        if(entry.length >= 2) {
          entries.put(entry[0].trim(), entry[1].trim());
        }
      }
    }
  }

}
