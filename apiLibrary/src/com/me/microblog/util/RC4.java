package com.me.microblog.util;

/**
 * @version 1.00.00
 * @description: 这个网上流传的版本，问题一大堆，与正常的编码后效果不同。
 * @author: archko 12-1-9
 */
public class RC4 {

    /*public RC4()
         {
             
         }*/

    public static void main(String[] args) {

        String aInput = "Fuck you fuck fuck fuck";
        String aKey = "shitdsgdsfgdsgsdfgsdfg";
        //System.out.println(new Integer(aKey));
        System.out.println(RunRC4(aInput, aKey));
        System.out.println(RunRC4(RunRC4(aInput, aKey), aKey));
    }

    public static String RunRC4(String aInput, String aKey) {
        int[] iS = new int[ 256 ];
        byte[] iK = new byte[ 256 ];

        for (int i = 0; i < 256; i++)
            iS[ i ] = i;

        int j = 1;

        for (short i = 0; i < 256; i++) {
            iK[ i ] = (byte) aKey.charAt((i % aKey.length()));
        }

        j = 0;

        for (int i = 0; i < 255; i++) {
            j = (j + iS[ i ] + iK[ i ]) % 256;
            int temp = iS[ i ];
            iS[ i ] = iS[ j ];
            iS[ j ] = temp;
        }

        int i = 0;
        j = 0;
        String rOutput = "";
        short iMask = 15;
        char[] iInputChar = aInput.toCharArray();
        char[] iOutputChar = new char[ iInputChar.length ];
        for (short x = 0; x < iInputChar.length; x++) {
            i = (i + 1) % 256;
            j = (j + iS[ i ]) % 256;
            int temp = iS[ i ];
            iS[ i ] = iS[ j ];
            iS[ j ] = temp;
            int t = (iS[ i ] + (iS[ j ] % 256)) % 256;
            int iY = iS[ t ];
            char iCY = (char) iY;
            iOutputChar[ x ] = (char) (iInputChar[ x ] ^ iCY);
        }

        return new String(iOutputChar);

    }
}
