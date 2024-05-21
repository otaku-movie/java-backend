package com.example.backend.utils;

public class StringUtils {
  public static String join(Object[] list, String operator) {
    String str = "";
//    int i = 0;

//    while (i < list.length) {
//      str += list[i] + operator;
//    }
    for (int i = 0; i < list.length - 1; i++) {
      str += list[i] + operator;
    }
    return  list.length > 0 ? str + list[list.length - 1] : str;
  }
//  public static String join(Object[] array, char separator, int startIndex, int endIndex) {
//    if (array == null) {
//      return null;
//    } else {
//      int noOfItems = endIndex - startIndex;
//      if (noOfItems <= 0) {
//        return "";
//      } else {
//        StringBuilder buf = new StringBuilder(noOfItems);
//
//        for(int i = startIndex; i < endIndex; ++i) {
//          if (i > startIndex) {
//            buf.append(separator);
//          }
//
//          if (array[i] != null) {
//            buf.append(array[i]);
//          }
//        }
//
//        return buf.toString();
//      }
//    }
//  }
}
