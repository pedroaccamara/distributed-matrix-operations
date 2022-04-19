package com.example.grpc.client.grpcclient;

import java.io.IOException;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.grpc.client.grpcclient.storage.StorageFileNotFoundException;
import com.example.grpc.client.grpcclient.storage.StorageService;

@Controller
public class FileUploadController {

	private final StorageService storageService;
	// public int[][] matrix1;
	// public int[][] matrix2;

	@Autowired
	public FileUploadController(StorageService storageService) {
		this.storageService = storageService;
	}

	@GetMapping("/")
	public String listUploadedFiles(Model model) throws IOException {

		model.addAttribute("files", storageService.loadAll().map(
				path -> MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,
						"serveFile", path.getFileName().toString()).build().toUri().toString())
				.collect(Collectors.toList()));

		return "uploadForm";
	}

	@GetMapping("/files/{filename:.+}")
	@ResponseBody
	public ResponseEntity<Resource> serveFile(@PathVariable String filename) {

		Resource file = storageService.loadAsResource(filename);
		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
				"attachment; filename=\"" + file.getFilename() + "\"").body(file);
	}

	@GetMapping("/delete")
	public String deleteFile(Model model, RedirectAttributes redirectAttributes) {
		storageService.deleteAll();
		// model.addAttribute("files", storageService.loadAll().map(
		// 		path -> MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,
		// 				"serveFile", path.getFileName().toString()).build().toUri().toString())
		// 		.collect(Collectors.toList()));
		TempStorage.setMatrix1(null);
		TempStorage.setMatrix2(null);
		TempStorage.setInitialised(false);
		return message(redirectAttributes, "Successfully emptied the storage of files");
	}

	@PostMapping("/")
	public String handleFileUpload(@RequestParam("file") MultipartFile[] files,
			RedirectAttributes redirectAttributes) {
		if (files.length == 0) return message(redirectAttributes, "Please choose a file before uploading!");
		if (TempStorage.getMatrix1() != null && files.length > 1) return message(redirectAttributes, "You can only upload one more matrix");
		if (files.length > 2) return message(redirectAttributes, "Please choose no more than two files before uploading!");

		String process;
		for (MultipartFile file : files) {
			process = processMatrix(file, redirectAttributes);
			if (process != "") return process;
			storageService.store(file);
		}
		if (files.length == 1) return message(redirectAttributes, "You successfully uploaded " + files[0].getOriginalFilename() + "!");
		return message(redirectAttributes, "You successfully uploaded " + files[0].getOriginalFilename() + " and " + files[1].getOriginalFilename() + "!");
	}

	@ExceptionHandler(StorageFileNotFoundException.class)
	public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
		return ResponseEntity.notFound().build();
	}

	private String message(RedirectAttributes redirectAttributes, String message) {
		return message(redirectAttributes, message, "redirect:/");
	}

	private String message(RedirectAttributes redirectAttributes, String message, String redirect) {
		redirectAttributes.addFlashAttribute("message", message);
		return redirect;
	}

	private String processMatrix(MultipartFile file, RedirectAttributes redirectAttributes) {
		try {
			String stringed = new String(file.getBytes());
			String[] matrix = stringed.trim().split("\n");
			String[] cols = matrix[0].trim().split(" ");
			int[][] m = new int[matrix.length][cols.length];
			if (!square(m)) return message(redirectAttributes, "Matrix is not squared");
			if (!sidesSquared(m)) return message(redirectAttributes, "Matrix's sides aren't a perfect square");
			String conversion = toInt(m, matrix);
			if (!conversion.equals("")) return message(redirectAttributes, conversion);
			if (TempStorage.getMatrix1() == null) {
				TempStorage.setMatrix1(m);
			} else {
				int size = TempStorage.getMatrix1().length;
				if (m.length != size) return message(redirectAttributes, "Make sure you upload matrices of the same size!\nThe first matrix was " + size + "x" + size);
				TempStorage.setMatrix2(m);
				TempStorage.setInitialised(true);
			}
			return "";
		}
		catch (Exception e) {
			System.out.println("FileUploadController@122\n" + e.toString());
			return message(redirectAttributes, "Check error");
		}
	}

	private String toInt(int[][] m, String[] matrix) {
		int noCols = matrix[0].trim().split(" ").length;
		int r = 0;
		int c = 0;
		for (String row : matrix) {
			for (String num : row.trim().split(" ")) {
				if (c == noCols) return "Columns don't all have the same size";
				m[r][c] = Integer.parseInt(num);
				c += 1;
			}
			if (c != noCols) return "Columns don't all have the same size";
			c = 0;
			r += 1;
		}
		return "";
	}

	private boolean square(int[][] m) {
		return m.length == m[0].length;
	}

	private boolean sidesSquared(int[][] m) {
		int side = m.length;
		if (side != 0) {
			while (true) {
				if (side % 2 == 0) {
					side /= 2;
					if (side == 1) return true;
				}
				else return false;
			}
		}

		return false;
	}
}