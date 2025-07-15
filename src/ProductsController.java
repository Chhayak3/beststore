package com.crudapplicaton.beststore.controllers;

import java.io.InputStream;
import java.nio.file.*;

import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.crudapplication.beststore.models.Product;
import com.crudapplication.beststore.models.ProductDto;
import com.crudapplication.beststore.productsrepository.ProductsRepository;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/products")
public class ProductsController {

	@Autowired
	private ProductsRepository  repo;
	
	@GetMapping({"","/"})
	public String showProductsList(Model model) {
		List<Product> products=repo.findAll(Sort.by(Sort.Direction.DESC, "id"));
		model.addAttribute("products",products);
		return "/products/index";
	}
	
	@GetMapping("/create")
	public String showCreatePage(Model model) {
		ProductDto productDto=new ProductDto();
		model.addAttribute("productDto",productDto);
		return "/products/CreateProduct";
	}
	
	
     @PostMapping("/create")
	public String createProduct(@Valid @ModelAttribute ProductDto productDto, BindingResult result) {
	   
	if(productDto.getImageFile().isEmpty()) {
		result.addError(new FieldError("productDto", "imageFile", "The image file is required"));
	}
     if(result.hasErrors()) {
    	 return "products/CreateProduct";
     }
     
     //save the image file
     MultipartFile image=productDto.getImageFile();
     Date createdAt=new Date();
     String storageFileName = createdAt.getTime() + "___" + image.getOriginalFilename();
     
     try {
    	 String uploadDir = "static/images";
    	 Path uploadPath=Paths.get(uploadDir);
    	 
    	 if(!Files.exists(uploadPath)) 
    	 {
    		 Files.createDirectories(uploadPath);
    	 }
    	 
    	 try(InputStream inputStream =image.getInputStream()){
    		 Files.copy(inputStream,Paths.get(uploadDir + "/" + storageFileName),StandardCopyOption.REPLACE_EXISTING);
    	 }
    	 
     }catch(Exception ex) {
    	 System.out.println("Exception: " + ex.getMessage());
     }
     
     Product product = new Product();
    product.setName(productDto.getName());
    System.out.println(productDto.getName());
     product.setBrand(productDto.getBrand());
     product.setCategory(productDto.getCategory());
     product.setPrice(productDto.getPrice());
     product.setDescription(productDto.getDescription());
     product.setCreatedAt(createdAt);
     product.setImageFileName(storageFileName);
     repo.save(product);
	return "redirect:/products";
	
	
}
     @PostMapping("/edit")
     public String processEdit(Model model,
             @Valid @ModelAttribute("productDto") ProductDto productDto,
             BindingResult result) {

         try {
             Product product = repo.findById(productDto.getId()).orElseThrow();

             model.addAttribute("product", product); // needed if validation fails

             if (result.hasErrors()) {
                 return "/products/EditProduct";
             }

             if (!productDto.getImageFile().isEmpty()) {
                 String uploadDir = new ClassPathResource("static/images/").getFile().getAbsolutePath();
                 Path oldImagePath = Paths.get(uploadDir, product.getImageFileName());

                 try {
                     Files.deleteIfExists(oldImagePath);
                 } catch (Exception ex) {
                     System.out.println("Failed to delete old image: " + ex.getMessage());
                 }

                 MultipartFile image = productDto.getImageFile();
                 String storageFileName = System.currentTimeMillis() + "_" + image.getOriginalFilename();

                 try (InputStream inputStream = image.getInputStream()) {
                     Files.copy(inputStream, Paths.get(uploadDir, storageFileName), StandardCopyOption.REPLACE_EXISTING);
                 }

                 product.setImageFileName(storageFileName);
             }

             product.setName(productDto.getName());
             product.setBrand(productDto.getBrand());
             product.setCategory(productDto.getCategory());
             product.setPrice(productDto.getPrice());
             product.setDescription(productDto.getDescription());

             repo.save(product);
         } catch (Exception ex) {
             System.out.println("Exception: " + ex.getMessage());
         }

         return "redirect:/products";
     }


	@PostMapping("/delete/{id}")
	public String deleteProduct(@PathVariable int id) {
		repo.deleteById(id);
		return "redirect:/products";
	}
}
