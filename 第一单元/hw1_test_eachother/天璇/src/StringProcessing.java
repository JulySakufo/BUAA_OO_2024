import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringProcessing {
    public String pretreatment(String expression) {
        // Remove all of the " " and "\t"
        String temp = expression.replaceAll("[ \\t]", "");

        // Merge consecutive plus and minus signs
        temp = mergeAddAndSub(temp);

        // Remove all of the "leading zeros"
        temp = removeAllLeadingZero(temp);

        return temp;
    }

    private String mergeAddAndSub(String expression) {
        Pattern pattern = Pattern.compile("[\\+-]+");
        Matcher matcher = pattern.matcher(expression);
        int start = 0;
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            result.append(expression, start, matcher.start());
            result.append(merge(matcher.group(0)));
            start = matcher.end();
        }
        if (start < expression.length()) {
            result.append(expression.substring(start));
        }
        return result.toString();
    }

    private String merge(String addSub) {
        int subNum = 0;
        for (int i = 0;i < addSub.length();i++) {
            if (addSub.charAt(i) == '-') {
                subNum++;
            }
        }
        if (subNum % 2 == 0) {
            return "+";
        } else {
            return "-";
        }
    }

    private String removeAllLeadingZero(String expression) {
        Pattern pattern = Pattern.compile("[0-9]+");
        Matcher matcher = pattern.matcher(expression);
        int start = 0;
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            result.append(expression, start, matcher.start());
            result.append(removeLeadingZero(matcher.group(0)));
            start = matcher.end();
        }
        if (start < expression.length()) {
            result.append(expression.substring(start));
        }
        return result.toString();
    }

    private String removeLeadingZero(String num) {
        if (num.length() == 1 && num.charAt(0) == '0') {
            return "0";
        }
        int leadingZeroNum = 0;
        for (int i = 0;i < num.length();i++) {
            if (num.charAt(i) == '0') {
                leadingZeroNum++;
            } else {
                break;
            }
        }
        if (leadingZeroNum == num.length()) {
            return "0";
        } else {
            return num.substring(leadingZeroNum);
        }
    }
}
