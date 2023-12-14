package core.io

import java.nio.file.Path

trait TestResourceLoader:
  def testResource(path: String): Path = Path.of(getClass.getClassLoader.getResource(path).toURI())
