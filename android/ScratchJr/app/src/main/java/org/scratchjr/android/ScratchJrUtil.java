package org.scratchjr.android;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * General utility class with static utility methods.
 *
 * @author markroth8
 */
public class ScratchJrUtil {

    private static final int BUFFER = 2048;

    /** Utility class private constructor so nobody creates an instance of this class */
    private ScratchJrUtil() {
    }

    /**
     * Convert the given JSONArray to an array of Strings.
     */
    public static String[] jsonArrayToStringArray(JSONArray values)
        throws JSONException
    {
        String[] result = new String[values.length()];
        for (int i = 0; i < values.length(); i++) {
            result[i] = values.getString(i);
        }
        return result;
    }

    /**
     * get sharing file extension based on APPLICATION_ID
     * @return extension
     */
    public static String getExtension() {
        String extension;
        if (BuildConfig.APPLICATION_ID.equals("org.pbskids.scratchjr")) {
            extension = ".psjr";
        } else {
            extension = ".sjr";
        }
        return extension;
    }

    /**
     * get sharing file mime-type based on APPLICATION_ID
     * @return mime type
     */
    public static String getMimeType() {
        String mimetype;
        if (BuildConfig.APPLICATION_ID.equals("org.pbskids.scratchjr")) {
            mimetype = "application/x-pbskids-scratchjr-project";
        } else {
            mimetype = "application/x-scratchjr-project";
        }
        return mimetype;
    }

    /**
     * remove file or folder
     * if `file` is a folder, it will be cleaned up before removing
     * @param file to be removed
     */
    public static void removeFile(File file) {
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                removeFile(f);
            }
        }
        file.delete();
    }

    /**
     * Copy file from a location to target location
     *
     * @param sourceLocation file path to be copied
     * @param targetLocation file path bo be copied to
     * @throws IOException
     */
    public static void copyFile(File sourceLocation, File targetLocation)
        throws IOException
    {
        InputStream in = new FileInputStream(sourceLocation);
        OutputStream out = new FileOutputStream(targetLocation);

        // Copy the bits from instream to outstream
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    /**
     * zip a folder to target location
     * @param sourcePath folder to compress
     * @param toLocation the target location to save the zip
     * @return successful or not
     */
    public static boolean zipFileAtPath(String sourcePath, String toLocation) {
        File sourceFile = new File(sourcePath);
        if (sourceFile.exists()) {
            sourceFile.delete();
        }
        try {
            BufferedInputStream origin;
            FileOutputStream dest = new FileOutputStream(toLocation);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(
                dest));
            if (sourceFile.isDirectory()) {
                zipSubFolder(out, sourceFile, sourceFile.getParent().length());
            } else {
                byte[] data = new byte[BUFFER];
                FileInputStream fi = new FileInputStream(sourcePath);
                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(getLastPathComponent(sourcePath));
                entry.setTime(sourceFile.lastModified()); // to keep modification time after unzipping
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
            }
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /*
     * Zips a subfolder
     */
    private static void zipSubFolder(ZipOutputStream out, File folder, int basePathLength)
        throws IOException
    {
        File[] fileList = folder.listFiles();
        BufferedInputStream origin;
        for (File file : fileList) {
            if (file.isDirectory()) {
                zipSubFolder(out, file, basePathLength);
            } else {
                byte[] data = new byte[BUFFER];
                String unmodifiedFilePath = file.getPath();
                String relativePath = unmodifiedFilePath
                    .substring(basePathLength);
                FileInputStream fi = new FileInputStream(unmodifiedFilePath);
                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(relativePath);
                entry.setTime(file.lastModified()); // to keep modification time after unzipping
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }
        }
    }

    /*
     * gets the last path component
     *
     * Example: getLastPathComponent("downloads/example/fileToZip");
     * Result: "fileToZip"
     */
    private static String getLastPathComponent(String filePath) {
        String[] segments = filePath.split(File.separator);
        if (segments.length == 0) {
            return "";
        }
        return segments[segments.length - 1];
    }
}
