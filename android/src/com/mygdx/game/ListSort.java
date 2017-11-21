package com.mygdx.game;


import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ListSort {

    public static String sortByASIIC(Map<String, String> map) {
        List<String> params = new ArrayList<String>();
        for (String key: map.keySet()) {
            String res = key + map.get(key);
            params.add(res);
        }


//        Collections.sort(params, Collator.getInstance(Locale.UK));
        Collections.sort(params);

//        Collections.sort(params, new Comparator<String>() {
//            @Override
//            public int compare(String lhs, String rhs) {
//                char left = lhs.charAt(0);
//                char right = rhs.charAt(0);
//
//                System.out.println("peiyin  " + left + "  " + right);
//                System.out.println("peiyin  " + (left>right));
//
//                return left > right ? 1:-1;
//            }
//        });

        StringBuilder sb = new StringBuilder();
        for (String item : params) {
            sb.append(item + "==");
        }
        return sb.toString();
    }
}