package org.joda.modulenames;

import static java.util.Comparator.comparing;

import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Single scan run summary. */
class Summary {

  /** Collection of suspicious modules. */
  class Suspicious {
    final List<Item> impostors = new ArrayList<>();
    final List<Item> naming = new ArrayList<>();
    final List<Item> syntax = new ArrayList<>();
  }

  /** Date and time. */
  final String timestamp = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss").format(new Date());

  /** Number of scanned lines. */
  long scanLineCounter = 0L;

  /** Number of scanned objects, i.e. csv files. */
  long scanObjectCounter = 0L;

  /** Number of scanned modules. */
  long scanModuleCounter = 0L;

  /** {@code modulescanner-report-2018_11_15_05_33_36.csv} or blank. */
  String lastProcessed = "";

  /** {@code modulescanner-report-2018_11_15_05_33_36.csv} or blank. */
  String firstProcessed = "";

  /** {@code modulescanner-report-2018_11_15_05_33_36.csv} or blank. */
  String startedAfter = "";

  /** Number of modules that were already well-known. */
  int startedWith = 0;

  /** New modules. */
  final Map<String, Item> uniques = new TreeMap<>();

  /** Updated modules. */
  final Map<String, Item> updates = new TreeMap<>();

  /** Suspicious or even plain invalid modules. */
  final Suspicious suspicious = new Suspicious();

  /** Defaults to volatile {@code target/workspace}. */
  Path workspace = Path.of("target", "workspace");

  List<String> toStrings() {
    return List.of(
        "Summary of " + timestamp,
        "",
        scanObjectCounter + " objects (`.csv` files) processed",
        scanLineCounter + " lines scanned",
        scanModuleCounter + " modules detected",
        "Started with " + startedWith + " well-known modules",
        "Started after file: " + startedAfter,
        "First processed file: " + firstProcessed,
        "Last processed file: " + lastProcessed,
        "",
        uniques.size() + " new modules found",
        updates.size() + " modules updated",
        "",
        suspicious.syntax.size() + " module names were syntactically invalid",
        suspicious.naming.size() + " module names didn't start with the Maven Group or an alias",
        suspicious.impostors.size() + " impostors detected");
  }

  List<String> toMarkdown() {
    var md = new ArrayList<String>();
    md.add("# Scan `" + timestamp + "`");
    md.add("");
    md.add("## Summary");
    md.add("");
    md.add("```");
    md.addAll(toStrings());
    md.add("```");
    md.add("");
    md.add("### " + uniques.size() + " new modules");
    uniques.values().forEach(it -> md.add("- `" + it.moduleName + "` -> " + it.line));
    md.add("");
    md.add("### " + updates.size() + " updated modules");
    updates.values().forEach(it -> md.add("- `" + it.moduleName + "` -> " + it.line));
    md.add("");
    md.add("## Suspicious Modules");
    md.add("");
    md.add("Modules listed below didn't make it into the `modules.properties` database.");
    md.add("");
    md.add("### Syntax Error (" + suspicious.syntax.size() + ")");
    md.add("");
    suspicious.syntax.sort(comparing(i -> i.moduleName));
    suspicious.syntax.forEach(it -> md.add("- `" + it.moduleName + "` -> " + it.line));
    md.add("");
    md.add("### Impostor (" + suspicious.impostors.size() + ")");
    md.add("");
    suspicious.impostors.sort(comparing(i -> i.moduleName));
    suspicious.impostors.forEach(it -> md.add("- `" + it.moduleName + "` -> " + it.line));
    md.add("");
    md.add("### Unexpected Naming (" + suspicious.naming.size() + ")");
    md.add("");
    suspicious.naming.sort(comparing(i -> i.moduleName));
    suspicious.naming.forEach(it -> md.add("- `" + it.moduleName + "` -> " + it.line));
    md.add("");
    return md;
  }
}
