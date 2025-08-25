package com.community_shop.backend.service.base;

import com.community_shop.backend.entity.Product;

import java.util.List;

public interface ProductService {
    // 获取所有商品
    List<Product> getAllProducts();

    // 获取商品详情
    Product getProductById(int id);

    // 添加商品
    int addProduct(Product product);

    // 更新商品信息
    int updateProduct(Product product);

    // 删除商品
    int deleteProduct(int id);


}
