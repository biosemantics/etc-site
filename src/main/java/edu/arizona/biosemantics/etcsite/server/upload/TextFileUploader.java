package edu.arizona.biosemantics.etcsite.server.upload;

import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.etcsite.server.Configuration;
import edu.arizona.biosemantics.etcsite.server.JavaZipper;
import edu.arizona.biosemantics.etcsite.server.process.file.IContentValidator;
import edu.arizona.biosemantics.etcsite.shared.model.ShortUser;
import edu.arizona.biosemantics.etcsite.shared.model.file.FileTypeEnum;
import gwtupload.shared.UConsts;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

public class TextFileUploader extends Uploader {

	@Override
	protected UploadResult uploadSingleFile(ShortUser shortUser, FileItem item, String targetPath, FileTypeEnum fileType) {
		File file = new File(targetPath + File.separator + item.getName());
		UploadResult uploadResult = new UploadResult(item, file, item.getName());
		if(!file.exists()) {
			if(isValidUTF8(item.get())) {
				String fileContent;
				try {
					fileContent = item.getString("UTF-8");
					try {
						file.createNewFile();
						item.write(file);
						return uploadResult;					
					} catch(Exception e) {
						String message = "Couldn't create file";
						log(LogLevel.ERROR, message, e);
						uploadResult.setWriteFailed(true);
					}
				} catch (UnsupportedEncodingException e) {
					String message = "Couldn't get UTF-8 encoding";
					log(LogLevel.ERROR, message, e);
					uploadResult.setWriteFailed(true);
				}
			} else {
				uploadResult.setInvalidEncoding(true);
			}
		} else {
			uploadResult.setFileExisted(true);
		}
		return uploadResult;
	}

	@Override
	protected UploadResult uploadZip(ShortUser shortUser, FileItem item, String targetPath, FileTypeEnum fileType) {
		File target = new File(targetPath); //new File(FilenameUtils.removeExtension(targetPath + File.separator + item.getName()));
		File temp = new File(Configuration.compressedFileBase + File.separator + shortUser.getId() + File.separator + UUID.randomUUID().toString());
		temp.mkdirs();
		UploadResult uploadResult = new UploadResult(item, target, item.getName());
		/*if(target.exists()) {
			uploadResult.setFileExisted(true);
			return uploadResult;
		}*/
			
		JavaZipper zipper = new JavaZipper();
		File tempFile = new File(temp, item.getName());
		try {
			tempFile.createNewFile();
			item.write(tempFile);
		} catch(Exception e) {
			log(LogLevel.ERROR, "Couldn't create temporary file", e);
			uploadResult.setWriteFailed(true);
			return uploadResult;
		}
		File tempOutput = new File(FilenameUtils.removeExtension(tempFile.getAbsolutePath()));
		if(tempOutput.exists())
			tempOutput.delete();
		try {
			zipper.unzip(tempFile.getAbsolutePath(), tempOutput.getAbsolutePath());
		} catch (IOException e) {
			log(LogLevel.ERROR, "Couldn't unzip file", e);
			uploadResult.setWriteFailed(true);
			return uploadResult;
		}
		
		List<UploadResult> childUploadResults = new LinkedList<UploadResult>();
		if(tempOutput.listFiles().length == 1 && tempOutput.listFiles()[0].isDirectory()) {
			childUploadResults.addAll(handleFile(item, tempOutput.listFiles()[0].listFiles(), fileType, tempOutput, target));
		} else {
			childUploadResults.addAll(handleFile(item, tempOutput.listFiles(), fileType, tempOutput, target));
		}
		
		/*for(File file : tempOutput.listFiles()) {	
			//disallow upload of zip files with directory sturctures
			//childUploadResults.addAll(handleFile(item, file, fileType, tempOutput, target));
		}*/
		uploadResult.setChildResults(childUploadResults);
		
		return uploadResult;
	}

	private Collection<? extends UploadResult> handleFile(FileItem item, File[] files, FileTypeEnum fileType, File tempOutput, File target) {
		List<UploadResult> result = new LinkedList<UploadResult>();
		for(File file : files) {
			UploadResult childUploadResult = new UploadResult(item, file, getRelativeFileName(file, tempOutput));
			if(file.isDirectory()) {
				childUploadResult.setDirectoryNotAllowedInZip(true);
			} else {
				byte[] bytes = null;
				try {
					bytes = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
					if(!isValidUTF8(bytes)) {
						childUploadResult.setInvalidEncoding(true);
					} else {
						try {
							FileUtils.copyFileToDirectory(file, target);
						} catch (IOException e) {
							childUploadResult.setWriteFailed(true);
							log(LogLevel.ERROR, "Couldn't copy extracted files to target directory", e);
						}
					}
				} catch (IOException e) {
					log(LogLevel.ERROR, "Could not read file", e);
					childUploadResult.setWriteFailed(true);
				}
			}
			result.add(childUploadResult);
		}
		return result;
	}

	//disallow upload of zip files with directory sturctures
	/*private Collection<UploadResult> handleFile(FileItem item, File file, FileTypeEnum fileType, File tempOutput, File target) {
		List<UploadResult> result = new LinkedList<UploadResult>();
		if(file.isDirectory()) {
			target = new File(target, file.getName());
			target.mkdirs();
			for(File childFile : file.listFiles()) {
				result.addAll(handleFile(item, childFile, fileType, tempOutput, target));
			}
		} else {
			UploadResult childUploadResult = new UploadResult(item, file, getRelativeFileName(file, tempOutput));
			byte[] bytes = null;
			try {
				bytes = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
				if(!isValidUTF8(bytes)) {
					childUploadResult.setInvalidEncoding(true);
				} else {
					try {
						FileUtils.copyFileToDirectory(file, target);
					} catch (IOException e) {
						childUploadResult.setWriteFailed(true);
						log(LogLevel.ERROR, "Couldn't copy extracted files to target directory", e);
					}
				}
			} catch (IOException e) {
				log(LogLevel.ERROR, "Could not read file", e);
				childUploadResult.setWriteFailed(true);
			}
			result.add(childUploadResult);
		}
		return result;
	}*/
}