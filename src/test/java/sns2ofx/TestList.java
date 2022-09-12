package sns2ofx;

import java.util.ArrayList;
import java.util.List;

public class TestList {

  public static void main(String[] args) {
    List<String> list = new ArrayList<>();

    // list.set(1, "new value");

//example ..

    list.add("one");
    list.add("two");
    list.add("three");
    System.out.println(list); // [one,two,three]
    list.set(1, "new");
    System.out.println(list); // [one,new,three]
  }
}
