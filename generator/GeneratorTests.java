import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.module.ModuleDescriptor;
import java.net.URI;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GeneratorTests {

  @BeforeEach
  void beforeEach() {
    Logger.getLogger(Generator.class.getName()).setLevel(Level.WARNING);
  }

  @Test
  void explicitModule() {
    assertEquals("ice.cream", describe("explicit-module.jar").name());
  }

  @Test
  void automaticModuleNamePresent() {
    assertEquals("org.apiguardian.api", describe("automatic-module-name-present.jar").name());
  }

  @Test
  void automaticModuleNameNotPresent() {
    var jar = "automatic-module-name-not-present.jar";
    assertThrows(AssertionError.class, () -> describe(jar));
    assertEquals("automatic.module.name.not.present", describe(jar, false).name());
  }

  @Test
  void metaInfNotPresent() {
    var jar = "automatic-metainf-not-present.jar";
    assertThrows(AssertionError.class, () -> describe(jar));
    assertEquals("automatic.metainf.not.present", describe(jar, false).name());
  }

  @Test
  void scanSimplePom() {
    var pom = "automatic-module-name-present.pom";
    var map = new Generator().mapPom(load(pom));
    assertAll(
        "scan " + pom,
        () -> assertEquals("org.apiguardian:apiguardian-api", map.get("name")),
        () -> assertEquals("org.apiguardian", map.get("group")),
        () -> assertEquals("apiguardian-api", map.get("artifact")));
  }

  @Test
  void scanPomWithParent() {
    var pom = "pom-with-parent.pom";
    var map = new Generator().mapPom(load(pom));
    assertAll(
        "scan " + pom,
        () -> assertEquals("BAZ of foo.bar", map.get("name")),
        () -> assertEquals("foo.bar", map.get("group")),
        () -> assertEquals("baz", map.get("artifact")));
  }

  private ModuleDescriptor describe(String jar) {
    return describe(jar, true);
  }

  private ModuleDescriptor describe(String jar, boolean reportFileNameBasedModuleAsEmpty) {
    return new Generator()
        .describeModule(Paths.get(load(jar)), reportFileNameBasedModuleAsEmpty)
        .orElseThrow(AssertionError::new);
  }

  private URI load(String name) {
    try {
      var resource = getClass().getClassLoader().getResource("test/" + name);
      if (resource == null) {
        throw new IllegalArgumentException("resource not found: " + name);
      }
      return resource.toURI();
    } catch (Exception e) {
      throw new AssertionError(e);
    }
  }
}