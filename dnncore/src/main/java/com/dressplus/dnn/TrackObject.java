package com.dressplus.dnn;

import java.util.ArrayList;
import java.util.List;

public class TrackObject {
    public float peak;

    public boolean lost = false;
    private int lost_count = 0;
    public String type;
    public float probability = 0;
    public float left, top, width, height;

    public String getName() {
        return type;
    }

    private static String[] labels = {"laptop",
            "可口可乐",
            "cup",
            "phone",
            "chair",
            "glass",
            "clock",
            "tv",
            "bottle"};

    public static List<TrackObject> segment(String str, String seg, int dir) {
        final int len_threshold = 6;
        final int NAME = 1, PROB = 2, LEFT = 3, TOP = 4, WIDTH = 5, HEIGHT = 6;
        List<TrackObject> result = new ArrayList<TrackObject>();
        String[] strs = str.split(seg);
        for (String sub_str : strs) {
            String[] tmp = sub_str.split(",");
            if (tmp.length > len_threshold) {
                TrackObject obj = new TrackObject();
                if (isObjectLabelRight(tmp[NAME], labels)) {
                    obj.probability = Float.valueOf(tmp[PROB]);
                    obj.left = Float.valueOf(tmp[LEFT]);
                    obj.top = Float.valueOf(tmp[TOP]);
                    obj.width = Float.valueOf(tmp[WIDTH]);
                    obj.height = Float.valueOf(tmp[HEIGHT]);
                    obj.type = tmp[NAME];
                    result.add(obj);
                }
            }
        }

        return result;
    }

    public void update(String str) {
        final int PEAK = 1, LEFT = 2, TOP = 3, WIDTH = 4, HEIGHT = 5;
        final float PEAK_THRESHOLD = 0.3f;
        final int LOST_NUM = 10;
        String[] tmp = str.split(",");
        peak = Float.valueOf(tmp[PEAK]);
        if (peak < PEAK_THRESHOLD) {
            lost_count++;
        }
        if (lost_count > LOST_NUM) {
            lost = true;
            lost_count = 0;
        }
        left = Float.valueOf(tmp[LEFT]);
        top = Float.valueOf(tmp[TOP]);
        width = Float.valueOf(tmp[WIDTH]);
        height = Float.valueOf(tmp[HEIGHT]);
    }

    private static boolean isObjectLabelRight(String label, String... labels) {
        if (labels == null) {
            return true;
        }
        for (String target : labels) {
            if (label.equals(target)) {
                return true;
            }
        }
        return false;
    }

    public void update(int[] rect, int screen_width, int screen_height) {
        left = (float) rect[0] / screen_width;
        top = (float) rect[1] / screen_height;
        width = (float) rect[2] / screen_width;
        height = (float) rect[3] / screen_height;
    }
}
