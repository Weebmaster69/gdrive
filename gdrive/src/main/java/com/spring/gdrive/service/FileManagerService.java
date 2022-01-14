package com.spring.gdrive.service;

import java.io.IOException;

import java.security.GeneralSecurityException;
import java.util.List;

import com.google.api.services.drive.model.File;

import org.springframework.web.multipart.MultipartFile;


/*
 * Define los métodos asociados a las operaciones al uso de la API de google drive,
 * <p>
 * 
 * @author Gonzalo

 */
public interface FileManagerService {
    /**
	 * Retorna una lista con todas los archivos y carpetas del drive
	 */
    public List<File> listEverything() throws IOException, GeneralSecurityException;


    /**
	 * Retorna el id del folder creado:
	 * @param path
	 * 			ruta de carpetas que se quiere crear Ejemplo: GoogleDrive/test
	 */
    public String getFolderId(String path) throws Exception;


    /**
	 * Sube el archivo a googledrive:  
	 * @param file
	 * 			el archivo a subir
	 * * @param path
	 * 			la carpeta a donde se subira, si no existe la crea
	 */
    public String uploadFile(MultipartFile file, String filePath);


    /**
	 * Descarga el archivo de google Drive:  
	 * @param id
	 * 			id del archivo en google Drive
	 */
    public String downloadFile(String id) throws IOException, GeneralSecurityException;

    /**
	 * Elimina el archivo o carpeta de google Drive:  
	 * @param id
	 * 			id del archivo o carpeta en google Drive
	 */
    public void delete(String id) throws Exception;
}
