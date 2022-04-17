package com.example.grpc.client.grpcclient;

import java.io.IOException;
import java.util.stream.Collectors;
import java.util.Arrays;

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

	@PostMapping("/")
	public String handleFileUpload(@RequestParam("file") MultipartFile file,
			RedirectAttributes redirectAttributes) {
		if (file.isEmpty()) {
			// redirectAttributes.addFlashAttribute("message", "Please choose a file before uploading!");
			// return "redirect:/";
			return message(redirectAttributes, "Please choose a file before uploading!");
		}

		try { // Deletables
			String stringed = new String(file.getBytes());
			String[] matrix = stringed.trim().split("\n");
			String[] cols = matrix[0].trim().split(" ");
			int[][] m = new int[matrix.length][cols.length];
			if (!square(m)) return message(redirectAttributes, "Matrix is not squared");
			if (!sidesSquared(m)) return message(redirectAttributes, "Matrix's sides aren't a perfect square");
			System.out.println("Tried string way successfuly: ");
			String conversion = toInt(m, matrix);
			if (!conversion.equals("")) return message(redirectAttributes, conversion);
			System.out.println("And converted to int successfuly: " + Arrays.deepToString(m));
		}
		catch (Exception e) {
			System.out.println("FileUploadController@63\n" + e.toString());
			return message(redirectAttributes, "Check error");
		}

		storageService.store(file);
		// redirectAttributes.addFlashAttribute("message",
		// 		"You successfully uploaded " + file.getOriginalFilename() + "!");

		// return "redirect:/";
		return message(redirectAttributes, "You successfully uploaded " + file.getOriginalFilename() + "!");
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
		System.out.println("M's dimensions are " + m.length + " and " + m[0].length);
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