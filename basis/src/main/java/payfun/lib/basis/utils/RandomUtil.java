package payfun.lib.basis.utils;

import java.util.Random;

/**
 * @author : 时光
 * e-mail : qurongzhen@pay.media
 * date   : 2022/5/12 9:59
 * desc   : <随机数生成工具类>
 * <p>参考自:apache.commons.lang3.RandomStringUtils类</>
 */
public class RandomUtil {
    private static final Random RANDOM = new Random();

    public static String randomAlphanumeric(int count) {
        return random(count, true, true);
    }

    public static String random(int count, boolean letters, boolean numbers) {
        return random(count, 0, 0, letters, numbers);
    }

    public static String random(int count, int start, int end, boolean letters, boolean numbers) {
        return random(count, start, end, letters, numbers, (char[]) null, RANDOM);
    }

    /**
     * <p>使用提供的随机源基于各种选项创建一个随机字符串。</p>
     *
     * <p>如果 start 和 end 都是 {@code 0}，则 start 和 end 设置为 {@code ' '}和 {@code 'z'}，
     * ASCII 可打印字符，将被使用，除非字母和数字都是 {@code false}，在这种情况下，
     * 开始和结束设置为 {@code 0} 和 {@link Character#MAX_CODE_POINT }。
     *
     * <p>如果 set 不是 {@code null}，则选择 start 和 end 之间的字符。</p>
     *
     * <p>此方法接受用户提供的 {@link Random} 实例作为随机源。通过使用固定种子播种单个 {@link Random} 实例并将其用于每次调用，
     * 可以重复且可预测地生成相同的随机字符串序列。</p>
     *
     * @param count   计算创建的随机字符长度
     * @param start   字符集在开始时的位置（包括）
     * @param end     字符集在开始时的位置（不包括）
     * @param letters true,生成的字符串可以包括字母字符
     * @param numbers true,生成的字符串可以包含数字字符
     * @param chars   要从中选择随机数的字符集，不能为空。如果 {@code null}，那么它将使用所有字符的集合。
     * @param random  随机性的来源。
     * @return 随机字符串
     * @throws ArrayIndexOutOfBoundsException 如果集合数组中没有 {@code (end - start) + 1} 个字符。
     * @throws IllegalArgumentException       如果 {@code count} < 0 或提供的字符数组为空。 @从 2.0 开始
     */
    public static String random(int count, int start, int end, boolean letters, boolean numbers, char[] chars, Random random) {
        if (count == 0) {
            return "";
        } else if (count < 0) {
            throw new IllegalArgumentException("Requested random string length " + count + " is less than 0.");
        }
        if (chars != null && chars.length == 0) {
            throw new IllegalArgumentException("The chars array must not be empty");
        }

        if (start == 0 && end == 0) {
            if (chars != null) {
                end = chars.length;
            } else {
                if (!letters && !numbers) {
                    end = Character.MAX_CODE_POINT;
                } else {
                    end = 'z' + 1;
                    start = ' ';
                }
            }
        } else {
            if (end <= start) {
                throw new IllegalArgumentException("Parameter end (" + end + ") must be greater than start (" + start + ")");
            }
        }

        final int zero_digit_ascii = 48;
        final int first_letter_ascii = 65;

        if (chars == null && (numbers && end <= zero_digit_ascii
                || letters && end <= first_letter_ascii)) {
            throw new IllegalArgumentException("Parameter end (" + end + ") must be greater then (" + zero_digit_ascii + ") for generating digits " +
                    "or greater then (" + first_letter_ascii + ") for generating letters.");
        }

        final StringBuilder builder = new StringBuilder(count);
        final int gap = end - start;

        while (count-- != 0) {
            int codePoint;
            if (chars == null) {
                codePoint = random.nextInt(gap) + start;

                switch (Character.getType(codePoint)) {
                    case Character.UNASSIGNED:
                    case Character.PRIVATE_USE:
                    case Character.SURROGATE:
                        count++;
                        continue;
                }

            } else {
                codePoint = chars[random.nextInt(gap) + start];
            }

            final int numberOfChars = Character.charCount(codePoint);
            if (count == 0 && numberOfChars > 1) {
                count++;
                continue;
            }

            if (letters && Character.isLetter(codePoint)
                    || numbers && Character.isDigit(codePoint)
                    || !letters && !numbers) {
                builder.appendCodePoint(codePoint);

                if (numberOfChars == 2) {
                    count--;
                }

            } else {
                count++;
            }
        }
        return builder.toString();
    }
}
