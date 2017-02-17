package org.lwjglb.engine.loaders.md5;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MD5Hierarchy {

    private List<MD5HierarchyData> hierarchyDataList;

    public List<MD5HierarchyData> getHierarchyDataList() {
        return hierarchyDataList;
    }

    public void setHierarchyDataList(List<MD5HierarchyData> hierarchyDataList) {
        this.hierarchyDataList = hierarchyDataList;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("hierarchy [" + System.lineSeparator());
        for (MD5HierarchyData hierarchyData : hierarchyDataList) {
            str.append(hierarchyData).append(System.lineSeparator());
        }
        str.append("]").append(System.lineSeparator());
        return str.toString();
    }

    public static MD5Hierarchy parse(List<String> blockBody) {
        MD5Hierarchy result = new MD5Hierarchy();
        List<MD5HierarchyData> hierarchyDataList = new ArrayList<>();
        result.setHierarchyDataList(hierarchyDataList);
        for (String line : blockBody) {
            MD5HierarchyData hierarchyData = MD5HierarchyData.parseLine(line);
            if (hierarchyData != null) {
                hierarchyDataList.add(hierarchyData);
            }
        }
        return result;
    }

    public static class MD5HierarchyData {

        private static final Pattern PATTERN_HIERARCHY = Pattern.compile("\\s*\\\"([^\\\"]+)\\\"\\s*([-]?\\d+)\\s*(\\d+)\\s*(\\d+).*");

        private String name;

        private int parentIndex;

        private int flags;

        private int startIndex;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getParentIndex() {
            return parentIndex;
        }

        public void setParentIndex(int parentIndex) {
            this.parentIndex = parentIndex;
        }

        public int getFlags() {
            return flags;
        }

        public void setFlags(int flags) {
            this.flags = flags;
        }

        public int getStartIndex() {
            return startIndex;
        }

        public void setStartIndex(int startIndex) {
            this.startIndex = startIndex;
        }

        @Override
        public String toString() {
            return "[name: " + name + ", parentIndex: " + parentIndex + ", flags: " + flags + ", startIndex: " + startIndex + "]";
        }

        public static MD5HierarchyData parseLine(String line) {
            MD5HierarchyData result = null;
            Matcher matcher = PATTERN_HIERARCHY.matcher(line);
            if (matcher.matches()) {
                result = new MD5HierarchyData();
                result.setName(matcher.group(1));
                result.setParentIndex(Integer.parseInt(matcher.group(2)));
                result.setFlags(Integer.parseInt(matcher.group(3)));
                result.setStartIndex(Integer.parseInt(matcher.group(4)));
            }
            return result;
        }

    }

}
