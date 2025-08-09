package app;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Categories {

    public HashMap<String, ArrayList<LinkedList<String>>> categories = new HashMap<>();
    private HashMap<String, ArrayList<TreeMap<String, Integer>>> counted = new HashMap<>();

    // category name: {  only:{}, ignore:{}, need:{}  }
    private static String[] categoryNames = {
            "targetTuples", "architecture", "vendor", "os", "environment",
            "pointerWidth", "wasm", "endian", "stage", "channel",
            "crossCompile", "compareMode", "coverage", "remote",
            "debugger", "debugAssert", "needFix", "llvm",
            "sanitizer", "capabilities", "addiDependency", "autoDiff",
            "other-only", "other-ignore", "other-need"  // uncategorized misc directives
    };
    private static HashMap<String, List<Pattern>> categoryPatterns = new HashMap<>();



    public Categories() {

        loadPatterns();

        for (String catName : categoryNames) {
            categories.put(catName, new ArrayList<>());
            counted.put(catName, new ArrayList<>());
            for (int i = 0; i < 3; i++) {
                categories.get(catName).add(new LinkedList<>());
                counted.get(catName).add(new TreeMap<>());
            }
        }
    }

    public HashMap<String, ArrayList<LinkedList<String>>> getCategories() {
        return categories;
    }

    public void loadDir(String dir, int key) {

        // key=0 for only, 1 for ignore, 2 for need

        String dirCategory = categorize(dir);

        categories.get(dirCategory).get(key).add(dir);
        counted.get(dirCategory).get(key).merge(dir.split(" ")[0], 1, Integer::sum);

    }

    public static String categorize(String dir) {
        for (String cat: categoryPatterns.keySet()) {
            // System.out.println(cat);
            for (Pattern pattern : categoryPatterns.get(cat)) {
                Matcher m = pattern.matcher(dir);

                if (m.find()) {
                    return cat;
                }
            }
        }

        if (dir.contains("-")) return "targetTuples";
        else return "other-only";

    }

    public HashMap<String, ArrayList<TreeMap<String, Integer>>> getCounted() {
        return counted;
    }

    public void display() {
        for (String cat : categoryNames) {
            System.out.println(cat);

            System.out.println("    only(" + categories.get(cat).get(0).size() + ")");
            for (String dir : categories.get(cat).get(0)) {
                System.out.println("        "+dir);
            }

            System.out.println("    ignore(" + categories.get(cat).get(1).size() + ")");
            for (String dir : categories.get(cat).get(1)) {
                System.out.println("        "+dir);
            }

            System.out.println("    need(" + categories.get(cat).get(2).size() + ")");
            for (String dir : categories.get(cat).get(2)) {
                System.out.println("        "+dir);
            }

        }
    }

    public static String[] getCategoryNames() {
        return categoryNames;
    }

    public void loadPatterns() {

//        categoryPatterns.put("targetTuples", Arrays.asList(
//                Pattern.compile("\\b-\\b", Pattern.CASE_INSENSITIVE)
//        ));

        categoryPatterns.put("architecture", Arrays.asList(
                Pattern.compile("\\baarch64\\s", Pattern.CASE_INSENSITIVE),
                Pattern.compile("\\baarch64\\z", Pattern.CASE_INSENSITIVE),
                Pattern.compile("\\bloongarch64\\s", Pattern.CASE_INSENSITIVE),
                Pattern.compile("\\bloongarch64\\z", Pattern.CASE_INSENSITIVE),
                Pattern.compile("\\barm\\b", Pattern.CASE_INSENSITIVE),
                Pattern.compile("\\bnvptx", Pattern.CASE_INSENSITIVE),
                Pattern.compile("\\briscv", Pattern.CASE_INSENSITIVE),
                Pattern.compile("\\bx86\\b", Pattern.CASE_INSENSITIVE),
                Pattern.compile("\\Ax86_64\\s", Pattern.CASE_INSENSITIVE),
                Pattern.compile("\\Ax86_64\\z", Pattern.CASE_INSENSITIVE),
                Pattern.compile("\\bbpf", Pattern.CASE_INSENSITIVE),
                Pattern.compile("\\bavr\\b", Pattern.CASE_INSENSITIVE),
                Pattern.compile("\\bmsp430\\b", Pattern.CASE_INSENSITIVE),
                Pattern.compile("\\bs390x\\b", Pattern.CASE_INSENSITIVE),
                Pattern.compile("\\bspirv\\b", Pattern.CASE_INSENSITIVE),
                Pattern.compile("\\bthumb\\b", Pattern.CASE_INSENSITIVE)
                ));

        categoryPatterns.put("vendor", Arrays.asList(
                Pattern.compile("\\Aapple\\b", Pattern.CASE_INSENSITIVE),
                Pattern.compile("\\buwp\\b", Pattern.CASE_INSENSITIVE)
        ));

        categoryPatterns.put("os", Arrays.asList(
                Pattern.compile("\\Alinux\\b", Pattern.CASE_INSENSITIVE),
                Pattern.compile("\\Aunix\\b", Pattern.CASE_INSENSITIVE),
                Pattern.compile("\\Awindows\\z", Pattern.CASE_INSENSITIVE),
                Pattern.compile("\\Awindows\\s", Pattern.CASE_INSENSITIVE),
                Pattern.compile("os\\b", Pattern.CASE_INSENSITIVE),
                Pattern.compile("\\bemscripten\\b", Pattern.CASE_INSENSITIVE),
                Pattern.compile("bsd\\b", Pattern.CASE_INSENSITIVE),
                Pattern.compile("\\bfuchsia\\b", Pattern.CASE_INSENSITIVE),
                Pattern.compile("\\bhaiku\\b", Pattern.CASE_INSENSITIVE),
                Pattern.compile("\\billumos\\b", Pattern.CASE_INSENSITIVE),
                Pattern.compile("\\bnto\\b", Pattern.CASE_INSENSITIVE),
                Pattern.compile("\\bvxworks\\b", Pattern.CASE_INSENSITIVE),
                Pattern.compile("\\bwasi\\b", Pattern.CASE_INSENSITIVE),
                Pattern.compile("\\bandroid\\b", Pattern.CASE_INSENSITIVE)
        ));

        categoryPatterns.put("environment", Arrays.asList(
                Pattern.compile("\\Agnu\\b", Pattern.CASE_INSENSITIVE),
                Pattern.compile("\\Amsvc\\b", Pattern.CASE_INSENSITIVE),
                Pattern.compile("\\Amusl\\b", Pattern.CASE_INSENSITIVE),
                Pattern.compile("\\Asgx\\b", Pattern.CASE_INSENSITIVE)
        ));

        categoryPatterns.put("pointerWidth", Arrays.asList(
                Pattern.compile("bit\\b", Pattern.CASE_INSENSITIVE)
        ));

        categoryPatterns.put("wasm", Arrays.asList(
                Pattern.compile("\\bwasm", Pattern.CASE_INSENSITIVE)
        ));

        categoryPatterns.put("endian", Arrays.asList(
                Pattern.compile("\\bendian", Pattern.CASE_INSENSITIVE)
        ));

        categoryPatterns.put("stage", Arrays.asList(
                Pattern.compile("\\bstage", Pattern.CASE_INSENSITIVE)
        ));

        categoryPatterns.put("channel", Arrays.asList(
                Pattern.compile("\\bnightly\\b", Pattern.CASE_INSENSITIVE),
                Pattern.compile("\\bstable\\b", Pattern.CASE_INSENSITIVE),
                Pattern.compile("\\bbeta\\b", Pattern.CASE_INSENSITIVE)
        ));

        categoryPatterns.put("crossCompile", Arrays.asList(
                Pattern.compile("\\bcross", Pattern.CASE_INSENSITIVE)
        ));

        categoryPatterns.put("compareMode", Arrays.asList(
                Pattern.compile("\\bcompare", Pattern.CASE_INSENSITIVE)
        ));

        categoryPatterns.put("coverage", Arrays.asList(
                Pattern.compile("\\bcoverage", Pattern.CASE_INSENSITIVE)
        ));

        categoryPatterns.put("remote", Arrays.asList(
                Pattern.compile("\\bremote", Pattern.CASE_INSENSITIVE)
        ));

        categoryPatterns.put("debugger", Arrays.asList(
                Pattern.compile("\\Acdb", Pattern.CASE_INSENSITIVE),
                Pattern.compile("\\Agdb", Pattern.CASE_INSENSITIVE),
                Pattern.compile("\\Alldb", Pattern.CASE_INSENSITIVE)
        ));

        categoryPatterns.put("debugAssert", Arrays.asList(
                Pattern.compile("\\bstd-debug", Pattern.CASE_INSENSITIVE)
        ));

        categoryPatterns.put("needFix", Arrays.asList(
                Pattern.compile("\\Atest", Pattern.CASE_INSENSITIVE)
        ));

        categoryPatterns.put("llvm", Arrays.asList(
                Pattern.compile("\\bllvm", Pattern.CASE_INSENSITIVE)
        ));

        categoryPatterns.put("sanitizer", Arrays.asList(
                Pattern.compile("\\bsanitizer", Pattern.CASE_INSENSITIVE)
        ));

        categoryPatterns.put("capabilities", Arrays.asList(
                Pattern.compile("\\basm", Pattern.CASE_INSENSITIVE),
                Pattern.compile("\\bunwind", Pattern.CASE_INSENSITIVE),
                Pattern.compile("\\bthreads", Pattern.CASE_INSENSITIVE),
                Pattern.compile("\\bpass\\b", Pattern.CASE_INSENSITIVE),
                Pattern.compile("\\bdlltool\\b", Pattern.CASE_INSENSITIVE),
                Pattern.compile("\\bhorizon\\b", Pattern.CASE_INSENSITIVE),
                Pattern.compile("\\bdeterministic", Pattern.CASE_INSENSITIVE)
        ));

        categoryPatterns.put("addiDependency", Arrays.asList(
                Pattern.compile("\\bprofiler", Pattern.CASE_INSENSITIVE),
                Pattern.compile("\\bxray", Pattern.CASE_INSENSITIVE)
        ));

        categoryPatterns.put("autoDiff", Arrays.asList(
                Pattern.compile("\\benzyme", Pattern.CASE_INSENSITIVE)
        ));


    }

}
