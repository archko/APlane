package com.me.microblog.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;

/**
 * @author archko
 */
public class StreamUtils {

    /**
     * 根据文件路径new一个文件输入流
     */
    public static InputStream loadStreamFromFile(String filePathName) throws FileNotFoundException, IOException {
        return new FileInputStream(filePathName);
    }

    /**
     * 将String保存到指定的文件中
     */
    public static void saveStringToFile(String text, String filePath) throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(text.getBytes("UTF-8"));
        saveStreamToFile(in, filePath);
    }

    /**
     * 将InputStream保存到指定的文件中
     */
    public static void saveStreamToFile(InputStream in, String filePath) throws IOException {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
            } else {
                File parent = file.getParentFile();
                if (! parent.exists()) {
                    parent.mkdirs();
                }
                file.createNewFile();
            }

            FileOutputStream fos = new FileOutputStream(file);
            copyStream(in, fos);
            fos.close();
        } catch (Exception e) {
        }
    }

    /**
     * 从输入流里面读出byte[]数组
     */
    public static byte[] readStream(InputStream in) throws IOException {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();

        byte[] buf = new byte[ 1024 ];
        int len = - 1;
        while ((len = in.read(buf)) != - 1) {
            byteOut.write(buf, 0, len);
        }

        byteOut.close();
        in.close();
        return byteOut.toByteArray();
    }

    /**
     * 从输入流里面读出每行文字
     */
    public static ArrayList<String> loadStringLinesFromStream(InputStream in) throws IOException {
        InputStreamReader reader = new InputStreamReader(in, "UTF-8");
        BufferedReader br = new BufferedReader(reader);
        String row;
        ArrayList<String> lines = new ArrayList<String>();
        int length = in.available();
        try {
            while ((row = br.readLine()) != null) {
                lines.add(row);
            }
        } catch (OutOfMemoryError e) {

        }
        br.close();
        reader.close();
        return lines;
    }

    /**
     * 拷贝流
     */
    public static void copyStream(InputStream in, OutputStream out) throws IOException {
        BufferedInputStream bin = new BufferedInputStream(in);
        BufferedOutputStream bout = new BufferedOutputStream(out);

        byte[] buffer = new byte[ 4096 ];

        while (true) {
            int doneLength = bin.read(buffer);
            if (doneLength == - 1)
                break;
            bout.write(buffer, 0, doneLength);
        }
        bout.flush();
    }

    /**
     * 刷新输入流
     */
    public static ByteArrayInputStream flushInputStream(InputStream in) throws IOException {
        BufferedInputStream bin = new BufferedInputStream(in);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BufferedOutputStream bout = new BufferedOutputStream(baos);
        ByteArrayInputStream bais = null;
        byte[] buffer = new byte[ 4096 ];
        int length = in.available();
        try {
            while (true) {
                int doneLength = bin.read(buffer);
                if (doneLength == - 1)
                    break;
                bout.write(buffer, 0, doneLength);
            }
            bout.flush();
            bout.close();
            /*
            java.lang.OutOfMemoryError
			at java.io.ByteArrayOutputStream.toByteArray(ByteArrayOutputStream.java:122)
			 */
            bais = new ByteArrayInputStream(baos.toByteArray());
        } catch (OutOfMemoryError e) {
            //GJApplication.getUserTracer().add(611, (length / 1024) + "kB");
            System.gc();
        }
        return bais;
    }

    /**
     * 将输入流转化为字符串输出
     */
    public static String getStringLineFromStream(InputStream is) {
        if (is != null) {
            StringBuffer buf = new StringBuffer();
            ArrayList<String> als;
            try {
                als = loadStringLinesFromStream(is);
                for (String string : als)
                    buf.append(string);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return buf.toString();
        }
        return "";
    }

    public static void closeStream(Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * 从输入流中读取字符串.
     *
     * @param is 输入流
     * @return
     * @throws IOException
     */
    public static String parseInputStream(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is), 1000);
        StringBuilder responseBody = new StringBuilder();
        String line = reader.readLine();
        while (line != null) {
            responseBody.append(line);
            line = reader.readLine();
        }
        String string = responseBody.toString();
        return string;
    }

    //---------------------------

    public static void serializeObject(Object obj, String filename) {
        // SLLog.d("serialization", "serialize: " + obj.getClass().getSimpleName());
        File file = new File(filename);
        try {
            if (! file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
            out.writeObject(obj);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            if (file.exists()) {
                file.delete();
            }
        }
    }

    public static Object deserializeObject(String filename) {
        // SLLog.d("serialization", "deserialize: " + filename);
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(
                filename));
            Object object = in.readObject();
            in.close();
            return object;
        } catch (Exception e) {
            e.printStackTrace();
            File file = new File(filename);
            if (file.exists()) {
                file.delete();
            }
        }
        return null;
    }

    public static Object deserializeObject(byte[] data) {
        // SLLog.d("serialization", "deserialize: " + data);
        if (data != null && data.length > 0) {
            try {
                ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(data));
                Object obj = in.readObject();
                in.close();
                return obj;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static byte[] getSerializedBytes(Object obj) {
        // SLLog.d("serialization", "deserialize: " + obj);
        try {
            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bao);
            out.writeObject(obj);
            out.flush();
            byte[] data = bao.toByteArray();
            out.close();
            bao.close();
            return data;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new byte[]{};
    }

    //---------------------------

    /**
     * 复制文件
     *
     * @param target 需要复制的目的地
     * @param source 源文件全路径
     * @return 是否复制成功
     */
    public static boolean copyFileToFile(String target, String source) {
        FileInputStream input = null;
        FileOutputStream output = null;
        try {
            input = new FileInputStream(source);
            output = new FileOutputStream(target);
            copy(input, output);
            return true;
        } catch (Exception e) {
            System.out.println("files copy error." + e);
        }
        return false;
    }

    public static void copy(final InputStream source, final OutputStream target) throws IOException {
        ReadableByteChannel in = null;
        WritableByteChannel out = null;
        try {
            in = Channels.newChannel(source);
            out = Channels.newChannel(target);
            final ByteBuffer buf = ByteBuffer.allocateDirect(512 * 1024);
            while (in.read(buf) > 0) {
                buf.flip();
                out.write(buf);
                buf.flip();
            }
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (final IOException ex) {
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (final IOException ex) {
                }
            }
        }
    }
}
