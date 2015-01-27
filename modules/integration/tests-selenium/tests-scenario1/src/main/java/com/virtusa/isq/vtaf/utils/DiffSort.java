
package com.virtusa.isq.vtaf.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.lang.Math;

public class DiffSort {
//!\"#$%&\'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\]^_`abcdefghijklmnopqrstuvwxyz{|}~
    //private static String order = "RWQOJMVAHBSGZXNTCIEKUPDYFL";
    //private static String order = " !\"#$%&'()*+,-./0123456789:;<=>@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~";
   // private static String order = "~}|{zyxwvutsrqponmlkjihgfedcba`_^]\\[ZYXWVUTSRQPONMLKJIHGFEDCBA@>=<;:9876543210/.-,+*)('&%$#\"! ";
   // private static String order ="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    
    static boolean Inorder = true;
    // sort with comparator

    public static Comparator<String> diffNaturalOrder1Aa = new Comparator<String>() {
        private String order =" !\"#$%&'()*+,-./0123456789:;<=>@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~";
        public int compare(String v, String w) {
            int diff = 0, iter = 0;
            Integer index1, index2;
            Integer len1 = v.length();
            Integer len2 = w.length();
            int len = Math.min(len1, len2); // lesser of 2 strings

            for(int i=0; i<len; i++) {
                index1 = order.indexOf(v.charAt(i));
                index2 = order.indexOf(w.charAt(i));
                // if both chars are absent in order string, use natural ordering
          
                if(index1 == -1 && index2 == -1)
                    diff = new Character(v.charAt(i)).compareTo(new Character(w.charAt(i)));
                else if(index1 == -1 && index2 > 0)
                    diff = 1;
                else if(index1 > 0 && index2 == -1)
                    diff = -1;
                else
                    diff = index1.compareTo(index2);
                // break if we found mismatch
                if(diff != 0) break;
      
            }

            // return smaller string first in sort
            if(diff == 0)
                diff = len1.compareTo(len2);
            return diff;

        }
        
    };


    public static Comparator<String> diffNaturalOrderAa1 = new Comparator<String>() {
        private String order =" !\"#$%&'()*+,-./ABCDEFGHIJKLMNOPQRSTUVWXYZ:;<=>@abcdefghijklmnopqrstuvwxyz[\\]^_`0123456789{|}~";
        public int compare(String v, String w) {
            int diff = 0, iter = 0;
            Integer index1, index2;
            Integer len1 = v.length();
            Integer len2 = w.length();
            int len = Math.min(len1, len2); // lesser of 2 strings


            for(int i=0; i<len; i++) {
                index1 = order.indexOf(v.charAt(i));
                index2 = order.indexOf(w.charAt(i));
                // if both chars are absent in order string, use natural ordering
                
                
                if(index1 == -1 && index2 == -1)
                    diff = new Character(v.charAt(i)).compareTo(new Character(w.charAt(i)));
                else if(index1 == -1 && index2 > 0)
                    diff = 1;
                else if(index1 > 0 && index2 == -1)
                    diff = -1;
                else
                    diff = index1.compareTo(index2);
                // break if we found mismatch
                if(diff != 0) break;

               
            }

            // return smaller string first in sort
            if(diff == 0)
                diff = len1.compareTo(len2);
            return diff;

        }
        
    };

    
    public static Comparator<String> diffNaturalOrderaA1 = new Comparator<String>() {
        private String order =" !\"#$%&'()*+,-./abcdefghijklmnopqrstuvwxyz:;<=>@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`0123456789{|}~";
        public int compare(String v, String w) {
            int diff = 0, iter = 0;
            Integer index1, index2;
            Integer len1 = v.length();
            Integer len2 = w.length();
            int len = Math.min(len1, len2); // lesser of 2 strings
            
            
            for(int i=0; i<len; i++) {
                index1 = order.indexOf(v.charAt(i));
                index2 = order.indexOf(w.charAt(i));
                // if both chars are absent in order string, use natural ordering
                
                
                if(index1 == -1 && index2 == -1)
                    diff = new Character(v.charAt(i)).compareTo(new Character(w.charAt(i)));
                else if(index1 == -1 && index2 > 0)
                    diff = 1;
                else if(index1 > 0 && index2 == -1)
                    diff = -1;
                else
                    diff = index1.compareTo(index2);
                // break if we found mismatch
                if(diff != 0) break;

               
            }

            // return smaller string first in sort
            if(diff == 0)
                diff = len1.compareTo(len2);
            return diff;
        }
        
    };
    

    
    // test client
    public static void main(String[] args) {
   

    }
}
