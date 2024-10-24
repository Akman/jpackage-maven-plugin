/*
  Copyright (C) 2020 - 2024 Alexander Kapitman

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

module hello {

  // picocli
  requires info.picocli;
  opens ru.akman.hello to info.picocli;

  // slf4j + logback
  requires org.slf4j;
  requires java.naming;
  requires ch.qos.logback.classic;

  // javafx
  requires javafx.graphics;
  exports ru.akman.hello to javafx.graphics;

}
