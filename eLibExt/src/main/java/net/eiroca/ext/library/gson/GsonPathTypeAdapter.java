package net.eiroca.ext.library.gson;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class GsonPathTypeAdapter extends TypeAdapter<Path> {

  @Override
  public void write(JsonWriter out, Path value) throws IOException {
    if (value == null) {
      out.nullValue();
    }
    else {
      // Converts the Path to its string representation (using standard forward/backward slashes based on OS)
      out.value(value.toString());
    }
  }

  @Override
  public Path read(JsonReader in) throws IOException {
    if (in.peek() == JsonToken.NULL) {
      in.nextNull();
      return null;
    }
    // Converts the JSON string back into an OS-appropriate Path object
    return Paths.get(in.nextString());
  }
}