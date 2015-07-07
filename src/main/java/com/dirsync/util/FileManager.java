package com.dirsync.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;

public class FileManager {

	/**
	 * Write a String to a file.
	 *
	 * @param directory
	 *            The directory to write to.
	 * @param fileName
	 *            The filename to write to.
	 * @param data
	 *            The data to write to the file.
	 * @throws IOException
	 */
	public void write(String directory, String fileName, String data)
			throws IOException {
		if (!directoryExists(directory)) {
			createDirectory(directory);
		}
		String filePathAndName = getFilePathAndName(directory, fileName);
		write(filePathAndName, data);
	}

	/**
	 * Write a String to a file.
	 *
	 * @param filePathAndName
	 *            The directory and name of a file.
	 * @param data
	 * @throws IOException
	 */
	public void write(String filePathAndName, String data) throws IOException {
		PrintWriter out = null;
		try {
			out = new PrintWriter(new BufferedWriter(new FileWriter(
					filePathAndName)));
			out.print(data);
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}

	/**
	 * Append bytes to a file.
	 *
	 * @param filePathAndName
	 *            The directory and name of a file.
	 * @param bytes
	 *            The bytes to append to the file.
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void appendToFile(String filePathAndName, byte[] bytes)
			throws FileNotFoundException, IOException {
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(filePathAndName, true);
			out.write(bytes);
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}

	/**
	 * The system-dependent default name-separator character, represented as a
	 * string for convenience. This string contains a single character, namely
	 * File
	 *
	 * @return File.seperatorChar
	 */
	public String getSeparator() {
		return File.separator;
	}

	/**
	 * Create a directory.
	 *
	 * @param directory
	 *            The directory to create.
	 */
	public void createDirectory(String directory) {
		File ldirectory = new File(directory);
		if ((ldirectory != null) && !ldirectory.exists()) {
			ldirectory.mkdir();
		}
	}

	/**
	 * Extract the file name from a file path and name. The file must exist.
	 * Uses File.getName().
	 *
	 * @param filePathAndName
	 *            The directory and name of a file.
	 * @return The file name from a path and name.
	 */
	public String getFileName(String filePathAndName) {
		String fileName = null;
		File file = new File(filePathAndName);
		fileName = file.getName();
		return fileName;
	}

	/**
	 * Extract the directory from a file path and name. The file must exist.
	 * Uses File.getParent().
	 *
	 * @param filePathAndName
	 * @return The directory.
	 */
	public String getFileDirectory(String filePathAndName) {
		String filePath = null;
		File file = new File(filePathAndName);
		filePath = file.getParent();
		return filePath;
	}

	/**
	 * Get a list of files in a directory with a recursive option.
	 * 
	 * @param directory
	 *            The directory to get a list of files for.
	 * @param recursively
	 *            If true then recurse directories, else do not recurse
	 *            directories.
	 * @param recursiveRootDirectory
	 *            The caller of this method should pass null for recursiveRootDirectory.
	 *            This value is set in the method when doing a recursive call.
	 * @return A collection of Strings containing filenames in the directory relative to the directory argument.
	 */
	public Collection<String> catalogDirectory(String directory, boolean recursively, String recursiveRootDirectory) {
		Collection<String> files = null;
		String catalogRootDirectory = null;
		if (recursiveRootDirectory == null) {
			// Make sure the directory argument separator is the same as the OS directory separator.
			if (getSeparator().equals("/")) {
				catalogRootDirectory = directory.replace("\\", getSeparator());				
			} else {
				// else the directory separator is a backslash.
				catalogRootDirectory = directory.replace("/", getSeparator());				
			}
			
			// On Windows, getAbsolutePath returns a single escaped backslash between directories.
			// The replaceFirst argument is a regularExpression which must escape the backslash to
			// provide a match for the returned value of getAbsolutePath.
			// If the directory argument contains a double backslash then catalogRootDirectory 
			// will already be properly escaped to find a match for the result of getAbsolutePath.
			if (!catalogRootDirectory.contains("\\\\")) {
				catalogRootDirectory = catalogRootDirectory.replace("\\", "\\\\");			
			}
		} else {
			catalogRootDirectory = recursiveRootDirectory;
		}
		files = new ArrayList<String>();
		File directoryFile = new File(directory);
		if ((directoryFile != null) && (directoryFile.isDirectory())) {
			for (File f : directoryFile.listFiles()) {
				if (f.isFile()) {
					String fileName = f.getAbsolutePath();
					// Remove the recursive root directory from the file name.
					fileName = fileName.replaceFirst(
							catalogRootDirectory, "");
					if (fileName.startsWith("\\") || fileName.startsWith("/")) {
						fileName = fileName.substring(1);
					}
					files.add(fileName);
				} else if (f.isDirectory() && recursively) {
					Collection<String> subFiles = catalogDirectory(
							f.getAbsolutePath(), recursively, catalogRootDirectory);
					files.addAll(subFiles);
				}
			}
		}
		return files;
	}

	/**
	 * Move a file from one directory to another.
	 * 
	 * @param fromPathAndName
	 *            Given the path and name of a file
	 * @param toPathAndName
	 *            move the file to this path and name
	 * @throws IOException
	 */
	public void moveFile(String fromPathAndName, String toPathAndName)
			throws IOException {
		String fromFileName = getFileName(fromPathAndName);
		String toFileName = getFileName(toPathAndName);
		String toDirectory = toPathAndName.substring(0, toPathAndName.length()
				- toFileName.length());
		if (!directoryExists(toDirectory)) {
			// create the directories.
			new File(toDirectory).mkdirs();
		}
		File file = new File(fromPathAndName);
		// Move file to new directory
		boolean success = file.renameTo(new File(toPathAndName));
		if (!success) {
			// File was not successfully moved
			throw new IOException("Unable to move file " + fromFileName
					+ " from " + fromPathAndName + " to directory "
					+ toPathAndName);
		}
	}

	/**
	 * Move a file from one directory to another.
	 *
	 * @param fromDirectory
	 * @param toDirectory
	 * @param fileName
	 * @throws IOException
	 */
	public void moveFile(String fromDirectory, String toDirectory,
			String fileName) throws IOException {
		// File (or directory) to be moved
		File file = new File(fromDirectory + File.separator + fileName);

		// Destination directory
		File dir = new File(toDirectory);

		// Move file to new directory
		boolean success = file.renameTo(new File(dir, file.getName()));
		if (!success) {
			// File was not successfully moved
			throw new IOException("Unable to move file " + fileName + " from "
					+ fromDirectory + " to directory " + toDirectory);
		}
	}

	/**
	 * Delete a file.
	 *
	 * @param directory
	 * @param fileName
	 * @throws IOException
	 */
	public void deleteFile(String directory, String fileName)
			throws IOException {
		boolean success = (new File(getFilePathAndName(directory, fileName)))
				.delete();
		if (!success) {
			// Deletion failed
			throw new IOException("Unable to delete file " + fileName
					+ " from directory " + directory);
		}
	}

	/**
	 * Delete a file.
	 *
	 * @param filePathAndName
	 * @throws IOException
	 *             Throw an exception if the file does not exist.
	 */
	public void deleteFile(String filePathAndName) throws IOException {
		boolean success = (new File(filePathAndName)).delete();
		if (!success) {
			// Deletion failed
			throw new IOException("Unable to delete file " + filePathAndName);
		}
	}

	/**
	 * Check to see if a file exists.
	 *
	 * @param directory
	 * @param fileName
	 * @return True if the file exists.
	 */
	public boolean fileExists(String directory, String fileName) {
		return ((new File(getFilePathAndName(directory, fileName))).exists());
	}

	/**
	 * Check for the existence of a directory.
	 *
	 * @param directory
	 * @return True if the directory exists.
	 */
	public boolean directoryExists(String directory) {
		return ((new File(directory)).exists());
	}

	/**
	 * Return the file name from a string. Parses the string to return only the
	 * filename.
	 *
	 * @param fileName
	 * @return The file name.
	 */
	public String extractFileName(String fileName) {
		String extractedFileName = null;
		if ((fileName != null) && (fileName.trim().length() > 0)) {
			extractedFileName = fileName;
			// Remove everthing preceding a forward slash, backslash, or colon.
			int index = extractedFileName.lastIndexOf("/");
			if (index > -1) {
				extractedFileName = fileName.substring(index + 1,
						extractedFileName.length());
			}
			index = extractedFileName.lastIndexOf("\\");
			if (index > -1) {
				extractedFileName = fileName.substring(index + 1,
						extractedFileName.length());
			}
			index = extractedFileName.lastIndexOf(":");
			if (index > -1) {
				extractedFileName = fileName.substring(index + 1,
						extractedFileName.length());
			}
		}
		return extractedFileName;
	}

	/**
	 * Return the file extension from the filename.
	 *
	 * @param fileName
	 * @return The extension of the file.
	 */
	public String extractFileExtension(String fileName) {
		String fileExtension = null;
		if (fileName != null) {
			int extensionIndex = fileName.lastIndexOf(".");
			fileExtension = fileName.substring(extensionIndex);
		}
		return fileExtension;
	}

	/**
	 * Append a string to the file name.
	 *
	 * @param fileName
	 * @param appendString
	 * @return The appended file name.
	 */
	public String appendToFileName(String fileName, String appendString) {
		String appendedFileName = null;
		if (fileName != null) {
			int extensionIndex = fileName.lastIndexOf(".");
			appendedFileName = fileName.substring(0, (extensionIndex))
					+ appendString + fileName.substring(extensionIndex);
		}
		return appendedFileName;
	}

	/**
	 * Given a directory and a fileName, return the file path and name with the
	 * proper seperator.
	 *
	 * @param directory
	 * @param fileName
	 * @return The file path and name with a proper seperator.
	 */
	public String getFilePathAndName(String directory, String fileName) {
		String filePathAndName = null;
		if ((directory != null) && (fileName != null)) {
			int lastIndex = 0;
			if (directory.trim().endsWith("/")) {
				lastIndex = directory.lastIndexOf("/");
			} else if (directory.trim().endsWith("\\")) {
				lastIndex = directory.lastIndexOf("\\");
			}
			if (lastIndex > 0) {
				filePathAndName = directory.substring(0, lastIndex)
						+ File.separator + fileName.trim();
			} else {
				filePathAndName = directory.trim() + File.separator
						+ fileName.trim();
			}
		} else if (fileName != null) {
			filePathAndName = fileName.trim();
		}
		return filePathAndName;
	}

	/**
	 * Get the length of a file.
	 *
	 * @param filePathAndName
	 * @return The length of a file.
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public Long fileLength(String filePathAndName)
			throws FileNotFoundException, IOException {
		Long length = null;
		RandomAccessFile randomAccessFile = null;
		try {
			File file = new File(filePathAndName);
			randomAccessFile = new RandomAccessFile(file, "r");
			length = randomAccessFile.length();
			randomAccessFile.close();
		} finally {
			if (randomAccessFile != null) {
				randomAccessFile.close();
			}
		}
		return length;
	}

	/**
	 * Perform a random access read to get the bytes from a file.
	 *
	 * @param filePathAndName
	 *            The directory and filename to read from.
	 * @param positionInFile
	 *            The position in the file to start a read.
	 * @param numBytesToRead
	 *            The number of bytes to read from the file.
	 * @return The bytes read from the file.
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public byte[] readBytes(String filePathAndName, int positionInFile,
			int numBytesToRead) throws FileNotFoundException, IOException {
		byte[] bytes = new byte[numBytesToRead];
		RandomAccessFile randomAccessFile = null;
		try {
			File file = new File(filePathAndName);
			randomAccessFile = new RandomAccessFile(file, "rw");
			randomAccessFile.seek(positionInFile);
			randomAccessFile.read(bytes, 0, numBytesToRead);
		} finally {
			if (randomAccessFile != null) {
				randomAccessFile.close();
			}
		}
		return bytes;
	}

	/**
	 *
	 * @param inputFile
	 * @param outputFile
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void copyFile(String fromPathAndName, String toPathAndName)
			throws FileNotFoundException, IOException {
		String toFileName = getFileName(toPathAndName);
		String toDirectory = toPathAndName.substring(0, toPathAndName.length()
				- toFileName.length());
		if (!directoryExists(toDirectory)) {
			// create the directories.
			new File(toDirectory).mkdirs();
		}

		File file1 = new File(fromPathAndName);
		File file2 = new File(toPathAndName);
		InputStream in = new FileInputStream(file1);
		OutputStream out = new FileOutputStream(file2);
		try {
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
		} finally {
			in.close();
			out.close();
		}
	}

	/**
	 * @param filePathAndName
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public byte[] readBytes(String filePathAndName)
			throws FileNotFoundException, IOException {
		InputStream inputStream = null;
		byte[] bytes = null;
		try {
			File file = new File(filePathAndName);
			inputStream = new FileInputStream(file);
			Long length = file.length();
			bytes = readBytes(inputStream, length);
		} finally {
			if (inputStream != null) {
				inputStream.close();
			}
		}
		return bytes;
	}

	/**
	 * @param is
	 * @param length
	 * @return
	 * @throws IOException
	 */
	private byte[] readBytes(InputStream is, long length) throws IOException {
		// You cannot create an array using a long type.
		// It needs to be an int type.
		// Before converting to an int type, check
		// to ensure that file is not larger than Integer.MAX_VALUE.
		if (length > Integer.MAX_VALUE) {
			// File is too large
			throw new IOException("File too large, cannot read file: "
					+ Integer.MAX_VALUE);
		}

		// Create the byte array to hold the data
		byte[] bytes = new byte[(int) length];

		// Read in the bytes
		int offset = 0;
		int numRead = 0;
		while (offset < bytes.length
				&& (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
			offset += numRead;
		}

		// Ensure all the bytes have been read.
		if (offset < bytes.length) {
			throw new IOException("InputStream to large!");
		}

		return bytes;
	}

	/**
	 * @param filePathAndName
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public String read(String filePathAndName) throws FileNotFoundException,
			IOException {
		byte[] bytes = readBytes(filePathAndName);
		return new String(bytes);
	}

	/**
	 * @param rootDirectory
	 * @param relativeFilePathAndNames
	 * @return
	 * @throws IOException
	 */
	public HashMap<String, BasicFileAttributes> getBasicFileAttributes(
			String rootDirectory, Collection<String> relativeFilePathAndNames)
			throws IOException {
		HashMap<String, BasicFileAttributes> fileAttributes = new HashMap<String, BasicFileAttributes>();
		for (int i = 0; i < relativeFilePathAndNames.size(); i++) {
			fileAttributes.put(
					((ArrayList<String>) relativeFilePathAndNames).get(i),
					this.getBasicFileAttributes(rootDirectory
							+ File.separator
							+ ((ArrayList<String>) relativeFilePathAndNames)
									.get(i)));
		}
		return fileAttributes;
	}

	/**
	 * @param filePathAndName
	 * @return
	 * @throws IOException
	 */
	public BasicFileAttributes getBasicFileAttributes(String filePathAndName)
			throws IOException {
		BasicFileAttributes attributes = null;
		FileSystem fs = FileSystems.getDefault();
		Path filePath = fs.getPath(filePathAndName);
		attributes = Files.readAttributes(filePath, BasicFileAttributes.class);
		return attributes;
	}

	/**
	 * @param directory
	 */
	public void removeEmptyDirectories(String directory) {
		File directoryFile = new File(directory);
		if ((directoryFile != null) && (directoryFile.isDirectory())) {
			for (File f : directoryFile.listFiles()) {
				if (f.isDirectory()) {
					removeEmptyDirectories(f.getAbsolutePath());
				}
			}
			// Check to see if this directory is now empty.
			directoryFile = new File(directory);
			if (directoryFile.listFiles().length == 0) {
				directoryFile.delete();
			}
		}
	}

	/**
	 * @param directory
	 * @param recursively
	 * @return
	 * @throws IOException
	 */
	public ArrayList<ArrayList<String>> findDuplicates(String directory,
			boolean recursively) throws IOException {
		ArrayList<ArrayList<String>> duplicateFiles = new ArrayList<ArrayList<String>>();
		// Get the list of files.
		Collection<String> files = catalogDirectory(directory, recursively, null);
		HashMap<String, BasicFileAttributes> basicFileAttributes = getBasicFileAttributes(
				directory, files);

		// Sort the files based on size.
		ArrayList<String> filesBySize = (ArrayList<String>) filesBySize(basicFileAttributes);

		// Compare files by size.
		int numFiles = filesBySize.size();
		ArrayList<String> addedDuplicates = new ArrayList<String>();
		for (int i = 0; i < numFiles; i++) {
			if (!addedDuplicates.contains(filesBySize.get(i))) {
				int j = i + 1;
				ArrayList<String> duplicates = null;
				File file1 = null;
				// Look forward in the list to see if this file has a duplicate.
				while ((j < numFiles)
						&& (((BasicFileAttributes) basicFileAttributes
								.get(filesBySize.get(i))).size() == ((BasicFileAttributes) basicFileAttributes
								.get(filesBySize.get(j))).size())) {
					if (j == (i + 1)) {
						file1 = new File(directory + File.separator
								+ filesBySize.get(i));
					}
					File file2 = new File(directory + File.separator
							+ filesBySize.get(j));
					// Compare files.
					if (FileUtils.contentEquals(file1, file2)) {
						// If true, create a paired array.
						if (duplicates == null) {
							duplicates = new ArrayList<String>();
							duplicates.add(filesBySize.get(i));
							addedDuplicates.add(filesBySize.get(i));
							duplicates.add(filesBySize.get(j));
							addedDuplicates.add(filesBySize.get(j));
						} else {
							duplicates.add(filesBySize.get(j));
							addedDuplicates.add(filesBySize.get(j));
						}
					}
					j = j + 1;
				}
				if (duplicates != null) {
					duplicateFiles.add(duplicates);
				}
			}
		}
		return duplicateFiles;
	}

	/**
	 * @param filePathAndName1
	 * @param filePathAndName2
	 * @return
	 */
	public Boolean compareFiles(String filePathAndName1, String filePathAndName2) {
		Boolean same = false;
		return same;
	}

	/**
	 * @param basicFileAttributes
	 * @return
	 */
	private Collection<String> filesBySize(
			HashMap<String, BasicFileAttributes> basicFileAttributes) {
		// return a list of files sorted by file size.
		// Sort values and keys in the same order.
		ArrayList<String> keys = new ArrayList<String>(
				basicFileAttributes.keySet());
		ArrayList<Long> values = new ArrayList<Long>();
		ArrayList<Long> sizes = new ArrayList<Long>();
		for (int i = 0; i < keys.size(); i++) {
			Long fileSize = basicFileAttributes.get(keys.get(i)).size();
			sizes.add(fileSize);
			values.add(fileSize);
		}
		ArrayList<String> filesBySize = new ArrayList<String>();

		Collections.sort(sizes);

		for (int i = 0; i < sizes.size(); i++) {
			int arrayIndex = values.indexOf(sizes.get(i));
			filesBySize.add(keys.get(arrayIndex));
			keys.remove(arrayIndex);
			values.remove(arrayIndex);
		}

		return filesBySize;
	}
	
}
