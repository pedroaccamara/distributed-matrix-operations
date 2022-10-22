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

	// Mapping responsible for clearing the storage of all files
	@GetMapping("/delete")
	public String deleteFile(Model model, RedirectAttributes redirectAttributes) {
		storageService.deleteAll();
		storageService.init();
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
			process = processMatrix(file, redirectAttributes); // Here a file is received and we check if it is valid
			if (process != "") return process; // If there is a message directed to the user from this process we'll return it
			storageService.store(file); // If there was no problem the file can be stored
		}
		if (files.length == 1) return message(redirectAttributes, "You successfully uploaded " + files[0].getOriginalFilename() + "!");
		return message(redirectAttributes, "You successfully uploaded " + files[0].getOriginalFilename() + " and " + files[1].getOriginalFilename() + "!");
	}

	@ExceptionHandler(StorageFileNotFoundException.class)
	public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
		return ResponseEntity.notFound().build();
	}

	// Method with the commonly used construct of adding message to redirectAttributes, and having a direction to redirect to
	private String message(RedirectAttributes redirectAttributes, String message, String redirect) {
		redirectAttributes.addFlashAttribute("message", message);
		return redirect;
	}

	// Method overloading taking advantage of previously defined method for common destination redirecting to "/"
	private String message(RedirectAttributes redirectAttributes, String message) {
		return message(redirectAttributes, message, "redirect:/");
	}

	
	

	// Main function processing the matrix received on file
	private String processMatrix(MultipartFile file, RedirectAttributes redirectAttributes) {
		try {
			String stringed = new String(file.getBytes());
			String[] matrix = stringed.trim().split("\n");
			String[] cols = matrix[0].trim().split(" ");
			int[][] m = new int[matrix.length][cols.length];

			// First checks from the dimensions of the given matrix looking at the first row and col
			if (!square(m)) return message(redirectAttributes, "Matrix is not squared");
			if (!sidesSquared(m)) return message(redirectAttributes, "Matrix's sides aren't a perfect square");

			// Conversion of the processed string array into a 2 dimensional integer matrix
			String conversion = toInt(m, matrix); // In which further rows will also be checked for consistency with the first row

			// If there is a message directed to the user from this conversion process we'll return it
			if (!conversion.equals("")) return message(redirectAttributes, conversion);

			// In this method we know there is storage space for at least one more matrix but we still need to check which storage space
			if (TempStorage.getMatrix1() == null) { // And the matrix1 space will be prioritised
				// If its a valid matrix and its the first one we're storing, there isn't anything else to check
				TempStorage.setMatrix1(m);
			} else { // If we're storing a second matrix, then we need to make sure it matches the dimensions of the first one
				int size = TempStorage.getMatrix1().length;
				if (m.length != size) return message(redirectAttributes, "Make sure you upload matrices of the same size!\nThe first matrix was " + size + "x" + size);
				TempStorage.setMatrix2(m);
				TempStorage.setInitialised(true); // Helpful variable to control whether the storage is filled or not
			}
			return ""; // No important messages to return if everything went accordingly
		}
		catch (Exception e) {
			System.out.println("FileUploadController@127\n" + e.toString());
			return message(redirectAttributes, "Check error");
		}
	}

	// In initial processing, convert string array inputed to integer matrix
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

	// Quickly check if by its sides a matrix is a square
	private boolean square(int[][] m) {
		return m.length == m[0].length;
	}

	// Check if the length of its sides are a number squared (or a perfect square)
	private boolean sidesSquared(int[][] m) {
		int side = m.length;
		if (side != 0) {
			while (true) { // The number should be divisible by 2 until it reaches 1
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