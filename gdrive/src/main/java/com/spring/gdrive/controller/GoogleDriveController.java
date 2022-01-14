package com.spring.gdrive.controller;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;


import com.google.api.services.drive.model.File;
import com.spring.gdrive.service.FileManagerService;


import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Recibe las solicitudes relacionadas al uso de la API de google drive
 * <p>
 * Comprende las siguientes operaciones:
 * <ul>
 * 	<li>Ver los archivos y carpetas en el drive
 * 	<li>Subir un archivo
 * 	<li>descargar un archivo
 * 	<li>Crear carpetas
 * 	<li>Eliminar un archivo o carpeta
 * </ul>
 * 
 * @author Gonzalo Artadi
 * @see FileManagerService
 */



@RestController
@AllArgsConstructor
@Slf4j
public class GoogleDriveController {
	/**
	 * Retorna una lista de todos las Areas existentes.  
	 * 			
	 * @return	retorna una lista de los archivos
	 */

    private FileManagerService fileManager;
    @GetMapping({"/"})
	public ResponseEntity<List<File>> listEverything() throws IOException, GeneralSecurityException {
		List<File> files = fileManager.listEverything();
		return ResponseEntity.ok(files);
	}
	/**
	 * Crea la carpeta o carpetas anidadas en Google Drive :  
	 * @param id
	 * 			ruta de carpetas que se quiere crear Ejemplo: GoogleDrive/test
	 * @return	el id de la carpeta
	 */
	@PostMapping("/createFolder")
	public ResponseEntity<String> createFolderPath (@RequestParam String path){
		String parentId;
		try {
			parentId=fileManager.getFolderId(path);
			return ResponseEntity.ok(parentId);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * Sube el archivo a googledrive:  
	 * @param file
	 * 			el archivo a subir
	 * * @param path
	 * 			la carpeta a donde se subira, si no existe la crea
	 * @return	el archivo codificado en base64
	 */

	@PostMapping(value = "/upload",
			consumes = {MediaType.MULTIPART_FORM_DATA_VALUE},
			produces = {MediaType.APPLICATION_JSON_VALUE} )
	public ResponseEntity<String> uploadSingleFile(@RequestBody MultipartFile file,@RequestParam(required = false) String path) {
		log.info("Request contains, File: " + file.getOriginalFilename());
		String encodePdf = fileManager.uploadFile(file, path);
		
		return ResponseEntity.ok(encodePdf);
	}

	/**
	 * Descarga el archivo de google Drive:  
	 * @param id
	 * 			id del archivo en google Drive
	 * @return	el archivo codificado en base64
	 */
    @GetMapping("/download/{id}")
	public String download(@PathVariable String id) throws IOException, GeneralSecurityException {
		String encodedPdf = fileManager.downloadFile(id);
		return encodedPdf;
	}

	/**
	 * Elimina el archivo o carpeta de google Drive:  
	 * @param id
	 * 			id del archivo o carpeta en google Drive
	 */
	@GetMapping("/delete/{id}")
	public void delete(@PathVariable String id) throws Exception {
		fileManager.delete(id);
	}
	
}
