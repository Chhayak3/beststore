package com.crudapplication.beststore.productsrepository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.crudapplication.beststore.models.Product;

public interface ProductsRepository extends JpaRepository <Product,Integer>{

}
