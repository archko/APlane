package cn.archko.microblog.smiley;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class for annotating a CharSequence with spans to convert textual emoticons
 * to graphical ones.
 */
public class AKSmileyParser {

    // Singleton stuff
    private static AKSmileyParser sInstance;

    public static AKSmileyParser getInstance(Context context) {
        if (null==sInstance) {
            sInstance=new AKSmileyParser(context);
        }
        return sInstance;
    }

    private final Context mContext;
    private final Pattern mPattern;
    public final String[] mSmileyTexts;

    private AKSmileyParser(Context context) {
        mContext=context;
        mSmileyTexts=AKSmiley.mSmileyMap.keySet().toArray(new String[0]);
        System.out.println("AKSmileyParser:"+mSmileyTexts+" ssm:"+AKSmiley.mSmileyMap.size());
        mPattern=buildPattern();
    }

    /**
     * Builds the regular expression we use to find smileys in {@link #addSmileySpans}.
     */
    private Pattern buildPattern() {
        int length=AKSmiley.mSmileyMap.size();
        // Set the StringBuilder capacity with the assumption that the average
        // smiley is 3 characters long.
        StringBuilder patternString=new StringBuilder(length*3);

        System.out.println("buildPattern:"+mSmileyTexts);
        // Build a regex that looks like (:-)|:-(|...), but escaping the smilies
        // properly so they will be interpreted literally by the regex matcher.
        patternString.append("(");
        for (String s : mSmileyTexts) {
            patternString.append(Pattern.quote(s));
            patternString.append('|');
        }
        // Replace the extra '|' with a ')'
        patternString.replace(patternString.length()-1, patternString.length(), ")");

        return Pattern.compile(patternString.toString());
    }

    /**
     * Adds ImageSpans to a CharSequence that replace textual emoticons such
     * as :-) with a graphical version.
     *
     * @param text A CharSequence possibly containing emoticons
     * @return A CharSequence annotated with ImageSpans covering any
     *         recognized emoticons.
     */
    public CharSequence addSmileySpans(CharSequence text) {
        SpannableStringBuilder builder=new SpannableStringBuilder(text);

        Matcher matcher=mPattern.matcher(text);
        while (matcher.find()) {
            int resId=AKSmiley.mSmileyMap.get(matcher.group());
            builder.setSpan(new ImageSpan(mContext, resId),
                matcher.start(), matcher.end(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return builder;
    }
}


