package person.terry.message.basic_nio;

import java.nio.CharBuffer;

/**
 * Created by terry on 2017/8/4.
 *
 * 填充和抽取 buffer的简单例子
 *
 * 主要是
 *
 * put get flip clear hasRemaining等几个方法的使用
 *
 *
 */
public class BufferFillDrainCase {

    private static String [] sampleStrs = {
            "A random string value",
            "The product of an infinite number of monkeys",
            "Hey hey we're the Monkees",
            "Opening act for the Monkees: Jimi Hendrix",
            "'Scuse me while I kiss this fly", // Sorry Jimi ;-)
            "Help Me! Help Me!",
    };

    public static void main(String[] args){
        CharBuffer buffer = CharBuffer.allocate(100);
        while(fillBuffer(buffer)){
            buffer.flip();
            drain(buffer);
            buffer.clear();
        }
    }

    private static void drain(CharBuffer charBuffer) {
        while (charBuffer.hasRemaining()) {
            System.out.print(charBuffer.get());
        }
        System.out.println("");
    }

    private static boolean fillBuffer (CharBuffer charBuffer) {

        if (index >= sampleStrs.length)
            return false;

        String string = sampleStrs[index++];
        for (int i = 0; i < string.length() ; i++) {
            charBuffer.put(string.charAt(i));
        }

        return true;

    }

    private static int index = 0;

}
