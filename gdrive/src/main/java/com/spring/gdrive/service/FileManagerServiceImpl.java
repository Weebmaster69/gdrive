package com.spring.gdrive.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.google.api.client.http.InputStreamContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import org.springframework.web.multipart.MultipartFile;
/**
	 * Ejecuta la lógica asociada a las operaciones al uso de la API de google drive
	 * <p>
	 * Esta clase, al ser un servicio {@literal @Service}
	 * implementa la interface {@link FileManagerService}
	 * 
	 * @author Gonzalo Artadi
 */

@Service
@AllArgsConstructor
@Slf4j
@EnableRetry
public class FileManagerServiceImpl implements FileManagerService {
	private GoogleDriveManager googleDriveManager;

	public List<File> listEverything() throws IOException, GeneralSecurityException {
		// Imprime los nombres y las IDS de 10 archivos.
		FileList result = googleDriveManager.getInstance().files().list()
				.setPageSize(10)
				.setFields("nextPageToken, files(id, name)")
				.execute();
		return result.getFiles();
	}

	private String findOrCreateFolder(String parentId, String folderName, Drive driveInstance) throws Exception {
		String folderId = searchFolderId(parentId, folderName, driveInstance);
		// Si la carpeta existe retorna el id
		if (folderId != null) {
			return folderId;
		}
		// Si la carpeta no existe, la crea y retorna el id
		File fileMetadata = new File();
		fileMetadata.setMimeType("application/vnd.google-apps.folder");
		fileMetadata.setName(folderName);

		if (parentId != null) {
			fileMetadata.setParents(Collections.singletonList(parentId));
		}
		return driveInstance.files().create(fileMetadata)
				.setFields("id")
				.execute()
				.getId();
	}
	private String searchFolderId(String parentId, String folderName, Drive service) throws Exception {
		String folderId = null;
		String pageToken = null;
		FileList result = null;

		File fileMetadata = new File();
		fileMetadata.setMimeType("application/vnd.google-apps.folder");
		fileMetadata.setName(folderName);

		do {
			String query = " mimeType = 'application/vnd.google-apps.folder' ";
			if (parentId == null) {
				query = query + " and 'root' in parents";
			} else {
				query = query + " and '" + parentId + "' in parents";
			}
			result = service.files().list().setQ(query)
					.setSpaces("drive")
					.setFields("nextPageToken, files(id, name)")
					.setPageToken(pageToken)
					.execute();

			for (File file : result.getFiles()) {
				if (file.getName().equalsIgnoreCase(folderName)) {
					folderId = file.getId();
				}
			}
			pageToken = result.getNextPageToken();
		} while (pageToken != null && folderId == null);

		return folderId;
	}

	public String getFolderId(String path) throws Exception {
		String parentId = null;
		String[] folderNames = path.split("/");

		Drive driveInstance = googleDriveManager.getInstance();
		for (String name : folderNames) {
			parentId = findOrCreateFolder(parentId, name, driveInstance);
		}
		return parentId;
	}

	
	@Retryable(
		value = {Exception.class},
		maxAttempts = 5,
		backoff = @Backoff(delay = 3000)
	)
	public String uploadFile(MultipartFile file, String filePath) {

		try {
			byte[] encoded = Base64.encodeBase64(file.getBytes());
			String encodedPdf = new String(encoded);
			String folderId = getFolderId(filePath);
			if (null != file) {
				File fileMetadata = new File();
				fileMetadata.setParents(Collections.singletonList(folderId));
				fileMetadata.setName(file.getOriginalFilename());
				googleDriveManager.getInstance()
						.files()
						.create(fileMetadata, new InputStreamContent(
								file.getContentType(),
								new ByteArrayInputStream(file.getBytes())))
						.setFields("id").execute();
				return encodedPdf;
			}
		} catch (Exception e) {
			log.error("Error: ", e);
		}

		return null;
	}

	@Override
	public String downloadFile(String id) throws IOException, GeneralSecurityException {
		if (id != null) {
			
			String fileId = id;
			InputStream inputStream = googleDriveManager.getInstance().files().get(fileId).executeMediaAsInputStream();
			byte[] bytes = IOUtils.toByteArray(inputStream);
			byte[] encoded = Base64.encodeBase64(bytes);
			String encodedPdf = new String(encoded);
			return encodedPdf;
		}
		return null;
	}

	public void delete(String fileId) throws Exception {
		googleDriveManager.getInstance().files().delete(fileId).execute();
	}
}
