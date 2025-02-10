package mini_java;

import java.util.HashMap;

public class Class_ {
  final String name;
  Class_ extends_;
  final HashMap<String, Method> methods;
  final HashMap<String, Attribute> attributes;

  Class_(String name) {
    this.name = name;
    this.extends_ = null;
    this.methods = new HashMap<>();
    this.attributes = new HashMap<>();
  }
}
